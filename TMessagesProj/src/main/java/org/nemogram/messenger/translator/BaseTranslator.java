package org.nemogram.messenger.translator;

import com.google.gson.Gson;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import androidx.annotation.Nullable;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

abstract public class BaseTranslator {

    public static final Gson GSON = new Gson();

    abstract public Result translate(String query, String fl, String tl) throws Exception;

    abstract public List<String> getTargetLanguages();

    protected String URLEncode(String s) throws UnsupportedEncodingException {
        //noinspection CharsetObjectCanBeUsed
        return URLEncoder.encode(s, "UTF-8");
    }

    public boolean supportLanguage(String language) {
        return getTargetLanguages().contains(language);
    }

    public static class Http {
        private static OkHttpClient okHttpClient;
        private static Cache okHttpCache;

        public static Cache getOkHttpCache() {
            if (okHttpCache != null) return okHttpCache;
            long size50MiB = 50 * 1024 * 1024;
            okHttpCache = new Cache(new File(ApplicationLoader.applicationContext.getCacheDir(), "http_cache"), size50MiB);
            return okHttpCache;
        }

        private final Request.Builder builder;

        private Http(String url) {
            if (okHttpClient == null) {
                var builder = new OkHttpClient.Builder()
                        .cache(getOkHttpCache());
                okHttpClient = builder.build();
            }
            builder = new Request.Builder()
                    .url(url);
        }

        public static Http url(String url) {
            return new Http(url);
        }

        public Http header(String key, String value) {
            builder.header(key, value);
            return this;
        }

        public Http data(String data) {
            return data(data, "application/x-www-form-urlencoded");
        }

        public Http data(String data, String mediaType) {
            builder.post(RequestBody.create(data, MediaType.get(mediaType)));
            return this;
        }

        public String request() throws IOException {
            try (Response response = okHttpClient.newCall(builder.build()).execute()) {
                int code = response.code();
                if (code == 429) {
                    throw new Http429Exception();
                }
                var body = response.body();
                String bodyStr = body != null ? body.string() : null;
                if (code != 200) {
                    FileLog.e("Translator HTTP " + code + ": " + bodyStr);
                    throw new IOException("HTTP " + code + ": " + bodyStr);
                }
                return bodyStr;
            }
        }
    }

    public static class Result {
        public String translation;
        @Nullable
        public String sourceLanguage;

        public Result(String translation, @Nullable String sourceLanguage) {
            this.translation = translation;
            this.sourceLanguage = sourceLanguage;
        }
    }
}