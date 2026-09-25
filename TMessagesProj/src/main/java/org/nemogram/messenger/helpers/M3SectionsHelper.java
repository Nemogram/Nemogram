package org.nemogram.messenger.helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;

import java.lang.Boolean;

public class M3SectionsHelper {

    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path path = new Path();
    private static final RectF rect = new RectF();
    private static final float[] radii = new float[8];

    static {
        paint.setStyle(Paint.Style.FILL);
    }

    public static boolean isEnabled() {
        return NemoConfig.m3SectionsStyle;
    }

    public static int shadowHeightDp(UItem nextItem, int stockDp) {
        if (!isEnabled()) return stockDp;
        return nextItem != null && isHeaderViewType(nextItem.viewType) ? 10 : 16;
    }

    public static boolean isHeaderViewType(int viewType) {
        return viewType == UniversalAdapter.VIEW_TYPE_HEADER
            || viewType == UniversalAdapter.VIEW_TYPE_BLACK_HEADER
            || viewType == UniversalAdapter.VIEW_TYPE_LARGE_HEADER
            || viewType == UniversalAdapter.VIEW_TYPE_ANIMATED_HEADER;
    }

    public static void markMerged(View view, boolean withPrev, boolean withNext) {
        if (!isEnabled()) return;
        view.setTag(R.id.m3_merge_with_prev, withPrev ? Boolean.TRUE : null);
        view.setTag(R.id.m3_merge_with_next, withNext ? Boolean.TRUE : null);
    }

    private static boolean isMergedWithPrev(View view) {
        return view != null && view.getTag(R.id.m3_merge_with_prev) == Boolean.TRUE;
    }

    private static boolean isMergedWithNext(View view) {
        return view != null && view.getTag(R.id.m3_merge_with_next) == Boolean.TRUE;
    }

    public static boolean isDetachedHeaderCell(View view) {
        return isEnabled() && view instanceof HeaderCell && !isMergedWithPrev(view) && !isMergedWithNext(view);
    }

    private static float outerR() {
        return AndroidUtilities.dp(20f);
    }

    private static float innerR() {
        return AndroidUtilities.dp(4f);
    }

    private static int gap() {
        return AndroidUtilities.dp(2f);
    }

    // ripple-check (asRippleCheck) rows render as the MD3 "primary toggle" pill — full pill rounding
    // (height/2) when the row stands alone; if stacked with content, the inner side falls back to
    // `innerR` like any other section corner.
    private static float outerRForChild(View child) {
        if (child instanceof TextCheckCell && ((TextCheckCell) child).drawCheckRipple) {
            return child.getHeight() / 2f;
        }
        return outerR();
    }

    public static void drawSectionsBackgrounds(Canvas canvas, RecyclerListView listView) {
        RecyclerListView.ListSectionsDecoration deco = listView.sectionsItemDecoration;
        if (deco == null) return;
        Utilities.CallbackReturn<View, Boolean> isSection = deco.isSectionItem;
        int bgColor = Theme.getColor(Theme.key_windowBackgroundWhite, listView.resourcesProvider);
        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);
            if (child == null || child.getVisibility() != View.VISIBLE || child.getAlpha() <= 0f) continue;
            if (!isSection.run(child)) continue;
            final float tR;
            final float bR;
            float[] radiiPair = computeRadii(listView, child, i, isSection);
            tR = radiiPair[0];
            bR = radiiPair[1];
            rect.set(
                child.getLeft(),
                RecyclerListView.top(child),
                child.getRight(),
                RecyclerListView.bottom(child)
            );
            setRadii(tR, bR);
            path.rewind();
            path.addRoundRect(rect, radii, Path.Direction.CW);
            paint.setColor(multAlpha(bgColor, child.getAlpha()));
            canvas.drawPath(path, paint);
        }
    }

    public static void clipChild(Canvas canvas, View child, RecyclerListView listView) {
        if (child == null) return;
        RecyclerListView.ListSectionsDecoration deco = listView.sectionsItemDecoration;
        if (deco == null) return;
        Utilities.CallbackReturn<View, Boolean> isSection = deco.isSectionItem;
        if (!isSection.run(child)) return;
        int index = listView.indexOfChild(child);
        float[] radiiPair = computeRadii(listView, child, index, isSection);
        rect.set(
            child.getX(),
            RecyclerListView.top(child),
            child.getX() + child.getWidth(),
            RecyclerListView.bottom(child)
        );
        setRadii(radiiPair[0], radiiPair[1]);
        path.rewind();
        path.addRoundRect(rect, radii, Path.Direction.CW);
        canvas.clipPath(path);
    }

    private static float[] computeRadii(RecyclerListView listView, View child, int childIndex, Utilities.CallbackReturn<View, Boolean> isSection) {
        View prev = childIndex >= 0 ? visualSibling(listView, childIndex, false) : null;
        View next = childIndex >= 0 ? visualSibling(listView, childIndex, true) : null;
        return m3Radii(
            child,
            prev != null && isSection.run(prev) ? prev : null,
            next != null && isSection.run(next) ? next : null
        );
    }

    private static View visualSibling(RecyclerListView listView, int fromIndex, boolean forward) {
        int step = forward ? 1 : -1;
        int i = fromIndex + step;
        while (i >= 0 && i < listView.getChildCount()) {
            View v = listView.getChildAt(i);
            if (v != null && v.getVisibility() == View.VISIBLE && v.getAlpha() > 0.01f) {
                return v;
            }
            i += step;
        }
        return null;
    }

    // M3 corner radii for an attached section child, via the same visual-sibling walk the
    // background/clip paths use. null result = not a section / not attached → caller falls back to stock.
    private static float[] sectionRadiiFor(RecyclerListView listView, View child) {
        RecyclerListView.ListSectionsDecoration deco = listView.sectionsItemDecoration;
        if (deco == null) return null;
        Utilities.CallbackReturn<View, Boolean> isSection = deco.isSectionItem;
        if (!isSection.run(child)) return null;
        int index = listView.indexOfChild(child);
        if (index < 0) return null;
        return computeRadii(listView, child, index, isSection);
    }

    private static float[] m3Radii(View child, View prev, View next) {
        float outer = outerRForChild(child);
        float tR;
        if (isMergedWithPrev(child) || isMergedWithNext(prev)) {
            tR = 0f;
        } else {
            tR = prev != null ? innerR() : outer;
        }
        float bR;
        if (isMergedWithNext(child) || isMergedWithPrev(next)) {
            bR = 0f;
        } else {
            bR = next != null ? innerR() : outer;
        }
        return new float[] { tR, bR };
    }

    public static Drawable makeClipBackground(RecyclerListView listView, View child) {
        float[] radiiPair = sectionRadiiFor(listView, child);
        if (radiiPair == null) return null;
        final float tR = radiiPair[0];
        final float bR = radiiPair[1];
        final int bgColor = Theme.getColor(Theme.key_windowBackgroundWhite, listView.resourcesProvider);
        final int cw = child.getWidth();
        final int ch = child.getHeight();
        final float[] radiiArr = new float[] { tR, tR, tR, tR, bR, bR, bR, bR };
        return new Drawable() {
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Path clipPath = new Path();
            private final RectF tmp = new RectF();

            @Override
            public void draw(Canvas canvas) {
                canvas.save();
                tmp.set(0f, 0f, cw, ch);
                clipPath.rewind();
                clipPath.addRoundRect(tmp, radiiArr, Path.Direction.CW);
                canvas.clipPath(clipPath);
                bgPaint.setColor(ColorUtils.setAlphaComponent(bgColor, bgPaint.getAlpha()));
                canvas.drawRect(tmp, bgPaint);
                canvas.restore();
            }

            @Override
            public void setAlpha(int alpha) {
                bgPaint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(ColorFilter cf) {}

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        };
    }

    public static void applyScrimClip(Canvas canvas, View child) {
        if (!(child.getParent() instanceof RecyclerListView)) return;
        RecyclerListView listView = (RecyclerListView) child.getParent();
        if (!listView.hasSections()) return;
        RecyclerListView.ListSectionsDecoration deco = listView.sectionsItemDecoration;
        if (deco == null) return;
        Utilities.CallbackReturn<View, Boolean> isSection = deco.isSectionItem;
        if (!isSection.run(child)) return;
        rect.set(0f, 0f, child.getWidth(), child.getHeight());
        path.rewind();
        if (isEnabled()) {
            float[] radiiPair = sectionRadiiFor(listView, child);
            if (radiiPair == null) return;
            setRadii(radiiPair[0], radiiPair[1]);
            path.addRoundRect(rect, radii, Path.Direction.CW);
        } else {
            // stock-fallback: mirror stock clipChild's adapter-position neighbour lookup
            int position = listView.getChildAdapterPosition(child);
            View prevView = position != RecyclerView.NO_POSITION ? listView.findViewByPosition(position - 1) : null;
            View nextView = position != RecyclerView.NO_POSITION ? listView.findViewByPosition(position + 1) : null;
            boolean prev = prevView != null && isSection.run(prevView);
            boolean next = nextView != null && isSection.run(nextView);
            if (prev && next) return;
            if (!prev && !next) {
                path.addRoundRect(rect, listView.sectionRadius, listView.sectionRadius, Path.Direction.CW);
            } else if (!prev) {
                path.addRoundRect(rect, listView.sectionRadiusTop, Path.Direction.CW);
            } else {
                path.addRoundRect(rect, listView.sectionRadiusBottom, Path.Direction.CW);
            }
        }
        canvas.clipPath(path);
    }

    public static void augmentItemOffsets(Rect outRect, RecyclerView listView, View view) {
        // disappearing holders in a change animation have no adapter position anymore; without the
        // layout-position fallback they'd lose the gap and jump while fading out. their layout
        // position indexes the old list, so the is-last test against the new count is skipped too
        int adapterPosition = listView.getChildAdapterPosition(view);
        boolean disappearing = adapterPosition == RecyclerView.NO_POSITION;
        int position = disappearing ? listView.getChildLayoutPosition(view) : adapterPosition;
        boolean isLast = !disappearing && position == (listView.getAdapter() != null ? listView.getAdapter().getItemCount() : 0) - 1;
        if (position >= 0 && !isLast && !isMergedWithNext(view)) {
            outRect.bottom += gap();
        }
    }

    public static void applySettingCellIcon(
        View iconLayout,
        ImageView iconView,
        int topColor,
        int bottomColor,
        SettingsActivity.SettingCell.Background cellBackground
    ) {
        if (!isEnabled()) return;

        resizeSquare(iconLayout, 36);
        resizeSquare(iconView, 22);
        applyIconColors(iconView, topColor, cellBackground);
    }

    public static void applyTextCellColorfulIcon(
        ImageView iconView,
        int topColor,
        int bottomColor,
        SettingsActivity.SettingCell.Background cellBackground
    ) {
        if (!isEnabled()) return;
        iconView.setTranslationX(0f);
        applyIconColors(iconView, topColor, cellBackground);
    }

    private static void applyIconColors(ImageView iconView, int topColor, SettingsActivity.SettingCell.Background cellBackground) {
        int bg = MonetHelper.getSettingsIconBackgroundColor(topColor);
        int fg = MonetHelper.getSettingsIconForegroundColor(Color.WHITE);
        iconView.setColorFilter(new android.graphics.PorterDuffColorFilter(fg, android.graphics.PorterDuff.Mode.SRC_IN));
        cellBackground.m3FlatColor = bg;
    }

    private static void resizeSquare(View view, int dp) {
        int size = AndroidUtilities.dp(dp);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) return;
        if (lp.width != size) {
            lp.width = size;
            lp.height = size;
            view.setLayoutParams(lp);
        }
    }

    public static void styleHeaderCell(HeaderCell cell) {
        cell.setBackgroundColor(0);
        cell.setBottomMargin(4);
        android.widget.TextView tv = cell.getTextView();
        if (tv == null) return;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
        tv.setTypeface(AndroidUtilities.bold());
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
    }

    private static void setRadii(float top, float bottom) {
        radii[0] = top; radii[1] = top; radii[2] = top; radii[3] = top;
        radii[4] = bottom; radii[5] = bottom; radii[6] = bottom; radii[7] = bottom;
    }

    private static int multAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * alpha);
        a = Math.max(0, Math.min(255, a));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
