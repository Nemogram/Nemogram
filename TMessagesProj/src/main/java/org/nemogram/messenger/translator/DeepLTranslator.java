package org.nemogram.messenger.translator;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class DeepLTranslator extends BaseTranslator {

    public static final int FORMALITY_DEFAULT = 0;
    public static final int FORMALITY_MORE = 1;
    public static final int FORMALITY_LESS = 2;

    private static int formality = FORMALITY_DEFAULT;

    private static final Pattern iPattern = Pattern.compile("i");
    private static final String clientId = UUID.randomUUID().toString();

    private final List<String> targetLanguages = Arrays.asList(
            "bg", "cs", "da", "de", "el", "en-GB", "en-US", "es", "et",
            "fi", "fr", "hu", "id", "it", "ja", "ko", "lt", "lv", "nb",
            "nl", "pl", "pt-BR", "pt-PT", "ro", "ru", "sk", "sl", "sv",
            "tr", "uk", "zh-Hans", "zh-Hant", "ar", "hr", "en");

    private final AtomicLong requestId;

    private static final class InstanceHolder {
        private static final DeepLTranslator instance = new DeepLTranslator();
    }

    public DeepLTranslator() {
        this.requestId = new AtomicLong(ThreadLocalRandom.current().nextLong(1000000000L));
    }

    public static DeepLTranslator getInstance() {
        return InstanceHolder.instance;
    }

    public static void setFormality(int value) {
        formality = value;
    }

    @Override
    public List<String> getTargetLanguages() {
        return targetLanguages;
    }

    @Override
    public Result translate(String query, String fl, String tl) throws Exception {
        String sourceLang = TextUtils.isEmpty(fl) ? "auto" : fl.toLowerCase();
        String targetLang = tl.toLowerCase();

        String targetLangBase = targetLang;
        String targetLangVariant = null;
        if (targetLang.contains("-")) {
            String[] parts = targetLang.split("-");
            targetLangBase = parts[0];
            targetLangVariant = parts[0] + "-" + parts[1].toUpperCase();
        }

        long id = requestId.incrementAndGet();

        int iCount = 1;
        java.util.regex.Matcher m = iPattern.matcher(query);
        while (m.find()) iCount++;

        long ts = System.currentTimeMillis();
        long adjustedTs = ts - (ts % iCount) + iCount;

        JsonObject root = new JsonObject();
        JsonObject params = new JsonObject();
        JsonObject lang = new JsonObject();
        JsonArray texts = new JsonArray();

        lang.addProperty("source_lang_user_selected", sourceLang);
        lang.addProperty("target_lang", targetLangBase);

        JsonObject textObj = new JsonObject();
        textObj.addProperty("text", query);
        textObj.addProperty("requestAlternatives", 0);
        texts.add(textObj);

        params.add("texts", texts);
        params.addProperty("splitting", "newlines");

        JsonObject commonJobParams = new JsonObject();
        commonJobParams.add("regionalVariant", targetLangVariant == null ? null : GSON.toJsonTree(targetLangVariant));
        commonJobParams.addProperty("wasSpoken", Boolean.FALSE);
        String formalityStr;
        switch (formality) {
            case FORMALITY_MORE: formalityStr = "formal"; break;
            case FORMALITY_LESS: formalityStr = "informal"; break;
            default: formalityStr = null;
        }
        commonJobParams.addProperty("formality", formalityStr);
        params.add("commonJobParams", commonJobParams);
        params.add("lang", lang);
        params.addProperty("timestamp", adjustedTs);

        root.addProperty("jsonrpc", "2.0");
        root.addProperty("method", "LMT_handle_texts");
        root.add("params", params);
        root.addProperty("id", id);

        String body = root.toString();
        if ((id + 3) % 13 == 0 || (id + 5) % 29 == 0) {
            body = body.replace("\"method\":\"", "\"method\" : \"");
        } else {
            body = body.replace("\"method\":\"", "\"method\": \"");
        }


        int retries = 3;
        String response = null;
        while (retries-- > 0) {
            try {
                response = Http.url("https://www2.deepl.com/jsonrpc")
                        .header("Accept", "*/*")
                        .header("x-app-os-name", "android")
                        .header("x-app-os-version", "13")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("User-Agent", "DeepL-Android/24.3 Android 13 (Pixel 7 Pro)")
                        .header("client-traceparent", clientId)
                        .data(body, "application/json; charset=utf-8")
                        .request();
                break;
            } catch (Http429Exception e) {
                if (retries <= 0) throw e;
                Thread.sleep(1000);
            } catch (ConnectException | SocketTimeoutException e) {
                if (retries <= 0) throw e;
            }
        }


        if (response == null) {
            throw new IOException("DeepL: empty response");
        }

        RpcResponse rpcResponse;
        try {
            rpcResponse = GSON.fromJson(response, RpcResponse.class);
        } catch (Exception e) {
            throw new IOException("DeepL: unexpected response: " + response, e);
        }
        if (rpcResponse == null || rpcResponse.result == null) {
            String errMsg = rpcResponse != null && rpcResponse.error != null ? rpcResponse.error.toString() : "DeepL: null result";
            throw new IOException(errMsg);
        }
        if (rpcResponse.result.texts == null || rpcResponse.result.texts.isEmpty()) {
            throw new IOException("DeepL: empty texts in result");
        }
        String translated = rpcResponse.result.texts.get(0).text;
        String detectedLang = rpcResponse.result.lang;
        return new Result(translated, detectedLang != null ? detectedLang.toLowerCase() : null);
    }

    public static class RpcResponse {
        @SerializedName("result") @Expose public RpcResult result;
        @SerializedName("error")  @Expose public com.google.gson.JsonElement error;
    }

    public static class RpcResult {
        @SerializedName("texts") @Expose public List<RpcText> texts;
        @SerializedName("lang")  @Expose public String lang;
    }

    public static class RpcText {
        @SerializedName("text") @Expose public String text;
    }
}