package org.nemogram.messenger.settings;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.PopupHelper;

public class NemoStickerSettingsActivity extends BaseNemoSettingsActivity implements NotificationCenter.NotificationCenterDelegate {

    private ActionBarMenuItem resetItem;

    private final int stickerSizeRow = rowId++;
    private final int gifSizeRow = rowId++;
    private final int hideTimeOnStickerRow = rowId++;
    private final int showTimeHintRow = rowId++;
    private final int reducedColorsRow = rowId++;
    private final int maxRecentStickersRow = rowId++;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    public View createView(Context context) {
        var fragmentView = super.createView(context);

        var menu = actionBar.createMenu();
        resetItem = menu.addItem(0, R.drawable.msg_reset);
        resetItem.setContentDescription(LocaleController.getString(R.string.ResetStickerSize));
        resetItem.setTag(null);
        resetItem.setOnClickListener(v -> {
            AndroidUtilities.updateViewVisibilityAnimated(resetItem, false, 0.5f, true);
            var stickerItem = listView.findItemByItemId(stickerSizeRow);
            var stickerCell = (StickerSizeCell) listView.findViewByItemId(stickerSizeRow);
            if (stickerCell != null) {
                ValueAnimator animator = ValueAnimator.ofFloat(NemoConfig.stickerSize, 14.0f);
                animator.setDuration(150);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(valueAnimator -> {
                    var floatValue = (float) valueAnimator.getAnimatedValue();
                    NemoConfig.setStickerSize(floatValue);
                    stickerCell.setValue(floatValue);
                });
                animator.start();
            } else {
                NemoConfig.setStickerSize(14.0f);
            }
            stickerItem.floatValue = 14.0f;

            var gifItem = listView.findItemByItemId(gifSizeRow);
            var gifCell = (GifSizeCell) listView.findViewByItemId(gifSizeRow);
            if (gifCell != null) {
                ValueAnimator animator = ValueAnimator.ofFloat(NemoConfig.gifSize, 17.5f);
                animator.setDuration(150);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(valueAnimator -> {
                    var floatValue = (float) valueAnimator.getAnimatedValue();
                    NemoConfig.setGifSize(floatValue);
                    gifCell.setValue(floatValue);
                });
                animator.start();
            } else {
                NemoConfig.setGifSize(17.5f);
            }
            gifItem.floatValue = 17.5f;
        });
        AndroidUtilities.updateViewVisibilityAnimated(resetItem, Float.compare(NemoConfig.stickerSize, 14.0f) != 0 || Float.compare(NemoConfig.gifSize, 17.5f) != 0, 1f, false);

        return fragmentView;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.AccDescrStickers)));
        items.add(StickerSizeCellFactory.of(stickerSizeRow, LocaleController.getString(R.string.StickerSize), NemoConfig.stickerSize, progress -> {
            NemoConfig.setStickerSize(progress);
            if (progress != 14.0f && resetItem.getVisibility() != View.VISIBLE) {
                AndroidUtilities.updateViewVisibilityAnimated(resetItem, true, 0.5f, true);
            }
        }).slug("stickerSize"));
        items.add(UItem.asCheck(hideTimeOnStickerRow, LocaleController.getString(R.string.HideTimeOnSticker)).slug("hideTimeOnSticker").setChecked(NemoConfig.hideTimeOnSticker));
        items.add(UItem.asCheck(showTimeHintRow, LocaleController.getString(R.string.ShowTimeHint), LocaleController.getString(R.string.ShowTimeHintDesc)).slug("showTimeHint").setChecked(NemoConfig.showTimeHint));
        items.add(UItem.asCheck(reducedColorsRow, LocaleController.getString(R.string.ReducedColors)).slug("reducedColors").setChecked(NemoConfig.reducedColors));
        items.add(TextSettingsCellFactory.of(maxRecentStickersRow, LocaleController.getString(R.string.MaxRecentStickers), String.valueOf(NemoConfig.maxRecentStickers)).slug("maxRecentStickers"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.AttachGif)));
        items.add(GifSizeCellFactory.of(gifSizeRow, LocaleController.getString(R.string.GifSize), NemoConfig.gifSize, progress -> {
            NemoConfig.setGifSize(progress);
            if (progress != 17.5f && resetItem.getVisibility() != View.VISIBLE) {
                AndroidUtilities.updateViewVisibilityAnimated(resetItem, true, 0.5f, true);
            }
        }).slug("gifSize"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id == hideTimeOnStickerRow) {
            NemoConfig.toggleHideTimeOnSticker();
            if (view instanceof org.telegram.ui.Cells.TextCheckCell) {
                ((org.telegram.ui.Cells.TextCheckCell) view).setChecked(NemoConfig.hideTimeOnSticker);
            }
            var stickerCell = listView.findViewByItemId(stickerSizeRow);
            if (stickerCell != null) stickerCell.invalidate();
        } else if (id == reducedColorsRow) {
            NemoConfig.toggleReducedColors();
            if (view instanceof org.telegram.ui.Cells.TextCheckCell) {
                ((org.telegram.ui.Cells.TextCheckCell) view).setChecked(NemoConfig.reducedColors);
            }
            var stickerCell = listView.findViewByItemId(stickerSizeRow);
            if (stickerCell != null) stickerCell.invalidate();
        } else if (id == showTimeHintRow) {
            NemoConfig.toggleShowTimeHint();
            if (view instanceof org.telegram.ui.Cells.TextCheckCell) {
                ((org.telegram.ui.Cells.TextCheckCell) view).setChecked(NemoConfig.showTimeHint);
            }
        } else if (id == maxRecentStickersRow) {
            int[] counts = {20, 30, 40, 50, 80, 100, 120, 150, 180, 200};
            ArrayList<String> types = new ArrayList<>();
            for (int count : counts) {
                if (count <= getMessagesController().maxRecentStickersCount) {
                    types.add(String.valueOf(count));
                }
            }
            PopupHelper.show(types, LocaleController.getString(R.string.MaxRecentStickers), types.indexOf(String.valueOf(NemoConfig.maxRecentStickers)), getParentActivity(), view, i -> {
                NemoConfig.setMaxRecentStickers(Integer.parseInt(types.get(i)));
                item.textValue = String.valueOf(NemoConfig.maxRecentStickers);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            }, resourcesProvider);
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.StickersAndGifs);
    }

    @Override
    protected String getKey() {
        return "s";
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }

    private static class StickerSizeCellFactory extends UItem.UItemFactory<StickerSizeCell> {
        static {
            setup(new StickerSizeCellFactory());
        }

        @Override
        public StickerSizeCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
            return new StickerSizeCell(context, resourcesProvider);
        }

        @Override
        public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
            var cell = (StickerSizeCell) view;
            var frameLayout = (FrameLayout) listView.getParent();
            cell.setFragmentView(frameLayout);
            cell.setValue(item.floatValue);
            cell.setOnDragListener((AltSeekbar.OnDrag) item.object);
        }

        public static UItem of(int id, String title, float value, AltSeekbar.OnDrag onDrag) {
            var item = UItem.ofFactory(StickerSizeCellFactory.class);
            item.id = id;
            item.text = title;
            item.object = onDrag;
            item.floatValue = value;
            return item;
        }

        @Override
        public boolean isClickable() {
            return false;
        }
    }

    private static class StickerSizeCell extends FrameLayout {

        private final StickerSizePreviewMessagesCell messagesCell;
        private final AltSeekbar sizeBar;

        private AltSeekbar.OnDrag onDrag;

        public StickerSizeCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);

            setWillNotDraw(false);

            sizeBar = new AltSeekbar(context, progress -> {
                setValue(progress);
                if (onDrag != null) onDrag.run(progress);
            }, 2, 20, LocaleController.getString(R.string.StickerSize), LocaleController.getString(R.string.StickerSizeLeft), LocaleController.getString(R.string.StickerSizeRight), resourcesProvider);
            sizeBar.setValue(NemoConfig.stickerSize);
            addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            messagesCell = new StickerSizePreviewMessagesCell(context, resourcesProvider);
            messagesCell.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            addView(messagesCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 112, 0, 0));
        }

        public void setOnDragListener(AltSeekbar.OnDrag onDrag) {
            this.onDrag = onDrag;
        }

        public void setFragmentView(FrameLayout fragmentView) {
            messagesCell.setFragmentView(fragmentView);
        }

        public void setValue(float value) {
            sizeBar.setValue(value);
            messagesCell.invalidate();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            messagesCell.invalidate();
        }
    }

    private static class GifSizeCellFactory extends UItem.UItemFactory<GifSizeCell> {
        static {
            setup(new GifSizeCellFactory());
        }

        @Override
        public GifSizeCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
            return new GifSizeCell(context, resourcesProvider);
        }

        @Override
        public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
            var cell = (GifSizeCell) view;
            cell.setValue(item.floatValue);
            cell.setOnDragListener((AltSeekbar.OnDrag) item.object);
        }

        public static UItem of(int id, String title, float value, AltSeekbar.OnDrag onDrag) {
            var item = UItem.ofFactory(GifSizeCellFactory.class);
            item.id = id;
            item.text = title;
            item.object = onDrag;
            item.floatValue = value;
            return item;
        }

        @Override
        public boolean isClickable() {
            return false;
        }
    }

    private static class GifSizeCell extends FrameLayout {

        private final AltSeekbar sizeBar;
        private AltSeekbar.OnDrag onDrag;

        public GifSizeCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            setWillNotDraw(false);
            sizeBar = new AltSeekbar(context, progress -> {
                setValue(progress);
                if (onDrag != null) onDrag.run(progress);
            }, 14, 20, LocaleController.getString(R.string.GifSize), LocaleController.getString(R.string.StickerSizeLeft), LocaleController.getString(R.string.StickerSizeRight), resourcesProvider);
            sizeBar.setValue(NemoConfig.gifSize);
            addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        public void setOnDragListener(AltSeekbar.OnDrag onDrag) {
            this.onDrag = onDrag;
        }

        public void setValue(float value) {
            sizeBar.setValue(value);
        }
    }

    @SuppressLint("ViewConstructor")
    private static class AltSeekbar extends FrameLayout {

        private final AnimatedTextView headerValue;
        private final TextView leftTextView;
        private final TextView rightTextView;
        private final SeekBarView seekBarView;
        private final Theme.ResourcesProvider resourcesProvider;

        private final int min, max;
        private float currentValue;
        private int roundedValue;

        public interface OnDrag {
            void run(float progress);
        }

        public AltSeekbar(Context context, AltSeekbar.OnDrag onDrag, int min, int max, String title, String left, String right, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;

            this.max = max;
            this.min = min;

            LinearLayout headerLayout = new LinearLayout(context);
            headerLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

            TextView headerTextView = new TextView(context);
            headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            headerTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            headerTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
            headerTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            headerTextView.setText(title);
            headerLayout.addView(headerTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

            headerValue = new AnimatedTextView(context, false, true, true) {
                final Drawable backgroundDrawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider), 0.15f));

                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundDrawable.setBounds(0, 0, (int) (getPaddingLeft() + getDrawable().getCurrentWidth() + getPaddingRight()), getMeasuredHeight());
                    backgroundDrawable.draw(canvas);

                    super.onDraw(canvas);
                }
            };
            headerValue.setAnimationProperties(.45f, 0, 240, CubicBezierInterpolator.EASE_OUT_QUINT);
            headerValue.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            headerValue.setPadding(AndroidUtilities.dp(5.33f), AndroidUtilities.dp(2), AndroidUtilities.dp(5.33f), AndroidUtilities.dp(2));
            headerValue.setTextSize(AndroidUtilities.dp(12));
            headerValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
            headerLayout.addView(headerValue, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 17, Gravity.CENTER_VERTICAL, 6, 1, 0, 0));

            addView(headerLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 21, 17, 21, 0));

            seekBarView = new SeekBarView(context, true, resourcesProvider);
            seekBarView.setReportChanges(true);
            seekBarView.setDelegate((stop, progress) -> {
                currentValue = min + (max - min) * progress;
                onDrag.run(currentValue);
                if (Math.round(currentValue) != roundedValue) {
                    roundedValue = Math.round(currentValue);
                    updateText();
                }
            });
            addView(seekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38 + 6, Gravity.TOP, 6, 68, 6, 0));

            FrameLayout valuesView = new FrameLayout(context);

            leftTextView = new TextView(context);
            leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            leftTextView.setGravity(Gravity.LEFT);
            leftTextView.setText(left);
            valuesView.addView(leftTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

            rightTextView = new TextView(context);
            rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            rightTextView.setGravity(Gravity.RIGHT);
            rightTextView.setText(right);
            valuesView.addView(rightTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

            addView(valuesView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 21, 52, 21, 0));
        }

        private void updateValues() {
            int middle = (max - min) / 2 + min;
            if (currentValue >= middle * 1.5f - min * 0.5f) {
                rightTextView.setTextColor(ColorUtils.blendARGB(
                        Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlueText, resourcesProvider),
                        (currentValue - (middle * 1.5f - min * 0.5f)) / (max - (middle * 1.5f - min * 0.5f))
                ));
                leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else if (currentValue <= (middle + min) * 0.5f) {
                leftTextView.setTextColor(ColorUtils.blendARGB(
                        Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlueText, resourcesProvider),
                        (currentValue - (middle + min) * 0.5f) / (min - (middle + min) * 0.5f)
                ));
                rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
                rightTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            }
        }

        public void setValue(float value) {
            currentValue = value;
            seekBarView.setProgress((value - min) / (float) (max - min));
            if (Math.round(currentValue) != roundedValue) {
                roundedValue = Math.round(currentValue);
                updateText();
            }
        }

        private void updateText() {
            headerValue.cancelAnimation();
            headerValue.setText(getTextForHeader(), true);
            updateValues();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(112), MeasureSpec.EXACTLY)
            );
        }

        public CharSequence getTextForHeader() {
            CharSequence text;
            if (roundedValue == min) {
                text = leftTextView.getText();
            } else if (roundedValue == max) {
                text = rightTextView.getText();
            } else {
                text = String.valueOf(roundedValue);
            }
            return text.toString().toUpperCase();
        }
    }
}