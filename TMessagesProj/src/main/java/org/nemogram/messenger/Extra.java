package org.nemogram.messenger;

import org.telegram.messenger.BuildConfig;

import org.nemogram.messenger.helpers.UserHelper;

public class Extra {

    public static int APP_ID = BuildConfig.API_ID;
    public static String APP_HASH = BuildConfig.API_HASH;
    public static String TWPIC_BOT_USERNAME = BuildConfig.TWPIC_BOT_USERNAME;

    public static boolean isDirectApp() {
        return "release".equals(BuildConfig.BUILD_TYPE) || "debug".equals(BuildConfig.BUILD_TYPE);
    }
}
