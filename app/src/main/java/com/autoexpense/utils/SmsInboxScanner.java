package com.autoexpense.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.repository.TransactionRepository;

/**
 * One-shot scan of recent SMS inbox rows to backfill transactions (requires READ_SMS).
 */
public final class SmsInboxScanner {

    private SmsInboxScanner() {
    }

    /**
     * @return number of new transactions inserted (after deduplication)
     */
    public static int scanRecent(Context context, int maxMessages) {
        if (maxMessages <= 0) {
            return 0;
        }
        Uri uri = android.provider.Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = new String[] {
                android.provider.Telephony.Sms.BODY,
                android.provider.Telephony.Sms.DATE
        };
        String sort = android.provider.Telephony.Sms.DEFAULT_SORT_ORDER;

        TransactionRepository repository = new TransactionRepository(context.getApplicationContext());

        int inserted = 0;
        try (Cursor c = context.getContentResolver().query(
                uri, projection, null, null, sort)) {
            if (c == null) {
                return 0;
            }
            int bodyIdx = c.getColumnIndexOrThrow(android.provider.Telephony.Sms.BODY);
            int dateIdx = c.getColumnIndexOrThrow(android.provider.Telephony.Sms.DATE);
            int n = 0;
            while (c.moveToNext() && n < maxMessages) {
                n++;
                String body = c.getString(bodyIdx);
                long dateMs = c.getLong(dateIdx);
                TransactionEntity t = SmsParser.parse(body, dateMs, "UPI");
                if (t == null) {
                    continue;
                }
                if (repository.insertAutoCapturedBlocking(t)) {
                    inserted++;
                }
            }
        }
        return inserted;
    }

    /**
     * Scans inbox messages newer than {@code sinceMs} and inserts any matching transactions
     * (after deduplication).
     */
    public static int scanSince(Context context, long sinceMs, int maxMessages) {
        if (maxMessages <= 0) {
            return 0;
        }

        Uri uri = android.provider.Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = new String[] {
                android.provider.Telephony.Sms.BODY,
                android.provider.Telephony.Sms.DATE
        };

        String sort = android.provider.Telephony.Sms.DEFAULT_SORT_ORDER;
        String selection = android.provider.Telephony.Sms.DATE + " >= ?";
        String[] selectionArgs = new String[] { String.valueOf(sinceMs) };

        TransactionRepository repository = new TransactionRepository(context.getApplicationContext());

        int inserted = 0;
        try (Cursor c = context.getContentResolver().query(
                uri,
                projection,
                selection,
                selectionArgs,
                sort)) {
            if (c == null) {
                return 0;
            }

            int bodyIdx = c.getColumnIndexOrThrow(android.provider.Telephony.Sms.BODY);
            int dateIdx = c.getColumnIndexOrThrow(android.provider.Telephony.Sms.DATE);

            int n = 0;
            while (c.moveToNext() && n < maxMessages) {
                n++;
                String body = c.getString(bodyIdx);
                long dateMs = c.getLong(dateIdx);
                TransactionEntity t = SmsParser.parse(body, dateMs, "UPI");
                if (t == null) {
                    continue;
                }
                if (repository.insertAutoCapturedBlocking(t)) {
                    inserted++;
                }
            }
        }
        return inserted;
    }
}
