package org.nemogram.messenger.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

public class SearchBarStyleActivity extends BaseNemoSettingsActivity {

    private final int previewRow = rowId++;
    private final int styleHeaderRow = rowId++;
    private final int normalStyleRow = rowId++;
    private final int compactStyleRow = rowId++;
    private final int styleShadowRow = rowId++;
    private final int placeholderHeaderRow = rowId++;
    private final int emptyPlaceholderRow = rowId++;
    private final int placeholderRow = rowId++;
    private final int placeholderShadowRow = rowId++;
    private final int scrollHeaderRow = rowId++;
    private final int hideOnScrollRow = rowId++;
    private final int scrollShadowRow = rowId++;

    private SearchBarPreviewCell previewCell;

    private boolean isNormalStyle() {
        return NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_NORMAL;
    }

    private String placeholderDisplayValue() {
        if (NemoConfig.hideSearchBarPlaceholder) {
            return LocaleController.getString(R.string.SearchBarPlaceholderEmpty);
        }
        if (NemoConfig.searchBarPlaceholder.isEmpty()) {
            return LocaleController.getString(R.string.SearchChats);
        }
        return NemoConfig.searchBarPlaceholder;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (previewCell == null) {
            previewCell = new SearchBarPreviewCell(getContext(), resourcesProvider);
        }
        items.add(UItem.asCustom(previewRow, previewCell));

        items.add(UItem.asHeader(styleHeaderRow,
                LocaleController.getString(R.string.SearchBarStyle)));
        items.add(UItem.asRadio(normalStyleRow,
                        LocaleController.getString(R.string.SearchBarStyleNormal))
                .setChecked(NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_NORMAL)
                .slug("searchBarStyleNormal"));
        items.add(UItem.asRadio(compactStyleRow,
                        LocaleController.getString(R.string.SearchBarStyleCompact))
                .setChecked(NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_COMPACT)
                .slug("searchBarStyleCompact"));
        items.add(UItem.asShadow(styleShadowRow, null));

        items.add(UItem.asHeader(placeholderHeaderRow,
                LocaleController.getString(R.string.Placeholder)));
        items.add(UItem.asCheck(emptyPlaceholderRow,
                        LocaleController.getString(R.string.SearchBarPlaceholderHide))
                .setChecked(NemoConfig.hideSearchBarPlaceholder)
                .setEnabled(isNormalStyle())
                .slug("hideSearchBarPlaceholder"));
        items.add(TextSettingsCellFactory.of(placeholderRow,
                        LocaleController.getString(R.string.SearchBarPlaceholder),
                        placeholderDisplayValue())
                .setEnabled(isNormalStyle() && !NemoConfig.hideSearchBarPlaceholder)
                .slug("searchBarPlaceholder"));
        items.add(UItem.asShadow(placeholderShadowRow, null));

        items.add(UItem.asHeader(scrollHeaderRow,
                LocaleController.getString(R.string.LocalOther)));
        items.add(UItem.asCheck(hideOnScrollRow,
                        LocaleController.getString(R.string.HideSearchBarOnScroll))
                .setChecked(NemoConfig.hideSearchBarOnScroll)
                .setEnabled(isNormalStyle())
                .slug("hideSearchBarOnScroll"));
        items.add(UItem.asShadow(scrollShadowRow, null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;

        if (id == normalStyleRow) {
            if (NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_NORMAL) return;
            NemoConfig.setSearchBarStyle(NemoConfig.SEARCH_BAR_NORMAL);
            refreshAllRows();
            showRestartBulletin();

        } else if (id == compactStyleRow) {
            if (NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_COMPACT) return;
            NemoConfig.setSearchBarStyle(NemoConfig.SEARCH_BAR_COMPACT);
            refreshAllRows();
            showRestartBulletin();

        } else if (id == emptyPlaceholderRow) {
            if (!isNormalStyle()) return;
            NemoConfig.toggleHideSearchBarPlaceholder();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideSearchBarPlaceholder);
            }
            listView.adapter.updateWithoutNotify();
            notifyItemChanged(placeholderRow);
            if (previewCell != null) previewCell.update();
            showRestartBulletin();

        } else if (id == placeholderRow) {
            if (!isNormalStyle() || NemoConfig.hideSearchBarPlaceholder) return;
            showPlaceholderDialog();

        } else if (id == hideOnScrollRow) {
            if (!isNormalStyle()) return;
            NemoConfig.toggleHideSearchBarOnScroll();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideSearchBarOnScroll);
            }
            if (previewCell != null) previewCell.update();
        }
    }

    private void showPlaceholderDialog() {
        var editText = new EditText(getParentActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(LocaleController.getString(R.string.SearchChats));
        editText.setText(NemoConfig.searchBarPlaceholder);
        editText.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(50)});
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray3, resourcesProvider));
        editText.setBackground(Theme.createEditTextDrawable(getParentActivity(), true));
        int padding = (int) (16 * getParentActivity().getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding / 2, padding, padding / 2);
        editText.setSelection(editText.getText().length());

        var builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.SearchBarPlaceholder));
        builder.setView(editText);
        builder.setPositiveButton(LocaleController.getString(R.string.Done), (dialog, which) -> {
            var text = editText.getText().toString().trim();
            NemoConfig.setSearchBarPlaceholder(text);
            listView.adapter.updateWithoutNotify();
            notifyItemChanged(placeholderRow);
            if (previewCell != null) previewCell.update();
            showRestartBulletin();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        var dialog = builder.create();
        dialog.setOnShowListener(d -> editText.requestFocus());
        showDialog(dialog);
    }

    private void refreshAllRows() {
        listView.adapter.updateWithoutNotify();
        notifyItemChanged(normalStyleRow);
        notifyItemChanged(compactStyleRow);
        notifyItemChanged(emptyPlaceholderRow);
        notifyItemChanged(placeholderRow);
        notifyItemChanged(hideOnScrollRow);
        if (previewCell != null) previewCell.update();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.SearchBarStyle);
    }

    @Override
    protected String getKey() {
        return "appearance";
    }

    @SuppressLint("ViewConstructor")
    private static class SearchBarPreviewCell extends FrameLayout {

        private static final int CARD_HEIGHT_NORMAL_DP = 48 + 48 + 72 * 3;
        private static final int CARD_HEIGHT_COMPACT_DP = 48 + 72 * 4;
        private final Theme.ResourcesProvider rp;
        private final FrameLayout searchBarRow;
        private final ImageView compactIcon;
        private final LinearLayout dialogsContainer;
        private final TextView placeholder;

        SearchBarPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            rp = resourcesProvider;
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, rp));
            setClipChildren(true);
            setClipToPadding(true);

            var inner = new LinearLayout(context);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setClipChildren(true);
            addView(inner, LayoutHelper.createFrame(
                    LayoutHelper.MATCH_PARENT, CARD_HEIGHT_NORMAL_DP));

            var abBg = new FrameLayout(context);
            abBg.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault, rp));
            inner.addView(abBg, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

            var back = new ImageView(context);
            back.setImageResource(R.drawable.ic_ab_back);
            back.setColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon, rp));
            abBg.addView(back, LayoutHelper.createFrame(24, 24,
                    Gravity.CENTER_VERTICAL | Gravity.START, 8, 0, 0, 0));

            var tvTitle = new TextView(context);
            tvTitle.setText(LocaleController.getString(R.string.SearchBarStylePreviewTitle));
            tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle, rp));
            tvTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
            tvTitle.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            abBg.addView(tvTitle, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.START, 48, 0, 0, 0));

            compactIcon = new ImageView(context);
            compactIcon.setImageResource(R.drawable.outline_header_search);
            compactIcon.setColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon, rp));
            compactIcon.setVisibility(GONE);
            abBg.addView(compactIcon, LayoutHelper.createFrame(24, 24,
                    Gravity.CENTER_VERTICAL | Gravity.END, 0, 0, 12, 0));

            searchBarRow = new FrameLayout(context);
            searchBarRow.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault, rp));
            searchBarRow.setPadding(
                    AndroidUtilities.dp(8), AndroidUtilities.dp(6),
                    AndroidUtilities.dp(8), AndroidUtilities.dp(6));

            var fieldBg = new FrameLayout(context);
            int fieldColor = ColorUtils.blendARGB(
                    Theme.getColor(Theme.key_actionBarDefault, rp), Color.WHITE, 0.15f);
            fieldBg.setBackground(
                    Theme.createRoundRectDrawable(AndroidUtilities.dp(18), fieldColor));

            var fieldIcon = new ImageView(context);
            fieldIcon.setImageResource(R.drawable.outline_header_search);
            fieldIcon.setColorFilter(Theme.getColor(Theme.key_actionBarDefaultSearch, rp));
            fieldBg.addView(fieldIcon, LayoutHelper.createFrame(
                    18, 18, Gravity.CENTER_VERTICAL | Gravity.START, 10, 0, 0, 0));

            placeholder = new TextView(context);
            placeholder.setTextColor(
                    Theme.getColor(Theme.key_actionBarDefaultSearchPlaceholder, rp));
            placeholder.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            fieldBg.addView(placeholder, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.START, 36, 0, 8, 0));

            searchBarRow.addView(fieldBg,
                    LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36));
            inner.addView(searchBarRow,
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

            dialogsContainer = new LinearLayout(context);
            dialogsContainer.setOrientation(LinearLayout.VERTICAL);
            inner.addView(dialogsContainer,
                    LayoutHelper.createLinear(
                            LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            update();
        }

        private void buildFakeDialogs(int count) {
            dialogsContainer.removeAllViews();
            String[] names = {"Artur", "Jose", "Charlie", "Maria"};
            String[] messages = {"What do you think about Telegram?", "See you tomorrow!", "Sent a photo", "=)"};
            boolean[] pinned = {true, false, false, false};
            int[] unread = {3, 0, 1, 0};
            int[] sent = {-1, 1, -1, 1};
            int now = (int) (System.currentTimeMillis() / 1000);

            for (int i = 0; i < count; i++) {
                var cell = new DialogCell(null, getContext(), false, false, 0, rp);
                var cd = new DialogCell.CustomDialog();
                cd.name = names[i];
                cd.message = messages[i];
                cd.id = i + 1;
                cd.unread_count = unread[i];
                cd.pinned = pinned[i];
                cd.muted = false;
                cd.type = 0;
                cd.date = now - 60 * (i + 1);
                cd.verified = false;
                cd.isMedia = false;
                cd.sent = sent[i];
                cell.setDialog(cd);
                cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite, rp));
                dialogsContainer.addView(cell, LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        void update() {
            boolean compact = NemoConfig.searchBarStyle == NemoConfig.SEARCH_BAR_COMPACT;

            compactIcon.setVisibility(compact ? VISIBLE : GONE);
            searchBarRow.setVisibility(compact ? GONE : VISIBLE);

            if (NemoConfig.hideSearchBarPlaceholder) {
                placeholder.setText("");
            } else {
                placeholder.setText(NemoConfig.searchBarPlaceholder.isEmpty()
                        ? LocaleController.getString(R.string.SearchChats)
                        : NemoConfig.searchBarPlaceholder);
            }

            buildFakeDialogs(compact ? 4 : 3);

            int targetDp = compact ? CARD_HEIGHT_COMPACT_DP : CARD_HEIGHT_NORMAL_DP;
            var lp = getChildAt(0).getLayoutParams();
            lp.height = AndroidUtilities.dp(targetDp);
            getChildAt(0).setLayoutParams(lp);
        }
    }
}