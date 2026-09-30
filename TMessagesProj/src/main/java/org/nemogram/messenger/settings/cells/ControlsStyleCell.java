package org.nemogram.messenger.settings.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.MonetHelper;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

/**
 * Picker for the switch / slider style. Shows one card per style, each with real
 * {@link Switch} and {@link SeekBarView} instances forced to that style, so the user
 * sees exactly what they will get.
 */
@SuppressLint("ViewConstructor")
public class ControlsStyleCell extends FrameLayout {


    private final Theme.ResourcesProvider resourcesProvider;
    private final Card[] cards = new Card[2];

    public ControlsStyleCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        final String[] names = {
                LocaleController.getString(R.string.ControlsStyleDefault),
                LocaleController.getString(R.string.ControlsStyleMD3)
        };
        final int[] styles = {
                NemoConfig.CONTROLS_STYLE_DEFAULT,
                NemoConfig.CONTROLS_STYLE_MD3
        };

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < cards.length; i++) {
            final int style = styles[i];
            cards[i] = new Card(context, style, names[i]);
            cards[i].setOnClickListener(v -> select(style, true));
            row.addView(cards[i], LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, i == 0 ? 0 : 8, 0, 0, 0));
        }
        addView(row, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL, 16, 14, 16, 14));

        setWillNotDraw(false);
        updateSelection(false);
    }

    /** Called after the user picked a different style. The config is already updated when this fires. */
    protected void onStyleSelected(int style) {

    }

    private void select(int style, boolean fromUser) {
        if (fromUser && style != NemoConfig.controlsStyle) {
            NemoConfig.setControlsStyle(style);
            try {
                if (NemoConfig.moreHapticFeedbacks) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                }
            } catch (Exception ignored) {
            }
            updateSelection(true);
            onStyleSelected(style);
        }
    }

    public void updateSelection(boolean animated) {
        for (Card card : cards) {
            card.setSelectedStyle(card.style == NemoConfig.controlsStyle, animated);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148), MeasureSpec.EXACTLY)
        );
    }

    private class Card extends FrameLayout {

        final int style;
        private final String name;
        private final Switch switchOn;
        private final TextView label;
        private final FrameLayout selectedPill;

        private boolean selected;

        Card(Context context, int style, String name) {
            super(context);
            this.style = style;
            this.name = name;

            setClickable(true);
            setFocusable(true);
            setContentDescription(name);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

            LinearLayout switches = new LinearLayout(context);
            switches.setOrientation(LinearLayout.HORIZONTAL);
            switches.setGravity(Gravity.CENTER);

            Switch switchOff = createSwitch(context, false);
            switchOn = createSwitch(context, true);
            int switchHeight = style == NemoConfig.CONTROLS_STYLE_MD3 ? 24 : 20;
            switches.addView(switchOff, LayoutHelper.createLinear(37, switchHeight));
            switches.addView(switchOn, LayoutHelper.createLinear(37, switchHeight, 6, 0, 0, 0));
            addView(switches, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 30, Gravity.TOP, 0, 14, 0, 0));

            SeekBarView seekBar = new SeekBarView(context, resourcesProvider);
            seekBar.setStyleOverride(style);
            seekBar.setReportChanges(false);
            seekBar.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            seekBar.setProgress(0.64f);
            addView(seekBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 32, Gravity.TOP, 6, 50, 6, 0));

            FrameLayout titleLayout = new FrameLayout(context);
            TextView unselectedLabel = new TextView(context);
            unselectedLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            unselectedLabel.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
            unselectedLabel.setTypeface(AndroidUtilities.bold());
            unselectedLabel.setGravity(Gravity.CENTER);
            unselectedLabel.setSingleLine(true);
            unselectedLabel.setText(name);
            unselectedLabel.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
            titleLayout.addView(unselectedLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 26, Gravity.CENTER));

            selectedPill = new FrameLayout(context);
            selectedPill.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
            selectedPill.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(13),
                    MonetHelper.getSettingsIconBackgroundColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider))));
            titleLayout.addView(selectedPill, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 26, Gravity.CENTER));

            label = new TextView(context);
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            label.setTextColor(MonetHelper.getSettingsIconForegroundColor(
                    Theme.getColor(Theme.key_windowBackgroundCheckText, resourcesProvider)));
            label.setTypeface(AndroidUtilities.bold());
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setText(name);
            selectedPill.addView(label, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
            addView(titleLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 26, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 11));
            setSelectedStyle(false, false);
        }

        private Switch createSwitch(Context context, boolean checked) {
            Switch s = new Switch(context, resourcesProvider);
            s.setStyleOverride(style);
            s.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            s.setChecked(checked, false);
            s.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            return s;
        }

        // The card owns every touch so the preview controls never react on their own.
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }

        void setSelectedStyle(boolean value, boolean animated) {
            boolean changed = selected != value;
            selected = value;
            selectedPill.animate().cancel();
            if (animated) {
                selectedPill.animate()
                        .scaleX(value ? 1f : 0f)
                        .scaleY(value ? 1f : 0f)
                        .alpha(value ? 1f : 0f)
                        .setInterpolator(org.telegram.ui.Components.CubicBezierInterpolator.EASE_OUT_QUINT)
                        .setDuration(320)
                        .start();
            } else {
                selectedPill.setScaleX(value ? 1f : 0f);
                selectedPill.setScaleY(value ? 1f : 0f);
                selectedPill.setAlpha(value ? 1f : 0f);
            }
            if (changed && value && animated) {
                switchOn.setChecked(false, false);
                switchOn.postDelayed(() -> switchOn.setChecked(true, true), 70);
            } else if (changed && animated) {
                switchOn.setChecked(false, true);
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.widget.RadioButton");
            info.setCheckable(true);
            info.setChecked(selected);
            info.setText(name);
        }
    }
}
