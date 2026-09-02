package org.nemogram.messenger.pgp;

import android.app.PendingIntent;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

public class PgpMessageDecryptor {

    public static void decryptAsync(MessageObject messageObject) {
        messageObject.pgpDecryptState = MessageObject.PGP_DECRYPT_STATE_DECRYPTING;
        if (!PgpConfig.isProviderConfigured() || messageObject.pgpArmoredText == null) {
            markFailed(messageObject);
            return;
        }
        PgpServiceManager.getInstance().decrypt(messageObject.pgpArmoredText, new PgpServiceManager.PgpResultCallback<String>() {
            @Override
            public void onSuccess(String plainText) {
                AndroidUtilities.runOnUIThread(() -> {
                    messageObject.pgpDecryptState = MessageObject.PGP_DECRYPT_STATE_DONE;
                    messageObject.pgpDecryptedText = plainText;
                    messageObject.messageText = plainText;
                    // without this the bubble would keep old size
                    messageObject.forceUpdate = true;
                    messageObject.generateLayout(null);
                    NotificationCenter.getInstance(messageObject.currentAccount)
                            .postNotificationName(NotificationCenter.messagePgpDecrypted, messageObject);
                });
            }

            @Override
            public void userInteractionRequired(PendingIntent pendingIntent) {
                markFailed(messageObject);
            }

            @Override
            public void onError(String message) {
                markFailed(messageObject);
            }
        });
    }

    private static void markFailed(MessageObject messageObject) {
        AndroidUtilities.runOnUIThread(() -> {
            messageObject.pgpDecryptState = MessageObject.PGP_DECRYPT_STATE_FAILED;
            messageObject.messageText = LocaleController.getString(R.string.PgpDecryptFailed);
            messageObject.forceUpdate = true;
            messageObject.generateLayout(null);
            NotificationCenter.getInstance(messageObject.currentAccount)
                    .postNotificationName(NotificationCenter.messagePgpDecrypted, messageObject);
        });
    }
}
