package org.nemogram.messenger.settings.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.IconShapeHelper;
import org.nemogram.messenger.helpers.Md3SectionsHelper;
import org.nemogram.messenger.helpers.MonetHelper;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.SettingsActivity;

@SuppressLint("ViewConstructor")
public class IconShapeCell extends LinearLayout {

    private static final int PREVIEW_BG_DP = 44;
    private static final int PREVIEW_ICON_DP = 38;

    private final SeekBarView seekBar;
    private final FrameLayout[] previews;

    public IconShapeCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        setOrientation(VERTICAL);

        final IconBackgroundColors[] colors = {
                IconBackgroundColors.BLUE, IconBackgroundColors.ORANGE, IconBackgroundColors.GREEN,
                IconBackgroundColors.RED, IconBackgroundColors.BLUE_DEEP
        };
        final int[] icons = {
                R.drawable.settings_account, R.drawable.settings_chat, R.drawable.settings_privacy,
                R.drawable.settings_sounds, R.drawable.settings_data
        };
        final boolean dark = resourcesProvider != null ? resourcesProvider.isDark() : Theme.isCurrentThemeDark();

        LinearLayout previewRow = new LinearLayout(context);
        previewRow.setOrientation(HORIZONTAL);
        previewRow.setGravity(Gravity.CENTER);
        previews = new FrameLayout[colors.length];
        for (int i = 0; i < colors.length; i++) {
            SettingsActivity.SettingCell.Background background = new SettingsActivity.SettingCell.Background();
            final int top = MonetHelper.getSettingsIconBackgroundColor(colors[i].top);
            final int bottom = MonetHelper.getSettingsIconBackgroundColor(colors[i].bottom);
            background.setColor(top, bottom, AndroidUtilities.dp(PREVIEW_BG_DP));
            background.setDrawBorder(dark);
            if (Md3SectionsHelper.isEnabled()) {
                background.md3FlatColor = top;
            }

            FrameLayout preview = new FrameLayout(context);
            preview.setBackground(background);

            ImageView icon = new ImageView(context);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setColorFilter(new PorterDuffColorFilter(MonetHelper.getSettingsIconForegroundColor(Color.WHITE), PorterDuff.Mode.SRC_IN));
            IconShapeHelper.setIcon(icon, icons[i]);
            preview.addView(icon, LayoutHelper.createFrame(PREVIEW_ICON_DP, PREVIEW_ICON_DP, Gravity.CENTER));

            previews[i] = preview;
            previewRow.addView(preview, LayoutHelper.createLinear(PREVIEW_BG_DP, PREVIEW_BG_DP, i == 0 ? 0 : 14, 0, 0, 0));
        }
        addView(previewRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 16, 0, 0));

        seekBar = new SeekBarView(context, true, resourcesProvider);
        seekBar.setReportChanges(true);
        seekBar.setDelegate((stop, progress) -> {
            final int value = Math.round(progress * 100f);
            if (value != NemoConfig.iconRoundness) {
                NemoConfig.iconRoundness = value;
                update();
            }
            if (stop) {
                NemoConfig.setIconRoundness(value);
            }
        });
        addView(seekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 6, 8, 6, 0));

        FrameLayout labels = new FrameLayout(context);
        labels.addView(createLabel(context, resourcesProvider, LocaleController.getString(R.string.IconShapeLeft), Gravity.LEFT), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));
        labels.addView(createLabel(context, resourcesProvider, LocaleController.getString(R.string.IconShapeRight), Gravity.RIGHT), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        addView(labels, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 21, 0, 21, 14));

        seekBar.setProgress(NemoConfig.iconRoundness / 100f);
        update();
    }

    private static TextView createLabel(Context context, Theme.ResourcesProvider resourcesProvider, String text, int gravity) {
        TextView label = new TextView(context);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        label.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        label.setGravity(gravity);
        label.setText(text);
        return label;
    }

    private void update() {
        for (FrameLayout preview : previews) {
            // background and glyph both read the current value at draw time
            preview.invalidate();
            preview.getChildAt(0).invalidate();
        }
    }
}
