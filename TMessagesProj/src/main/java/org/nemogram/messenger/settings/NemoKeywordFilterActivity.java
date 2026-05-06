package org.nemogram.messenger.settings;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
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
        var editText = new EditText(getParentActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(LocaleController.getString(R.string.KeywordFilterHint));
        editText.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(300)});
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray3, resourcesProvider));
        editText.setBackground(Theme.createEditTextDrawable(getParentActivity(), true));
        int padding = (int) (16 * getParentActivity().getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding / 2, padding, padding / 2);
        var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.AddKeyword));
        builder.setView(editText);
        builder.setPositiveButton(LocaleController.getString(R.string.Add), (dialog, which) -> {
            var keyword = editText.getText().toString().trim();
            if (isChannel) {
                if (!keyword.isEmpty() && !channelKeywords.contains(keyword)) {
                    var newSet = new HashSet<>(NemoConfig.blockedKeywordsChannels != null ? NemoConfig.blockedKeywordsChannels : new HashSet<>());
                    newSet.add(keyword);
                    NemoConfig.saveBlockedKeywordsChannels(newSet);
                    reloadKeywords();
                }
            } else {
                if (!keyword.isEmpty() && !chatKeywords.contains(keyword)) {
                    var newSet = new HashSet<>(NemoConfig.blockedKeywordsChats != null ? NemoConfig.blockedKeywordsChats : new HashSet<>());
                    newSet.add(keyword);
                    NemoConfig.saveBlockedKeywordsChats(newSet);
                    reloadKeywords();
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        var dialog = builder.create();
        dialog.setOnShowListener(d -> editText.requestFocus());
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