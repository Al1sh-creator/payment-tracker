package com.autoexpense;

/**
 * SharedPreferences file name and keys used across auto-capture features.
 */
public final class PreferenceKeys {

    private PreferenceKeys() {
    }

    public static final String PREFS_NAME = "AutoExpensePrefs";
    public static final String KEY_SMS_DETECTION = "sms_detection_enabled";
    /** When true, parse transaction-like text from posted notifications (requires system access). */
    public static final String KEY_NOTIFICATION_CAPTURE = "notification_capture_enabled";
    public static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";
    public static final String KEY_DARK_MODE = "dark_mode_enabled";
    public static final String KEY_MONTHLY_BUDGET = "monthly_budget";
}
