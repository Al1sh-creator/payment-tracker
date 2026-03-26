package com.autoexpense.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.autoexpense.PreferenceKeys;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.repository.TransactionRepository;
import com.autoexpense.utils.SmsParser;

/**
 * BroadcastReceiver that listens for incoming SMS messages.
 * Filters UPI/bank transaction SMS and auto-inserts them into the database.
 *
 * Registered in AndroidManifest.xml for action:
 * android.provider.Telephony.SMS_RECEIVED
 */
public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if SMS auto-detection is enabled in Settings
        SharedPreferences prefs = context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean(PreferenceKeys.KEY_SMS_DETECTION, true);
        if (!smsEnabled)
            return;

        // Extract SMS messages from intent
        Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0)
            return;

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (format != null) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (smsMessage == null)
                continue;

            String messageBody = smsMessage.getMessageBody();
            if (messageBody == null || messageBody.isEmpty())
                continue;

            // Attempt to parse as a transaction SMS
            TransactionEntity transaction = SmsParser.parse(messageBody);

            if (transaction != null) {
                TransactionRepository repository = new TransactionRepository(
                        context.getApplicationContext());
                repository.insertAutoCaptured(transaction);
            }
        }
    }
}
