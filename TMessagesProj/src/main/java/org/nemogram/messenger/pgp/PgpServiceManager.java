package org.nemogram.messenger.pgp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PgpServiceManager {

    public static final int REQUEST_CODE_USER_INTERACTION = 71583;
    private static volatile PgpServiceManager instance;
    private final List<Runnable> onBoundQueue = new ArrayList<>();
    private OpenPgpServiceConnection serviceConnection;
    private String boundPackage;
    private PendingResume pendingResume;
    private OpenPgpApi openPgpApi;

    public static PgpServiceManager getInstance() {
        if (instance == null) {
            synchronized (PgpServiceManager.class) {
                if (instance == null) {
                    instance = new PgpServiceManager();
                }
            }
        }
        return instance;
    }

    private static byte[] toBytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    public static void startUserInteraction(Activity activity, PendingIntent pendingIntent) {
        try {
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_USER_INTERACTION, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            FileLog.e(e);
        }
    }

    public List<String> findAvailableProviders() {
        List<String> result = new ArrayList<>();
        try {
            PackageManager pm = ApplicationLoader.applicationContext.getPackageManager();
            Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
            List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);
            if (resolveInfos != null) {
                for (ResolveInfo info : resolveInfos) {
                    if (info.serviceInfo != null) {
                        result.add(info.serviceInfo.packageName);
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return result;
    }

    public boolean isProviderInstalled(String packageName) {
        try {
            ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private synchronized void bindIfNeeded(Runnable onBound) {
        String provider = PgpConfig.getProviderPackage();
        if (provider == null) {
            return;
        }
        if (serviceConnection != null && provider.equals(boundPackage) && serviceConnection.isBound()) {
            onBound.run();
            return;
        }
        if (serviceConnection != null && !provider.equals(boundPackage)) {
            serviceConnection.unbindFromService();
            serviceConnection = null;
            openPgpApi = null;
        }
        boundPackage = provider;
        onBoundQueue.add(onBound);
        if (serviceConnection == null) {
            openPgpApi = null;
            Context context = ApplicationLoader.applicationContext;
            serviceConnection = new OpenPgpServiceConnection(context, provider, new OpenPgpServiceConnection.OnBound() {
                @Override
                public void onBound(IOpenPgpService2 service) {
                    List<Runnable> queue;
                    synchronized (PgpServiceManager.this) {
                        queue = new ArrayList<>(onBoundQueue);
                        onBoundQueue.clear();
                    }
                    for (Runnable r : queue) {
                        r.run();
                    }
                }

                @Override
                public void onError(Exception e) {
                    FileLog.e("PGP: failed to bind to " + boundPackage, e);
                    synchronized (PgpServiceManager.this) {
                        onBoundQueue.clear();
                    }
                }
            });
            serviceConnection.bindToService();
        }
    }

    private synchronized OpenPgpApi api() {
        if (openPgpApi == null) {
            openPgpApi = new OpenPgpApi(ApplicationLoader.applicationContext, serviceConnection.getService());
        }
        return openPgpApi;
    }

    public void encrypt(long[] recipientKeyIds, String plainText, PgpResultCallback<String> callback) {
        bindIfNeeded(() -> {
            if (serviceConnection.getService() == null) {
                callback.onError("OpenPGP provider is not available");
                return;
            }
            List<Long> keyIds = new ArrayList<>();
            for (long id : recipientKeyIds) {
                if (id != 0) {
                    keyIds.add(id);
                }
            }
            long myKeyId = PgpConfig.getMyKeyId();
            if (myKeyId != 0 && !keyIds.contains(myKeyId)) {
                keyIds.add(myKeyId);
            }
            if (keyIds.isEmpty()) {
                callback.onError("No recipient key selected for this chat");
                return;
            }
            long[] ids = new long[keyIds.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = keyIds.get(i);
            }

            Intent data = new Intent();
            data.setAction(myKeyId != 0 ? OpenPgpApi.ACTION_SIGN_AND_ENCRYPT : OpenPgpApi.ACTION_ENCRYPT);
            data.putExtra(OpenPgpApi.EXTRA_KEY_IDS, ids);
            if (myKeyId != 0) {
                data.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, myKeyId);
            }
            data.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);

            requestText(data, toBytes(plainText), callback);
        });
    }

    public void decrypt(String armoredCipherText, PgpResultCallback<String> callback) {
        bindIfNeeded(() -> {
            if (serviceConnection.getService() == null) {
                callback.onError("OpenPGP provider is not available");
                return;
            }
            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
            requestText(data, toBytes(armoredCipherText), callback);
        });
    }

    private void requestText(Intent data, byte[] inputBytes, PgpResultCallback<String> callback) {
        ByteArrayInputStream is = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi.IOpenPgpCallback apiCallback = result -> handleTextResult(result, os, inputBytes, callback);
        api().executeApiAsync(data, is, os, apiCallback);
    }

    private void handleTextResult(Intent result, ByteArrayOutputStream os, byte[] originalInputBytes, PgpResultCallback<String> callback) {
        int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
        switch (resultCode) {
            case OpenPgpApi.RESULT_CODE_SUCCESS: {
                callback.onSuccess(os.toString(StandardCharsets.UTF_8));
                break;
            }
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
                PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                pendingResume = (resultCode2, resumedData) -> {
                    if (resultCode2 != Activity.RESULT_OK || resumedData == null) {
                        callback.onError("Cancelled");
                        return;
                    }
                    requestText(resumedData, originalInputBytes, callback);
                };
                callback.userInteractionRequired(pi);
                break;
            }
            case OpenPgpApi.RESULT_CODE_ERROR:
            default: {
                OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                callback.onError(error != null ? error.getMessage() : "Unknown OpenPGP provider error");
                break;
            }
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_USER_INTERACTION || pendingResume == null) {
            return false;
        }
        PendingResume resume = pendingResume;
        pendingResume = null;
        resume.onResumed(resultCode, data);
        return true;
    }

    public void pickMyKey(PgpResultCallback<Long> callback) {
        bindIfNeeded(() -> {
            if (serviceConnection.getService() == null) {
                callback.onError("OpenPGP provider is not available");
                return;
            }
            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
            requestGetSignKeyId(data, callback);
        });
    }

    private void requestGetSignKeyId(Intent data, PgpResultCallback<Long> callback) {
        OpenPgpApi.IOpenPgpCallback apiCallback = result -> handleGetSignKeyIdResult(result, callback);
        api().executeApiAsync(data, null, null, apiCallback);
    }

    private void handleGetSignKeyIdResult(Intent result, PgpResultCallback<Long> callback) {
        int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
        if (resultCode == OpenPgpApi.RESULT_CODE_SUCCESS) {
            long keyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, 0);
            callback.onSuccess(keyId);
            if (keyId != 0) {
                PgpConfig.setMyKey(keyId, null);
            }
        } else if (resultCode == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
            pendingResume = (resultCode2, resumedData) -> {
                if (resultCode2 != Activity.RESULT_OK || resumedData == null) {
                    callback.onError("Cancelled");
                    return;
                }
                requestGetSignKeyId(resumedData, callback);
            };
            callback.userInteractionRequired(pi);
        } else {
            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
            callback.onError(error != null ? error.getMessage() : "Unknown OpenPGP provider error");
        }
    }

    public void ensureKeyAvailable(long keyId, PgpResultCallback<Boolean> callback) {
        bindIfNeeded(() -> {
            if (serviceConnection.getService() == null) {
                callback.onError("OpenPGP provider is not available");
                return;
            }
            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_GET_KEY);
            data.putExtra(OpenPgpApi.EXTRA_KEY_ID, keyId);
            requestGetKey(data, callback);
        });
    }

    private void requestGetKey(Intent data, PgpResultCallback<Boolean> callback) {
        OpenPgpApi.IOpenPgpCallback apiCallback = result -> handleGetKeyResult(result, callback);
        api().executeApiAsync(data, null, null, apiCallback);
    }

    private void handleGetKeyResult(Intent result, PgpResultCallback<Boolean> callback) {
        int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
        if (resultCode == OpenPgpApi.RESULT_CODE_SUCCESS) {
            callback.onSuccess(true);
        } else if (resultCode == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
            pendingResume = (resultCode2, resumedData) -> {
                if (resultCode2 != Activity.RESULT_OK || resumedData == null) {
                    callback.onError("Cancelled");
                    return;
                }
                requestGetKey(resumedData, callback);
            };
            callback.userInteractionRequired(pi);
        } else {
            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
            callback.onError(error != null ? error.getMessage() : "Key not found");
        }
    }

    public synchronized void resetConnection() {
        if (serviceConnection != null) {
            serviceConnection.unbindFromService();
            serviceConnection = null;
        }
        boundPackage = null;
        openPgpApi = null;
        pendingResume = null;
        onBoundQueue.clear();
    }

    public interface PgpResultCallback<T> {
        void onSuccess(T result);

        void userInteractionRequired(PendingIntent pendingIntent);

        void onError(String message);
    }

    private interface PendingResume {
        void onResumed(int resultCode, Intent data);
    }
}
