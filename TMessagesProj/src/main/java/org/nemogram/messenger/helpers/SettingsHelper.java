package org.nemogram.messenger.helpers;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;
import java.util.function.Consumer;

import org.nemogram.messenger.settings.BaseNemoSettingsActivity;
import org.nemogram.messenger.settings.NemoAppearanceSettingsActivity;
import org.nemogram.messenger.settings.NemoChatSettingsActivity;
import org.nemogram.messenger.settings.NemoEmojiSettingsActivity;
import org.nemogram.messenger.settings.NemoExperimentalSettingsActivity;
import org.nemogram.messenger.settings.NemoGeneralSettingsActivity;
import org.nemogram.messenger.settings.NemoPasscodeSettingsActivity;
import org.nemogram.messenger.settings.NemoKeywordFilterActivity;
import org.nemogram.messenger.settings.NemoStickerSettingsActivity;
import org.nemogram.messenger.settings.SearchBarStyleActivity;

public class SettingsHelper {

    public static void processDeepLink(Uri uri, Consumer<BaseFragment> callback, Runnable unknown, Browser.Progress progress) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2) {
            unknown.run();
            return;
        }
        BaseNemoSettingsActivity fragment;
        var segment = segments.get(1);
        if (PasscodeHelper.getSettingsKey().equals(segment)) {
            fragment = new NemoPasscodeSettingsActivity();
        } else {
            switch (segment.toLowerCase(Locale.US)) {
                case "appearance":
                case "a":
                    fragment = new NemoAppearanceSettingsActivity();
                    break;
                case "chat":
                case "chats":
                case "c":
                    fragment = new NemoChatSettingsActivity();
                    break;
                case "experimental":
                case "e":
                    fragment = new NemoExperimentalSettingsActivity();
                    break;
                case "emoji":
                    fragment = new NemoEmojiSettingsActivity();
                    break;
                case "general":
                case "g":
                    fragment = new NemoGeneralSettingsActivity();
                    break;
                case "keywordfilter":
                case "kf":
                    fragment = new NemoKeywordFilterActivity();
                    break;
                case "stickers":
                case "s":
                    fragment = new NemoStickerSettingsActivity();
                    break;
                case "searchbarstyle":
                case "sbs":
                    fragment = new SearchBarStyleActivity();
                    break;
                case "update":
                    LaunchActivity.instance.checkAppUpdate(true, progress);
                    return;
                default:
                    unknown.run();
                    return;
            }
        }
        callback.accept(fragment);
        var row = uri.getQueryParameter("r");
        if (TextUtils.isEmpty(row)) {
            row = uri.getQueryParameter("row");
        }
        if (!TextUtils.isEmpty(row)) {
            var rowFinal = row;
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow(rowFinal, unknown));
        }
    }
}