package com.autoexpense.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.autoexpense.PreferenceKeys;
import com.autoexpense.R;
import com.autoexpense.ui.activities.MainActivity;

import java.util.Calendar;

public class BudgetAlertHelper {

    private static final String CHANNEL_ID = "budget_alerts_channel";

    public static void checkAndFireBudgetAlert(Context context, double debitTotal, float budget) {
        if (budget <= 0) return;

        SharedPreferences prefs = context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int savedMonth = prefs.getInt(PreferenceKeys.KEY_BUDGET_ALERT_MONTH, -1);

        if (currentMonth != savedMonth) {
            prefs.edit()
                    .putInt(PreferenceKeys.KEY_BUDGET_ALERT_MONTH, currentMonth)
                    .putBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_80, false)
                    .putBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_100, false)
                    .apply();
        }

        boolean sent80 = prefs.getBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_80, false);
        boolean sent100 = prefs.getBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_100, false);

        double percent = (debitTotal / budget) * 100;

        if (percent >= 100 && !sent100) {
            fireNotification(context, "Over Budget!", "🚨 You've exceeded your monthly budget of ₹" + String.format("%.2f", budget));
            prefs.edit()
                    .putBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_100, true)
                    .putBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_80, true)
                    .apply();
            return;
        }

        if (percent >= 80 && percent < 100 && !sent80) {
            fireNotification(context, "Budget Warning", "⚠️ You've used over 80% of your budget.");
            prefs.edit().putBoolean(PreferenceKeys.KEY_BUDGET_ALERT_SENT_80, true).apply();
        }
    }

    private static void fireNotification(Context context, String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Alerts";
            String description = "Notifications when you reach your budget limits";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
