package org.nemogram.messenger.helpers.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nemogram.messenger.Extra;
import org.nemogram.messenger.NemoConfig;

public class ConfigHelper {

    private static final String CONFIG_KEY = "get_config";
    private static final String CONFIG_UPDATE_TIME_KEY = CONFIG_KEY + "_update_time";
    private static final long CONFIG_TTL_MS = 24 * 60 * 60 * 1000;

    private static final SharedPreferences preferences =
            ApplicationLoader.applicationContext.getSharedPreferences("nemoremoteconfig", Context.MODE_PRIVATE);
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static final List<Long> DEFAULT_VERIFY_LIST = Arrays.asList(
            1349472891L,
            1339737452L,
            1302242053L,
            1715773134L
    );

    private static final int TYPE_NEWS = 0;
    private static final int TYPE_PROXY = 1;
    private static final int TYPE_SUGGESTION = 2;

    private static final class InstanceHolder {
        private static final ConfigHelper instance = new ConfigHelper();
    }

    public static ConfigHelper getInstance() {
        return InstanceHolder.instance;
    }

    public static boolean isChatCat(TLRPC.Chat chat) {
        return getVerify().stream().anyMatch(id -> id == chat.id || id == -2000000000000L - chat.id);
    }

    public static List<Long> getVerify() {
        Config config = getInstance().getConfig();
        if (config == null || config.verify == null) {
            return DEFAULT_VERIFY_LIST;
        }
        return config.verify;
    }

    public static List<Crypto> getCryptos() {
        Config config = getInstance().getConfig();
        if (config == null || config.cryptos == null) {
            return Collections.emptyList();
        }
        return config.cryptos;
    }

    private static List<News> getNews() {
        Config config = getInstance().getConfig();
        if (config == null || config.news == null) {
            return Collections.emptyList();
        }
        ArrayList<News> newsItems = new ArrayList<>();
        config.news.forEach(news -> {
            if (news.mcc != null && news.mcc != NemoConfig.userMcc) {
                return;
            }
            if (news.direct != null && news.direct && !Extra.isDirectApp()) {
                return;
            }
            if (news.source != null && news.source.equals(BuildConfig.BUILD_TYPE)) {
                return;
            }
            if (news.maxVersion != null && news.maxVersion < BuildConfig.VERSION_CODE) {
                return;
            }
            if (news.minVersion != null && news.minVersion > BuildConfig.VERSION_CODE) {
                return;
            }
            newsItems.add(news);
        });
        return newsItems;
    }

    public static List<News> getNewsForProxy() {
        return getNews()
                .stream()
                .filter(news -> news.type == TYPE_PROXY)
                .filter(news -> news.id == null || !preferences.getBoolean("news_dismissed_" + news.id, false))
                .toList();
    }

    public static List<News> getNewsForSettings() {
        return getNews()
                .stream()
                .filter(news -> news.type == TYPE_NEWS || news.type == TYPE_PROXY)
                .filter(news -> news.id == null || !preferences.getBoolean("news_dismissed_" + news.id, false))
                .toList();
    }

    public static TLRPC.TL_pendingSuggestion getNewsSuggestion() {
        return getNews()
                .stream()
                .filter(news -> news.type == TYPE_SUGGESTION)
                .filter(news -> news.id == null || !preferences.getBoolean("news_dismissed_" + news.id, false))
                .findAny()
                .map(news -> {
                    var suggestion = new TLRPC.TL_pendingSuggestion();
                    suggestion.title = new TLRPC.TL_textWithEntities();
                    suggestion.title.text = news.title;
                    suggestion.title.entities.addAll(parseBotAPIEntities(news.titleEntities, false));
                    suggestion.description = new TLRPC.TL_textWithEntities();
                    suggestion.description.text = news.summary;
                    suggestion.description.entities.addAll(parseBotAPIEntities(news.summaryEntities, false));
                    suggestion.url = news.url;
                    suggestion.suggestion = news.id;
                    return suggestion;
                })
                .orElse(null);
    }

    public static void removeNews(String id) {
        preferences.edit().putBoolean("news_dismissed_" + id, true).apply();
    }

    public void onLoadSuccess(String result) {
        if (result == null) {
            config = null;
            preferences.edit()
                    .remove(CONFIG_UPDATE_TIME_KEY)
                    .remove(CONFIG_KEY)
                    .apply();
            return;
        }
        preferences.edit()
                .putLong(CONFIG_UPDATE_TIME_KEY, System.currentTimeMillis())
                .putString(CONFIG_KEY, result)
                .apply();
        try {
            config = GSON.fromJson(result, Config.class);
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    private Config config;

    private Config getConfig() {
        if (config == null) {
            var json = getJSON();
            if (json == null) {
                return null;
            }
            try {
                config = GSON.fromJson(json, Config.class);
            } catch (Throwable t) {
                FileLog.e(t);
            }
        }
        return config;
    }

    private static String getJSON() {
        var json = preferences.getString(CONFIG_KEY, "");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        var updateTime = preferences.getLong(CONFIG_UPDATE_TIME_KEY, 0);
        if (Math.abs(System.currentTimeMillis() - updateTime) > CONFIG_TTL_MS) {
            return null;
        }
        return json;
    }

    private static List<TLRPC.MessageEntity> parseBotAPIEntities(MessageEntity[] botEntities, boolean emojiOnly) {
        if (botEntities == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(botEntities)
                .filter(e -> !emojiOnly || e.customEmojiId != null)
                .map(e -> {
                    var entity = switch (e.type) {
                        case "mention" -> new TLRPC.TL_messageEntityMention();
                        case "hashtag" -> new TLRPC.TL_messageEntityHashtag();
                        case "cashtag" -> new TLRPC.TL_messageEntityCashtag();
                        case "bot_command" -> new TLRPC.TL_messageEntityBotCommand();
                        case "url" -> new TLRPC.TL_messageEntityUrl();
                        case "email" -> new TLRPC.TL_messageEntityEmail();
                        case "phone_number" -> new TLRPC.TL_messageEntityPhone();
                        case "bold" -> new TLRPC.TL_messageEntityBold();
                        case "italic" -> new TLRPC.TL_messageEntityItalic();
                        case "underline" -> new TLRPC.TL_messageEntityUnderline();
                        case "strikethrough" -> new TLRPC.TL_messageEntityStrike();
                        case "spoiler" -> new TLRPC.TL_messageEntitySpoiler();
                        case "text_link" -> new TLRPC.TL_messageEntityTextUrl();
                        case "custom_emoji" -> {
                            var emoji = new TLRPC.TL_messageEntityCustomEmoji();
                            emoji.document_id = e.customEmojiId;
                            yield emoji;
                        }
                        default -> new TLRPC.TL_messageEntityUnknown();
                    };
                    entity.offset = e.offset;
                    entity.length = e.length;
                    entity.url = e.url;
                    return entity;
                })
                .toList();
    }

    public static class News {
        @SerializedName("id")
        @Expose
        public String id;
        @SerializedName("title")
        @Expose
        public String title;
        @SerializedName("title_entities")
        @Expose
        public MessageEntity[] titleEntities;
        @SerializedName("summary")
        @Expose
        public String summary;
        @SerializedName("summary_entities")
        @Expose
        public MessageEntity[] summaryEntities;
        @SerializedName("type")
        @Expose
        public Integer type;
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("language")
        @Expose
        public String language;
        @SerializedName("mcc")
        @Expose
        public Integer mcc;
        @SerializedName("direct")
        @Expose
        public Boolean direct;
        @SerializedName("source")
        @Expose
        public String source;
        @SerializedName("max_version")
        @Expose
        public Integer maxVersion;
        @SerializedName("min_version")
        @Expose
        public Integer minVersion;
    }

    public static class Crypto {
        @SerializedName("currency")
        @Expose
        public String currency;
        @SerializedName("chain")
        @Expose
        public String chain;
        @SerializedName("address")
        @Expose
        public String address;
    }

    public static class Config {
        @SerializedName("verify")
        @Expose
        public List<Long> verify;
        @SerializedName("news")
        @Expose
        public List<News> news;
        @SerializedName("cryptos")
        @Expose
        public List<Crypto> cryptos;
    }

    public static class MessageEntity {
        @SerializedName("type")
        @Expose
        public String type;
        @SerializedName("offset")
        @Expose
        public Integer offset;
        @SerializedName("length")
        @Expose
        public Integer length;
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("custom_emoji_id")
        @Expose
        public Long customEmojiId;
    }
}
