package org.nemogram.messenger.settings.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;

@SuppressLint("ViewConstructor")
public class InputPanelStyleCell extends LinearLayout {

    private static final int BOX_RADIUS_DP = 10;

    private static final int PANEL_BAR_ALPHA = 60;
    private static final int PANEL_BUTTON_ALPHA = 204;

    private class StyleBox extends FrameLayout {

        private final RadioButton button;
        private final boolean isLegacy;
        private final RectF rect = new RectF();
        private final Path path = new Path();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Theme.ResourcesProvider resourcesProvider;

        public StyleBox(Context context, boolean legacy, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

            isLegacy = legacy;
            setContentDescription(legacy ? LocaleController.getString(R.string.LegacyInputPanel) : LocaleController.getString(R.string.ModernInputPanel));

            textPaint.setTextSize(AndroidUtilities.dp(13));

            button = new RadioButton(context) {
                @Override
                public void invalidate() {
                    super.invalidate();
                    StyleBox.this.invalidate();
                }
            };
            button.setSize(AndroidUtilities.dp(20));
            addView(button, LayoutHelper.createFrame(22, 22, Gravity.RIGHT | Gravity.TOP, 0, 26, 10, 0));
            button.setChecked(isLegacy == NemoConfig.legacyInputPanel, false);
        }

        void setChecked(boolean checked, boolean animated) {
            button.setChecked(checked, animated);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int accent = Theme.getColor(Theme.key_switchTrack, resourcesProvider);
            int r = Color.red(accent), g = Color.green(accent), b = Color.blue(accent);

            int gray = Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider);
            int gr = Color.red(gray), gg = Color.green(gray), gb = Color.blue(gray);

            button.setColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_radioBackgroundChecked, resourcesProvider));

            int width = getMeasuredWidth();

            paint.setColor(Color.argb((int) (255 * button.getProgress()), gr, gg, gb));
            rect.set(AndroidUtilities.dp(1), AndroidUtilities.dp(1), width - AndroidUtilities.dp(1), AndroidUtilities.dp(89));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(BOX_RADIUS_DP), AndroidUtilities.dp(BOX_RADIUS_DP), paint);

            paint.setColor(Color.argb((int) (31 * (1.0f - button.getProgress())), r, g, b));
            rect.set(0, 0, width, AndroidUtilities.dp(90));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(BOX_RADIUS_DP), AndroidUtilities.dp(BOX_RADIUS_DP), paint);

            drawChatArea(canvas, width, r, g, b);
            if (isLegacy) {
                drawLegacyPanel(canvas, width, r, g, b);
            } else {
                drawModernPanel(canvas, width, r, g, b);
            }

            String text = isLegacy ? LocaleController.getString(R.string.LegacyInputPanel) : LocaleController.getString(R.string.ModernInputPanel);
            int textWidth = (int) Math.ceil(textPaint.measureText(text));
            textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            canvas.drawText(text, (width - textWidth) / 2f, AndroidUtilities.dp(112), textPaint);
        }

        private void drawChatArea(Canvas canvas, int width, int r, int g, int b) {
            paint.setColor(Color.argb(PANEL_BUTTON_ALPHA, r, g, b));

            float left = AndroidUtilities.dp(14);
            float maxRight = width - AndroidUtilities.dp(8);
            float rowHeight = AndroidUtilities.dp(11);
            float gap = AndroidUtilities.dp(6);
            int[] widthsDp = {58, 74, 44};

            float boxTop = AndroidUtilities.dp(1);
            float panelTop = AndroidUtilities.dp(58);
            float contentHeight = widthsDp.length * rowHeight + (widthsDp.length - 1) * gap;
            float top = boxTop + (panelTop - boxTop - contentHeight) / 2f;

            for (int i = 0; i < widthsDp.length; i++) {
                float rowTop = top + i * (rowHeight + gap);
                float right = Math.min(maxRight, left + AndroidUtilities.dp(widthsDp[i]));
                rect.set(left, rowTop, right, rowTop + rowHeight);
                canvas.drawRoundRect(rect, rowHeight / 2f, rowHeight / 2f, paint);
            }
        }

        private void drawLegacyPanel(Canvas canvas, int width, int r, int g, int b) {
            float barTop = AndroidUtilities.dp(58);
            float barBottom = AndroidUtilities.dp(89);
            float cy = (barTop + barBottom) / 2f;

            paint.setColor(Color.argb(PANEL_BAR_ALPHA, r, g, b));
            rect.set(AndroidUtilities.dp(1), barTop, width - AndroidUtilities.dp(1), barBottom);
            float bottomRadius = AndroidUtilities.dp(BOX_RADIUS_DP - 1);
            path.reset();
            path.addRoundRect(rect, new float[]{0, 0, 0, 0, bottomRadius, bottomRadius, bottomRadius, bottomRadius}, Path.Direction.CW);
            canvas.drawPath(path, paint);

            paint.setColor(Color.argb(PANEL_BUTTON_ALPHA, r, g, b));
            canvas.drawCircle(width - AndroidUtilities.dp(13), cy, AndroidUtilities.dp(8), paint);
        }

        private void drawModernPanel(Canvas canvas, int width, int r, int g, int b) {
            float barTop = AndroidUtilities.dp(58);
            float barBottom = AndroidUtilities.dp(80);
            float cy = (barTop + barBottom) / 2f;
            float pillRadius = (barBottom - barTop) / 2f;

            paint.setColor(Color.argb(PANEL_BAR_ALPHA, r, g, b));
            rect.set(AndroidUtilities.dp(8), barTop, width - AndroidUtilities.dp(8), barBottom);
            canvas.drawRoundRect(rect, pillRadius, pillRadius, paint);

            paint.setColor(Color.argb(PANEL_BUTTON_ALPHA, r, g, b));
            canvas.drawCircle(width - AndroidUtilities.dp(8) - pillRadius, cy, pillRadius - AndroidUtilities.dp(4), paint);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(RadioButton.class.getName());
            info.setChecked(button.isChecked());
            info.setCheckable(true);
            info.setContentDescription(isLegacy ? LocaleController.getString(R.string.LegacyInputPanel) : LocaleController.getString(R.string.ModernInputPanel));
        }
    }

    private final StyleBox[] boxes = new StyleBox[2];

    public InputPanelStyleCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        setOrientation(HORIZONTAL);
        setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(10), AndroidUtilities.dp(21), 0);

        for (int a = 0; a < boxes.length; a++) {
            boolean isLegacy = a == 1;
            boxes[a] = new StyleBox(context, isLegacy, resourcesProvider);
            addView(boxes[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0.5f, a == 1 ? 10 : 0, 0, 0, 0));
            boxes[a].setOnClickListener(v -> {
                for (int bIdx = 0; bIdx < boxes.length; bIdx++) {
                    boxes[bIdx].setChecked(boxes[bIdx] == v, true);
                }
                onStyleSelected(isLegacy);
            });
        }
    }

    public void updateSelection(boolean animated) {
        for (StyleBox box : boxes) {
            box.setChecked(box.isLegacy == NemoConfig.legacyInputPanel, animated);
        }
    }

    protected void onStyleSelected(boolean legacy) {

    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (StyleBox box : boxes) {
            box.invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(139), MeasureSpec.EXACTLY));
    }
}
