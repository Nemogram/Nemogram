package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class SlideChooseView extends View {

    public final SeekBarAccessibilityDelegate accessibilityDelegate;

    private Paint paint;
    private Paint linePaint;
    private TextPaint textPaint;
    private int lastDash;

    private int circleSize;
    private int gapSize;
    private int sideSide;
    private int lineSize;

    private int dashedFrom = -1;

    private int styleOverride = -1;

    private final Path styledPath = new Path();
    private final RectF styledRect = new RectF();
    private final float[] styledRadii = new float[8];

    private boolean moving;
    private boolean startMoving;
    private float xTouchDown;
    private float yTouchDown;

    private int startMovingPreset;

    private String[] optionsStr;
    private int[] optionsSizes;
    private Drawable[] leftDrawables;

    private int selectedIndex;
    private int minIndex = Integer.MIN_VALUE;
    private float selectedIndexTouch;
    private AnimatedFloat selectedIndexAnimatedHolder = new AnimatedFloat(this, 120, CubicBezierInterpolator.DEFAULT);
    private AnimatedFloat movingAnimatedHolder = new AnimatedFloat(this, 150, CubicBezierInterpolator.DEFAULT);

    private Callback callback;
    private final Theme.ResourcesProvider resourcesProvider;

    private boolean touchWasClose = false;

    public SlideChooseView(Context context) {
        this(context, null);
    }

    public SlideChooseView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(AndroidUtilities.dp(2));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setTextSize(AndroidUtilities.dp(13));

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        accessibilityDelegate = new IntSeekBarAccessibilityDelegate() {
            @Override
            protected int getProgress() {
                return selectedIndex;
            }

            @Override
            protected void setProgress(int progress) {
                setOption(progress);
            }

            @Override
            protected int getMaxValue() {
                return optionsStr.length - 1;
            }

            @Override
            protected CharSequence getContentDescription(View host) {
                return selectedIndex < optionsStr.length ? optionsStr[selectedIndex] : null;
            }
        };
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setOptions(int selected, String... options) {
        setOptions(selected, null, options);
    }

    public void setOptions(int selected, Drawable[] leftDrawables, String... options) {
        this.optionsStr = options;
        this.leftDrawables = leftDrawables;
        selectedIndex = selected;
        optionsSizes = new int[optionsStr.length];
        for (int i = 0; i < optionsStr.length; i++) {
            optionsSizes[i] = (int) Math.ceil(textPaint.measureText(optionsStr[i]));
        }
        if (this.leftDrawables != null) {
            for (Drawable drawable : this.leftDrawables) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
        }
        requestLayout();
    }

    public void setMinAllowedIndex(int index) {
        if (index != -1 && optionsStr != null) {
            index = Math.min(index, optionsStr.length - 1);
        }
        if (minIndex != index) {
            minIndex = index;
            if (selectedIndex < index) {
                selectedIndex = index;
            }
            invalidate();
        }
    }

    public void setDashedFrom(int from) {
        dashedFrom = from;
    }

    public void setStyleOverride(int style) {
        if (styleOverride != style) {
            styleOverride = style;
            invalidate();
        }
    }

    private int getEffectiveStyle() {
        return styleOverride >= 0 ? styleOverride : NemoConfig.controlsStyle;
    }

    private void setStyledPath(float l, float t, float r, float b, float leftRadius, float rightRadius) {
        styledRect.set(l, t, r, b);
        styledRadii[0] = styledRadii[1] = styledRadii[6] = styledRadii[7] = leftRadius;
        styledRadii[2] = styledRadii[3] = styledRadii[4] = styledRadii[5] = rightRadius;
        styledPath.reset();
        styledPath.addRoundRect(styledRect, styledRadii, Path.Direction.CW);
    }

    private void fillStyledPiece(Canvas canvas, float l, float t, float r, float b, float leftRadius, float rightRadius, int color) {
        if (r - l < 1f) {
            return;
        }
        setStyledPath(l, t, r, b, leftRadius, rightRadius);
        paint.setColor(color);
        canvas.drawPath(styledPath, paint);
    }

    private void drawMd3Track(Canvas canvas, float selectedIndexAnimated, float movingAnimated, float cy) {
        final float step = lineSize + gapSize * 2 + circleSize;
        final float trackL = sideSide + circleSize / 2f;
        final float trackR = trackL + step * (optionsStr.length - 1);
        final float trackH = AndroidUtilities.dp(8);
        final float top = cy - trackH / 2f, bottom = cy + trackH / 2f;
        final float full = trackH / 2f, small = AndroidUtilities.dp(3);

        // thumb is a thin vertical handle splitting the track, thinner while dragging
        final float thumbC = trackL + step * selectedIndexAnimated;
        final float thumbW = AndroidUtilities.dp(4) - AndroidUtilities.dp(1.5f) * movingAnimated;
        final float gap = AndroidUtilities.dp(5) + AndroidUtilities.dp(1.5f) * movingAnimated;
        final float splitL = Math.min(thumbC - thumbW / 2f - gap, trackR);
        final float splitR = Math.max(thumbC + thumbW / 2f + gap, trackL);

        final int activeColor = getThemedColor(Theme.key_switchTrackChecked);
        final int inactiveColor = getThemedColor(Theme.key_switchTrack);

        // filled part up to the thumb, dimmed before minIndex the way the classic style dims those stops
        final float minC = minIndex > 0 ? trackL + step * minIndex : -1;
        final float activeEnd = Math.min(thumbC, splitL);
        if (activeEnd > trackL) {
            if (minC > trackL) {
                fillStyledPiece(canvas, trackL, top, Math.min(activeEnd, minC), bottom, full, small, Theme.multAlpha(activeColor, .5f));
            }
            if (activeEnd > Math.max(trackL, minC)) {
                fillStyledPiece(canvas, Math.max(trackL, minC), top, activeEnd, bottom, small, small, activeColor);
            }
        }

        // rest of the track, dashed after dashedFrom like the classic style
        final float dashC = dashedFrom >= 0 ? MathUtils.clamp(trackL + step * dashedFrom, splitR, trackR) : trackR;
        fillStyledPiece(canvas, splitR, top, dashC, bottom, small, small, inactiveColor);
        if (dashC < trackR - AndroidUtilities.dp(2)) {
            int width = (int) (trackR - dashC) - AndroidUtilities.dp(2);
            int dash = Math.max(1, width / AndroidUtilities.dp(13));
            float gapPx = (width - dash * AndroidUtilities.dp(8)) / (float) dash;
            linePaint.setPathEffect(new DashPathEffect(new float[]{AndroidUtilities.dp(6), Math.max(gapPx, AndroidUtilities.dp(4))}, 0));
            linePaint.setColor(inactiveColor);
            canvas.drawLine(dashC + AndroidUtilities.dp(1), cy, trackR - AndroidUtilities.dp(1), cy, linePaint);
        }

        final float handleH = trackH + AndroidUtilities.dp(12);
        styledRect.set(thumbC - thumbW / 2f, cy - handleH / 2f, thumbC + thumbW / 2f, cy + handleH / 2f);
        paint.setColor(activeColor);
        canvas.drawRoundRect(styledRect, thumbW / 2f, thumbW / 2f, paint);

        // stop indicators as holes in the inactive part
        final int holeColor = getThemedColor(Theme.key_windowBackgroundWhite);
        for (int a = (int) Math.floor(selectedIndexAnimated) + 1; a < optionsStr.length; a++) {
            float cx = trackL + step * a;
            if (cx < splitR + AndroidUtilities.dp(2) || cx > trackR - AndroidUtilities.dp(4) || cx >= dashC) {
                continue;
            }
            paint.setColor(ColorUtils.blendARGB(inactiveColor, holeColor, .55f));
            canvas.drawCircle(cx, cy, AndroidUtilities.dp(1.5f), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float indexTouch = MathUtils.clamp((x - sideSide + circleSize / 2f) / (lineSize + gapSize * 2 + circleSize), 0, optionsStr.length - 1);
        boolean isClose = Math.abs(indexTouch - Math.round(indexTouch)) < .35f;
        if (isClose) {
            indexTouch = Math.round(indexTouch);
        }
        if (minIndex != Integer.MIN_VALUE) {
            indexTouch = Math.max(indexTouch, minIndex);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xTouchDown = x;
            yTouchDown = y;
            selectedIndexTouch = indexTouch;
            startMovingPreset = selectedIndex;
            startMoving = true;
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!moving) {
                if (Math.abs(xTouchDown - x) > Math.abs(yTouchDown - y)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
            if (startMoving) {
                if (Math.abs(xTouchDown - x) >= AndroidUtilities.touchSlop) {
                    moving = true;
                    startMoving = false;
                }
            }
            if (moving) {
                selectedIndexTouch = indexTouch;
                invalidate();
                if (Math.round(selectedIndexTouch) != selectedIndex && isClose) {
                    setOption(Math.round(selectedIndexTouch));
                }
            }
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (!moving) {
                selectedIndexTouch = indexTouch;
                if (event.getAction() == MotionEvent.ACTION_UP && Math.round(selectedIndexTouch) != selectedIndex) {
                    setOption(Math.round(selectedIndexTouch));
                }
            } else {
                if (selectedIndex != startMovingPreset) {
                    setOption(selectedIndex);
                }
            }
            if (callback != null) {
                callback.onTouchEnd();
            }
            startMoving = false;
            moving = false;
            invalidate();
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return true;
    }

    private void setOption(int index) {
        int prevIndex = selectedIndex;
        if (selectedIndex != index) {
            AndroidUtilities.vibrateCursor(this);
        }
        selectedIndex = index;
        if (callback != null) {
            callback.onOptionSelected(index);
        }
        invalidate();
        if (prevIndex != selectedIndex) accessibilityDelegate.postAccessibilityEventRunnable(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(74), MeasureSpec.EXACTLY));
        circleSize = AndroidUtilities.dp(6);
        gapSize = AndroidUtilities.dp(2);
        sideSide = AndroidUtilities.dp(22);
        lineSize = (getMeasuredWidth() - circleSize * optionsStr.length - gapSize * 2 * (optionsStr.length - 1) - sideSide * 2) / Math.max(1, optionsStr.length - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float selectedIndexAnimated = selectedIndexAnimatedHolder.set(selectedIndex);
        float movingAnimated = movingAnimatedHolder.set(moving ? 1 : 0);
        int cy = getMeasuredHeight() / 2 + AndroidUtilities.dp(11);
        final boolean md3 = getEffectiveStyle() == NemoConfig.CONTROLS_STYLE_MD3;

        if (md3) {
            drawMd3Track(canvas, selectedIndexAnimated, movingAnimated, cy);
        }

        for (int a = 0; a < optionsStr.length; a++) {
            int cx = sideSide + (lineSize + gapSize * 2 + circleSize) * a + circleSize / 2;
            float t = Math.max(0, 1f - Math.abs(a - selectedIndexAnimated));
            if (!md3) {
                drawClassicStop(canvas, a, cx, t, selectedIndexAnimated, cy);
            }
            int size = optionsSizes[a];
            String text = optionsStr[a];
            textPaint.setColor(ColorUtils.blendARGB(getThemedColor(Theme.key_windowBackgroundWhiteGrayText), getThemedColor(Theme.key_windowBackgroundWhiteBlueText), t));

            if (leftDrawables != null) {
                canvas.save();
                if (a == 0) {
                    canvas.translate(AndroidUtilities.dp(12), AndroidUtilities.dp(15.5f));
                } else if (a == optionsStr.length - 1) {
                    canvas.translate(getMeasuredWidth() - size - AndroidUtilities.dp(22) - AndroidUtilities.dp(10), AndroidUtilities.dp(28) - AndroidUtilities.dp(12.5f));
                } else {
                    canvas.translate(cx - size / 2 - AndroidUtilities.dp(10), AndroidUtilities.dp(28) - AndroidUtilities.dp(12.5f));
                }
                leftDrawables[a].setColorFilter(textPaint.getColor(), PorterDuff.Mode.MULTIPLY);
                leftDrawables[a].draw(canvas);
                canvas.restore();
                canvas.save();
                canvas.translate((leftDrawables[a].getIntrinsicWidth() / 2f) - AndroidUtilities.dp(a == 0 ? 3 : 2), 0);
            }

            if (a == 0) {
                canvas.drawText(text, AndroidUtilities.dp(22), AndroidUtilities.dp(28), textPaint);
            } else if (a == optionsStr.length - 1) {
                canvas.drawText(text, getMeasuredWidth() - size - AndroidUtilities.dp(22), AndroidUtilities.dp(28), textPaint);
            } else {
                canvas.drawText(text, cx - size / 2, AndroidUtilities.dp(28), textPaint);
            }

            if (leftDrawables != null) {
                canvas.restore();
            }
        }

        if (!md3) {
            float cx = sideSide + (lineSize + gapSize * 2 + circleSize) * selectedIndexAnimated + circleSize / 2;
            paint.setColor(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_switchTrackChecked), 80));
            canvas.drawCircle(cx, cy, AndroidUtilities.dp(12 * movingAnimated), paint);
            paint.setColor(getThemedColor(Theme.key_switchTrackChecked));
            canvas.drawCircle(cx, cy, AndroidUtilities.dp(6), paint);
        }
    }

    private void drawClassicStop(Canvas canvas, int a, int cx, float t, float selectedIndexAnimated, int cy) {
        float ut = MathUtils.clamp(selectedIndexAnimated - a + 1f, 0, 1);
        int color = ColorUtils.blendARGB(getThemedColor(Theme.key_switchTrack), Theme.multAlpha(getThemedColor(Theme.key_switchTrackChecked), minIndex != Integer.MIN_VALUE && a <= minIndex ? .50f : 1.0f), ut);
        paint.setColor(color);
        linePaint.setColor(color);
        canvas.drawCircle(cx, cy, AndroidUtilities.lerp(circleSize / 2, AndroidUtilities.dp(6), t), paint);
        if (a != 0) {
            int x = cx - circleSize / 2 - gapSize - lineSize;
            int width = lineSize;
            if (dashedFrom != -1 && a - 1 >= dashedFrom) {
                x += AndroidUtilities.dp(3);
                width -= AndroidUtilities.dp(3);
                int dash = width / AndroidUtilities.dp(13);
                if (lastDash != dash) {
                    float gap = (width - dash * AndroidUtilities.dp(8)) / (float) (dash - 1);
                    linePaint.setPathEffect(new DashPathEffect(new float[]{AndroidUtilities.dp(6), gap}, 0));
                    lastDash = dash;
                }
                canvas.drawLine(x + AndroidUtilities.dp(1), cy, x + width - AndroidUtilities.dp(1), cy, linePaint);
            } else {
                float nt = MathUtils.clamp(1f - Math.abs(a - selectedIndexAnimated - 1), 0, 1);
                float nct = MathUtils.clamp(1f - Math.min(Math.abs(a - selectedIndexAnimated), Math.abs(a - selectedIndexAnimated - 1)), 0, 1);
                width -= AndroidUtilities.dp(3) * nct;
                x += AndroidUtilities.dp(3) * nt;
                canvas.drawRect(x, cy - AndroidUtilities.dp(1), x + width, cy + AndroidUtilities.dp(1), paint);
            }
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        accessibilityDelegate.onInitializeAccessibilityEvent(this, event);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        accessibilityDelegate.onInitializeAccessibilityNodeInfoInternal(this, info);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        return super.performAccessibilityAction(action, arguments) || accessibilityDelegate.performAccessibilityActionInternal(this, action, arguments);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }


    public interface Callback {
        void onOptionSelected(int index);

        default void onTouchEnd() {

        }
    }
}
