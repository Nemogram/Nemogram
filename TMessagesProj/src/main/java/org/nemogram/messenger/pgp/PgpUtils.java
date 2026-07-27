package org.nemogram.messenger.pgp;

import android.text.SpannableStringBuilder;

import java.util.regex.Pattern;

public class PgpUtils {

    public static final String ARMOR_BEGIN = "-----BEGIN PGP MESSAGE-----";
    public static final String ARMOR_END = "-----END PGP MESSAGE-----";

    private static final Pattern HEX_CLEANUP = Pattern.compile("[^0-9A-Fa-f]");

    private PgpUtils() {
    }

    public static boolean isArmoredMessage(CharSequence text) {
        if (text == null) {
            return false;
        }
        String s = text.toString().trim();
        return s.startsWith(ARMOR_BEGIN) && s.contains(ARMOR_END);
    }

    public static long parseKeyId(String input) {
        if (input == null) {
            return 0;
        }
        String cleaned = HEX_CLEANUP.matcher(input.trim()).replaceAll("");
        if (cleaned.isEmpty()) {
            return 0;
        }
        if (cleaned.length() > 16) {
            if (cleaned.length() != 40) {
                return 0;
            }
            cleaned = cleaned.substring(cleaned.length() - 16);
        }
        try {
            return Long.parseUnsignedLong(cleaned, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatKeyId(long keyId) {
        return "0x" + Long.toHexString(keyId).toUpperCase();
    }

    public static CharSequence withLockPrefix(CharSequence text) {
        if (text == null) {
            return "\uD83D\uDD12 ";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder("\uD83D\uDD12 ");
        builder.append(text);
        return builder;
    }
}
