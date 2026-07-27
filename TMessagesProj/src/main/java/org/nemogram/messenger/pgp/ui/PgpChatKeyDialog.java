package org.nemogram.messenger.pgp.ui;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nemogram.messenger.pgp.PgpConfig;
import org.nemogram.messenger.pgp.PgpServiceManager;
import org.nemogram.messenger.pgp.PgpUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class PgpChatKeyDialog {

    public static void show(BaseFragment fragment, long dialogId) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        if (!PgpConfig.isProviderConfigured()) {
            BulletinFactory.of(fragment).createErrorBulletin(LocaleController.getString(R.string.PgpSelectProviderFirst)).show();
            return;
        }

        var context = fragment.getParentActivity();
        var resourcesProvider = fragment.getResourceProvider();
        var builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(LocaleController.getString(R.string.PgpChatEncryption));
        builder.setMessage(LocaleController.getString(R.string.PgpChatEncryptionAbout));
        builder.setCustomViewOffset(0);

        var ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        var editText = new EditTextBoldCursor(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        long existingKeyId = PgpConfig.getDialogKeyId(dialogId);
        editText.setText(existingKeyId != 0 ? PgpUtils.formatKeyId(existingKeyId) : "");
        editText.setHintText(LocaleController.getString(R.string.PgpRecipientKey));
        editText.setHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setHeaderHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
        editText.setSingleLine(true);
        editText.setFocusable(true);
        editText.setTransformHintToHeader(true);
        editText.setLineColors(Theme.getColor(Theme.key_windowBackgroundWhiteInputField, resourcesProvider), Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated, resourcesProvider), Theme.getColor(Theme.key_text_RedRegular, resourcesProvider));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setBackground(null);
        editText.requestFocus();
        editText.setPadding(0, 0, 0, 0);
        ll.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 0, 24, 12, 24, 0));

        var hintView = new TextView(context);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        hintView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        hintView.setText(LocaleController.getString(R.string.PgpRecipientKeyHint));
        ll.addView(hintView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 24, 6, 24, 12));

        builder.setView(ll);
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.setPositiveButton(LocaleController.getString(R.string.Save), null);
        if (existingKeyId != 0) {
            builder.setNeutralButton(LocaleController.getString(R.string.Disable), (dialog, which) -> {
                PgpConfig.setDialogKeyId(dialogId, 0);
                PgpConfig.setDialogEncrypted(dialogId, false);
            });
        }

        var dialog = builder.create();
        fragment.showDialog(dialog);

        var button = (android.widget.TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (button == null) {
            return;
        }
        button.setOnClickListener(v -> {
            CharSequence raw = editText.getText();
            if (TextUtils.isEmpty(raw)) {
                PgpConfig.setDialogKeyId(dialogId, 0);
                PgpConfig.setDialogEncrypted(dialogId, false);
                dialog.dismiss();
                return;
            }
            long keyId = PgpUtils.parseKeyId(raw.toString());
            if (keyId == 0) {
                AndroidUtilities.shakeViewSpring(editText, -6);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
                return;
            }
            button.setEnabled(false);
            button.setText(LocaleController.getString(R.string.PgpRecipientKeyChecking));
            PgpServiceManager.getInstance().ensureKeyAvailable(keyId, new PgpServiceManager.PgpResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    AndroidUtilities.runOnUIThread(() -> {
                        PgpConfig.setDialogKeyId(dialogId, keyId);
                        PgpConfig.setDialogEncrypted(dialogId, true);
                        BulletinFactory.of(fragment).createSuccessBulletin(LocaleController.getString(R.string.PgpRecipientKeySaved)).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void userInteractionRequired(android.app.PendingIntent pendingIntent) {
                    AndroidUtilities.runOnUIThread(() -> {
                        dialog.dismiss();
                        if (fragment.getParentActivity() != null) {
                            PgpServiceManager.startUserInteraction(fragment.getParentActivity(), pendingIntent);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    AndroidUtilities.runOnUIThread(() -> {
                        button.setEnabled(true);
                        button.setText(LocaleController.getString(R.string.Save));
                        AndroidUtilities.shakeViewSpring(editText, -6);
                        BotWebViewVibrationEffect.APP_ERROR.vibrate();
                        BulletinFactory.of(fragment).createErrorBulletin(LocaleController.formatString(R.string.PgpError, message)).show();
                    });
                }
            });
        });
    }
}
