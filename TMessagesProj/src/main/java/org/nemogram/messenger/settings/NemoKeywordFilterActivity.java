package org.nemogram.messenger.settings;

import android.text.TextUtils;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.HashSet;

public class NemoKeywordFilterActivity extends BaseNemoSettingsActivity {

    private final int filterInChatsRow = rowId++;
    private final int spoilerInChatsRow = rowId++;
    private final int addChatKeywordRow = rowId++;
    private final int filterInChannelsRow = rowId++;
    private final int spoilerInChannelsRow = rowId++;
    private final int addChannelKeywordRow = rowId++;

    private final ArrayList<String> chatKeywords = new ArrayList<>();
    private final ArrayList<Integer> chatKeywordRows = new ArrayList<>();
    private final ArrayList<String> channelKeywords = new ArrayList<>();
    private final ArrayList<Integer> channelKeywordRows = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        reloadKeywords();
    }

    private void reloadKeywords() {
        chatKeywords.clear();
        chatKeywordRows.clear();
        if (NemoConfig.blockedKeywordsChats != null) {
            chatKeywords.addAll(NemoConfig.blockedKeywordsChats);
        }
        for (int i = 0; i < chatKeywords.size(); i++) {
            chatKeywordRows.add(rowId++);
        }

        channelKeywords.clear();
        channelKeywordRows.clear();
        if (NemoConfig.blockedKeywordsChannels != null) {
            channelKeywords.addAll(NemoConfig.blockedKeywordsChannels);
        }
        for (int i = 0; i < channelKeywords.size(); i++) {
            channelKeywordRows.add(rowId++);
        }

        if (listView != null) {
            listView.adapter.update(false);
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.FilterKeywordsChatsHeader)));
        items.add(UItem.asCheck(filterInChatsRow, LocaleController.getString(R.string.FilterKeywordsInChats)).slug("filterInChats").setChecked(NemoConfig.filterKeywordsInChats));
        items.add(UItem.asCheck(spoilerInChatsRow, LocaleController.getString(R.string.SpoilerKeywordsInChats)).setChecked(NemoConfig.spoilerKeywordsInChats));
        items.add(TextSettingsCellFactory.of(addChatKeywordRow, LocaleController.getString(R.string.AddKeyword)).slug("addChatKeyword").accent());
        if (!chatKeywords.isEmpty()) {
            for (int i = 0; i < chatKeywords.size(); i++) {
                items.add(TextSettingsCellFactory.of(chatKeywordRows.get(i), chatKeywords.get(i)).slug("chatkw_" + i));
            }
        }
        items.add(UItem.asShadow(LocaleController.getString(R.string.FilterKeywordsInChatsAbout)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.FilterKeywordsChannelsHeader)));
        items.add(UItem.asCheck(filterInChannelsRow, LocaleController.getString(R.string.FilterKeywordsInChannels)).slug("filterInChannels").setChecked(NemoConfig.filterKeywordsInChannels));
        items.add(UItem.asCheck(spoilerInChannelsRow, LocaleController.getString(R.string.SpoilerKeywordsInChannels)).setChecked(NemoConfig.spoilerKeywordsInChannels));
        items.add(TextSettingsCellFactory.of(addChannelKeywordRow, LocaleController.getString(R.string.AddKeyword)).slug("addChannelKeyword").accent());
        if (!channelKeywords.isEmpty()) {
            for (int i = 0; i < channelKeywords.size(); i++) {
                items.add(TextSettingsCellFactory.of(channelKeywordRows.get(i), channelKeywords.get(i)).slug("channelkw_" + i));
            }
        }
        items.add(UItem.asShadow(LocaleController.getString(R.string.FilterKeywordsInChannelsAbout)));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == filterInChatsRow) {
            NemoConfig.toggleFilterKeywordsInChats();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.filterKeywordsInChats);
            }
        } else if (id == spoilerInChatsRow) {
            NemoConfig.toggleSpoilerKeywordsInChats();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.spoilerKeywordsInChats);
            }
        } else if (id == filterInChannelsRow) {
            NemoConfig.toggleFilterKeywordsInChannels();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.filterKeywordsInChannels);
            }
        } else if (id == spoilerInChannelsRow) {
            NemoConfig.toggleSpoilerKeywordsInChannels();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.spoilerKeywordsInChannels);
            }
        } else if (id == addChatKeywordRow) {
            showAddKeywordDialog(false);
        } else if (id == addChannelKeywordRow) {
            showAddKeywordDialog(true);
        } else {
            int chatIdx = chatKeywordRows.indexOf(id);
            if (chatIdx >= 0) {
                showDeleteKeywordDialog(chatIdx, false);
                return;
            }
            int channelIdx = channelKeywordRows.indexOf(id);
            if (channelIdx >= 0) {
                showDeleteKeywordDialog(channelIdx, true);
            }
        }
    }

    @Override
    protected boolean onItemLongClick(UItem item, View view, int position, float x, float y) {
        int chatIdx = chatKeywordRows.indexOf(item.id);
        if (chatIdx >= 0) {
            showDeleteKeywordDialog(chatIdx, false);
            return true;
        }
        int channelIdx = channelKeywordRows.indexOf(item.id);
        if (channelIdx >= 0) {
            showDeleteKeywordDialog(channelIdx, true);
            return true;
        }
        return false;
    }

    private void showAddKeywordDialog(boolean isChannel) {
        var context = getParentActivity();
        var builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.AddKeyword));
        builder.setCustomViewOffset(0);

        var container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        var editText = new EditTextBoldCursor(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(300)});
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setHintText(LocaleController.getString(R.string.KeywordFilterHint));
        editText.setHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setHeaderHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
        editText.setSingleLine(true);
        editText.setFocusable(true);
        editText.setTransformHintToHeader(true);
        editText.setLineColors(
                Theme.getColor(Theme.key_windowBackgroundWhiteInputField, resourcesProvider),
                Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated, resourcesProvider),
                Theme.getColor(Theme.key_text_RedRegular, resourcesProvider)
        );
        editText.setBackground(null);
        editText.setPadding(0, 0, 0, 0);
        container.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 0, 24, 12, 24, 0));

        builder.setView(container);
        builder.setPositiveButton(LocaleController.getString(R.string.Add), null);
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        var dialog = builder.create();
        dialog.setOnShowListener(d -> {
            editText.requestFocus();
            var button = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (button == null) {
                return;
            }
            button.setOnClickListener(v -> {
                var keyword = editText.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    AndroidUtilities.shakeViewSpring(editText, -6);
                    BotWebViewVibrationEffect.APP_ERROR.vibrate();
                    return;
                }
                if (isChannel) {
                    if (!channelKeywords.contains(keyword)) {
                        var newSet = new HashSet<>(NemoConfig.blockedKeywordsChannels != null ? NemoConfig.blockedKeywordsChannels : new HashSet<>());
                        newSet.add(keyword);
                        NemoConfig.saveBlockedKeywordsChannels(newSet);
                        reloadKeywords();
                    }
                } else {
                    if (!chatKeywords.contains(keyword)) {
                        var newSet = new HashSet<>(NemoConfig.blockedKeywordsChats != null ? NemoConfig.blockedKeywordsChats : new HashSet<>());
                        newSet.add(keyword);
                        NemoConfig.saveBlockedKeywordsChats(newSet);
                        reloadKeywords();
                    }
                }
                dialog.dismiss();
            });
        });
        showDialog(dialog);
    }

    private void showDeleteKeywordDialog(int idx, boolean isChannel) {
        var keyword = isChannel ? channelKeywords.get(idx) : chatKeywords.get(idx);
        var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.DeleteKeyword));
        builder.setMessage(LocaleController.formatString(R.string.DeleteKeywordConfirm, keyword));
        builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
            if (isChannel) {
                var newSet = new HashSet<>(NemoConfig.blockedKeywordsChannels != null ? NemoConfig.blockedKeywordsChannels : new HashSet<>());
                newSet.remove(keyword);
                NemoConfig.saveBlockedKeywordsChannels(newSet);
            } else {
                var newSet = new HashSet<>(NemoConfig.blockedKeywordsChats != null ? NemoConfig.blockedKeywordsChats : new HashSet<>());
                newSet.remove(keyword);
                NemoConfig.saveBlockedKeywordsChats(newSet);
            }
            reloadKeywords();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.KeywordFilter);
    }

    @Override
    protected String getKey() {
        return "kf";
    }
}