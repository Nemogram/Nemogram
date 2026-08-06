package org.nemogram.messenger.settings.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.LayoutHelper;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.folder.FolderIconHelper;

@SuppressLint("ViewConstructor")
public class FolderTabsPreviewCell extends FrameLayout {

    private final FrameLayout preview;
    private final Theme.ResourcesProvider resourcesProvider;
    private final RectF rect = new RectF();
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float allChatsProgress;
    private float iconProgress;
    private float titleProgress;
    private float counterProgress;

    private final String[][] filters = new String[][]{
            {getString(R.string.FilterAllChats), "\uD83D\uDCAC"},
            {getString(R.string.FilterGroups), "\uD83D\uDC65"},
            {getString(R.string.FilterBots), "\uD83E\uDD16"},
            {getString(R.string.FilterChannels), "\uD83D\uDCE2"},
            {getString(R.string.FilterNameNonMuted), "\uD83D\uDD14"},
            {getString(R.string.FilterContacts), "\uD83C\uDFE0"},
            {getString(R.string.FilterNonContacts), "\uD83C\uDFAD"},
            {getString(R.string.FilterNameUnread), "\uD83D\uDCEC"}
    };

    public FolderTabsPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setWillNotDraw(false);

        preview = new FrameLayout(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                drawPreview(canvas);
            }
        };
        preview.setWillNotDraw(false);
        addView(preview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.CENTER, 15, 0, 15, 9));
        updateAllChatsTabVisibility(false);
        updateTabTitle(false);
        updateTabIcons(false);
        updateTabCounter(false);
    }

    private void drawPreview(Canvas canvas) {
        float width = preview.getMeasuredWidth();
        float height = preview.getMeasuredHeight();
        float chipHeight = dp(32);
        float frameInset = dp(5);
        float framePadding = dp(8);
        float centerY = height / 2f;
        float frameTop = centerY - chipHeight / 2f - framePadding;
        float frameBottom = centerY + chipHeight / 2f + framePadding;
        float frameLeft = frameInset;
        float frameRight = width - frameInset;
        int tabColor = Theme.getColor(Theme.key_switchTrack, resourcesProvider);

        paint.setColor(Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider));
        rect.set(frameLeft, frameTop, frameRight, frameBottom);
        canvas.drawRoundRect(rect, dp(12), dp(12), paint);

        canvas.save();
        Path clip = new Path();
        clip.addRoundRect(rect, dp(12), dp(12), Path.Direction.CW);
        canvas.clipPath(clip);

        float dividerTop = height - dp(4) - dpf2(0.5f);
        canvas.clipRect(0, 0, width, dividerTop);
        textPaint.setTypeface(AndroidUtilities.bold());

        float cursor = frameLeft + dp(5);
        for (int index = 0; index < filters.length; index++) {
            float activeProgress = index == 0 ? allChatsProgress : index == 1 ? 1f - allChatsProgress : 0f;
            if (activeProgress > 0f) {
                cursor += drawActiveTab(canvas, index, cursor, centerY, chipHeight, activeProgress, tabColor);
            } else if (index > 1) {
                cursor += drawTab(canvas, index, cursor, centerY, tabColor);
            }
        }
        canvas.restore();
    }

    private float drawActiveTab(Canvas canvas, int index, float x, float centerY, float chipHeight, float progress, int tabColor) {
        float contentWidth = getTabWidth(index, true);
        float chipExtra = dpf2(4) * (1f - titleProgress) * (1f - counterProgress) + dpf2(22);
        float chipRight = x + (contentWidth + chipExtra) * progress;
        float chipTop = centerY - chipHeight / 2f;
        float chipBottom = centerY + chipHeight / 2f;

        int selectedColor = Theme.getColor(Theme.key_windowBackgroundWhiteValueText, resourcesProvider);
        int chipColor = ColorUtils.setAlphaComponent(selectedColor, 0x2F);
        paint.setColor(ColorUtils.blendARGB(0x00, chipColor, progress));
        canvas.save();
        canvas.clipRect(x, chipTop, chipRight, chipBottom);
        canvas.drawRoundRect(x, chipTop, chipRight, chipBottom, chipHeight / 2f, chipHeight / 2f, paint);

        if (NemoConfig.strokeOnViews) {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(Math.max(5, dp(0.5f)));
            strokePaint.setColor(ColorUtils.blendARGB(0x00, selectedColor, progress));
            canvas.drawRoundRect(x, chipTop, chipRight, chipBottom, chipHeight / 2f, chipHeight / 2f, strokePaint);
        }

        textPaint.setTextSize(dp(15));
        textPaint.setTextScaleX(titleProgress);
        textPaint.setColor(ColorUtils.blendARGB(0x00, selectedColor, progress * titleProgress));
        float iconLeft = x + dpf2(6) * (1f - titleProgress) * (1f - counterProgress) + dpf2(11);
        drawIcon(canvas, index, iconLeft, centerY, selectedColor, iconProgress * progress);
        drawText(canvas, filters[index][0], x + dp(30) * iconProgress + dpf2(10) + 7f * (1f - iconProgress) * titleProgress, centerY);

        float badgeProgress = progress * counterProgress;
        if (badgeProgress > 0f) {
            textPaint.setColor(ColorUtils.blendARGB(0x00, selectedColor, badgeProgress));
            float badgeX = x + contentWidth - dpf2(3.5f);
            canvas.drawCircle(badgeX, centerY, dp(10) * badgeProgress, textPaint);
            textPaint.setTextSize(dp(14) * badgeProgress);
            textPaint.setTextScaleX(badgeProgress);
            textPaint.setColor(ColorUtils.blendARGB(0x00, Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider), badgeProgress));
            drawText(canvas, "11", badgeX - textPaint.measureText("11") / 2f, centerY);
        }
        canvas.restore();
        return (contentWidth + dpf2(22) + dp(10)) * progress;
    }

    private float drawTab(Canvas canvas, int index, float x, float centerY, int tabColor) {
        textPaint.setTextSize(dp(15));
        textPaint.setTextScaleX(titleProgress);
        textPaint.setColor(ColorUtils.blendARGB(0x00, tabColor, titleProgress));
        drawIcon(canvas, index, x, centerY, tabColor, iconProgress);
        drawText(canvas, filters[index][0], x + dp(30) * iconProgress, centerY);
        return getTabWidth(index, false) + dp(10) + dpf2(5);
    }

    private float getTabWidth(int index, boolean active) {
        textPaint.setTextSize(dp(15));
        textPaint.setTextScaleX(titleProgress);
        float width = textPaint.measureText(filters[index][0]) + dp(34) * iconProgress + 14f * (1f - iconProgress) * titleProgress;
        if (active) {
            width += dpf2(24) * counterProgress - dp(4) * iconProgress * (1f - titleProgress) * counterProgress;
        } else {
            width += 1f;
        }
        return width;
    }

    private void drawIcon(Canvas canvas, int index, float x, float centerY, int color, float progress) {
        Drawable icon = ContextCompat.getDrawable(getContext(), FolderIconHelper.getTabIcon(filters[index][1])).mutate();
        icon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(0x00, color, progress), PorterDuff.Mode.MULTIPLY));
        int halfSize = dp(13);
        icon.setBounds((int) x, (int) (centerY - halfSize), (int) (x + dp(26) * progress), (int) (centerY + halfSize));
        icon.draw(canvas);
    }

    private void drawText(Canvas canvas, String text, float x, float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, x, centerY - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }

    public void updateAllChatsTabVisibility(boolean animate) {
        animateProgress(allChatsProgress, NemoConfig.hideAllTab ? 0f : 1f, animate, value -> allChatsProgress = value);
    }

    public void updateTabTitle(boolean animate) {
        animateProgress(titleProgress, NemoConfig.tabsTitleType != NemoConfig.TITLE_TYPE_ICON ? 1f : 0f, animate, value -> titleProgress = value);
    }

    public void updateTabIcons(boolean animate) {
        animateProgress(iconProgress, NemoConfig.tabsTitleType != NemoConfig.TITLE_TYPE_TEXT ? 1f : 0f, animate, value -> iconProgress = value);
    }

    public void updateTabCounter(boolean animate) {
        animateProgress(counterProgress, NemoConfig.hideFolderUnreadBadge ? 0f : 1f, animate, value -> counterProgress = value);
    }

    private void animateProgress(float from, float to, boolean animate, ProgressSetter setter) {
        if (from == to) {
            return;
        }
        if (!animate) {
            setter.set(to);
            invalidate();
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(from, to).setDuration(250);
        animator.setInterpolator(Easings.easeInOutQuad);
        animator.addUpdateListener(animation -> {
            setter.set((Float) animation.getAnimatedValue());
            invalidate();
        });
        animator.start();
    }

    public void invalidatePreview() {
        updateAllChatsTabVisibility(true);
        updateTabTitle(true);
        updateTabIcons(true);
        updateTabCounter(true);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        preview.invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(LocaleController.isRTL ? 0 : dp(21), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? dp(21) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dp(80), MeasureSpec.EXACTLY));
    }

    private interface ProgressSetter {
        void set(float value);
    }
}
