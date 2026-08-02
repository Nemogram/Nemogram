package org.nemogram.messenger.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.Locale;

public class NemoAboutSettingsActivity extends BaseNemoSettingsActivity {

    private final int channelRow = rowId++;
    private final int sourceCodeRow = rowId++;
    private final int nemoChannelRow = rowId++;

    private FrameLayout topView;

    @Override
    public View createView(Context context) {
        topView = new FrameLayout(context);

        var logoContainer = new FrameLayout(context);
        var logoView = new BackupImageView(context);

        logoView.setImageDrawable(AppCompatResources.getDrawable(context, R.mipmap.ic_launcher));
        logoContainer.addView(logoView, LayoutHelper.createFrame(90, 90, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15, 0, 0));
        topView.addView(logoContainer, LayoutHelper.createFrame(120, 120, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 11, 0, 0));

        var titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setGravity(Gravity.CENTER);
        titleView.setSingleLine();
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setText(LocaleController.getString(R.string.AppNameNemo));
        titleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        topView.addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 126, 0, 0));

        var subtitleView = new TextView(context);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setSingleLine();
        subtitleView.setEllipsize(TextUtils.TruncateAt.END);
        subtitleView.setText(String.format(Locale.US, "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        subtitleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
        topView.addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 156, 0, 0));

        return super.createView(context);
    }

    @Override
    protected boolean needActionBarPadding() {
        return false;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asCustomShadow(topView, 188));
        items.add(SettingsActivity.SettingCell.Factory.of(channelRow, IconBackgroundColors.CYAN.top, IconBackgroundColors.CYAN.bottom, R.drawable.settings_channel, LocaleController.getString(R.string.OfficialChannel), "@NemogramUpdates").slug("channel"));
        items.add(SettingsActivity.SettingCell.Factory.of(sourceCodeRow, IconBackgroundColors.BLUE_ALT.top, IconBackgroundColors.BLUE_ALT.bottom, R.drawable.settings_faq, LocaleController.getString(R.string.ViewSourceCode), "GitHub").slug("sourceCode"));
        items.add(UItem.asShadow(null));
        items.add(SettingsActivity.SettingCell.Factory.of(nemoChannelRow, IconBackgroundColors.BLUE_LIGHT.top, IconBackgroundColors.BLUE_LIGHT.bottom, R.drawable.settings_channel, "Nekogram", "@nekoupdates").slug("nekoChannel"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == channelRow) {
            getMessagesController().openByUserName("NemogramUpdates", this, 1);
        } else if (id == nemoChannelRow) {
            getMessagesController().openByUserName("nekoupdates", this, 1);
        } else if (id == sourceCodeRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/Nemogram/Nemogram");
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.NemoAbout);
    }
}
