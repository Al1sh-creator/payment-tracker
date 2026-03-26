package com.autoexpense.service;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.autoexpense.PreferenceKeys;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.repository.TransactionRepository;
import com.autoexpense.utils.SmsParser;

/**
 * Captures transaction-like text from posted notifications (bank / UPI apps) and inserts parsed
 * rows. Requires the user to enable this app under system Notification access settings.
 */
public class BankNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences prefs = getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(PreferenceKeys.KEY_NOTIFICATION_CAPTURE, false)) {
            return;
        }

        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }

        String combined = extractNotificationText(notification.extras);
        if (TextUtils.isEmpty(combined)) {
            return;
        }

        long when = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();
        TransactionEntity transaction = SmsParser.parse(combined, when, "Notification");
        if (transaction == null) {
            return;
        }

        TransactionRepository repository = new TransactionRepository(getApplicationContext());
        repository.insertAutoCaptured(transaction);
    }

    private static String extractNotificationText(Bundle extras) {
        if (extras == null) {
            return "";
        }
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        CharSequence sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        StringBuilder sb = new StringBuilder();
        appendChunk(sb, title);
        appendChunk(sb, text);
        appendChunk(sb, sub);
        appendChunk(sb, big);
        return sb.toString().trim();
    }

    private static void appendChunk(StringBuilder sb, CharSequence chunk) {
        if (chunk == null || chunk.length() == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(chunk.toString().trim());
    }
}
