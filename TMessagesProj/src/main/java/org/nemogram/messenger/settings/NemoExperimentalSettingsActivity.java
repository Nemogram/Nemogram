package org.nemogram.messenger.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LaunchActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.nemogram.messenger.Extra;
import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.PopupHelper;
import org.nemogram.messenger.helpers.remote.UpdateHelper;

public class NemoExperimentalSettingsActivity extends BaseNemoSettingsActivity {

    private static final int REQUEST_CODE_EXPORT_SETTINGS = 4001;
    private static final int REQUEST_CODE_IMPORT_SETTINGS = 4002;
    private static final String FISH_MIME_TYPE = "application/octet-stream";

    private final int moreHapticFeedbacksRow = rowId++;
    private final int localCustomEmojiRow = rowId++;
    private final int keepFormattingRow = rowId++;
    private final int autoInlineBotRow = rowId++;
    private final int forceFontWeightFallbackRow = rowId++;
    private final int highRoundVideoBitrateRow = rowId++;
    private final int contentRestrictionRow = rowId++;

    private final int checkUpdateRow = rowId++;
    private final int autoCheckUpdatesRow = rowId++;
    private final int autoCheckUpdatesIntervalRow = rowId++;

    private final int exportSettingsRow = rowId++;
    private final int importSettingsRow = rowId++;

    private final int deleteAccountRow = rowId++;

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.Experiment)));
        items.add(UItem.asCheck(moreHapticFeedbacksRow, LocaleController.getString(R.string.MoreHapticFeedback)).slug("moreHapticFeedbacks").setChecked(NemoConfig.moreHapticFeedbacks));
        items.add(UItem.asCheck(localCustomEmojiRow, LocaleController.getString(R.string.LocalCustomEmoji)).slug("localCustomEmoji").setChecked(NemoConfig.localCustomEmoji));
        items.add(UItem.asCheck(keepFormattingRow, LocaleController.getString(R.string.TranslationKeepFormatting)).slug("keepFormatting").setChecked(NemoConfig.keepFormatting));
        items.add(UItem.asCheck(autoInlineBotRow, LocaleController.getString(R.string.AutoInlineBot), LocaleController.getString(R.string.AutoInlineBotDesc)).slug("autoInlineBot").setChecked(NemoConfig.autoInlineBot));
        items.add(UItem.asCheck(forceFontWeightFallbackRow, LocaleController.getString(R.string.ForceFontWeightFallback)).slug("forceFontWeightFallback").setChecked(NemoConfig.forceFontWeightFallback));
        items.add(UItem.asCheck(highRoundVideoBitrateRow, LocaleController.getString(R.string.HighRoundVideoBitrate), LocaleController.getString(R.string.HighRoundVideoBitrateDesc)).slug("highRoundVideoBitrate").setChecked(NemoConfig.highRoundVideoBitrate));
        if (Extra.isDirectApp()) {
            items.add(UItem.asCheck(contentRestrictionRow, LocaleController.getString(R.string.IgnoreContentRestriction)).slug("contentRestriction").setChecked(NemoConfig.ignoreContentRestriction));
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NemoSettingsSectionImportExport)));
        items.add(TextSettingsCellFactory.of(exportSettingsRow, LocaleController.getString(R.string.ExportNemoSettings), "").slug("exportSettings"));
        items.add(TextSettingsCellFactory.of(importSettingsRow, LocaleController.getString(R.string.ImportNemoSettings), "").slug("importSettings"));
        items.add(UItem.asShadow(null));

        if (getParentActivity() instanceof LaunchActivity) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.NemoSettingsSectionUpdates)));
            items.add(TextDetailSettingsCellFactory.of(checkUpdateRow, LocaleController.getString(R.string.CheckUpdate), UpdateHelper.formatDateUpdate(SharedConfig.lastUpdateCheckTime)).slug("checkUpdate"));
            items.add(UItem.asCheck(autoCheckUpdatesRow, LocaleController.getString(R.string.AutoCheckUpdates)).slug("autoCheckUpdates").setChecked(NemoConfig.autoCheckUpdates));
            items.add(TextSettingsCellFactory.of(autoCheckUpdatesIntervalRow, LocaleController.getString(R.string.AutoCheckUpdatesInterval), formatAutoCheckInterval(NemoConfig.autoCheckUpdatesIntervalHours)).slug("autoCheckUpdatesInterval"));
            items.add(UItem.asShadow(null));
        }

        items.add(TextSettingsCellFactory.of(deleteAccountRow, LocaleController.getString(R.string.DeleteAccount), "").slug("deleteAccount").red());
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (false) {
            var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            var message = new TextView(getParentActivity());
            message.setText(getSpannedString(R.string.SoonRemovedOption, "https://t.me/" + LocaleController.getString(R.string.OfficialChannelUsername)));
            message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            message.setLinkTextColor(getThemedColor(Theme.key_dialogTextLink));
            message.setHighlightColor(getThemedColor(Theme.key_dialogLinkSelection));
            message.setPadding(AndroidUtilities.dp(23), 0, AndroidUtilities.dp(23), 0);
            message.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
            message.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            builder.setView(message);
            builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
            showDialog(builder.create());
        }
        if (id == deleteAccountRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder.setMessage(LocaleController.getString(R.string.TosDeclineDeleteAccount));
            builder.setTitle(LocaleController.getString(R.string.DeleteAccount));
            builder.setPositiveButton(LocaleController.getString(R.string.Deactivate), (dialog, which) -> {
                if (BuildConfig.DEBUG) return;
                final AlertDialog progressDialog = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
                progressDialog.setCanCancel(false);

                ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(getMessagesController().getAllDialogs());
                for (TLRPC.Dialog TLdialog : dialogs) {
                    if (TLdialog instanceof TLRPC.TL_dialogFolder) {
                        continue;
                    }
                    TLRPC.Peer peer = getMessagesController().getPeer((int) TLdialog.id);
                    if (peer.channel_id != 0) {
                        TLRPC.Chat chat = getMessagesController().getChat(peer.channel_id);
                        if (!chat.broadcast) {
                            getMessageHelper().deleteUserHistoryWithSearch(NemoExperimentalSettingsActivity.this, TLdialog.id);
                        }
                    }
                    if (peer.user_id != 0) {
                        getMessagesController().deleteDialog(TLdialog.id, 0, true);
                    }
                }

                Utilities.globalQueue.postRunnable(() -> {
                    TL_account.deleteAccount req = new TL_account.deleteAccount();
                    req.reason = "Meow";
                    getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        if (response instanceof TLRPC.TL_boolTrue) {
                            getMessagesController().performLogout(0);
                        } else if (error == null || error.code != -1000) {
                            String errorText = LocaleController.getString(R.string.ErrorOccurred);
                            if (error != null) {
                                errorText += "\n" + error.text;
                            }
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                            builder1.setTitle(LocaleController.getString(R.string.AppName));
                            builder1.setMessage(errorText);
                            builder1.setPositiveButton(LocaleController.getString(R.string.OK), null);
                            builder1.show();
                        }
                    }));
                }, 20000);
                progressDialog.show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> {
                var button = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                button.setEnabled(false);
                var buttonText = button.getText();
                new CountDownTimer(60000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        button.setText(String.format(Locale.getDefault(), "%s (%d)", buttonText, millisUntilFinished / 1000 + 1));
                    }

                    @Override
                    public void onFinish() {
                        button.setText(buttonText);
                        button.setEnabled(true);
                    }
                }.start();
            });
            showDialog(dialog);
        } else if (id == moreHapticFeedbacksRow) {
            NemoConfig.toggleMoreHapticFeedbacks();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.moreHapticFeedbacks);
            }
        } else if (id == contentRestrictionRow) {
            NemoConfig.toggleIgnoreContentRestriction();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.ignoreContentRestriction);
            }
        } else if (id == autoInlineBotRow) {
            NemoConfig.toggleAutoInlineBot();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.autoInlineBot);
            }
        } else if (id == forceFontWeightFallbackRow) {
            NemoConfig.toggleForceFontWeightFallback();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.forceFontWeightFallback);
            }
            showRestartBulletin();
        } else if (id == highRoundVideoBitrateRow) {
            NemoConfig.toggleHighRoundVideoBitrate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.highRoundVideoBitrate);
            }
        } else if (id == keepFormattingRow) {
            NemoConfig.toggleKeepFormatting();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.keepFormatting);
            }
        } else if (id == checkUpdateRow) {
            if (getParentActivity() instanceof LaunchActivity launchActivity) {
                launchActivity.checkAppUpdate(true, new Browser.Progress() {
                    @Override
                    public void end() {
                        item.subtext = UpdateHelper.formatDateUpdate(SharedConfig.lastUpdateCheckTime);
                        listView.adapter.notifyItemChanged(position);
                    }
                });
                item.subtext = LocaleController.getString(R.string.CheckingUpdate);
                listView.adapter.notifyItemChanged(position);
            }
        } else if (id == localCustomEmojiRow) {
            NemoConfig.toggleLocalCustomEmoji();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.localCustomEmoji);
            }
        } else if (id == autoCheckUpdatesRow) {
            NemoConfig.toggleAutoCheckUpdates();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.autoCheckUpdates);
            }
        } else if (id == autoCheckUpdatesIntervalRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> intervals = new ArrayList<>();
            for (int hours : new int[]{1, 3, 6, 12, 24}) {
                arrayList.add(formatAutoCheckInterval(hours));
                intervals.add(hours);
            }
            PopupHelper.show(arrayList, LocaleController.getString(R.string.AutoCheckUpdatesInterval), intervals.indexOf(NemoConfig.autoCheckUpdatesIntervalHours), getParentActivity(), view, i -> {
                NemoConfig.setAutoCheckUpdatesIntervalHours(intervals.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        } else if (id == exportSettingsRow) {
            startExportSettings();
        } else if (id == importSettingsRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.ImportNemoSettingsConfirmTitle));
            builder.setMessage(LocaleController.getString(R.string.ImportNemoSettingsConfirmMessage));
            builder.setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> startImportSettings());
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder.create());
        }
    }

    private String exportFileName() {
        String stamp = DateFormat.format("yyyy-MM-dd_HHmm", new Date()).toString();
        return "nemogram_settings_" + stamp + NemoConfig.FISH_EXTENSION;
    }

    private void startExportSettings() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(FISH_MIME_TYPE);
            intent.putExtra(Intent.EXTRA_TITLE, exportFileName());
            startActivityForResult(intent, REQUEST_CODE_EXPORT_SETTINGS);
        } catch (Exception e) {
            FileLog.e(e);
            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
        }
    }

    private void startImportSettings() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_IMPORT_SETTINGS);
        } catch (Exception e) {
            FileLog.e(e);
            BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_EXPORT_SETTINGS && requestCode != REQUEST_CODE_IMPORT_SETTINGS) {
            super.onActivityResultFragment(requestCode, resultCode, data);
            return;
        }
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        if (requestCode == REQUEST_CODE_EXPORT_SETTINGS) {
            writeExportToUri(uri);
        } else {
            readImportFromUri(uri);
        }
    }

    private void writeExportToUri(Uri uri) {
        Utilities.globalQueue.postRunnable(() -> {
            boolean success = false;
            try (OutputStream os = getParentActivity().getContentResolver().openOutputStream(uri)) {
                if (os != null) {
                    String json = NemoConfig.exportConfigs();
                    byte[] obfuscated = NemoConfig.obfuscateExport(json);
                    os.write(obfuscated);
                    os.flush();
                    success = true;
                }
            } catch (IOException e) {
                FileLog.e(e);
            }
            boolean finalSuccess = success;
            AndroidUtilities.runOnUIThread(() -> {
                if (finalSuccess) {
                    BulletinFactory.of(this).createSuccessBulletin(LocaleController.getString(R.string.ExportNemoSettingsSuccess)).show();
                } else {
                    BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.UnknownError)).show();
                }
            });
        });
    }

    private void readImportFromUri(Uri uri) {
        Utilities.globalQueue.postRunnable(() -> {
            byte[] raw = null;
            try (InputStream is = getParentActivity().getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    byte[] chunk = new byte[8192];
                    int read;
                    while ((read = is.read(chunk)) != -1) {
                        buffer.write(chunk, 0, read);
                    }
                    raw = buffer.toByteArray();
                }
            } catch (IOException e) {
                FileLog.e(e);
            }

            String json = raw != null ? NemoConfig.deobfuscateExport(raw) : null;
            boolean success = false;
            if (json != null) {
                try {
                    NemoConfig.importConfigs(json);
                    success = true;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }

            boolean finalSuccess = success;
            AndroidUtilities.runOnUIThread(() -> {
                if (finalSuccess) {
                    listView.adapter.update(true);
                    showRestartBulletin();
                } else {
                    BulletinFactory.of(this).createErrorBulletin(LocaleController.getString(R.string.ImportNemoSettingsError)).show();
                }
            });
        });
    }

    private String formatAutoCheckInterval(int hours) {
        if (hours == 24) {
            return LocaleController.getString(R.string.MessageScheduledRepeatOptionDaily);
        }
        return LocaleController.formatPluralString("Hours", hours);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.NotificationsOther);
    }

    @Override
    protected String getKey() {
        return "e";
    }

    @Override
    public Integer getSelectorColor(int position) {
        var item = listView.adapter.getItem(position);
        if (item.id == deleteAccountRow) {
            return Theme.multAlpha(getThemedColor(Theme.key_text_RedRegular), .1f);
        }
        return super.getSelectorColor(position);
    }
}
