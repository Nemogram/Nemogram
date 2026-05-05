package org.nemogram.messenger.export;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChatExportManager {

    private static final int MAX_PARALLEL_DOWNLOADS = 3;
    private static final int THREAD_POOL_SIZE = MAX_PARALLEL_DOWNLOADS + 2;
    private static final int[] RETRY_DELAYS_MS = {5_000, 15_000, 30_000};
    private static ChatExportManager instance;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public static ChatExportManager getInstance() {
        if (instance == null) instance = new ChatExportManager();
        return instance;
    }

    public static String getSenderName(MessageObject msg) {
        int account = UserConfig.selectedAccount;
        if (msg.messageOwner.from_id == null) {
            TLRPC.User self = UserConfig.getInstance(account).getCurrentUser();
            return self != null ? self.first_name : "You";
        }
        if (msg.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
            long uid = msg.messageOwner.from_id.user_id;
            TLRPC.User u = MessagesController.getInstance(account).getUser(uid);
            if (u != null) return u.first_name + (u.last_name != null ? " " + u.last_name : "");
        }
        if (msg.messageOwner.from_id instanceof TLRPC.TL_peerChannel
                || msg.messageOwner.from_id instanceof TLRPC.TL_peerChat) {
            return "Channel";
        }
        return "Unknown";
    }

    public void startExport(long dialogId, ExportSettings settings, ExportListener listener) {
        cancelled.set(false);
        int account = UserConfig.selectedAccount;

        // use an unbounded cached pool for upload tasks
        // so that submit() never throws a RejectedExecutionException
        ExecutorService ioExecutor = Executors.newCachedThreadPool();

        ioExecutor.submit(() -> {
            try {
                File exportDir = new File(AndroidUtilities.getCacheDir(), "export_" + dialogId);
                deleteDir(exportDir);
                exportDir.mkdirs();

                ExportWriter writer = settings.format == ExportSettings.Format.HTML
                        ? new HtmlExportWriter(exportDir)
                        : new JsonExportWriter(exportDir);

                String title = resolveTitle(account, dialogId);
                writer.begin(title);

                int offsetId = 0;
                int total = 0;
                long mediaBytesUsed = 0L;
                Semaphore downloadSlots = new Semaphore(MAX_PARALLEL_DOWNLOADS);

                outer:
                while (!cancelled.get()) {
                    ArrayList<MessageObject> page;
                    try {
                        page = loadPage(account, dialogId, offsetId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (page == null) throw new java.io.IOException(
                            "Failed to load messages after " + (RETRY_DELAYS_MS.length + 1) +
                                    " attempts (flood wait or no network)");
                    if (page.isEmpty()) break;

                    File[] mediaFiles = new File[page.size()];
                    CountDownLatch pageLatch = new CountDownLatch(page.size());

                    for (int i = 0; i < page.size(); i++) {
                        final int idx = i;
                        final MessageObject msg = page.get(i);

                        boolean shouldDownload =
                                (settings.includePhotos && msg.isPhoto()) ||
                                        (settings.includeVideos && msg.isVideo()) ||
                                        (settings.includeFiles && msg.isDocument() && !msg.isMusic() && !msg.isVoice()) ||
                                        (settings.includeVoice && (msg.isVoice() || msg.isRoundVideo())) ||
                                        (settings.includeMusic && msg.isMusic());

                        if (!shouldDownload) {
                            pageLatch.countDown();
                            continue;
                        }

                        if (settings.maxMediaBytes > 0 && mediaBytesUsed >= settings.maxMediaBytes) {
                            pageLatch.countDown();
                            continue;
                        }
                        try {
                            ioExecutor.submit(() -> {
                                try {
                                    downloadSlots.acquire();
                                    try {
                                        File f = downloadMediaSync(account, msg);
                                        mediaFiles[idx] = f;
                                    } finally {
                                        downloadSlots.release();
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    pageLatch.countDown();
                                }
                            });
                        } catch (Exception submitEx) {
                            pageLatch.countDown();
                        }
                    }

                    pageLatch.await(120, TimeUnit.SECONDS);

                    for (int i = 0; i < page.size(); i++) {
                        if (cancelled.get()) break outer;

                        MessageObject msg = page.get(i);
                        File media = mediaFiles[i];

                        if (media != null && media.exists()) {
                            mediaBytesUsed += media.length();
                            if (settings.maxMediaBytes > 0 && mediaBytesUsed > settings.maxMediaBytes) {
                                media = null;
                            }
                        }

                        writer.writeMessage(msg, media);
                        total++;

                        if (settings.maxMessages > 0 && total >= settings.maxMessages) {
                            break outer;
                        }
                    }

                    int finalTotal = total;
                    AndroidUtilities.runOnUIThread(() -> listener.onProgress(finalTotal));

                    offsetId = page.get(page.size() - 1).getId();
                    if (page.size() < 100) break;
                }

                writer.end();
                ioExecutor.shutdown();

                File zip = zipExportDir(exportDir, title);
                AndroidUtilities.runOnUIThread(() -> listener.onDone(zip));

            } catch (Exception e) {
                ioExecutor.shutdown();
                AndroidUtilities.runOnUIThread(() -> listener.onError(e));
            }
        });
    }

    private String resolveTitle(int account, long dialogId) {
        if (dialogId < 0) {
            TLRPC.Chat chat = MessagesController.getInstance(account).getChat(-dialogId);
            if (chat != null) return chat.title;
        } else {
            TLRPC.User user = MessagesController.getInstance(account).getUser(dialogId);
            if (user != null)
                return user.first_name + (user.last_name != null ? " " + user.last_name : "");
        }
        return "Chat";
    }

    private File zipExportDir(File exportDir, String chatTitle) throws Exception {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String safeName = chatTitle.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        File zipFile = new File(AndroidUtilities.getCacheDir(), safeName + "_" + timestamp + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new java.io.FileOutputStream(zipFile))) {
            zipDir(exportDir, exportDir, zos);
        }
        return zipFile;
    }

    private void zipDir(File root, File dir, ZipOutputStream zos) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                zipDir(root, file, zos);
                continue;
            }
            String entryName = root.toURI().relativize(file.toURI()).getPath();
            zos.putNextEntry(new ZipEntry(entryName));
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = fis.read(buf)) != -1) zos.write(buf, 0, n);
            }
            zos.closeEntry();
        }
    }

    // returns null only if all attempts have been exhausted
    private ArrayList<MessageObject> loadPage(int account, long dialogId, int offsetId) throws InterruptedException {
        for (int attempt = 0; ; attempt++) {
            ArrayList<MessageObject> result = loadPageOnce(account, dialogId, offsetId);
            if (result != null) return result; // успех

            if (attempt >= RETRY_DELAYS_MS.length) return null;

            int delay = RETRY_DELAYS_MS[attempt];
            android.util.Log.w("ChatExport",
                    "loadPage failed, retry " + (attempt + 1) + "/" + RETRY_DELAYS_MS.length +
                            " after " + (delay / 1000) + "s");
            Thread.sleep(delay);

            if (cancelled.get()) return null;
        }
    }

    private ArrayList<MessageObject> loadPageOnce(int account, long dialogId, int offsetId) {
        final ArrayList<MessageObject>[] result = new ArrayList[]{null};
        CountDownLatch latch = new CountDownLatch(1);

        AndroidUtilities.runOnUIThread(() -> {
            TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
            req.peer = MessagesController.getInstance(account).getInputPeer(dialogId);
            req.limit = 100;
            req.offset_id = offsetId;
            req.add_offset = 0;
            req.max_id = 0;
            req.min_id = 0;
            req.hash = 0;

            ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> {
                if (response instanceof TLRPC.messages_Messages res) {
                    ArrayList<MessageObject> objects = new ArrayList<>(res.messages.size());
                    for (TLRPC.Message msg : res.messages) {
                        objects.add(new MessageObject(account, msg, true, true));
                    }
                    result[0] = objects;
                }
                latch.countDown();
            });
        });

        try {
            if (!latch.await(30, TimeUnit.SECONDS)) return null; // таймаут
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return result[0];
    }

    private File downloadMediaSync(int account, MessageObject msg) {
        File local = FileLoader.getInstance(account).getPathToMessage(msg.messageOwner);
        if (local != null && local.exists()) return local;

        final String expectedFileName;
        if (msg.isPhoto()) {
            TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(msg.photoThumbs, 1280);
            expectedFileName = photoSize != null ? FileLoader.getAttachFileName(photoSize) : null;
        } else {
            expectedFileName = msg.getDocument() != null
                    ? FileLoader.getAttachFileName(msg.getDocument()) : null;
        }
        if (expectedFileName == null) return null;

        final File[] result = {null};
        CountDownLatch latch = new CountDownLatch(1);

        AndroidUtilities.runOnUIThread(() -> {
            // register the observer before calling loadFile
            NotificationCenter.NotificationCenterDelegate observer =
                    new NotificationCenter.NotificationCenterDelegate() {
                        @Override
                        public void didReceivedNotification(int id, int acc, Object... args) {
                            if (!(args[0] instanceof String fileName)) return;
                            if (!expectedFileName.equals(fileName)) return;

                            NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.fileLoaded);
                            NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.fileLoadFailed);

                            if (id == NotificationCenter.fileLoaded) {
                                if (args.length > 1 && args[1] instanceof File) {
                                    result[0] = (File) args[1];
                                } else {
                                    File f = FileLoader.getInstance(account).getPathToMessage(msg.messageOwner);
                                    if (f != null && f.exists()) result[0] = f;
                                }
                            }
                            latch.countDown();
                        }
                    };

            NotificationCenter.getInstance(account).addObserver(observer, NotificationCenter.fileLoaded);
            NotificationCenter.getInstance(account).addObserver(observer, NotificationCenter.fileLoadFailed);

            // recheck the file after registering observer
            File alreadyLoaded = FileLoader.getInstance(account).getPathToMessage(msg.messageOwner);
            if (alreadyLoaded != null && alreadyLoaded.exists()) {
                NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoaded);
                NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoadFailed);
                result[0] = alreadyLoaded;
                latch.countDown();
                return;
            }

            if (msg.isPhoto()) {
                TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(msg.photoThumbs, 1280);
                if (photoSize != null) {
                    FileLoader.getInstance(account).loadFile(
                            ImageLocation.getForObject(photoSize, msg.photoThumbsObject),
                            msg, null, FileLoader.PRIORITY_LOW, 1);
                } else {
                    NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoaded);
                    NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoadFailed);
                    latch.countDown();
                }
            } else {
                TLRPC.Document doc = msg.getDocument();
                if (doc != null) {
                    FileLoader.getInstance(account).loadFile(doc, msg, FileLoader.PRIORITY_LOW, 1);
                } else {
                    NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoaded);
                    NotificationCenter.getInstance(account).removeObserver(observer, NotificationCenter.fileLoadFailed);
                    latch.countDown();
                }
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result[0];
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    public void cancel() {
        cancelled.set(true);
    }

    public interface ExportListener {
        void onProgress(int count);

        void onDone(File outputFile);

        void onError(Exception e);
    }
}