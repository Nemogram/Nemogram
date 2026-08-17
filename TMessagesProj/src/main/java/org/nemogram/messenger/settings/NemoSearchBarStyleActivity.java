package org.nemogram.messenger.settings;

import android.text.TextUtils;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
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

public class NemoSearchBarStyleActivity extends BaseNemoSettingsActivity {

    private static final int[] TRIANGLE_STYLES = {
            NemoConfig.SEARCH_BAR_NORMAL,
            NemoConfig.SEARCH_BAR_COMPACT,
            NemoConfig.SEARCH_BAR_MATERIAL
    };

    private final int styleHeaderRow = rowId++;
    private final int styleSwitcherRow = rowId++;
    private final int styleShadowRow = rowId++;
    private final int placeholderHeaderRow = rowId++;
    private final int emptyPlaceholderRow = rowId++;
    private final int placeholderRow = rowId++;
    private final int placeholderShadowRow = rowId++;
    private final int scrollHeaderRow = rowId++;
    private final int hideOnScrollRow = rowId++;
    private final int scrollShadowRow = rowId++;

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

    private int selectedStyleIndex() {
        for (int i = 0; i < TRIANGLE_STYLES.length; i++) {
            if (TRIANGLE_STYLES[i] == NemoConfig.searchBarStyle) return i;
        }
        return 0;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(styleHeaderRow,
                LocaleController.getString(R.string.SearchBarStyle)));
        items.add(SearchBarStyleSwitcher.Factory.asSwitcher(styleSwitcherRow, index -> {
            int newStyle = TRIANGLE_STYLES[index];
            if (NemoConfig.searchBarStyle == newStyle) return;
            NemoConfig.setSearchBarStyle(newStyle);
            refreshAllRows();
            showRestartBulletin();
        }, selectedStyleIndex()));
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

        if (id == emptyPlaceholderRow) {
            if (!isNormalStyle()) return;
            NemoConfig.toggleHideSearchBarPlaceholder();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NemoConfig.hideSearchBarPlaceholder);
            }
            listView.adapter.updateWithoutNotify();
            notifyItemChanged(placeholderRow);
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
        }
    }

    private void showPlaceholderDialog() {
        var context = getParentActivity();
        var builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.SearchBarPlaceholder));
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
        editText.setHintText(LocaleController.getString(R.string.SearchChats));
        editText.setText(NemoConfig.searchBarPlaceholder);
        editText.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(50)});
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
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
        editText.setSelection(editText.getText().length());
        container.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 0, 24, 12, 24, 0));

        builder.setView(container);
        builder.setPositiveButton(LocaleController.getString(R.string.Done), null);
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        var dialog = builder.create();
        dialog.setOnShowListener(d -> {
            editText.requestFocus();
            var button = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (button == null) {
                return;
            }
            button.setOnClickListener(v -> {
                var text = editText.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    AndroidUtilities.shakeViewSpring(editText, -6);
                    BotWebViewVibrationEffect.APP_ERROR.vibrate();
                    return;
                }
                NemoConfig.setSearchBarPlaceholder(text);
                listView.adapter.updateWithoutNotify();
                notifyItemChanged(placeholderRow);
                showRestartBulletin();
                dialog.dismiss();
            });
        });
        showDialog(dialog);
    }

    private void refreshAllRows() {
        listView.adapter.updateWithoutNotify();
        notifyItemChanged(emptyPlaceholderRow);
        notifyItemChanged(placeholderRow);
        notifyItemChanged(hideOnScrollRow);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.SearchBarStyle);
    }

    @Override
    protected String getKey() {
        return "searchbarstyle";
    }

}
