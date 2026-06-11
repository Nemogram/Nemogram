package org.nemogram.messenger.settings;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.ThemePreviewMessagesCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.EntitiesHelper;
import org.nemogram.messenger.helpers.PopupHelper;
import org.nemogram.messenger.helpers.VoiceEnhancementsHelper;
import org.nemogram.messenger.helpers.WhisperHelper;

public class NemoChatSettingsActivity extends BaseNemoSettingsActivity {

    private final int stickerSettingsRow = rowId++;

    private final int ignoreBlockedRow = rowId++;
    private final int quickForwardRow = rowId++;
    private final int hideKeyboardOnChatScrollRow = rowId++;
    private final int tryToOpenAllLinksInIVRow = rowId++;
    private final int disableJumpToNextRow = rowId++;
    private final int disableGreetingStickerRow = rowId++;
    private final int hideChannelBottomButtonsRow = rowId++;
    private final int hideAiButtonRow = rowId++;
    private final int doubleTapActionRow = rowId++;

    private final int transcribeProviderRow = rowId++;
    private final int cfCredentialsRow = rowId++;

    private final int markdownEnableRow = rowId++;
    private final int markdownParserRow = rowId++;
    private final int markdownParseLinksRow = rowId++;
    private final int markdown2Row = rowId++;

    private final int voiceEnhancementsRow = rowId++;
    private final int rearVideoMessagesRow = rowId++;
    private final int confirmAVRow = rowId++;
    private final int disableProximityEventsRow = rowId++;
    private final int disableVoiceMessageAutoPlayRow = rowId++;
    private final int unmuteVideosWithVolumeButtonsRow = rowId++;
    private final int autoPauseVideoRow = rowId++;
    private final int preferOriginalQualityRow = rowId++;

    private final int messageMenuRow = 100;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        return super.createView(context);
    }

    public String getDoubleTapActionText(int action) {
        return switch (action) {
            case NemoConfig.DOUBLE_TAP_ACTION_REACTION ->
                    LocaleController.getString(R.string.Reactions);
            case NemoConfig.DOUBLE_TAP_ACTION_TRANSLATE ->
                    LocaleController.getString(R.string.TranslateMessage);
            case NemoConfig.DOUBLE_TAP_ACTION_REPLY -> LocaleController.getString(R.string.Reply);
            case NemoConfig.DOUBLE_TAP_ACTION_SAVE ->
                    LocaleController.getString(R.string.AddToSavedMessages);
            case NemoConfig.DOUBLE_TAP_ACTION_REPEAT -> LocaleController.getString(R.string.Repeat);
            case NemoConfig.DOUBLE_TAP_ACTION_EDIT -> LocaleController.getString(R.string.Edit);
            default -> LocaleController.getString(R.string.Disable);
        };
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asButton(stickerSettingsRow, R.drawable.msg_sticker, LocaleController.getString(R.string.StickersAndGifs)).slug("stickerSettings"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Chat)));
        items.add(UItem.asCheck(ignoreBlockedRow, LocaleController.getString(R.string.IgnoreBlocked), LocaleController.getString(R.string.IgnoreBlockedAbout)).slug("ignoreBlocked").setChecked(NemoConfig.ignoreBlocked));
        items.add(UItem.asCheck(quickForwardRow, LocaleController.getString(R.string.QuickForward)).slug("quickForward").setChecked(NemoConfig.quickForward));
        items.add(UItem.asCheck(hideKeyboardOnChatScrollRow, LocaleController.getString(R.string.HideKeyboardOnChatScroll)).slug("hideKeyboardOnChatScroll").setChecked(NemoConfig.hideKeyboardOnChatScroll));
        items.add(UItem.asCheck(tryToOpenAllLinksInIVRow, LocaleController.getString(R.string.OpenAllLinksInInstantView)).slug("tryToOpenAllLinksInIV").setChecked(NemoConfig.tryToOpenAllLinksInIV));
        items.add(UItem.asCheck(disableJumpToNextRow, LocaleController.getString(R.string.DisableJumpToNextChannel)).slug("disableJumpToNext").setChecked(NemoConfig.disableJumpToNextChannel));
        items.add(UItem.asCheck(disableGreetingStickerRow, LocaleController.getString(R.string.DisableGreetingSticker)).slug("disableGreetingSticker").setChecked(NemoConfig.disableGreetingSticker));
        items.add(UItem.asCheck(hideChannelBottomButtonsRow, LocaleController.getString(R.string.HideChannelBottomButtons)).slug("hideChannelBottomButtons").setChecked(NemoConfig.hideChannelBottomButtons));
        items.add(UItem.asCheck(hideAiButtonRow, LocaleController.getString(R.string.HideAiButton)).slug("hideAiButton").setChecked(NemoConfig.hideAiButton));
        items.add(TextSettingsCellFactory.of(doubleTapActionRow, LocaleController.getString(R.string.DoubleTapAction), NemoConfig.doubleTapInAction == NemoConfig.doubleTapOutAction ?
                getDoubleTapActionText(NemoConfig.doubleTapInAction) :
                getDoubleTapActionText(NemoConfig.doubleTapInAction) + ", " + getDoubleTapActionText(NemoConfig.doubleTapOutAction)).slug("doubleTapAction"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.PremiumPreviewVoiceToText)));
        items.add(TextSettingsCellFactory.of(transcribeProviderRow, LocaleController.getString(R.string.TranscribeProviderShort), switch (NemoConfig.transcribeProvider) {
            case NemoConfig.TRANSCRIBE_AUTO ->
                    LocaleController.getString(R.string.TranscribeProviderAuto);
            case NemoConfig.TRANSCRIBE_WORKERSAI ->
                    LocaleController.getString(R.string.TranscribeProviderWorkersAI);
            default -> LocaleController.getString(R.string.TelegramPremium);
        }).slug("transcribeProvider"));
        items.add(TextSettingsCellFactory.of(cfCredentialsRow, LocaleController.getString(R.string.CloudflareCredentials), "").slug("cfCredentials"));
        items.add(UItem.asShadow(LocaleController.formatString(R.string.TranscribeProviderDesc, LocaleController.getString(R.string.TranscribeProviderWorkersAI))));

        items.add(UItem.asHeader(LocaleController.getString(R.string.Markdown)));
        items.add(UItem.asCheck(markdownEnableRow, LocaleController.getString(R.string.MarkdownEnableByDefault)).slug("markdownEnable").setChecked(!NemoConfig.disableMarkdownByDefault));
        items.add(TextSettingsCellFactory.of(markdownParserRow, LocaleController.getString(R.string.MarkdownParser), NemoConfig.newMarkdownParser ? "Nekogram" : "Telegram").slug("markdownParser"));
        if (NemoConfig.newMarkdownParser) {
            items.add(UItem.asCheck(markdownParseLinksRow, LocaleController.getString(R.string.MarkdownParseLinks)).slug("markdownParseLinks").setChecked(NemoConfig.markdownParseLinks));
        }
        items.add(UItem.asShadow(markdown2Row, TextUtils.expandTemplate(EntitiesHelper.parseMarkdown(NemoConfig.newMarkdownParser && NemoConfig.markdownParseLinks ? LocaleController.getString(R.string.MarkdownAbout) : LocaleController.getString(R.string.MarkdownAbout2)), "**", "__", "~~", "`", "||", "[", "](", ")")));

        items.add(UItem.asHeader(LocaleController.getString(R.string.SharedMediaTab2)));
        if (VoiceEnhancementsHelper.isAvailable()) {
            items.add(UItem.asCheck(voiceEnhancementsRow, LocaleController.getString(R.string.VoiceEnhancements), LocaleController.getString(R.string.VoiceEnhancementsAbout)).slug("voiceEnhancements").setChecked(NemoConfig.voiceEnhancements));
        }
        items.add(UItem.asCheck(rearVideoMessagesRow, LocaleController.getString(R.string.RearVideoMessages)).slug("rearVideoMessages").setChecked(NemoConfig.rearVideoMessages));
        items.add(UItem.asCheck(confirmAVRow, LocaleController.getString(R.string.ConfirmAVMessage)).slug("confirmAV").setChecked(NemoConfig.confirmAVMessage));
        items.add(UItem.asCheck(disableProximityEventsRow, LocaleController.getString(R.string.DisableProximityEvents)).slug("disableProximityEvents").setChecked(NemoConfig.disableProximityEvents));
        items.add(UItem.asCheck(disableVoiceMessageAutoPlayRow, LocaleController.getString(R.string.DisableVoiceMessagesAutoPlay)).slug("disableVoiceMessageAutoPlay").setChecked(NemoConfig.disableVoiceMessageAutoPlay));
        items.add(UItem.asCheck(unmuteVideosWithVolumeButtonsRow, LocaleController.getString(R.string.UnmuteVideosWithVolumeButtons)).slug("unmuteVideosWithVolumeButtons").setChecked(NemoConfig.unmuteVideosWithVolumeButtons));
        items.add(UItem.asCheck(autoPauseVideoRow, LocaleController.getString(R.string.AutoPauseVideo), LocaleController.getString(R.string.AutoPauseVideoAbout)).slug("autoPauseVideo").setChecked(NemoConfig.autoPauseVideo));
        items.add(UItem.asCheck(preferOriginalQualityRow, LocaleController.getString(R.string.PreferOriginalQuality), LocaleController.getString(R.string.PreferOriginalQualityDesc)).slug("preferOriginalQuality").setChecked(NemoConfig.preferOriginalQuality));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.MessageMenu)));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 1, LocaleController.getString(R.string.DeleteDownloadedFile)).slug("showDeleteDownloadedFile").setChecked(NemoConfig.showDeleteDownloadedFile));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 2, LocaleController.getString(R.string.NoQuoteForward)).slug("showNoQuoteForward").setChecked(NemoConfig.showNoQuoteForward));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 3, LocaleController.getString(R.string.AddToSavedMessages)).slug("showAddToSavedMessages").setChecked(NemoConfig.showAddToSavedMessages));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 4, LocaleController.getString(R.string.Repeat)).slug("showRepeat").setChecked(NemoConfig.showRepeat));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 5, LocaleController.getString(R.string.Prpr)).slug("showPrPr").setChecked(NemoConfig.showPrPr));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 6, LocaleController.getString(R.string.TranslateMessage)).slug("showTranslate").setChecked(NemoConfig.showTranslate));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 7, LocaleController.getString(R.string.ReportChat)).slug("showReport").setChecked(NemoConfig.showReport));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 8, LocaleController.getString(R.string.MessageDetails)).slug("showMessageDetails").setChecked(NemoConfig.showMessageDetails));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 9, LocaleController.getString(R.string.CopyPhoto)).slug("showCopyPhoto").setChecked(NemoConfig.showCopyPhoto));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 10, LocaleController.getString(R.string.SetReminder)).slug("showSetReminder").setChecked(NemoConfig.showSetReminder));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 11, LocaleController.getString(R.string.QrCode)).slug("showQrCode").setChecked(NemoConfig.showQrCode));
        items.add(TextCheckbox2CellFactory.of(messageMenuRow + 12, LocaleController.getString(R.string.OpenInExternalApp)).slug("showOpenIn").setChecked(NemoConfig.showOpenIn));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id == stickerSettingsRow) {
            presentFragment(new NemoStickerSettingsActivity());
        } else if (id == ignoreBlockedRow) {
            NemoConfig.toggleIgnoreBlocked();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.ignoreBlocked);
            }
        } else if (id == hideKeyboardOnChatScrollRow) {
            NemoConfig.toggleHideKeyboardOnChatScroll();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideKeyboardOnChatScroll);
            }
        } else if (id == rearVideoMessagesRow) {
            NemoConfig.toggleRearVideoMessages();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.rearVideoMessages);
            }
        } else if (id == confirmAVRow) {
            NemoConfig.toggleConfirmAVMessage();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.confirmAVMessage);
            }
        } else if (id == disableProximityEventsRow) {
            NemoConfig.toggleDisableProximityEvents();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.disableProximityEvents);
            }
            showRestartBulletin();
        } else if (id == tryToOpenAllLinksInIVRow) {
            NemoConfig.toggleTryToOpenAllLinksInIV();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.tryToOpenAllLinksInIV);
            }
        } else if (id == autoPauseVideoRow) {
            NemoConfig.toggleAutoPauseVideo();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.autoPauseVideo);
            }
        } else if (id == disableJumpToNextRow) {
            NemoConfig.toggleDisableJumpToNextChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.disableJumpToNextChannel);
            }
        } else if (id == disableGreetingStickerRow) {
            NemoConfig.toggleDisableGreetingSticker();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.disableGreetingSticker);
            }
        } else if (id == disableVoiceMessageAutoPlayRow) {
            NemoConfig.toggleDisableVoiceMessageAutoPlay();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.disableVoiceMessageAutoPlay);
            }
        } else if (id == unmuteVideosWithVolumeButtonsRow) {
            NemoConfig.toggleUnmuteVideosWithVolumeButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.unmuteVideosWithVolumeButtons);
            }
        } else if (id == doubleTapActionRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.Disable));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_NONE);
            arrayList.add(LocaleController.getString(R.string.Reactions));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_REACTION);
            arrayList.add(LocaleController.getString(R.string.TranslateMessage));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_TRANSLATE);
            arrayList.add(LocaleController.getString(R.string.Reply));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_REPLY);
            arrayList.add(LocaleController.getString(R.string.AddToSavedMessages));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_SAVE);
            arrayList.add(LocaleController.getString(R.string.Repeat));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_REPEAT);
            arrayList.add(LocaleController.getString(R.string.Edit));
            types.add(NemoConfig.DOUBLE_TAP_ACTION_EDIT);

            var context = getParentActivity();
            var builder = new AlertDialog.Builder(context, resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.DoubleTapAction));

            var linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            builder.setView(linearLayout);

            var messagesCell = new ThemePreviewMessagesCell(context, parentLayout, 0);
            messagesCell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            linearLayout.addView(messagesCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            var hLayout = new LinearLayout(context);
            hLayout.setOrientation(LinearLayout.HORIZONTAL);
            hLayout.setPadding(0, AndroidUtilities.dp(8), 0, 0);
            linearLayout.addView(hLayout);

            for (int i = 0; i < 2; i++) {
                var out = i == 1;
                var layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                hLayout.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, .5f));

                for (int a = 0; a < arrayList.size(); a++) {

                    var cell = new RadioColorCell(context, resourcesProvider);
                    cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                    cell.setTag(a);
                    cell.setTextAndValue(arrayList.get(a), a == types.indexOf(out ? NemoConfig.doubleTapOutAction : NemoConfig.doubleTapInAction));
                    cell.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), out ? AndroidUtilities.dp(6) : 0, out ? 0 : AndroidUtilities.dp(6), out ? 0 : AndroidUtilities.dp(6), out ? AndroidUtilities.dp(6) : 0));
                    layout.addView(cell);
                    cell.setOnClickListener(v -> {
                        var which = (Integer) v.getTag();
                        var old = out ? NemoConfig.doubleTapOutAction : NemoConfig.doubleTapInAction;
                        if (types.get(which) == old) {
                            return;
                        }
                        if (out) {
                            NemoConfig.setDoubleTapOutAction(types.get(which));
                        } else {
                            NemoConfig.setDoubleTapInAction(types.get(which));
                        }
                        ((RadioColorCell) layout.getChildAt(types.indexOf(old))).setChecked(false, true);
                        cell.setChecked(true, true);
                        item.textValue = NemoConfig.doubleTapInAction == NemoConfig.doubleTapOutAction ?
                                getDoubleTapActionText(NemoConfig.doubleTapInAction) :
                                getDoubleTapActionText(NemoConfig.doubleTapInAction) + ", " + getDoubleTapActionText(NemoConfig.doubleTapOutAction);
                        listView.adapter.notifyItemChanged(position, PARTIAL);
                    });
                }
            }

            builder.setOnPreDismissListener(dialog -> listView.adapter.notifyItemChanged(position, PARTIAL));
            builder.setNegativeButton(LocaleController.getString(R.string.OK), null);
            builder.show();
        } else if (id == markdownEnableRow) {
            NemoConfig.toggleDisableMarkdownByDefault();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(!NemoConfig.disableMarkdownByDefault);
            }
        } else if (id > messageMenuRow) {
            TextCheckbox2Cell cell = ((TextCheckbox2Cell) view);
            int menuPosition = id - messageMenuRow - 1;
            if (menuPosition == 0) {
                NemoConfig.toggleShowDeleteDownloadedFile();
                cell.setChecked(NemoConfig.showDeleteDownloadedFile);
            } else if (menuPosition == 1) {
                NemoConfig.toggleShowNoQuoteForward();
                cell.setChecked(NemoConfig.showNoQuoteForward);
            } else if (menuPosition == 2) {
                NemoConfig.toggleShowAddToSavedMessages();
                cell.setChecked(NemoConfig.showAddToSavedMessages);
            } else if (menuPosition == 3) {
                NemoConfig.toggleShowRepeat();
                cell.setChecked(NemoConfig.showRepeat);
            } else if (menuPosition == 4) {
                NemoConfig.toggleShowPrPr();
                cell.setChecked(NemoConfig.showPrPr);
            } else if (menuPosition == 5) {
                NemoConfig.toggleShowTranslate();
                cell.setChecked(NemoConfig.showTranslate);
            } else if (menuPosition == 6) {
                NemoConfig.toggleShowReport();
                cell.setChecked(NemoConfig.showReport);
            } else if (menuPosition == 7) {
                NemoConfig.toggleShowMessageDetails();
                cell.setChecked(NemoConfig.showMessageDetails);
            } else if (menuPosition == 8) {
                NemoConfig.toggleShowCopyPhoto();
                cell.setChecked(NemoConfig.showCopyPhoto);
            } else if (menuPosition == 9) {
                NemoConfig.toggleShowSetReminder();
                cell.setChecked(NemoConfig.showSetReminder);
            } else if (menuPosition == 10) {
                NemoConfig.toggleShowQrCode();
                cell.setChecked(NemoConfig.showQrCode);
            } else if (menuPosition == 11) {
                NemoConfig.toggleShowOpenIn();
                cell.setChecked(NemoConfig.showOpenIn);
            }
        } else if (id == voiceEnhancementsRow) {
            NemoConfig.toggleVoiceEnhancements();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.voiceEnhancements);
            }
        } else if (id == markdownParserRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("Nekogram");
            arrayList.add("Telegram");
            boolean oldParser = NemoConfig.newMarkdownParser;
            PopupHelper.show(arrayList, LocaleController.getString(R.string.MarkdownParser), NemoConfig.newMarkdownParser ? 0 : 1, getParentActivity(), view, i -> {
                NemoConfig.setNewMarkdownParser(i == 0);
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
                if (oldParser != NemoConfig.newMarkdownParser) {
                    if (oldParser) {
                        notifyItemRemoved(markdownParseLinksRow);
                        updateRows();
                    } else {
                        updateRows();
                        notifyItemInserted(markdownParseLinksRow);
                    }
                    notifyItemChanged(markdown2Row);
                }
            }, resourcesProvider);
        } else if (id == markdownParseLinksRow) {
            NemoConfig.toggleMarkdownParseLinks();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.markdownParseLinks);
            }
            notifyItemChanged(markdown2Row);
        } else if (id == quickForwardRow) {
            NemoConfig.toggleQuickForward();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.quickForward);
            }
        } else if (id == transcribeProviderRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.TranscribeProviderAuto));
            types.add(NemoConfig.TRANSCRIBE_AUTO);
            arrayList.add(LocaleController.getString(R.string.TelegramPremium));
            types.add(NemoConfig.TRANSCRIBE_PREMIUM);
            arrayList.add(LocaleController.getString(R.string.TranscribeProviderWorkersAI));
            types.add(NemoConfig.TRANSCRIBE_WORKERSAI);
            PopupHelper.show(arrayList, LocaleController.getString(R.string.TranscribeProviderShort), types.indexOf(NemoConfig.transcribeProvider), getParentActivity(), view, i -> {
                NemoConfig.setTranscribeProvider(types.get(i));
                item.textValue = arrayList.get(i);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        } else if (id == cfCredentialsRow) {
            WhisperHelper.showCfCredentialsDialog(this);
        } else if (id == preferOriginalQualityRow) {
            NemoConfig.togglePreferOriginalQuality();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.preferOriginalQuality);
            }
        } else if (id == hideChannelBottomButtonsRow) {
            NemoConfig.toggleHideChannelBottomButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideChannelBottomButtons);
            }
        } else if (id == hideAiButtonRow) {
            NemoConfig.toggleHideAiButton();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideAiButton);
            }
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.Chat);
    }

    @Override
    protected String getKey() {
        return "c";
    }

}