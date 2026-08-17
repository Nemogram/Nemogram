package org.nemogram.messenger.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nemogram.messenger.helpers.MonetHelper;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.TextHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

public class SearchBarStyleSwitcher extends LinearLayout {

    private static final int IMAGE_SIZE_DP = 96;
    private static final int PREVIEW_TOP_MARGIN = 14;
    private static final int GAP_DP = 18;
    private static final int PILL_HEIGHT_DP = 26;
    private static final int CARD_HEIGHT_DP = PREVIEW_TOP_MARGIN + IMAGE_SIZE_DP + GAP_DP + PILL_HEIGHT_DP + 10;

    private final Theme.ResourcesProvider resourcesProvider;
    private final Card[] cards = new Card[3];

    public SearchBarStyleSwitcher(Context context, Theme.ResourcesProvider resourcesProvider,
                                   int[] titleResIds) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setOrientation(VERTICAL);
        setPadding(dp(8), dp(4), dp(8), dp(4));

        var topRow = new LinearLayout(context);
        topRow.setOrientation(HORIZONTAL);
        addView(topRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        cards[0] = new Card(context, 0, titleResIds[0]);
        topRow.addView(cards[0].root, cardSlot(1f, dp(4), 0, dp(4), 0));

        cards[1] = new Card(context, 1, titleResIds[1]);
        topRow.addView(cards[1].root, cardSlot(1f, dp(4), 0, dp(4), 0));

        var bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(HORIZONTAL);
        addView(bottomRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, dp(8), 0, 0));

        bottomRow.addView(new View(context), spacerSlot(0.5f));

        cards[2] = new Card(context, 2, titleResIds[2]);
        bottomRow.addView(cards[2].root, cardSlot(1f, dp(4), 0, dp(4), 0));

        bottomRow.addView(new View(context), spacerSlot(0.5f));
    }

    private static LinearLayout.LayoutParams cardSlot(float weight, int l, int t, int r, int b) {
        return LayoutHelper.createLinear(0, CARD_HEIGHT_DP, weight, l, t, r, b);
    }

    private static LinearLayout.LayoutParams spacerSlot(float weight) {
        return LayoutHelper.createLinear(0, 1, weight);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        for (int i = 0; i < cards.length; i++) {
            final int index = i;
            cards[i].root.setOnClickListener(v -> listener.onCardClick(index));
        }
    }

    public void setChecked(int selectedIndex, boolean animated) {
        for (int i = 0; i < cards.length; i++) {
            cards[i].setChecked(i == selectedIndex, animated);
        }
    }

    public interface OnCardClickListener {
        void onCardClick(int index);
    }

    private class Card {
        final FrameLayout root;
        final StylePreview preview;
        final FrameLayout titleLayout;
        final TextView titleUnselected;
        final FrameLayout titleBackground;
        final TextView titleSelected;

        Card(Context context, int styleIndex, int titleResId) {
            root = new FrameLayout(context);
            ScaleStateListAnimator.apply(root, .05f, 1.25f);

            preview = new StylePreview(context, styleIndex, resourcesProvider);
            root.addView(preview, LayoutHelper.createFrame(
                    IMAGE_SIZE_DP, IMAGE_SIZE_DP, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, PREVIEW_TOP_MARGIN, 0, 0));

            titleLayout = new FrameLayout(context);
            titleUnselected = TextHelper.makeTextView(context, 14, Theme.key_windowBackgroundWhiteGrayText2, true, resourcesProvider);
            titleUnselected.setText(LocaleController.getString(titleResId));
            titleUnselected.setPadding(dp(12), 0, dp(12), 0);
            titleLayout.addView(titleUnselected, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            titleBackground = new FrameLayout(context);
            titleBackground.setPadding(dp(12), 0, dp(12), 0);
            titleBackground.setBackground(Theme.createRoundRectDrawable(dp(13),
                    MonetHelper.getSettingsIconBackgroundColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider))));
            titleLayout.addView(titleBackground, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, PILL_HEIGHT_DP, Gravity.CENTER));

            titleSelected = TextHelper.makeTextView(context, 14, Theme.key_windowBackgroundCheckText, true, resourcesProvider);
            titleSelected.setText(LocaleController.getString(titleResId));
            titleSelected.setTextColor(MonetHelper.getSettingsIconForegroundColor(
                    Theme.getColor(Theme.key_windowBackgroundCheckText, resourcesProvider)));
            titleBackground.addView(titleSelected, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            root.addView(titleLayout, LayoutHelper.createFrame(
                    LayoutHelper.WRAP_CONTENT, PILL_HEIGHT_DP, Gravity.CENTER_HORIZONTAL | Gravity.TOP,
                    0, PREVIEW_TOP_MARGIN + IMAGE_SIZE_DP + GAP_DP, 0, 0));

            setChecked(false, false);
        }

        void setChecked(boolean checked, boolean animated) {
            if (animated) {
                titleBackground.animate()
                        .scaleX(checked ? 1f : 0f)
                        .scaleY(checked ? 1f : 0f)
                        .alpha(checked ? 1f : 0f)
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                        .setDuration(320)
                        .start();
            } else {
                titleBackground.animate().cancel();
                titleBackground.setScaleX(checked ? 1f : 0f);
                titleBackground.setScaleY(checked ? 1f : 0f);
                titleBackground.setAlpha(checked ? 1f : 0f);
            }
        }
    }

    private static class StylePreview extends View {

        static final int STYLE_NORMAL = 0;
        static final int STYLE_COMPACT = 1;
        static final int STYLE_MATERIAL = 2;

        private static final float ICON_RADIUS = dp(2.2f);

        private final int styleIndex;
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        StylePreview(Context context, int styleIndex, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.styleIndex = styleIndex;
            this.resourcesProvider = resourcesProvider;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(dp(IMAGE_SIZE_DP), dp(IMAGE_SIZE_DP));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setColor(Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider));
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, dp(10), dp(10), paint);

            int accent = Theme.getColor(Theme.key_switchTrack, resourcesProvider);
            int r = Color.red(accent), g = Color.green(accent), b = Color.blue(accent);

            switch (styleIndex) {
                case STYLE_NORMAL:
                    drawNormal(canvas, r, g, b);
                    break;
                case STYLE_COMPACT:
                    drawCompact(canvas, r, g, b);
                    break;
                case STYLE_MATERIAL:
                default:
                    drawMaterial(canvas, r, g, b);
                    break;
            }
        }

        private void drawTitle(Canvas canvas, int r, int g, int b) {
            drawArgb(canvas, r, g, b, 204, dp(10), dp(9), dp(38), dp(14), dp(2));
        }

        private void drawSearchField(Canvas canvas, int r, int g, int b, int fieldTop, int fieldBottom) {
            float radius = (fieldBottom - fieldTop) / 2f;
            float cy = (fieldTop + fieldBottom) / 2f;

            drawArgb(canvas, r, g, b, 40, dp(8), fieldTop, dp(88), fieldBottom, radius);
            drawSearchIcon(canvas, r, g, b, dp(17), cy, ICON_RADIUS);
            drawArgb(canvas, r, g, b, 90, dp(25), cy - dp(1), dp(50), cy + dp(1), dp(1));
        }

        private void drawNormal(Canvas canvas, int r, int g, int b) {
            drawTitle(canvas, r, g, b);

            int fieldTop = dp(24), fieldBottom = dp(40);
            drawSearchField(canvas, r, g, b, fieldTop, fieldBottom);

            drawChatRows(canvas, r, g, b, fieldBottom + dp(8));
        }

        private void drawCompact(Canvas canvas, int r, int g, int b) {
            drawTitle(canvas, r, g, b);
            drawSearchIcon(canvas, r, g, b, dp(80), dp(11.5f), ICON_RADIUS);

            drawChatRows(canvas, r, g, b, dp(24));
        }

        private void drawMaterial(Canvas canvas, int r, int g, int b) {
            int fieldTop = dp(8), fieldBottom = dp(26);
            drawSearchField(canvas, r, g, b, fieldTop, fieldBottom);

            drawChatRows(canvas, r, g, b, fieldBottom + dp(8));
        }

        private void drawArgb(Canvas canvas, int r, int g, int b, int alpha, float l, float t, float rr, float bb, float radius) {
            paint.setColor(Color.argb(alpha, r, g, b));
            rect.set(l, t, rr, bb);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }

        private void drawSearchIcon(Canvas canvas, int r, int g, int b, float cx, float cy, float radius) {
            paint.setColor(Color.argb(204, r, g, b));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1f));
            canvas.drawCircle(cx, cy, radius, paint);
            canvas.drawLine(cx + radius * 0.7f, cy + radius * 0.7f, cx + radius * 1.6f, cy + radius * 1.6f, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        private void drawChatRows(Canvas canvas, int r, int g, int b, float startY) {
            for (int i = 0; i < 2; i++) {
                float cy = startY + dp(21) * i + dp(9);
                paint.setColor(Color.argb(i == 0 ? 204 : 90, r, g, b));
                canvas.drawCircle(dp(16), cy, dp(8), paint);

                drawArgb(canvas, r, g, b, i == 0 ? 204 : 90, dp(30), cy - dp(6), dp(70), cy - dp(2), dp(1.5f));
                drawArgb(canvas, r, g, b, i == 0 ? 204 : 90, dp(30), cy + dp(1), dp(56), cy + dp(4), dp(1.5f));
            }
        }
    }

    public static final class Factory extends UItem.UItemFactory<SearchBarStyleSwitcher> {
        static { setup(new Factory()); }

        @Override
        public SearchBarStyleSwitcher createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
            return new SearchBarStyleSwitcher(context, resourcesProvider,
                    new int[]{R.string.SearchBarStyleNormal, R.string.SearchBarStyleCompact, R.string.SearchBarStyleMaterial});
        }

        @Override
        public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
            var switcher = (SearchBarStyleSwitcher) view;
            var listener = (SearchBarStyleSwitcher.OnCardClickListener) item.object;
            switcher.setOnCardClickListener(index -> {
                switcher.setChecked(index, true);
                listener.onCardClick(index);
            });
            switcher.setChecked(item.intValue, false);
        }

        @Override
        public boolean isClickable() {
            return false;
        }

        public static UItem asSwitcher(int id, OnCardClickListener listener, int checkedIndex) {
            UItem item = UItem.ofFactory(Factory.class);
            item.id = id;
            item.object = listener;
            item.intValue = checkedIndex;
            return item;
        }
    }
}
