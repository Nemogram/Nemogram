package org.nemogram.messenger.pgp;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.telegram.messenger.ApplicationLoader;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PgpConfig {

    private static final String PREFS_NAME = "nemo_pgp";
    private static final Object sync = new Object();
    public static String providerPackage;
    public static long myKeyId;
    public static String myUserId;
    private static SharedPreferences preferences;
    private static Gson gson;
    private static boolean loaded;
    private static Map<Long, Long> dialogKeys = new HashMap<>();
    private static Set<Long> encryptedDialogs = new HashSet<>();

    private static void ensureLoaded() {
        synchronized (sync) {
            if (loaded) {
                return;
            }
            preferences = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            gson = new GsonBuilder().create();

            providerPackage = preferences.getString("providerPackage", null);
            myKeyId = preferences.getLong("myKeyId", 0);
            myUserId = preferences.getString("myUserId", null);

            dialogKeys = readLongLongMap(preferences.getString("dialogKeys", null));
            encryptedDialogs = readLongSet(preferences.getString("encryptedDialogs", null));

            loaded = true;
        }
    }

    private static Map<Long, Long> readLongLongMap(String json) {
        if (json == null) {
            return new HashMap<>();
        }
        try {
            Type type = new TypeToken<HashMap<Long, Long>>() {
            }.getType();
            Map<Long, Long> map = gson.fromJson(json, type);
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static Set<Long> readLongSet(String json) {
        if (json == null) {
            return new HashSet<>();
        }
        try {
            Type type = new TypeToken<HashSet<Long>>() {
            }.getType();
            Set<Long> set = gson.fromJson(json, type);
            return set != null ? set : new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    public static boolean isProviderConfigured() {
        ensureLoaded();
        return providerPackage != null && !providerPackage.isEmpty();
    }

    public static String getProviderPackage() {
        ensureLoaded();
        return providerPackage;
    }

    public static void setProviderPackage(String packageName) {
        ensureLoaded();
        providerPackage = packageName;
        preferences.edit().putString("providerPackage", packageName).apply();
        // switching providers nukes the previously selected key
        setMyKey(0, null);
    }

    public static long getMyKeyId() {
        ensureLoaded();
        return myKeyId;
    }

    public static String getMyUserId() {
        ensureLoaded();
        return myUserId;
    }

    public static void setMyKey(long keyId, String userId) {
        ensureLoaded();
        myKeyId = keyId;
        myUserId = userId;
        preferences.edit()
                .putLong("myKeyId", keyId)
                .putString("myUserId", userId)
                .apply();
    }

    public static boolean isDialogEncrypted(long dialogId) {
        ensureLoaded();
        return encryptedDialogs.contains(dialogId);
    }

    public static void setDialogEncrypted(long dialogId, boolean encrypted) {
        ensureLoaded();
        if (encrypted) {
            encryptedDialogs.add(dialogId);
        } else {
            encryptedDialogs.remove(dialogId);
        }
        preferences.edit().putString("encryptedDialogs", gson.toJson(encryptedDialogs)).apply();
    }

    public static long getDialogKeyId(long dialogId) {
        ensureLoaded();
        Long keyId = dialogKeys.get(dialogId);
        return keyId != null ? keyId : 0;
    }

    public static void setDialogKeyId(long dialogId, long keyId) {
        ensureLoaded();
        if (keyId == 0) {
            dialogKeys.remove(dialogId);
        } else {
            dialogKeys.put(dialogId, keyId);
        }
        preferences.edit().putString("dialogKeys", gson.toJson(dialogKeys)).apply();
    }

    public static void resetAll() {
        ensureLoaded();
        providerPackage = null;
        myKeyId = 0;
        myUserId = null;
        dialogKeys = new HashMap<>();
        encryptedDialogs = new HashSet<>();
        preferences.edit().clear().apply();
        PgpServiceManager.getInstance().resetConnection();
    }
}
