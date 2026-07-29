package org.nemogram.messenger;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogsMenuItems {

    public static final String THEME = "theme";
    public static final String MY_PROFILE = "my_profile";
    public static final String ARCHIVE = "archive";
    public static final String NEW_GROUP = "new_group";
    public static final String CONTACTS = "contacts";
    public static final String CALLS = "calls";
    public static final String SAVED_MESSAGES = "saved_messages";
    public static final String REPLIES = "replies";
    public static final String DOWNLOADS = "downloads";
    public static final String PASSCODE_LOCK = "passcode_lock";
    public static final String ACCOUNTS = "accounts";
    public static final String SETTINGS = "settings";
    public static final String NEMO_SETTINGS = "nemo_settings";
    public static final String PROXY = "proxy";

    public static final List<String> DEFAULT_ORDER = Arrays.asList(
            THEME,
            MY_PROFILE,
            ARCHIVE,
            NEW_GROUP,
            CONTACTS,
            CALLS,
            SAVED_MESSAGES,
            REPLIES,
            PASSCODE_LOCK,
            DOWNLOADS,
            SETTINGS,
            NEMO_SETTINGS,
            ACCOUNTS,
            PROXY
    );

    private static final Set<String> LOCKED_IDS = new HashSet<>(Arrays.asList(SETTINGS));

    public static boolean isLocked(String id) {
        return LOCKED_IDS.contains(id);
    }

    public static int getIcon(String id) {
        switch (id) {
            case THEME:
                return R.drawable.menu_night_mode_24;
            case MY_PROFILE:
                return R.drawable.left_status_profile;
            case ARCHIVE:
                return R.drawable.msg_archive;
            case NEW_GROUP:
                return R.drawable.outline_groups_24;
            case CONTACTS:
                return R.drawable.msg_contacts;
            case CALLS:
                return R.drawable.msg_calls;
            case SAVED_MESSAGES:
                return R.drawable.outline_saved_24;
            case REPLIES:
                return R.drawable.menu_reply;
            case DOWNLOADS:
                return R.drawable.msg_download;
            case PASSCODE_LOCK:
                return R.drawable.outline_header_lock_24;
            case ACCOUNTS:
                return R.drawable.settings_account;
            case SETTINGS:
                return R.drawable.msg_settings_old;
            case NEMO_SETTINGS:
                return R.drawable.filled_profile_settings;
            case PROXY:
                return R.drawable.outline_shield_plain_24;
            default:
                return R.drawable.msg_settings_old;
        }
    }

    public static String getTitle(String id) {
        switch (id) {
            case THEME:
                return LocaleController.getString(R.string.SwitchThemeToNight);
            case MY_PROFILE:
                return LocaleController.getString(R.string.MyProfile);
            case ARCHIVE:
                return LocaleController.getString(R.string.ArchivedChats);
            case NEW_GROUP:
                return LocaleController.getString(R.string.NewGroup);
            case CONTACTS:
                return LocaleController.getString(R.string.Contacts);
            case CALLS:
                return LocaleController.getString(R.string.Calls);
            case SAVED_MESSAGES:
                return LocaleController.getString(R.string.SavedMessages);
            case REPLIES:
                return LocaleController.getString(R.string.RepliesTitle);
            case DOWNLOADS:
                return LocaleController.getString(R.string.DownloadsTabs);
            case PASSCODE_LOCK:
                return LocaleController.getString(R.string.AccDescrPasscodeLock);
            case ACCOUNTS:
                return LocaleController.getString(R.string.SelectAccount);
            case SETTINGS:
                return LocaleController.getString(R.string.Settings);
            case NEMO_SETTINGS:
                return LocaleController.getString(R.string.NemoSettings);
            case PROXY:
                return LocaleController.getString(R.string.MenuProxyTitle);
            default:
                return "";
        }
    }
}
