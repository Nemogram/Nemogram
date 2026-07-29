package org.nemogram.messenger.settings;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nemogram.messenger.DialogsMenuItems;
import org.nemogram.messenger.NemoConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.HashSet;

public class DialogsMenuSettingsActivity extends BaseFragment {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private static final int MENU_RESET = 1;
    private final ArrayList<String> items = new ArrayList<>();
    private final HashSet<String> hiddenItems = new HashSet<>();
    private RecyclerListView listView;
    private ListAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    @Override
    public boolean onFragmentCreate() {
        reloadFromConfig();
        return super.onFragmentCreate();
    }

    private void reloadFromConfig() {
        items.clear();
        items.addAll(NemoConfig.getDialogsMenuOrder());
        hiddenItems.clear();
        hiddenItems.addAll(NemoConfig.dialogsMenuHiddenItems);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.DialogsMenuSettings));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == MENU_RESET) {
                    showResetAlert();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(MENU_RESET, R.drawable.msg_reset);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, getResourceProvider()));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            toggleItem(items.get(position), (MenuItemCell) view);
        });
        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void toggleItem(String id, MenuItemCell cell) {
        if (DialogsMenuItems.isLocked(id)) {
            return;
        }
        boolean hiddenNow = !hiddenItems.contains(id);
        if (hiddenNow) {
            hiddenItems.add(id);
        } else {
            hiddenItems.remove(id);
        }
        NemoConfig.setDialogsMenuItemHidden(id, hiddenNow);
        if (cell != null) {
            cell.setChecked(!hiddenNow);
        }
    }

    private void showResetAlert() {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle(LocaleController.getString(R.string.DialogsMenuSettingsResetAlertTitle));
        builder.setMessage(LocaleController.getString(R.string.DialogsMenuSettingsResetAlertMessage));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.setPositiveButton(LocaleController.getString(R.string.Reset), (dialog, which) -> {
            NemoConfig.resetDialogsMenuSettings();
            reloadFromConfig();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
        AlertDialog dialog = builder.create();
        showDialog(dialog);
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return true;
    }

    public static class MenuItemCell extends FrameLayout {

        private final ImageView iconView;
        private final TextView titleView;
        private final CheckBox2 checkBox;
        private final ImageView reorderView;
        private boolean needDivider;

        @SuppressLint("ClickableViewAccessibility")
        public MenuItemCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            setWillNotDraw(false);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            iconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider), PorterDuff.Mode.SRC_IN));
            addView(iconView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 8, 0, 8, 0));

            titleView = new TextView(context);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setLines(1);
            titleView.setMaxLines(1);
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL,
                    LocaleController.isRTL ? 88 : 64, 0, LocaleController.isRTL ? 64 : 88, 0));

            checkBox = new CheckBox2(context, 20, resourcesProvider);
            checkBox.setDrawUnchecked(true);
            checkBox.setDrawBackgroundAsArc(10);
            checkBox.setDuration(100);
            checkBox.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
            addView(checkBox, LayoutHelper.createFrame(20, 20, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 52, 0, 52, 0));

            reorderView = new ImageView(context);
            reorderView.setScaleType(ImageView.ScaleType.CENTER);
            reorderView.setImageResource(R.drawable.list_reorder);
            reorderView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider), PorterDuff.Mode.SRC_IN));
            addView(reorderView, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 16, 0, 16, 0));
        }

        public void setItem(String id, boolean checked, boolean locked, boolean divider) {
            iconView.setImageResource(DialogsMenuItems.getIcon(id));
            titleView.setText(DialogsMenuItems.getTitle(id));
            checkBox.setChecked(locked || checked, false);
            checkBox.setAlpha(locked ? 0.5f : 1f);
            needDivider = divider;
            setWillNotDraw(!needDivider);
            invalidate();
        }

        public void setChecked(boolean checked) {
            checkBox.setChecked(checked, true);
        }

        public void setOnReorderButtonTouchListener(OnTouchListener listener) {
            reorderView.setOnTouchListener(listener);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(56), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0 : dp(64), getHeight() - 1, getWidth() - (LocaleController.isRTL ? dp(64) : 0), getHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    private class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != VIEW_TYPE_ITEM) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            adapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                if (viewHolder != null) {
                    viewHolder.itemView.setPressed(true);
                }
            } else {
                NemoConfig.saveDialogsMenuOrder(items);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position < items.size() ? VIEW_TYPE_ITEM : VIEW_TYPE_FOOTER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_ITEM) {
                MenuItemCell cell = new MenuItemCell(mContext, getResourceProvider());
                cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite, getResourceProvider()));
                cell.setOnReorderButtonTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(listView.getChildViewHolder(cell));
                    }
                    return false;
                });
                view = cell;
            } else {
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, getResourceProvider());
                cell.setText(LocaleController.getString(R.string.DialogsMenuSettingsInfo));
                view = cell;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
                String id = items.get(position);
                ((MenuItemCell) holder.itemView).setItem(id, !hiddenItems.contains(id), DialogsMenuItems.isLocked(id), position != items.size() - 1);
            }
        }

        public void swapElements(int fromPosition, int toPosition) {
            if (fromPosition < 0 || toPosition < 0 || fromPosition >= items.size() || toPosition >= items.size()) {
                return;
            }
            String from = items.get(fromPosition);
            items.set(fromPosition, items.get(toPosition));
            items.set(toPosition, from);
            notifyItemMoved(fromPosition, toPosition);
            if (fromPosition == items.size() - 1 || toPosition == items.size() - 1) {
                notifyItemChanged(fromPosition);
                notifyItemChanged(toPosition);
            }
        }
    }
}
