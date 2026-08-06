package org.nemogram.messenger.settings.cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Stories.recorder.HintView2;

import org.nemogram.messenger.NemoConfig;
import org.nemogram.messenger.helpers.MessageHelper;

@SuppressLint("ViewConstructor")
public class StickerSizePreviewMessagesCell extends LinearLayout {

    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;

    private FrameLayout fragmentView;
    private final ChatMessageCell[] cells = new ChatMessageCell[3];
    private final MessageObject[] messageObjects = new MessageObject[3];
    private final Drawable shadowDrawable;

    public StickerSizePreviewMessagesCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);

        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, dp(11), 0, dp(11));

        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.getColor(Theme.key_windowBackgroundGrayShadow, resourcesProvider));

        int date = (int) (System.currentTimeMillis() / 1000) - 60 * 60;
        TLRPC.TL_message message = new TLRPC.TL_message();
        message.date = date + 10;
        message.dialog_id = 1;
        message.flags = 257;
        message.from_id = new TLRPC.TL_peerUser();
        message.from_id.user_id = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
        message.id = 1;
        message.media = new TLRPC.TL_messageMediaDocument();
        message.media.flags = 1;
        message.media.document = new TLRPC.TL_document();
        message.media.document.mime_type = "image/webp";
        message.media.document.file_reference = new byte[0];
        message.media.document.access_hash = 0;
        message.media.document.date = date;
        TLRPC.TL_documentAttributeSticker attributeSticker = new TLRPC.TL_documentAttributeSticker();
        attributeSticker.alt = "🐟";
        message.media.document.attributes.add(attributeSticker);
        TLRPC.TL_documentAttributeImageSize attributeImageSize = new TLRPC.TL_documentAttributeImageSize();
        attributeImageSize.h = 512;
        attributeImageSize.w = 512;
        message.media.document.attributes.add(attributeImageSize);
        message.message = "";
        message.out = true;
        message.peer_id = new TLRPC.TL_peerUser();
        message.peer_id.user_id = 0;
        messageObjects[0] = new MessageObject(UserConfig.selectedAccount, message, true, false);
        messageObjects[0].useCustomPhoto = true;

        message = new TLRPC.TL_message();
        message.message = LocaleController.getString(R.string.StickerSizeDialogMessageReplyTo);
        message.date = date + 1270;
        message.dialog_id = -1;
        message.flags = 259;
        message.id = 2;
        message.media = new TLRPC.TL_messageMediaEmpty();
        message.out = false;
        message.peer_id = new TLRPC.TL_peerUser();
        message.peer_id.user_id = 1;
        messageObjects[0].customReplyName = "BigFuguFish";
        messageObjects[0].replyMessageObject = new MessageObject(UserConfig.selectedAccount, message, true, false);

        // GIF from the interlocutor, right after the sticker; its on-screen size is
        // controlled by NemoConfig.gifSize the same way a real incoming GIF is (see
        // ChatMessageCell's TYPE_GIF sizing branch), so it scales proportionally with the slider.
        message = new TLRPC.TL_message();
        message.date = date + 640;
        message.dialog_id = -1;
        message.flags = 259;
        message.id = 3;
        message.media = new TLRPC.TL_messageMediaDocument();
        message.media.flags = 1;
        message.media.document = new TLRPC.TL_document();
        message.media.document.mime_type = "video/mp4";
        message.media.document.file_reference = new byte[0];
        message.media.document.access_hash = 0;
        message.media.document.date = date;
        TLRPC.TL_documentAttributeAnimated attributeAnimated = new TLRPC.TL_documentAttributeAnimated();
        message.media.document.attributes.add(attributeAnimated);
        TLRPC.TL_documentAttributeVideo attributeVideo = new TLRPC.TL_documentAttributeVideo();
        attributeVideo.w = 320;
        attributeVideo.h = 240;
        message.media.document.attributes.add(attributeVideo);
        message.message = "";
        message.out = false;
        message.peer_id = new TLRPC.TL_peerUser();
        message.peer_id.user_id = 1;
        messageObjects[1] = new MessageObject(UserConfig.selectedAccount, message, true, false);
        messageObjects[1].useCustomPhoto = true;

        message = new TLRPC.TL_message();
        message.message = LocaleController.getString(R.string.StickerSizeDialogMessage);
        message.date = date + 1270;
        message.dialog_id = -1;
        message.flags = 259;
        message.id = 4;
        message.reply_to = new TLRPC.TL_messageReplyHeader();
        message.reply_to.flags |= 16;
        message.reply_to.reply_to_msg_id = 2;
        message.media = new TLRPC.TL_messageMediaEmpty();
        message.out = false;
        message.peer_id = new TLRPC.TL_peerUser();
        message.peer_id.user_id = 1;
        messageObjects[2] = new MessageObject(UserConfig.selectedAccount, message, true, false);
        messageObjects[2].replyMessageObject = messageObjects[0];

        for (int a = 0; a < cells.length; a++) {
            cells[a] = new ChatMessageCell(context, UserConfig.selectedAccount, false, null, resourcesProvider);
            cells[a].setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {

                @Override
                public boolean canPerformActions() {
                    return true;
                }

                @Override
                public void didPressTime(ChatMessageCell cell) {
                    showTimeHint(cell);
                }
            });
            cells[a].isChat = false;
            cells[a].setFullyDraw(true);
            cells[a].setMessageObject(messageObjects[a], null, false, false, false);
            addView(cells[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
    }

    public void setFragmentView(FrameLayout view) {
        this.fragmentView = view;
    }

    private HintView2 timeHint;

    private void showTimeHint(ChatMessageCell cell) {
        if (fragmentView == null || cell == null || cell.timeLayout == null || cell.getMessageObject() == null ||
                cell.getMessageObject().messageOwner == null ||
                (NemoConfig.hideTimeOnSticker && cell.getMessageObject().isAnyKindOfSticker())
        ) {
            return;
        }
        FrameLayout parent = fragmentView;
        if (timeHint != null) {
            var hint = timeHint;
            hint.setOnHiddenListener(() -> parent.removeView(hint));
            hint.hide();
            timeHint = null;
        }
        timeHint = new HintView2(getContext(), HintView2.DIRECTION_BOTTOM)
                .setMultilineText(true)
                .setDuration(2000);

        timeHint.setText(MessageHelper.getTimeHintText(cell.getMessageObject()));
        timeHint.setMaxWidthPx(parent.getMeasuredWidth());
        timeHint.bringToFront();
        parent.addView(timeHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 120, Gravity.TOP | Gravity.FILL_HORIZONTAL, 16, 0, 16, 0));
        var hint = timeHint;
        parent.post(() -> {
            var loc = new int[2];
            cell.getLocationInWindow(loc);
            hint.setTranslationY(loc[1] - hint.getTop() - dp(120) + cell.getTimeY());
            hint.setJointPx(0, -dp(16) + loc[0] + cell.timeX + cell.timeWidth - cell.timeTextWidth / 2f + cell.signWidth / 2f);
            hint.show();
        });
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (int a = 0; a < cells.length; a++) {
            if (cells[a] == null) {
                continue;
            }
            cells[a].setMessageObject(messageObjects[a], null, false, false, false);
            cells[a].invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        Drawable drawable = Theme.getCachedWallpaperNonBlocking();
        if (drawable == null) {
            return;
        }
        drawable.setAlpha(255);
        if (drawable instanceof ColorDrawable || drawable instanceof GradientDrawable || drawable instanceof MotionBackgroundDrawable) {
            drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            if (drawable instanceof BackgroundGradientDrawable backgroundGradientDrawable) {
                backgroundGradientDisposable = backgroundGradientDrawable.drawExactBoundsSize(canvas, this);
            } else {
                drawable.draw(canvas);
            }
        } else if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getTileModeX() == Shader.TileMode.REPEAT) {
                canvas.save();
                float scale = 2.0f / AndroidUtilities.density;
                canvas.scale(scale, scale);
                drawable.setBounds(0, 0, (int) Math.ceil(getMeasuredWidth() / scale), (int) Math.ceil(getMeasuredHeight() / scale));
            } else {
                int viewHeight = getMeasuredHeight();
                float scaleX = (float) getMeasuredWidth() / (float) drawable.getIntrinsicWidth();
                float scaleY = (float) (viewHeight) / (float) drawable.getIntrinsicHeight();
                float scale = Math.max(scaleX, scaleY);
                int width = (int) Math.ceil(drawable.getIntrinsicWidth() * scale);
                int height = (int) Math.ceil(drawable.getIntrinsicHeight() * scale);
                int x = (getMeasuredWidth() - width) / 2;
                int y = (viewHeight - height) / 2;
                canvas.save();
                canvas.clipRect(0, 0, width, getMeasuredHeight());
                drawable.setBounds(x, y, x + width, y + height);
            }
            drawable.draw(canvas);
            canvas.restore();
        }
        shadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        shadowDrawable.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

    }
}
