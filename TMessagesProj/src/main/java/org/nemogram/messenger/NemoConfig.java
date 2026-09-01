package org.nemogram.messenger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import org.nemogram.messenger.helpers.LensHelper;
import org.nemogram.messenger.translator.Translator;
import org.nemogram.messenger.translator.TranslatorApps;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import app.nekogram.translator.DeepLTranslator;

public class NemoConfig {
    public static final int TITLE_TYPE_TEXT = 0;
    public static final int TITLE_TYPE_ICON = 1;
    public static final int TITLE_TYPE_MIX = 2;

    public static final int ID_TYPE_HIDDEN = 0;
    public static final int ID_TYPE_API = 1;
    public static final int ID_TYPE_BOTAPI = 2;

    public static final int TRANS_TYPE_NEMO = 0;
    public static final int TRANS_TYPE_TG = 1;
    public static final int TRANS_TYPE_EXTERNAL = 2;

    public static final int DOUBLE_TAP_ACTION_NONE = 0;
    public static final int DOUBLE_TAP_ACTION_REACTION = 1;
    public static final int DOUBLE_TAP_ACTION_TRANSLATE = 2;
    public static final int DOUBLE_TAP_ACTION_REPLY = 3;
    public static final int DOUBLE_TAP_ACTION_SAVE = 4;
    public static final int DOUBLE_TAP_ACTION_REPEAT = 5;
    public static final int DOUBLE_TAP_ACTION_EDIT = 6;

    public static final int SEARCH_BAR_NORMAL = 0;
    public static final int SEARCH_BAR_COMPACT = 1;
    public static final int SEARCH_BAR_MATERIAL = 2;

    public static final int BOOST_NONE = 0;
    public static final int BOOST_AVERAGE = 1;
    public static final int BOOST_EXTREME = 2;

    public static final int TRANSCRIBE_AUTO = 0;
    public static final int TRANSCRIBE_PREMIUM = 1;
    public static final int TRANSCRIBE_WORKERSAI = 2;
    public static final int TRANSCRIBE_LOCAL = 3;



    private static final Object sync = new Object();
    public static boolean preferIPv6 = false;

    public static boolean useSystemEmoji = false;
    public static boolean ignoreBlocked = false;
    public static boolean hideKeyboardOnChatScroll = false;
    public static boolean rearVideoMessages = false;
    public static boolean hideAllTab = false;
    public static boolean confirmAVMessage = false;
    public static boolean askBeforeCall = true;
    public static boolean disableNumberRounding = false;
    public static boolean disableGreetingSticker = false;
    public static boolean autoTranslate = true;
    public static float stickerSize = 14.0f;
    public static float gifSize = 17.5f;
    public static String translationProvider = Translator.PROVIDER_GOOGLE;
    public static String translationTarget = "app";
    public static int deepLFormality = DeepLTranslator.FORMALITY_DEFAULT;
    public static int tabsTitleType = TITLE_TYPE_MIX;
    public static int idType = ID_TYPE_API;
    public static int maxRecentStickers = 20;
    public static int transType = TRANS_TYPE_NEMO;
    public static int doubleTapInAction = DOUBLE_TAP_ACTION_REACTION;
    public static int doubleTapOutAction = DOUBLE_TAP_ACTION_REACTION;
    public static int downloadSpeedBoost = BOOST_NONE;
    public static Set<String> restrictedLanguages;
    public static Set<String> blockedKeywordsChats;
    public static Set<String> blockedKeywordsChannels;
    private static Set<String> blockedKeywordsChatsLower = new HashSet<>();
    private static Set<String> blockedKeywordsChannelsLower = new HashSet<>();
    public static String externalTranslationProvider;
    public static int transcribeProvider = TRANSCRIBE_PREMIUM;
    public static String cfAccountID = "";
    public static String cfApiToken = "";


    public static boolean showAddToSavedMessages = true;
    public static boolean showSetReminder = false;
    public static boolean showReport = false;
    public static boolean showDeleteDownloadedFile = false;
    public static boolean showMessageDetails = false;
    public static boolean showTranslate = true;
    public static boolean showRepeat = true;
    public static boolean showNoQuoteForward = false;
    public static boolean showCopyPhoto = false;
    public static boolean showQrCode = false;
    public static boolean showOpenIn = false;

    public static boolean openArchiveOnPull = false;
    public static boolean hideGifts = false;
    public static boolean musicViewAlternativeLayout = false;
    public static int nameOrder = 1;
    public static boolean mediaPreview = true;
    public static boolean autoPauseVideo = true;
    public static boolean disableProximityEvents = false;
    public static boolean voiceEnhancements = false;
    public static boolean disableInstantCamera = false;
    public static boolean tryToOpenAllLinksInIV = false;
    public static boolean formatTimeWithSeconds = false;
    public static boolean accentAsNotificationColor = false;
    public static boolean silenceNonContacts = false;
    public static boolean disableJumpToNextChannel = false;
    public static boolean disableVoiceMessageAutoPlay = false;
    public static boolean unmuteVideosWithVolumeButtons = true;
    public static boolean hideTimeOnSticker = false;
    public static boolean showOriginal = true;
    public static boolean hideStories = false;
    public static boolean quickForward = false;
    public static boolean reducedColors = false;
    public static boolean ignoreContentRestriction = false;
    public static boolean showTimeHint = false;
    public static boolean preferOriginalQuality = false;
    public static boolean autoInlineBot = false;
    public static boolean forceFontWeightFallback = false;
    public static boolean minimizedStickerCreator = false;
    public static boolean miniSenderAvatar = false;
    public static boolean hideChannelBottomButtons = false;
    public static boolean hideAiButton = false;
    public static boolean keepFormatting = true;
    public static boolean localCustomEmoji = false;
    public static boolean predictiveBackAnimation = false;
    public static boolean hideBottomNavigationBar = false;
    public static boolean bottomFilterTabs = false;
    public static boolean hideFolderUnreadBadge = false;
    public static boolean strokeOnViews = false;
    public static boolean legacyInputPanel = false;
    public static boolean legacyChatActionBar = false;
    public static boolean disableGooeyAvatarAnimation = false;
    public static boolean filterKeywordsInChats = false;
    public static boolean filterKeywordsInChannels = false;
    public static boolean spoilerKeywordsInChats = false;
    public static boolean spoilerKeywordsInChannels = false;
    public static boolean autoCheckUpdates = true;
    public static int autoCheckUpdatesIntervalHours = 6;
    public static boolean hideSearchBarOnScroll = true;
    public static boolean hideSearchBarPlaceholder = false;
    public static String searchBarPlaceholder = "";
    public static boolean moreHapticFeedbacks = false;
    public static boolean highRoundVideoBitrate = true;

    public static int userMcc = 0;
    public static int searchBarStyle = SEARCH_BAR_NORMAL;

    public static String dialogsMenuOrder = "";
    public static Set<String> dialogsMenuHiddenItems = new HashSet<>();


    private static boolean configLoaded;
    private static Gson gson;

    static {
        loadConfig(false);
    }

    // an imported config can hold the wrong type or a value the seekbars can never produce,
    // both of which blow up later (getFloat throws, an out of range size overflows the sticker layout)
    private static float readSize(SharedPreferences preferences, String key, float defaultValue, float min, float max) {
        float value;
        try {
            value = preferences.getFloat(key, defaultValue);
        } catch (ClassCastException e) {
            FileLog.e(e);
            preferences.edit().remove(key).apply();
            value = defaultValue;
        }
        if (Float.isNaN(value)) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

    public static void loadConfig(boolean force) {
        synchronized (sync) {
            if (configLoaded && !force) {
                return;
            }
            userMcc = ApplicationLoader.applicationContext.getResources().getConfiguration().mcc;

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
            preferIPv6 = preferences.getBoolean("preferIPv6", false);
            ignoreBlocked = preferences.getBoolean("ignoreBlocked2", false);
            hideGifts = preferences.getBoolean("hideGifts", false);
            musicViewAlternativeLayout = preferences.getBoolean("musicViewAlternativeLayout", false);
            nameOrder = preferences.getInt("nameOrder", 1);
            showAddToSavedMessages = preferences.getBoolean("showAddToSavedMessages", true);
            showSetReminder = preferences.getBoolean("showSetReminder", false);
            showReport = preferences.getBoolean("showReport", false);
            showDeleteDownloadedFile = preferences.getBoolean("showDeleteDownloadedFile", false);
            showMessageDetails = preferences.getBoolean("showMessageDetails", false);
            showTranslate = preferences.getBoolean("showTranslate", true);
            showRepeat = preferences.getBoolean("showRepeat", true);
            stickerSize = readSize(preferences, "stickerSize", 14.0f, 2.0f, 20.0f);
            gifSize = readSize(preferences, "gifSize", 17.5f, 14.0f, 20.0f);
            translationProvider = preferences.getString("translationProvider2", Translator.PROVIDER_GOOGLE);
            openArchiveOnPull = preferences.getBoolean("openArchiveOnPull", false);
            hideKeyboardOnChatScroll = preferences.getBoolean("hideKeyboardOnChatScroll", false);
            useSystemEmoji = preferences.getBoolean("useSystemEmoji", false);
            rearVideoMessages = preferences.getBoolean("rearVideoMessages", false);
            hideAllTab = preferences.getBoolean("hideAllTab", false);
            tabsTitleType = preferences.getInt("tabsTitleType2", TITLE_TYPE_MIX);
            confirmAVMessage = preferences.getBoolean("confirmAVMessage", false);
            askBeforeCall = preferences.getBoolean("askBeforeCall", true);
            disableNumberRounding = preferences.getBoolean("disableNumberRounding", false);
            mediaPreview = preferences.getBoolean("mediaPreview", true);
            idType = preferences.getInt("idType", ID_TYPE_API);
            autoPauseVideo = preferences.getBoolean("autoPauseVideo", true);
            disableProximityEvents = preferences.getBoolean("disableProximityEvents", false);
            voiceEnhancements = preferences.getBoolean("voiceEnhancements", false);
            disableInstantCamera = preferences.getBoolean("disableInstantCamera", false);
            tryToOpenAllLinksInIV = preferences.getBoolean("tryToOpenAllLinksInIV", false);
            formatTimeWithSeconds = preferences.getBoolean("formatTimeWithSeconds", false);
            accentAsNotificationColor = preferences.getBoolean("accentAsNotificationColor", false);
            silenceNonContacts = preferences.getBoolean("silenceNonContacts", false);
            showNoQuoteForward = preferences.getBoolean("showNoQuoteForward", false);
            translationTarget = preferences.getString("translationTarget", "app");
            maxRecentStickers = preferences.getInt("maxRecentStickers", 20);
            disableJumpToNextChannel = preferences.getBoolean("disableJumpToNextChannel", false);
            disableGreetingSticker = preferences.getBoolean("disableGreetingSticker", false);
            autoTranslate = preferences.getBoolean("autoTranslate", true);
            disableVoiceMessageAutoPlay = preferences.getBoolean("disableVoiceMessageAutoPlay", false);
            unmuteVideosWithVolumeButtons = preferences.getBoolean("unmuteVideosWithVolumeButtons", true);
            transType = preferences.getInt("transType", TRANS_TYPE_NEMO);
            deepLFormality = preferences.getInt("deepLFormality", DeepLTranslator.FORMALITY_DEFAULT);
            showCopyPhoto = preferences.getBoolean("showCopyPhoto", false);
            doubleTapInAction = preferences.getInt("doubleTapAction", DOUBLE_TAP_ACTION_REACTION);
            doubleTapOutAction = preferences.getInt("doubleTapOutAction", doubleTapInAction);
            restrictedLanguages = preferences.getStringSet("restrictedLanguages", null);
            blockedKeywordsChats = preferences.getStringSet("blockedKeywordsChats", new HashSet<>());
            blockedKeywordsChannels = preferences.getStringSet("blockedKeywordsChannels", new HashSet<>());
            rebuildBlockedKeywordsLowerCache();
            filterKeywordsInChats = preferences.getBoolean("filterKeywordsInChats", false);
            filterKeywordsInChannels = preferences.getBoolean("filterKeywordsInChannels", false);
            spoilerKeywordsInChats = preferences.getBoolean("spoilerKeywordsInChats", false);
            spoilerKeywordsInChannels = preferences.getBoolean("spoilerKeywordsInChannels", false);
            hideTimeOnSticker = preferences.getBoolean("hideTimeOnSticker", false);
            showOriginal = preferences.getBoolean("showOriginal", true);
            downloadSpeedBoost = preferences.getInt("downloadSpeedBoost2", BOOST_NONE);
            showQrCode = preferences.getBoolean("showQrCode", false);
            showOpenIn = preferences.getBoolean("showOpenIn", false);
            hideStories = preferences.getBoolean("hideStories", false);
            quickForward = preferences.getBoolean("quickForward", false);
            reducedColors = preferences.getBoolean("reducedColors", false);
            ignoreContentRestriction = preferences.getBoolean("ignoreContentRestriction", false);
            externalTranslationProvider = preferences.getString("externalTranslationProvider", "");
            TranslatorApps.loadTranslatorAppsAsync();
            showTimeHint = preferences.getBoolean("showTimeHint", false);
            transcribeProvider = preferences.getInt("transcribeProvider", TRANSCRIBE_PREMIUM);
            cfAccountID = preferences.getString("cfAccountID", "");
            cfApiToken = preferences.getString("cfApiToken", "");
            preferOriginalQuality = preferences.getBoolean("preferOriginalQuality", false);
            autoInlineBot = preferences.getBoolean("autoInlineBot", false);
            forceFontWeightFallback = preferences.getBoolean("forceFontWeightFallback", false);
            minimizedStickerCreator = preferences.getBoolean("minimizedStickerCreator", false);
            miniSenderAvatar = preferences.getBoolean("miniSenderAvatar", false);
            hideChannelBottomButtons = preferences.getBoolean("hideChannelBottomButtons", false);
            hideAiButton = preferences.getBoolean("hideAiButton", false);
            keepFormatting = preferences.getBoolean("keepFormatting", true);
            localCustomEmoji = preferences.getBoolean("localCustomEmoji", false);
            predictiveBackAnimation = preferences.getBoolean("predictiveBackAnimation", false);
            hideBottomNavigationBar = preferences.getBoolean("hideBottomNavigationBar", false);
            bottomFilterTabs = preferences.getBoolean("bottomFilterTabs", false);
            hideFolderUnreadBadge = preferences.getBoolean("hideFolderUnreadBadge", false);
            strokeOnViews = preferences.getBoolean("strokeOnViews", false);
            legacyInputPanel = preferences.getBoolean("legacyInputPanel", false);
            legacyChatActionBar = preferences.getBoolean("legacyChatActionBar", false);
            disableGooeyAvatarAnimation = preferences.getBoolean("disableGooeyAvatarAnimation", SharedConfig.getDevicePerformanceClass() <= SharedConfig.PERFORMANCE_CLASS_AVERAGE);
            autoCheckUpdates = preferences.getBoolean("autoCheckUpdates", true);
            autoCheckUpdatesIntervalHours = preferences.getInt("autoCheckUpdatesIntervalHours", 6);
            searchBarStyle = preferences.getInt("searchBarStyle", SEARCH_BAR_NORMAL);
            hideSearchBarOnScroll = preferences.getBoolean("hideSearchBarOnScroll", true);
            hideSearchBarPlaceholder = preferences.getBoolean("hideSearchBarPlaceholder", false);
            searchBarPlaceholder = preferences.getString("searchBarPlaceholder", "");
            moreHapticFeedbacks = preferences.getBoolean("moreHapticFeedbacks", true);
            highRoundVideoBitrate = preferences.getBoolean("highRoundVideoBitrate", true);
            dialogsMenuOrder = preferences.getString("dialogsMenuOrder", "");
            if (preferences.contains("dialogsMenuHiddenItems")) {
                dialogsMenuHiddenItems = new HashSet<>(preferences.getStringSet("dialogsMenuHiddenItems", new HashSet<>()));
            } else {
                dialogsMenuHiddenItems = new HashSet<>(java.util.Arrays.asList(DialogsMenuItems.NEMO_SETTINGS, DialogsMenuItems.REPLIES));
            }

            LensHelper.checkLensSupportAsync();

            configLoaded = true;
        }
    }

    // magic header identifying our obfuscated settings export
    // also doubles as an XOR key extension
    private static final byte[] FISH_MAGIC = {'N', 'M', 'F', 'I', 'S', 'H', '1'};
    private static final byte FISH_XOR_KEY = (byte) 0x5A;
    public static final String FISH_EXTENSION = ".fish";

    public static String exportConfigs() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create();
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        Map<String, ?> all = preferences.getAll();
        return gson.toJson(all);
    }

    public static byte[] obfuscateExport(String json) {
        byte[] payload = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] out = new byte[FISH_MAGIC.length + payload.length];
        System.arraycopy(FISH_MAGIC, 0, out, 0, FISH_MAGIC.length);
        for (int i = 0; i < payload.length; i++) {
            byte key = (byte) (FISH_XOR_KEY ^ FISH_MAGIC[i % FISH_MAGIC.length] ^ (i & 0xFF));
            out[FISH_MAGIC.length + i] = (byte) (payload[i] ^ key);
        }
        return out;
    }

    public static String deobfuscateExport(byte[] data) {
        if (data == null || data.length < FISH_MAGIC.length) {
            return null;
        }
        for (int i = 0; i < FISH_MAGIC.length; i++) {
            if (data[i] != FISH_MAGIC[i]) {
                return null;
            }
        }
        int payloadLen = data.length - FISH_MAGIC.length;
        byte[] payload = new byte[payloadLen];
        for (int i = 0; i < payloadLen; i++) {
            byte key = (byte) (FISH_XOR_KEY ^ FISH_MAGIC[i % FISH_MAGIC.length] ^ (i & 0xFF));
            payload[i] = (byte) (data[FISH_MAGIC.length + i] ^ key);
        }
        return new String(payload, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static String readFishExportFile(java.io.File file) {
        if (file == null || !file.exists() || file.length() == 0 || file.length() > 1024 * 1024) {
            return null;
        }
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = fis.read(data);
            if (read != data.length) {
                return null;
            }
            String json = deobfuscateExport(data);
            if (json == null) {
                return null;
            }
            if (gson == null) {
                gson = new GsonBuilder()
                        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                        .create();
            }
            Map<?, ?> map = gson.fromJson(json, Map.class);
            return map != null && !map.isEmpty() ? json : null;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    public static void importConfigs(String config) {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create();
        }
        //noinspection unchecked
        Map<String, ?> map = gson.fromJson(config, Map.class);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        var editor = preferences.edit();
        editor.clear();
        map.forEach((BiConsumer<String, Object>) (s, o) -> {
            try {
                if (o instanceof Integer) {
                    editor.putInt(s, (Integer) o);
                } else if (o instanceof String) {
                    editor.putString(s, (String) o);
                } else if (o instanceof Boolean) {
                    editor.putBoolean(s, (Boolean) o);
                } else if (o instanceof Long) {
                    if ("stickerSize".equals(s) || "gifSize".equals(s)) {
                        // these are read back with getFloat, storing them as int makes it throw
                        editor.putFloat(s, ((Long) o).floatValue());
                    } else {
                        editor.putInt(s, ((Long) o).intValue());
                    }
                } else if (o instanceof Float) {
                    editor.putFloat(s, (Float) o);
                } else if (o instanceof Double) {
                    editor.putFloat(s, ((Double) o).floatValue());
                } else if (o instanceof ArrayList) {
                    //noinspection unchecked
                    editor.putStringSet(s, new HashSet<>((ArrayList<String>) o));
                } else {
                    FileLog.e("error putting " + s + " " + o.getClass().getName());
                }
            } catch (Exception e) {
                FileLog.e("error putting " + s, e);
            }
        });
        editor.apply();
        loadConfig(true);
    }

    public static void setTranscribeProvider(int provider) {
        transcribeProvider = provider;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("transcribeProvider", transcribeProvider);
        editor.apply();
    }

    public static void setCfAccountID(String accountID) {
        cfAccountID = accountID;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cfAccountID", cfAccountID);
        editor.apply();
    }

    public static void setCfApiToken(String apiToken) {
        cfApiToken = apiToken;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cfApiToken", cfApiToken);
        editor.apply();
    }

    public static void setExternalTranslationProvider(String provider) {
        externalTranslationProvider = provider;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("externalTranslationProvider", externalTranslationProvider);
        editor.apply();
    }

    public static List<String> getDialogsMenuOrder() {
        List<String> order = new ArrayList<>();
        if (!TextUtils.isEmpty(dialogsMenuOrder)) {
            for (String id : dialogsMenuOrder.split(",")) {
                if (!TextUtils.isEmpty(id) && !order.contains(id)) {
                    order.add(id);
                }
            }
        }
        for (String id : DialogsMenuItems.DEFAULT_ORDER) {
            if (!order.contains(id)) {
                order.add(id);
            }
        }
        return order;
    }

    public static void saveDialogsMenuOrder(List<String> order) {
        dialogsMenuOrder = TextUtils.join(",", order);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("dialogsMenuOrder", dialogsMenuOrder);
        editor.apply();
    }

    public static boolean isDialogsMenuItemHidden(String id) {
        if (DialogsMenuItems.isLocked(id)) {
            return false;
        }
        return dialogsMenuHiddenItems.contains(id);
    }

    public static void setDialogsMenuItemHidden(String id, boolean hidden) {
        if (DialogsMenuItems.isLocked(id)) {
            return;
        }
        Set<String> set = new HashSet<>(dialogsMenuHiddenItems);
        if (hidden) {
            set.add(id);
        } else {
            set.remove(id);
        }
        dialogsMenuHiddenItems = set;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("dialogsMenuHiddenItems", set);
        editor.apply();
    }

    public static void resetDialogsMenuSettings() {
        dialogsMenuOrder = "";
        dialogsMenuHiddenItems = new HashSet<>(java.util.Arrays.asList(DialogsMenuItems.NEMO_SETTINGS, DialogsMenuItems.REPLIES));
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("dialogsMenuOrder");
        editor.remove("dialogsMenuHiddenItems");
        editor.apply();
    }

    public static void saveRestrictedLanguages(Set<String> languages) {
        restrictedLanguages = languages;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("restrictedLanguages", languages);
        editor.apply();
    }


    public static void setDoubleTapInAction(int action) {
        doubleTapInAction = action;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("doubleTapAction", doubleTapInAction);
        editor.apply();
    }

    public static void setDoubleTapOutAction(int action) {
        doubleTapOutAction = action;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("doubleTapOutAction", doubleTapOutAction);
        editor.apply();
    }

    public static void setTransType(int type) {
        transType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("transType", transType);
        editor.apply();
    }

    public static void setDownloadSpeedBoost(int boost) {
        downloadSpeedBoost = boost;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("downloadSpeedBoost2", boost);
        editor.apply();
    }

    public static void setBottomFilterTabs(boolean bottom) {
        bottomFilterTabs = bottom;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("bottomFilterTabs", bottomFilterTabs);
        editor.apply();
    }

    public static void toggleStrokeOnViews() {
        strokeOnViews = !strokeOnViews;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("strokeOnViews", strokeOnViews);
        editor.apply();
    }

    public static void toggleLegacyInputPanel() {
        legacyInputPanel = !legacyInputPanel;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("legacyInputPanel", legacyInputPanel);
        editor.apply();
    }

    public static void toggleLegacyChatActionBar() {
        legacyChatActionBar = !legacyChatActionBar;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("legacyChatActionBar", legacyChatActionBar);
        editor.apply();
    }

    public static void togglePredictiveBackAnimation() {
        predictiveBackAnimation = !predictiveBackAnimation;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("predictiveBackAnimation", predictiveBackAnimation);
        editor.apply();
    }

    public static void toggleHideBottomNavigationBar() {
        hideBottomNavigationBar = !hideBottomNavigationBar;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideBottomNavigationBar", hideBottomNavigationBar);
        editor.apply();
    }

    public static void toggleKeepFormatting() {
        keepFormatting = !keepFormatting;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("keepFormatting", keepFormatting);
        editor.apply();
    }

    public static void toggleLocalCustomEmoji() {
        localCustomEmoji = !localCustomEmoji;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("localCustomEmoji", localCustomEmoji);
        editor.apply();
    }

    public static void toggleHideChannelBottomButtons() {
        hideChannelBottomButtons = !hideChannelBottomButtons;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideChannelBottomButtons", hideChannelBottomButtons);
        editor.apply();
    }

    public static void toggleHideAiButton() {
        hideAiButton = !hideAiButton;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("hideAiButton", hideAiButton).apply();
    }

    public static void toggleMinimizedStickerCreator() {
        minimizedStickerCreator = !minimizedStickerCreator;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("minimizedStickerCreator", minimizedStickerCreator);
        editor.apply();
    }

    public static void toggleMiniSenderAvatar() {
        miniSenderAvatar = !miniSenderAvatar;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("miniSenderAvatar", miniSenderAvatar);
        editor.apply();
    }

    public static void toggleForceFontWeightFallback() {
        forceFontWeightFallback = !forceFontWeightFallback;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("forceFontWeightFallback", forceFontWeightFallback);
        editor.apply();
    }

    public static void toggleAutoInlineBot() {
        autoInlineBot = !autoInlineBot;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoInlineBot", autoInlineBot);
        editor.apply();
    }

    public static void togglePreferOriginalQuality() {
        preferOriginalQuality = !preferOriginalQuality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("preferOriginalQuality", preferOriginalQuality);
        editor.apply();
    }

    public static void toggleShowTimeHint() {
        showTimeHint = !showTimeHint;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showTimeHint", showTimeHint);
        editor.apply();
    }

    public static void toggleIgnoreContentRestriction() {
        ignoreContentRestriction = !ignoreContentRestriction;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ignoreContentRestriction", ignoreContentRestriction);
        editor.apply();
    }

    public static void toggleReducedColors() {
        reducedColors = !reducedColors;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("reducedColors", reducedColors);
        editor.apply();
    }

    public static void toggleQuickForward() {
        quickForward = !quickForward;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("quickForward", quickForward);
        editor.apply();
    }

    public static void toggleHideStories() {
        hideStories = !hideStories;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideStories", hideStories);
        editor.apply();
    }

    public static void toggleShowQrCode() {
        showQrCode = !showQrCode;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showQrCode", showQrCode);
        editor.apply();
    }

    public static void toggleShowOpenIn() {
        showOpenIn = !showOpenIn;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showOpenIn", showOpenIn);
        editor.apply();
    }

    public static void toggleShowOriginal() {
        showOriginal = !showOriginal;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showOriginal", showOriginal);
        editor.apply();
    }

    public static void toggleHideTimeOnSticker() {
        hideTimeOnSticker = !hideTimeOnSticker;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideTimeOnSticker", hideTimeOnSticker);
        editor.apply();
    }

    public static void toggleShowAddToSavedMessages() {
        showAddToSavedMessages = !showAddToSavedMessages;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showAddToSavedMessages", showAddToSavedMessages);
        editor.apply();
    }

    public static void toggleShowSetReminder() {
        showSetReminder = !showSetReminder;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showSetReminder", showSetReminder);
        editor.apply();
    }

    public static void toggleShowReport() {
        showReport = !showReport;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showReport", showReport);
        editor.apply();
    }

    public static void toggleShowDeleteDownloadedFile() {
        showDeleteDownloadedFile = !showDeleteDownloadedFile;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showDeleteDownloadedFile", showDeleteDownloadedFile);
        editor.apply();
    }

    public static void toggleShowMessageDetails() {
        showMessageDetails = !showMessageDetails;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showMessageDetails", showMessageDetails);
        editor.apply();
    }

    public static void toggleShowRepeat() {
        showRepeat = !showRepeat;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showRepeat", showRepeat);
        editor.apply();
    }

    public static void toggleIPv6() {
        preferIPv6 = !preferIPv6;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("preferIPv6", preferIPv6);
        editor.apply();
    }

    public static void toggleIgnoreBlocked() {
        ignoreBlocked = !ignoreBlocked;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ignoreBlocked2", ignoreBlocked);
        editor.apply();
    }

    public static void toggleHideGifts() {
        hideGifts = !hideGifts;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("hideGifts", hideGifts).apply();
    }

    public static void setNameOrder(int order) {
        nameOrder = order;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("nameOrder", nameOrder);
        editor.apply();
    }

    public static void toggleShowTranslate() {
        showTranslate = !showTranslate;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showTranslate", showTranslate);
        editor.apply();
    }

    public static void setStickerSize(float size) {
        stickerSize = size;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("stickerSize", stickerSize);
        editor.apply();
    }

    public static void setGifSize(float size) {
        gifSize = size;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("gifSize", gifSize);
        editor.apply();
    }

    public static void setTranslationProvider(String provider) {
        translationProvider = provider;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("translationProvider2", translationProvider);
        editor.apply();
    }

    public static void setTranslationTarget(String target) {
        translationTarget = target;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("translationTarget", translationTarget);
        editor.apply();
    }

    public static void setDeepLFormality(int formality) {
        deepLFormality = formality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("deepLFormality", deepLFormality);
        editor.apply();
    }

    public static void toggleOpenArchiveOnPull() {
        openArchiveOnPull = !openArchiveOnPull;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("openArchiveOnPull", openArchiveOnPull);
        editor.apply();
    }

    public static void toggleHideKeyboardOnChatScroll() {
        hideKeyboardOnChatScroll = !hideKeyboardOnChatScroll;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideKeyboardOnChatScroll", hideKeyboardOnChatScroll);
        editor.apply();
    }

    public static void toggleUseSystemEmoji() {
        useSystemEmoji = !useSystemEmoji;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemEmoji", useSystemEmoji);
        editor.apply();
    }

    public static void toggleRearVideoMessages() {
        rearVideoMessages = !rearVideoMessages;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("rearVideoMessages", rearVideoMessages);
        editor.apply();
    }

    public static void toggleHideAllTab() {
        hideAllTab = !hideAllTab;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideAllTab", hideAllTab);
        editor.apply();
    }

    public static void toggleHideFolderUnreadBadge() {
        hideFolderUnreadBadge = !hideFolderUnreadBadge;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideFolderUnreadBadge", hideFolderUnreadBadge);
        editor.apply();
    }

    public static void setTabsTitleType(int type) {
        tabsTitleType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("tabsTitleType2", tabsTitleType);
        editor.apply();
    }

    public static void toggleConfirmAVMessage() {
        confirmAVMessage = !confirmAVMessage;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("confirmAVMessage", confirmAVMessage);
        editor.apply();
    }

    public static void toggleAskBeforeCall() {
        askBeforeCall = !askBeforeCall;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("askBeforeCall", askBeforeCall);
        editor.apply();
    }

    public static void toggleDisableNumberRounding() {
        disableNumberRounding = !disableNumberRounding;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableNumberRounding", disableNumberRounding);
        editor.apply();
    }

    public static void toggleDisableGreetingSticker() {
        disableGreetingSticker = !disableGreetingSticker;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableGreetingSticker", disableGreetingSticker);
        editor.apply();
    }

    public static void toggleDisableGooeyAvatarAnimation() {
        disableGooeyAvatarAnimation = !disableGooeyAvatarAnimation;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableGooeyAvatarAnimation", disableGooeyAvatarAnimation);
        editor.apply();
    }

    public static void toggleMediaPreview() {
        mediaPreview = !mediaPreview;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("mediaPreview", mediaPreview);
        editor.apply();
    }

    public static void setIdType(int type) {
        idType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("idType", idType);
        editor.apply();
    }

    public static void toggleAutoPauseVideo() {
        autoPauseVideo = !autoPauseVideo;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoPauseVideo", autoPauseVideo);
        editor.apply();
    }

    public static void toggleDisableProximityEvents() {
        disableProximityEvents = !disableProximityEvents;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableProximityEvents", disableProximityEvents);
        editor.apply();
    }

    public static void toggleVoiceEnhancements() {
        voiceEnhancements = !voiceEnhancements;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("voiceEnhancements", voiceEnhancements);
        editor.apply();
    }

    public static void toggleDisabledInstantCamera() {
        disableInstantCamera = !disableInstantCamera;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableInstantCamera", disableInstantCamera);
        editor.apply();
    }

    public static void toggleTryToOpenAllLinksInIV() {
        tryToOpenAllLinksInIV = !tryToOpenAllLinksInIV;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("tryToOpenAllLinksInIV", tryToOpenAllLinksInIV);
        editor.apply();
    }

    public static void toggleFormatTimeWithSeconds() {
        formatTimeWithSeconds = !formatTimeWithSeconds;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("formatTimeWithSeconds", formatTimeWithSeconds);
        editor.apply();
    }

    public static void toggleAccentAsNotificationColor() {
        accentAsNotificationColor = !accentAsNotificationColor;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("accentAsNotificationColor", accentAsNotificationColor);
        editor.apply();
    }

    public static void toggleSilenceNonContacts() {
        silenceNonContacts = !silenceNonContacts;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("silenceNonContacts", silenceNonContacts);
        editor.apply();
    }

    public static void toggleDisableJumpToNextChannel() {
        disableJumpToNextChannel = !disableJumpToNextChannel;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableJumpToNextChannel", disableJumpToNextChannel);
        editor.apply();
    }

    public static void toggleShowNoQuoteForward() {
        showNoQuoteForward = !showNoQuoteForward;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showNoQuoteForward", showNoQuoteForward);
        editor.apply();
    }

    public static void toggleShowCopyPhoto() {
        showCopyPhoto = !showCopyPhoto;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showCopyPhoto", showCopyPhoto);
        editor.apply();
    }

    public static void toggleAutoTranslate() {
        autoTranslate = !autoTranslate;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoTranslate", autoTranslate);
        editor.apply();
    }

    public static void toggleDisableVoiceMessageAutoPlay() {
        disableVoiceMessageAutoPlay = !disableVoiceMessageAutoPlay;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableVoiceMessageAutoPlay", disableVoiceMessageAutoPlay);
        editor.apply();
    }

    public static void toggleUnmuteVideosWithVolumeButtons() {
        unmuteVideosWithVolumeButtons = !unmuteVideosWithVolumeButtons;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("unmuteVideosWithVolumeButtons", unmuteVideosWithVolumeButtons);
        editor.apply();
    }

    public static void setMaxRecentStickers(int size) {
        maxRecentStickers = size;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("maxRecentStickers", maxRecentStickers);
        editor.apply();
    }

    public static void saveBlockedKeywordsChats(Set<String> keywords) {
        blockedKeywordsChats = new HashSet<>(keywords);
        rebuildBlockedKeywordsLowerCache();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putStringSet("blockedKeywordsChats", blockedKeywordsChats).apply();
    }

    public static void saveBlockedKeywordsChannels(Set<String> keywords) {
        blockedKeywordsChannels = new HashSet<>(keywords);
        rebuildBlockedKeywordsLowerCache();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putStringSet("blockedKeywordsChannels", blockedKeywordsChannels).apply();
    }

    private static void rebuildBlockedKeywordsLowerCache() {
        Set<String> chatsLower = new HashSet<>();
        if (blockedKeywordsChats != null) {
            for (String keyword : blockedKeywordsChats) {
                if (keyword != null) {
                    chatsLower.add(keyword.toLowerCase(Locale.ROOT));
                }
            }
        }
        blockedKeywordsChatsLower = chatsLower;

        Set<String> channelsLower = new HashSet<>();
        if (blockedKeywordsChannels != null) {
            for (String keyword : blockedKeywordsChannels) {
                if (keyword != null) {
                    channelsLower.add(keyword.toLowerCase(Locale.ROOT));
                }
            }
        }
        blockedKeywordsChannelsLower = channelsLower;
    }

    public static boolean isKeywordBlockedInChats(String text) {
        if (blockedKeywordsChatsLower == null || blockedKeywordsChatsLower.isEmpty() || text == null)
            return false;
        var lower = text.toLowerCase(Locale.ROOT);
        for (var keyword : blockedKeywordsChatsLower) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    public static boolean isKeywordBlockedInChannels(String text) {
        if (blockedKeywordsChannelsLower == null || blockedKeywordsChannelsLower.isEmpty() || text == null)
            return false;
        var lower = text.toLowerCase(Locale.ROOT);
        for (var keyword : blockedKeywordsChannelsLower) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    public static void toggleFilterKeywordsInChats() {
        filterKeywordsInChats = !filterKeywordsInChats;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("filterKeywordsInChats", filterKeywordsInChats).apply();
    }

    public static void toggleFilterKeywordsInChannels() {
        filterKeywordsInChannels = !filterKeywordsInChannels;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("filterKeywordsInChannels", filterKeywordsInChannels).apply();
    }

    public static void toggleSpoilerKeywordsInChats() {
        spoilerKeywordsInChats = !spoilerKeywordsInChats;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("spoilerKeywordsInChats", spoilerKeywordsInChats).apply();
    }

    public static void toggleSpoilerKeywordsInChannels() {
        spoilerKeywordsInChannels = !spoilerKeywordsInChannels;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("spoilerKeywordsInChannels", spoilerKeywordsInChannels).apply();
    }

    public static void toggleAutoCheckUpdates() {
        autoCheckUpdates = !autoCheckUpdates;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("autoCheckUpdates", autoCheckUpdates).apply();
    }

    public static void setAutoCheckUpdatesIntervalHours(int hours) {
        autoCheckUpdatesIntervalHours = hours;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putInt("autoCheckUpdatesIntervalHours", autoCheckUpdatesIntervalHours).apply();
    }

    public static void setSearchBarStyle(int style) {
        searchBarStyle = style;
        SharedPreferences preferences = ApplicationLoader.applicationContext
                .getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putInt("searchBarStyle", searchBarStyle).apply();
    }

    public static void toggleHideSearchBarOnScroll() {
        hideSearchBarOnScroll = !hideSearchBarOnScroll;
        SharedPreferences preferences = ApplicationLoader.applicationContext
                .getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("hideSearchBarOnScroll", hideSearchBarOnScroll).apply();
    }

    public static void toggleHideSearchBarPlaceholder() {
        hideSearchBarPlaceholder = !hideSearchBarPlaceholder;
        SharedPreferences preferences = ApplicationLoader.applicationContext
                .getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("hideSearchBarPlaceholder", hideSearchBarPlaceholder).apply();
    }

    public static void setSearchBarPlaceholder(String text) {
        searchBarPlaceholder = text;
        SharedPreferences preferences = ApplicationLoader.applicationContext
                .getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putString("searchBarPlaceholder", searchBarPlaceholder).apply();
    }

    public static void toggleMusicViewAlternativeLayout() {
        musicViewAlternativeLayout = !musicViewAlternativeLayout;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("musicViewAlternativeLayout", musicViewAlternativeLayout).apply();
    }

    public static void toggleMoreHapticFeedbacks() {
        moreHapticFeedbacks = !moreHapticFeedbacks;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("moreHapticFeedbacks", moreHapticFeedbacks);
        editor.apply();
    }

    public static void toggleHighRoundVideoBitrate() {
        highRoundVideoBitrate = !highRoundVideoBitrate;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nemoconfig", Activity.MODE_PRIVATE);
        preferences.edit().putBoolean("highRoundVideoBitrate", highRoundVideoBitrate).apply();
    }

    public static int getNotificationColor() {
        if (accentAsNotificationColor) {
            int color = 0;
            if (Theme.getActiveTheme().hasAccentColors()) {
                color = Theme.getActiveTheme().getAccentColor(Theme.getActiveTheme().currentAccentId);
            }
            if (color == 0) {
                color = Theme.getColor(Theme.key_actionBarDefault) | 0xff000000;
            }
            float brightness = AndroidUtilities.computePerceivedBrightness(color);
            if (brightness >= 0.721f || brightness <= 0.279f) {
                color = Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader) | 0xff000000;
            }
            return color;
        } else {
            return 0xff11acfa;
        }
    }
}
