package org.nemogram.messenger.settings.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.core.math.MathUtils;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

@SuppressLint("ViewConstructor")
public class ChatTopBarStyleCell extends FrameLayout {

    public static final int STYLE_MODERN = 0;
    public static final int STYLE_LEGACY = 1;

    private static final int CARD_RADIUS_DP = 10;

    private final Theme.ResourcesProvider resourcesProvider;

    private final String[] strings = new String[2];

    Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rect = new RectF();
    private final Path path = new Path();

    private StylePicker picker;

    float legacyProgress;

    private class StylePicker extends NumberPicker {

        public StylePicker(Context context) {
            super(context, 13);
        }

        void changeValueByOnePublic(boolean increment) {
            changeValueByOne(increment);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float y = AndroidUtilities.dp(31);
            pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked, resourcesProvider));
            canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);

            y = getMeasuredHeight() - AndroidUtilities.dp(31);
            canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);
        }
    }

    public ChatTopBarStyleCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        strings[STYLE_MODERN] = LocaleController.getString(R.string.ModernChatActionBar);
        strings[STYLE_LEGACY] = LocaleController.getString(R.string.LegacyChatActionBar);

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(AndroidUtilities.dp(1));

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));

        picker = new StylePicker(context);
        picker.setMinValue(0);
        picker.setDrawDividers(false);
        picker.setMaxValue(strings.length - 1);
        picker.setAllItemsCount(strings.length);
        picker.setWrapSelectorWheel(false);
        picker.setFormatter(value -> strings[MathUtils.clamp(value, 0, strings.length - 1)]);
        picker.setOnValueChangedListener((p, oldVal, newVal) -> {
            onStyleSelected(newVal == STYLE_LEGACY);
            invalidate();
            try {
                picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            } catch (Exception ignored) {
            }
        });
        picker.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        picker.setValue(NemoConfig.legacyChatActionBar ? STYLE_LEGACY : STYLE_MODERN);
        picker.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        legacyProgress = picker.getValue() == STYLE_LEGACY ? 1f : 0f;

        addView(picker, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 21, 0, 21, 0));

        setWillNotDraw(false);
    }

    public void updateSelection() {
        int value = NemoConfig.legacyChatActionBar ? STYLE_LEGACY : STYLE_MODERN;
        if (picker.getValue() != value) {
            picker.setValue(value);
        }
        invalidate();
    }

    protected void onStyleSelected(boolean legacy) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean legacy = picker.getValue() == STYLE_LEGACY;

        if (legacy && legacyProgress != 1f) {
            legacyProgress += 16 / 300f;
            if (legacyProgress > 1f) {
                legacyProgress = 1f;
            } else {
                invalidate();
            }
        } else if (!legacy && legacyProgress != 0f) {
            legacyProgress -= 16 / 300f;
            if (legacyProgress < 0f) {
                legacyProgress = 0f;
            } else {
                invalidate();
            }
        }

        outlinePaint.setColor(Theme.getColor(Theme.key_switchTrack, resourcesProvider));

        int right = getMeasuredWidth() - (AndroidUtilities.dp(132) + AndroidUtilities.dp(21) + AndroidUtilities.dp(16));
        int left = AndroidUtilities.dp(21);

        int verticalPadding = (getMeasuredHeight() - AndroidUtilities.dp(60)) / 2;

        rect.set(left, verticalPadding, right, getMeasuredHeight() - verticalPadding);
        rect.inset(-AndroidUtilities.dp(1), -AndroidUtilities.dp(1));

        filledPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider));
        filledPaint.setAlpha(255);
        canvas.drawRoundRect(rect, AndroidUtilities.dp(CARD_RADIUS_DP), AndroidUtilities.dp(CARD_RADIUS_DP), filledPaint);
        outlinePaint.setAlpha(31);
        canvas.drawRoundRect(rect, AndroidUtilities.dp(CARD_RADIUS_DP), AndroidUtilities.dp(CARD_RADIUS_DP), outlinePaint);

        canvas.save();
        canvas.clipRect(rect);
        drawTopBar(canvas, rect);
        canvas.restore();
    }

    private void drawTopBar(Canvas canvas, RectF cardRect) {
        int accent = Theme.getColor(Theme.key_switchTrack, resourcesProvider);
        int r = Color.red(accent), g = Color.green(accent), b = Color.blue(accent);

        if (legacyProgress > 0f) {
            drawLegacyBar(canvas, cardRect, Color.argb((int) (60 * legacyProgress), r, g, b));
        }
        if (legacyProgress < 1f) {
            drawModernBar(canvas, cardRect, Color.argb((int) (60 * (1f - legacyProgress)), r, g, b));
        }
    }

    private void drawLegacyBar(Canvas canvas, RectF cardRect, int color) {
        filledPaint.setColor(color);
        rect.set(cardRect.left + AndroidUtilities.dp(1), cardRect.top + AndroidUtilities.dp(1), cardRect.right - AndroidUtilities.dp(1), cardRect.top + AndroidUtilities.dp(30));
        float topRadius = AndroidUtilities.dp(CARD_RADIUS_DP - 1);
        path.reset();
        path.addRoundRect(rect, new float[]{topRadius, topRadius, topRadius, topRadius, 0, 0, 0, 0}, Path.Direction.CW);
        canvas.drawPath(path, filledPaint);
    }

    private void drawModernBar(Canvas canvas, RectF cardRect, int color) {
        filledPaint.setColor(color);
        float top = cardRect.top + AndroidUtilities.dp(11);
        float bottom = cardRect.top + AndroidUtilities.dp(33);
        float radius = (bottom - top) / 2f;
        float gap = AndroidUtilities.dp(6);

        float backCx = cardRect.left + AndroidUtilities.dp(13) + radius;
        canvas.drawCircle(backCx, top + radius, radius, filledPaint);

        float rightPillLeft = cardRect.right - AndroidUtilities.dp(13) - AndroidUtilities.dp(38);
        rect.set(rightPillLeft, top, cardRect.right - AndroidUtilities.dp(13), bottom);
        canvas.drawRoundRect(rect, radius, radius, filledPaint);

        float titleLeft = backCx + radius + gap;
        float titleRight = Math.max(titleLeft + AndroidUtilities.dp(24), rightPillLeft - gap);
        rect.set(titleLeft, top, titleRight, bottom);
        canvas.drawRoundRect(rect, radius, radius, filledPaint);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        picker.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        picker.invalidate();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(true);
        info.setContentDescription(strings[picker.getValue()]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null));
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            int newValue = picker.getValue() + 1;
            if (newValue > picker.getMaxValue() || newValue < 0) {
                newValue = 0;
            }
            setContentDescription(strings[newValue]);
            picker.changeValueByOnePublic(picker.getValue() < picker.getMaxValue());
        }
    }
}
