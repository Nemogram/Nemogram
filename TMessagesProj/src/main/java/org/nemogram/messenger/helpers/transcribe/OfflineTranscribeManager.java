package org.nemogram.messenger.helpers.transcribe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;

import org.opentranscribe.api.TranscriberCapabilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OfflineTranscribeManager {

    public static final String SUGGESTED_PACKAGE = "org.scrib.transcriber";
    public static final String SUGGESTED_FDROID_URL = "https://f-droid.org/packages/" + SUGGESTED_PACKAGE + "/";

    private static final String PROVIDER_PREF = "offlineSttProvider";
    private static final long CACHE_TTL_MS = 15_000L;

    private static final ConcurrentHashMap<String, AidlOfflineTranscriber> transcribers = new ConcurrentHashMap<>();

    private static volatile String cachedId;
    private static volatile OfflineTranscribeProvider cachedProvider;
    private static volatile long cachedAt;

    public static List<OfflineTranscribeProvider> availableProviders() {
        List<OfflineTranscribeProvider> result = new ArrayList<>();
        var context = ApplicationLoader.applicationContext;
        if (context == null) {
            return result;
        }
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> services;
        try {
            services = pm.queryIntentServices(new Intent(AidlOfflineTranscriber.ACTION), 0);
        } catch (Exception e) {
            return result;
        }
        for (ResolveInfo resolved : services) {
            if (resolved.serviceInfo == null) {
                continue;
            }
            CharSequence label;
            try {
                label = resolved.serviceInfo.applicationInfo.loadLabel(pm);
            } catch (Exception e) {
                label = resolved.serviceInfo.packageName;
            }
            result.add(new OfflineTranscribeProvider(resolved.serviceInfo.packageName, resolved.serviceInfo.name, label));
        }
        return result;
    }

    public static String selectedProviderId() {
        return MessagesController.getGlobalMainSettings().getString(PROVIDER_PREF, "");
    }

    public static void setProvider(OfflineTranscribeProvider provider) {
        MessagesController.getGlobalMainSettings().edit()
                .putString(PROVIDER_PREF, provider != null ? provider.id() : "")
                .apply();
        invalidate();
    }

    public static OfflineTranscribeProvider selectedProvider() {
        String id = selectedProviderId();
        if (id.isEmpty()) {
            return null;
        }
        long now = SystemClock.elapsedRealtime();
        if (id.equals(cachedId) && now - cachedAt < CACHE_TTL_MS) {
            return cachedProvider;
        }
        OfflineTranscribeProvider resolved = null;
        for (OfflineTranscribeProvider provider : availableProviders()) {
            if (provider.id().equals(id)) {
                resolved = provider;
                break;
            }
        }
        cachedId = id;
        cachedProvider = resolved;
        cachedAt = now;
        return resolved;
    }

    public static String selectedProviderLabel() {
        OfflineTranscribeProvider provider = selectedProvider();
        return provider != null ? String.valueOf(provider.label) : null;
    }

    public static boolean isEnabled() {
        return !selectedProviderId().isEmpty();
    }

    public static boolean isAnyProviderInstalled() {
        return !availableProviders().isEmpty();
    }

    public static boolean isActive() {
        return selectedProvider() != null;
    }

    public static void invalidate() {
        cachedId = null;
        cachedProvider = null;
        cachedAt = 0L;
    }

    public static TranscriberCapabilities capabilitiesOf(OfflineTranscribeProvider provider) {
        AidlOfflineTranscriber transcriber = transcriberFor(provider);
        return transcriber != null ? transcriber.capabilities() : null;
    }

    public static OfflineTranscribeSession requestTranscription(String audioFilePath, String languageHint,
                                                                  Consumer<String> onProgress,
                                                                  BiConsumer<String, Exception> onFinal) {
        OfflineTranscribeProvider provider = selectedProvider();
        if (provider == null) {
            return null;
        }
        AidlOfflineTranscriber transcriber = transcriberFor(provider);
        if (transcriber == null) {
            return null;
        }
        return transcriber.requestTranscription(audioFilePath, languageHint, onProgress, onFinal);
    }

    private static AidlOfflineTranscriber transcriberFor(OfflineTranscribeProvider provider) {
        return transcribers.computeIfAbsent(provider.id(), key -> new AidlOfflineTranscriber(provider.packageName));
    }
}
