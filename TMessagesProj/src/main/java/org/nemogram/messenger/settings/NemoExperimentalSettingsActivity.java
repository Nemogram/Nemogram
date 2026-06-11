package org.nemogram.messenger.settings;

import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Locale;

import org.nemogram.messenger.Extra;
import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.PopupHelper;
import org.nemogram.messenger.helpers.remote.UpdateHelper;

public class NemoExperimentalSettingsActivity extends BaseNemoSettingsActivity {

    private final int moreHapticFeedbacksRow = rowId++;
    private final int downloadSpeedBoostRow = rowId++;
    private final int keepFormattingRow = rowId++;
    private final int autoInlineBotRow = rowId++;
    private final int forceFontWeightFallbackRow = rowId++;
    private final int contentRestrictionRow = rowId++;
    private final int showRPCErrorRow = rowId++;

    private final int checkUpdateRow = rowId++;
    private final int autoCheckUpdatesRow = rowId++;

    private final int deleteAccountRow = rowId++;

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.Experiment)));
        items.add(UItem.asCheck(moreHapticFeedbacksRow, LocaleController.getString(R.string.MoreHapticFeedback)).slug("moreHapticFeedbacks").setChecked(NemoConfig.moreHapticFeedbacks));
        if (!MessagesController.getInstance(currentAccount).getfileExperimentalParams) {
            items.add(TextSettingsCellFactory.of(downloadSpeedBoostRow, LocaleController.getString(R.string.DownloadSpeedBoost), switch (NemoConfig.downloadSpeedBoost) {
                case NemoConfig.BOOST_NONE ->
                        LocaleController.getString(R.string.DownloadSpeedBoostNone);
                case NemoConfig.BOOST_EXTREME ->
                        LocaleController.getString(R.string.DownloadSpeedBoostExtreme);
                default -> LocaleController.getString(R.string.DownloadSpeedBoostAverage);
            }).slug("downloadSpeedBoost"));
        }
        items.add(UItem.asCheck(keepFormattingRow, LocaleController.getString(R.string.TranslationKeepFormatting)).slug("keepFormatting").setChecked(NemoConfig.keepFormatting));
        items.add(UItem.asCheck(autoInlineBotRow, LocaleController.getString(R.string.AutoInlineBot), LocaleController.getString(R.string.AutoInlineBotDesc)).slug("autoInlineBot").setChecked(NemoConfig.autoInlineBot));
        items.add(UItem.asCheck(forceFontWeightFallbackRow, LocaleController.getString(R.string.ForceFontWeightFallback)).slug("forceFontWeightFallback").setChecked(NemoConfig.forceFontWeightFallback));
        if (Extra.isDirectApp()) {
            items.add(UItem.asCheck(contentRestrictionRow, LocaleController.getString(R.string.IgnoreContentRestriction)).slug("contentRestriction").setChecked(NemoConfig.ignoreContentRestriction));
        }
        items.add(UItem.asCheck(showRPCErrorRow, LocaleController.getString(R.string.ShowRPCError), LocaleController.formatString(R.string.ShowRPCErrorException, "FILE_REFERENCE_EXPIRED")).slug("showRPCError").setChecked(NemoConfig.showRPCError));
        items.add(UItem.asShadow(null));

        if (getParentActivity() instanceof LaunchActivity) {
            items.add(TextDetailSettingsCellFactory.of(checkUpdateRow, LocaleController.getString(R.string.CheckUpdate), UpdateHelper.formatDateUpdate(SharedConfig.lastUpdateCheckTime)).slug("checkUpdate"));
            items.add(UItem.asCheck(autoCheckUpdatesRow, LocaleController.getString(R.string.AutoCheckUpdates)).slug("autoCheckUpdates").setChecked(NemoConfig.autoCheckUpdates));
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
        } else if (id == showRPCErrorRow) {
            NemoConfig.toggleShowRPCError();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.showRPCError);
            }
        } else if (id == moreHapticFeedbacksRow) {
            NemoConfig.toggleMoreHapticFeedbacks();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.moreHapticFeedbacks);
            }
        } else if (id == downloadSpeedBoostRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.DownloadSpeedBoostNone));
            types.add(NemoConfig.BOOST_NONE);
            arrayList.add(LocaleController.getString(R.string.DownloadSpeedBoostAverage));
            types.add(NemoConfig.BOOST_AVERAGE);
            arrayList.add(LocaleController.getString(R.string.DownloadSpeedBoostExtreme));
            types.add(NemoConfig.BOOST_EXTREME);
            PopupHelper.show(arrayList, LocaleController.getString(R.string.DownloadSpeedBoost), types.indexOf(NemoConfig.downloadSpeedBoost), getParentActivity(), view, i -> {
                NemoConfig.setDownloadSpeedBoost(types.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
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
        } else if (id == autoCheckUpdatesRow) {
            NemoConfig.toggleAutoCheckUpdates();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.autoCheckUpdates);
            }
        }
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
