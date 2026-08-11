package org.nemogram.messenger.helpers.transcribe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import org.opentranscribe.api.ErrorType;
import org.opentranscribe.api.ITranscriptionCallback;
import org.opentranscribe.api.ITranscriptionService;
import org.opentranscribe.api.ITranscriptionSession;
import org.opentranscribe.api.TranscriberCapabilities;
import org.opentranscribe.api.TranscriptionError;
import org.opentranscribe.api.TranscriptionRequest;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AidlOfflineTranscriber {

    public static final String ACTION = "org.opentranscribe.api.ITranscriptionService";
    public static final int CONTRACT_VERSION = 1;

    private static final long BIND_TIMEOUT_MS = 5000L;
    private static final long IDLE_TIMEOUT_MS = 300_000L;
    private static final long POLL_INTERVAL_MS = 1000L;

    private final String servicePackage;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object bindLock = new Object();

    private volatile ITranscriptionService service;
    private ServiceConnection connection;
    private CountDownLatch bindLatch;

    public AidlOfflineTranscriber(String servicePackage) {
        this.servicePackage = servicePackage;
    }

    public TranscriberCapabilities capabilities() {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            return null;
        }
        try {
            ITranscriptionService bound = ensureService();
            return bound != null ? bound.getCapabilities() : null;
        } catch (Exception e) {
            service = null;
            return null;
        }
    }

    public OfflineTranscribeSession requestTranscription(String audioFilePath, String languageHint,
                                                           Consumer<String> onProgress,
                                                           BiConsumer<String, Exception> onFinal) {
        Session session = new Session();
        executor.execute(() -> {
            try {
                String text = transcribe(audioFilePath, languageHint, onProgress, session);
                if (session.cancelled) {
                    onFinal.accept(null, new TranscriptionCancelledException());
                } else {
                    onFinal.accept(text, null);
                }
            } catch (Exception e) {
                onFinal.accept(null, session.cancelled ? new TranscriptionCancelledException() : e);
            }
        });
        return session;
    }

    private String transcribe(String audioFilePath, String languageHint,
                               Consumer<String> onProgress, Session session) {
        File file = new File(audioFilePath);
        ITranscriptionService bound = ensureService();
        if (bound == null) {
            return null;
        }

        AtomicReference<String> resultRef = new AtomicReference<>(null);
        AtomicLong lastActivity = new AtomicLong(SystemClock.elapsedRealtime());

        ITranscriptionCallback callback = new ITranscriptionCallback.Stub() {
            @Override
            public void onTranscriptionProgress(String text) {
                lastActivity.set(SystemClock.elapsedRealtime());
                if (text != null && onProgress != null) {
                    onProgress.accept(text);
                }
            }

            @Override
            public void onTranscriptionResult(String text) {
                resultRef.set(text);
                session.latch.countDown();
            }

            @Override
            public void onTranscriptionError(TranscriptionError error) {
                if (error != null && error.type == ErrorType.CANCELLED) {
                    session.cancelled = true;
                }
                session.latch.countDown();
            }
        };

        ParcelFileDescriptor descriptor;
        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            return null;
        }

        try {
            TranscriptionRequest request = new TranscriptionRequest();
            request.fileName = file.getName();
            request.mimeType = mimeTypeOf(file.getName());
            request.languageHint = languageHint != null ? languageHint : "";

            ITranscriptionSession remote = bound.transcribe(descriptor, request, callback);
            session.attach(remote);

            while (!session.latch.await(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)) {
                if (session.cancelled) {
                    break;
                }
                if (SystemClock.elapsedRealtime() - lastActivity.get() > IDLE_TIMEOUT_MS) {
                    session.cancelRemote();
                    break;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
            service = null;
            return null;
        } finally {
            try {
                descriptor.close();
            } catch (Exception ignore) {
            }
        }
        return resultRef.get();
    }

    private ITranscriptionService ensureService() {
        ITranscriptionService current = service;
        if (current != null) {
            return current;
        }
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return null;
        }
        synchronized (bindLock) {
            current = service;
            if (current != null) {
                return current;
            }
            CountDownLatch latch = bindLatch;
            if (connection == null) {
                latch = new CountDownLatch(1);
                CountDownLatch awaiting = latch;
                ServiceConnection conn = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder binder) {
                        service = ITranscriptionService.Stub.asInterface(binder);
                        awaiting.countDown();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        service = null;
                    }
                };
                Intent intent = new Intent(ACTION).setPackage(servicePackage);
                boolean bound;
                try {
                    bound = context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
                } catch (Exception e) {
                    bound = false;
                }
                if (!bound) {
                    try {
                        context.unbindService(conn);
                    } catch (Exception ignore) {
                    }
                    return null;
                }
                connection = conn;
                bindLatch = latch;
            }
            try {
                if (latch != null) {
                    latch.await(BIND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return service;
        }
    }

    private static String mimeTypeOf(String fileName) {
        String name = fileName.toLowerCase();
        if (name.endsWith(".ogg") || name.endsWith(".oga") || name.endsWith(".opus")) {
            return "audio/ogg";
        } else if (name.endsWith(".mp4")) {
            return "video/mp4";
        } else if (name.endsWith(".m4a")) {
            return "audio/mp4";
        } else if (name.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (name.endsWith(".wav")) {
            return "audio/wav";
        }
        return null;
    }

    public static class TranscriptionCancelledException extends Exception {
    }

    private static class Session implements OfflineTranscribeSession {
        final CountDownLatch latch = new CountDownLatch(1);
        volatile boolean cancelled = false;
        private volatile ITranscriptionSession remote;

        void attach(ITranscriptionSession remoteSession) {
            remote = remoteSession;
            if (cancelled) {
                cancelRemote();
            }
        }

        void cancelRemote() {
            try {
                if (remote != null) {
                    remote.cancel();
                }
            } catch (Exception ignore) {
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            cancelRemote();
            latch.countDown();
        }
    }
}
