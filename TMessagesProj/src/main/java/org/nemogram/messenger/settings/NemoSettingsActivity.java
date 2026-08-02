package org.nemogram.messenger.settings;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.ProfileActivity.SearchAdapter.SearchResult;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;
import org.nemogram.messenger.accessibility.AccessibilitySettingsActivity;
import org.nemogram.messenger.helpers.PasscodeHelper;

public class NemoSettingsActivity extends BaseNemoSettingsActivity implements FactorAnimator.Target {

    private static final int ANIMATOR_ID_SEARCH_PAGE_VISIBLE = 0;

    private final BoolAnimator animatorSearchPageVisible = new BoolAnimator(ANIMATOR_ID_SEARCH_PAGE_VISIBLE,
            this, CubicBezierInterpolator.EASE_OUT_QUINT, 350);

    private final int generalRow = rowId++;
    private final int appearanceRow = rowId++;
    private final int chatRow = rowId++;
    private final int keywordFilterRow = rowId++;
    private final int passcodeRow = rowId++;
    private final int experimentRow = rowId++;
    private final int accessibilityRow = rowId++;

    private final int pgpRow = rowId++;
    private final int aboutRow = rowId++;

    private final ArrayList<SearchResult> searchArray = createSearchArray();
    private final ArrayList<CharSequence> resultNames = new ArrayList<>();
    private final ArrayList<SearchResult> searchResults = new ArrayList<>();
    private boolean searchWas;
    private Runnable searchRunnable;
    private String lastSearchString;

    @Override
    public View createView(Context context) {
        var fragmentView = super.createView(context);

        var menu = actionBar.createMenu();
        createSearchItem(menu, new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            @Override
            public void onSearchCollapse() {
                animatorSearchPageVisible.setValue(false, true);
                updateActionBarVisible();
                listView.adapter.update(true);
            }

            @Override
            public void onSearchExpand() {
                animatorSearchPageVisible.setValue(true, true);
                updateActionBarVisible();
                search("");
                listView.adapter.update(true);
            }

            @Override
            public void onTextChanged(EditText editText) {
                search(editText.getText().toString());
            }
        });

        return fragmentView;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (isSearchFieldVisible()) {
            items.add(UItem.asSpace(ActionBar.getCurrentActionBarHeight()));
            fillSearchItems(items);
            return;
        }

        items.add(UItem.asHeader(LocaleController.getString(R.string.NemoSettingsSectionMain)));
        items.add(SettingsActivity.SettingCell.Factory.of(generalRow, IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom, R.drawable.filled_poll_multiple_24, LocaleController.getString(R.string.General)).slug("general"));
        items.add(SettingsActivity.SettingCell.Factory.of(appearanceRow, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom, R.drawable.settings_features, LocaleController.getString(R.string.ChangeChannelNameColor2)).slug("appearance"));
        items.add(SettingsActivity.SettingCell.Factory.of(chatRow, IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom, R.drawable.settings_chat, LocaleController.getString(R.string.Chat)).slug("chat"));
        items.add(SettingsActivity.SettingCell.Factory.of(experimentRow, IconBackgroundColors.ORANGE_DEEP.top, IconBackgroundColors.ORANGE_DEEP.bottom, R.drawable.filled_premium_away, LocaleController.getString(R.string.NotificationsOther)).slug("experiment"));
        AccessibilityManager am = (AccessibilityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null && am.isTouchExplorationEnabled()) {
            items.add(SettingsActivity.SettingCell.Factory.of(accessibilityRow, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_language, LocaleController.getString(R.string.AccessibilitySettings)).slug("accessibility"));
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NemoSettingsSectionSecurity)));
        items.add(SettingsActivity.SettingCell.Factory.of(keywordFilterRow, IconBackgroundColors.RED.top, IconBackgroundColors.RED.bottom, R.drawable.msg_filled_blocked, LocaleController.getString(R.string.KeywordFilter)).slug("keywordFilter"));
        if (!PasscodeHelper.isSettingsHidden()) {
            items.add(SettingsActivity.SettingCell.Factory.of(passcodeRow, IconBackgroundColors.BLUE_DEEP.top, IconBackgroundColors.BLUE_DEEP.bottom, R.drawable.settings_privacy, LocaleController.getString(R.string.PasscodeNemo)).slug("passcode"));
        }
        items.add(SettingsActivity.SettingCell.Factory.of(pgpRow, IconBackgroundColors.GRAY.top, IconBackgroundColors.GRAY.bottom, R.drawable.settings_policy, LocaleController.getString(R.string.PgpSettings)).slug("pgp"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NemoAbout)));
        items.add(SettingsActivity.SettingCell.Factory.of(aboutRow, IconBackgroundColors.BLUE_ALT.top, IconBackgroundColors.BLUE_ALT.bottom, R.drawable.filled_info, LocaleController.getString(R.string.NemoAbout)).slug("about"));
        items.add(UItem.asShadow(null));

    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        if (item.instanceOf(SettingsSearchCell.Factory.class)) {
            if (item.object instanceof SearchResult r) {
                r.open(null);
            }
            return;
        }
        var id = item.id;
        if (id == chatRow) {
            presentFragment(new NemoChatSettingsActivity());
        } else if (id == keywordFilterRow) {
        presentFragment(new NemoKeywordFilterActivity());
        } else if (id == generalRow) {
            presentFragment(new NemoGeneralSettingsActivity());
        } else if (id == appearanceRow) {
            presentFragment(new NemoAppearanceSettingsActivity());
        } else if (id == passcodeRow) {
            presentFragment(new NemoPasscodeSettingsActivity());
        } else if (id == experimentRow) {
            presentFragment(new NemoExperimentalSettingsActivity());
        } else if (id == accessibilityRow) {
            presentFragment(new AccessibilitySettingsActivity());
        } else if (id == pgpRow) {
            presentFragment(new org.nemogram.messenger.pgp.ui.PgpSettingsActivity());
        } else if (id == aboutRow) {
            presentFragment(new NemoAboutSettingsActivity());
        }
    }

    @Override
    protected boolean onItemLongClick(UItem item, View view, int position, float x, float y) {
        return super.onItemLongClick(item, view, position, x, y);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.NemoSettings);
    }

    @Override
    protected String getKey() {
        return "";
    }

    @Override
    public void onFactorChanged(int id, float factor, float fraction, FactorAnimator callee) {
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return !animatorSearchPageVisible.getValue();
    }

    private static BaseNemoSettingsActivity createFragment(int icon) {
        if (icon == R.drawable.filled_poll_multiple_24) {
            return new NemoGeneralSettingsActivity();
        } else if (icon == R.drawable.settings_features) {
            return new NemoAppearanceSettingsActivity();
        } else if (icon == R.drawable.settings_chat) {
            return new NemoChatSettingsActivity();
        } else if (icon == R.drawable.filled_premium_away) {
            return new NemoExperimentalSettingsActivity();
        } else if (icon == R.drawable.msg_filled_blocked) {
            return new NemoKeywordFilterActivity();
        }
        return new NemoSettingsActivity();
    }

    private ArrayList<SearchResult> createSearchArray() {
        var searchResultList = new ArrayList<SearchResult>();
        var icons = new int[]{
                R.drawable.filled_poll_multiple_24,
                R.drawable.settings_features,
                R.drawable.settings_chat,
                R.drawable.filled_premium_away,
                R.drawable.msg_filled_blocked,
        };
        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var fragment = createFragment(icon);
            var items = new ArrayList<UItem>();
            fragment.fillItems(items, null);
            var fragmentTitle = fragment.getActionBarTitle();
            String headerText = null;
            for (var item : items) {
                if (item.viewType == UniversalAdapter.VIEW_TYPE_HEADER) {
                    headerText = item.text.toString();
                    continue;
                } else if (item.viewType == UniversalAdapter.VIEW_TYPE_SHADOW) {
                    headerText = null;
                    continue;
                }
                if (TextUtils.isEmpty(item.slug)) continue;
                searchResultList.add(new SearchResult(i * 1000 + item.id, item.text.toString(), null, fragmentTitle, fragmentTitle.equals(headerText) ? null : headerText, icon, () -> {
                    var fragment1 = createFragment(icon);
                    presentFragment(fragment1);
                    AndroidUtilities.runOnUIThread(() -> fragment1.scrollToRow(item.slug, () -> {
                    }));
                }));
            }
            searchResultList.add(new SearchResult(10000 + i, fragmentTitle, icon, () -> presentFragment(fragment)));
        }
        searchResultList.add(new SearchResult(8000, LocaleController.getString(R.string.EmojiUseDefault), null, LocaleController.getString(R.string.ChangeChannelNameColor2), LocaleController.getString(R.string.EmojiSets), R.drawable.settings_chat, () -> {
            var fragment = new NemoEmojiSettingsActivity();
            presentFragment(fragment);
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow("useSystemEmoji", () -> {
            }));
        }));

        searchResultList.add(new SearchResult(20000, LocaleController.getString(R.string.OfficialChannel), "@NemogramUpdates", R.drawable.settings_channel, () -> {
            var fragment = new NemoAboutSettingsActivity();
            presentFragment(fragment);
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow("channel", () -> {
            }));
        }));
        searchResultList.add(new SearchResult(20002, LocaleController.getString(R.string.ViewSourceCode), "GitHub", R.drawable.settings_faq, () -> {
            var fragment = new NemoAboutSettingsActivity();
            presentFragment(fragment);
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow("sourceCode", () -> {
            }));
        }));
        searchResultList.add(new SearchResult(20004, "Nekogram", "@nekoupdates", R.drawable.settings_channel, () -> {
            var fragment = new NemoAboutSettingsActivity();
            presentFragment(fragment);
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow("nekoChannel", () -> {
            }));
        }));
        searchResultList.add(new SearchResult(20006, LocaleController.getString(R.string.PgpSettings), R.drawable.settings_policy, () -> {
            presentFragment(new org.nemogram.messenger.pgp.ui.PgpSettingsActivity());
        }));

        return searchResultList;
    }

    private void fillSearchItems(ArrayList<UItem> items) {
        if (searchWas) {
            for (int i = 0; i < searchResults.size(); i++) {
                items.add(SettingsSearchCell.Factory.of(resultNames.get(i), searchResults.get(i)));
            }
            if (!searchResults.isEmpty()) items.add(UItem.asShadow(null));
        }
    }

    private void search(String text) {
        lastSearchString = text;
        if (searchRunnable != null) {
            Utilities.searchQueue.cancelRunnable(searchRunnable);
            searchRunnable = null;
        }
        if (TextUtils.isEmpty(text)) {
            searchWas = false;
            searchResults.clear();
            resultNames.clear();
            listView.adapter.update(true);
            return;
        }
        Utilities.searchQueue.postRunnable(searchRunnable = () -> {
            var results = new ArrayList<SearchResult>();
            var names = new ArrayList<CharSequence>();
            var lowerQuery = text.toLowerCase();
            for (var result : searchArray) {
                var title = result.searchTitle.toLowerCase();
                var index = title.indexOf(lowerQuery);
                var matchLen = lowerQuery.length();
                if (index < 0) continue;
                var ssb = new SpannableStringBuilder(result.searchTitle);
                ssb.setSpan(new ForegroundColorSpan(getThemedColor(Theme.key_windowBackgroundWhiteBlueText4)), index, Math.min(index + matchLen, ssb.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                results.add(result);
                names.add(ssb);
            }

            AndroidUtilities.runOnUIThread(() -> {
                if (!text.equals(lastSearchString)) {
                    return;
                }
                searchWas = true;
                searchResults.clear();
                resultNames.clear();
                searchResults.addAll(results);
                resultNames.addAll(names);
                listView.adapter.update(true);
            });
        }, 300);
    }
}
