package org.nemogram.messenger.translator;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranslateAlert2;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.translator.html.HTMLKeeper;

public class TextWithEntitiesTranslator implements Translator.ITranslator {

    private static final HashMap<String, TextWithEntitiesTranslator> wrappedTranslators = new HashMap<>();

    public static TextWithEntitiesTranslator of(String type) {
        // Sync per-provider settings before returning
        if (Translator.PROVIDER_DEEPL.equals(type)) {
            DeepLTranslator.setFormality(NemoConfig.deepLFormality);
        }
        return wrappedTranslators.computeIfAbsent(type, type1 -> {
            var translator = switch (type1) {
                case Translator.PROVIDER_YANDEX -> YandexTranslator.getInstance();
                case Translator.PROVIDER_DEEPL -> DeepLTranslator.getInstance();
                default -> GoogleAppTranslator.getInstance();
            };
            return new TextWithEntitiesTranslator(translator);
        });
    }

    private final BaseTranslator translator;

    private TextWithEntitiesTranslator(BaseTranslator translator) {
        this.translator = translator;
    }

    @Override
    public Translator.TranslationResult translate(TLRPC.TL_textWithEntities query, String fl, String tl) throws Exception {
        if (NemoConfig.keepFormatting) {
            var html = HTMLKeeper.entitiesToHtml(query.text, query.entities, false);
            var result = translator.translate(html, null, tl);
            if (result == null || result.translation == null) {
                throw new IOException("Translation failed: empty response from provider");
            }
            var textAndEntitiesTranslated = HTMLKeeper.htmlToEntities(result.translation, query.entities, false);
            return Translator.TranslationResult.of(
                    TranslateAlert2.preprocess(query, textAndEntitiesTranslated),
                    result.sourceLanguage
            );
        } else {
            var result = translator.translate(query.text, null, tl);
            if (result == null || result.translation == null) {
                throw new IOException("Translation failed: empty response from provider");
            }
            return Translator.TranslationResult.of(
                    Translator.textWithEntities(result.translation, null),
                    result.sourceLanguage
            );
        }
    }

    @Override
    public boolean supportLanguage(String language) {
        return translator.supportLanguage(language);
    }

    @Override
    public List<String> getTargetLanguages() {
        return translator.getTargetLanguages();
    }
}