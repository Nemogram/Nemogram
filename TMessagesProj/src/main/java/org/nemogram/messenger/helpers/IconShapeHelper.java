package org.nemogram.messenger.helpers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nemogram.messenger.NemoConfig;
public class IconShapeHelper {
    private static final float MIN_CORNER_RATIO = 0.15f;
    private static final float MAX_CORNER_RATIO = 0.5f;
    private static final float MIN_ICON_SCALE = 0.9f;

    public static float roundness() {
        return Math.clamp(NemoConfig.iconRoundness / 100f, 0f, 1f);
    }

    public static float cornerRatio() {
        return MIN_CORNER_RATIO + (MAX_CORNER_RATIO - MIN_CORNER_RATIO) * roundness();
    }

    public static float iconScale() {
        return 1f - (1f - MIN_ICON_SCALE) * roundness();
    }

    public static void draw(Canvas canvas, RectF rect, Paint paint) {
        final float r = Math.min(rect.width(), rect.height()) * cornerRatio();
        canvas.drawRoundRect(rect, r, r, paint);
    }

    public static void setIcon(ImageView imageView, int resId) {
        imageView.setImageResource(resId);
        final Drawable drawable = imageView.getDrawable();
        if (drawable != null && !(drawable instanceof ScaledIconDrawable)) {
            imageView.setImageDrawable(new ScaledIconDrawable(drawable));
        }
    }

    private static class ScaledIconDrawable extends Drawable implements Drawable.Callback {
        private final Drawable inner;

        ScaledIconDrawable(Drawable inner) {
            this.inner = inner;
            inner.setCallback(this);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final Rect b = getBounds();
            final float scale = iconScale();
            final int save = canvas.save();
            canvas.scale(scale, scale, b.exactCenterX(), b.exactCenterY());
            inner.draw(canvas);
            canvas.restoreToCount(save);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            inner.setBounds(bounds);
        }

        @Override
        public void setAlpha(int alpha) {
            inner.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            inner.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return inner.getOpacity() == PixelFormat.OPAQUE ? PixelFormat.TRANSLUCENT : inner.getOpacity();
        }

        @Override
        public int getIntrinsicWidth() {
            return inner.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return inner.getIntrinsicHeight();
        }

        @NonNull
        @Override
        public Drawable mutate() {
            inner.mutate();
            return this;
        }

        @Override
        public void invalidateDrawable(@NonNull Drawable who) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
            unscheduleSelf(what);
        }
    }
}
