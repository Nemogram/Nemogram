package org.nemogram.messenger.helpers.remote;

import android.content.pm.PackageInfo;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateHelper {

    private static final String GITHUB_API = "https://api.github.com/repos/Nemogram/Nemogram/releases/latest";

    private static final class InstanceHolder {
        private static final UpdateHelper instance = new UpdateHelper();
    }

    public static UpdateHelper getInstance() {
        return InstanceHolder.instance;
    }

    public void checkNewVersionAvailable(Delegate delegate) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                URL url = new URL(GITHUB_API);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    AndroidUtilities.runOnUIThread(() -> delegate.onTLResponse(null, "HTTP " + responseCode));
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                connection.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                String tagName = json.getString("tag_name").replaceAll("[^0-9]", "");
                int remoteVersionCode = Integer.parseInt(tagName);

                if (remoteVersionCode <= BuildConfig.VERSION_CODE) {
                    AndroidUtilities.runOnUIThread(() -> delegate.onTLResponse(null, null));
                    return;
                }

                TLRPC.TL_help_appUpdate update = new TLRPC.TL_help_appUpdate();
                update.version = json.optString("name", tagName);
                update.url = json.optString("html_url", "");
                update.flags |= 4;
                update.can_not_skip = false;
                update.text = json.optString("body", "");
                update.entities = new java.util.ArrayList<>();

                JSONArray assets = json.optJSONArray("assets");
                if (assets != null) {
                    JSONObject bestAsset = null;
                    int bestScore = Integer.MIN_VALUE;
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        String name = asset.optString("name", "");
                        if (name.endsWith(".apk")) {
                            int score = scoreApkAsset(name);
                            if (score > bestScore) {
                                bestScore = score;
                                bestAsset = asset;
                            }
                        }
                    }
                    if (bestAsset != null) {
                        update.url = bestAsset.getString("browser_download_url");
                    }
                }

                final TLRPC.TL_help_appUpdate finalUpdate = update;
                AndroidUtilities.runOnUIThread(() -> delegate.onTLResponse(finalUpdate, null));

            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> delegate.onTLResponse(null, e.getMessage()));
            }
        });
    }

    public static String formatDateUpdate(long date) {
        long epoch;
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            epoch = pInfo.lastUpdateTime;
        } catch (Exception e) {
            epoch = 0;
        }
        if (date <= epoch) {
            return LocaleController.formatString(R.string.LastUpdateNever);
        }
        try {
            Calendar rightNow = Calendar.getInstance();
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);
            rightNow.setTimeInMillis(date);
            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
            int dateYear = rightNow.get(Calendar.YEAR);

            if (dateDay == day && year == dateYear) {
                if (Math.abs(System.currentTimeMillis() - date) < 60000L) {
                    return LocaleController.formatString(R.string.LastUpdateRecently);
                }
                return LocaleController.formatString(R.string.LastUpdateFormatted, LocaleController.formatString(R.string.TodayAtFormatted,
                        LocaleController.getInstance().getFormatterDay().format(new Date(date))));
            } else if (dateDay + 1 == day && year == dateYear) {
                return LocaleController.formatString(R.string.LastUpdateFormatted, LocaleController.formatString(R.string.YesterdayAtFormatted,
                        LocaleController.getInstance().getFormatterDay().format(new Date(date))));
            } else if (Math.abs(System.currentTimeMillis() - date) < 31536000000L) {
                String format = LocaleController.formatString(R.string.formatDateAtTime,
                        LocaleController.getInstance().getFormatterDayMonth().format(new Date(date)),
                        LocaleController.getInstance().getFormatterDay().format(new Date(date)));
                return LocaleController.formatString(R.string.LastUpdateDateFormatted, format);
            } else {
                String format = LocaleController.formatString(R.string.formatDateAtTime,
                        LocaleController.getInstance().getFormatterYear().format(new Date(date)),
                        LocaleController.getInstance().getFormatterDay().format(new Date(date)));
                return LocaleController.formatString(R.string.LastUpdateDateFormatted, format);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return "LOC_ERR";
    }

    public interface Delegate {
        void onTLResponse(TLRPC.TL_help_appUpdate res, String error);
    }

    private static int scoreApkAsset(String assetName) {
        String name = assetName.toLowerCase(Locale.US);
        int score = 0;

        String[] supportedAbis = Build.SUPPORTED_ABIS;
        for (int i = 0; i < supportedAbis.length; i++) {
            String abi = supportedAbis[i];
            if (abi == null || abi.isEmpty()) {
                continue;
            }
            int abiScore = scoreAbiMatch(name, abi.toLowerCase(Locale.US));
            if (abiScore > 0) {
                score += abiScore - i * 10;
                break;
            }
        }

        if (score == 0 && name.contains("universal")) {
            score = 100;
        }

        return score;
    }

    private static int scoreAbiMatch(String assetName, String abi) {
        return switch (abi) {
            case "arm64-v8a" -> assetName.contains("arm64-v8a") || assetName.contains("arm64") || assetName.contains("aarch64") ? 400 : 0;
            case "armeabi-v7a" -> assetName.contains("armeabi-v7a") || assetName.contains("armv7") || assetName.contains("armeabi") ? 350 : 0;
            case "x86_64" -> assetName.contains("x86_64") ? 300 : 0;
            case "x86" -> assetName.contains("x86") && !assetName.contains("x86_64") ? 250 : 0;
            default -> 0;
        };
    }
}
