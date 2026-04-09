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

    // ── Budget Alert flags (reset each calendar month) ─────────────────────────
    /** True once the 80% budget notification has fired this month. */
    public static final String KEY_BUDGET_ALERT_SENT_80 = "budget_alert_80_sent";
    /** True once the 100% budget notification has fired this month. */
    public static final String KEY_BUDGET_ALERT_SENT_100 = "budget_alert_100_sent";
    /**
     * The calendar month (1–12) for which the budget alert flags are valid.
     * When the stored month != current month the flags are reset.
     */
    public static final String KEY_BUDGET_ALERT_MONTH = "budget_alert_month";

    // ── Daily Digest ────────────────────────────────────────────────────────────
    /** When true, a daily morning digest notification is scheduled. */
    public static final String KEY_DAILY_DIGEST_ENABLED = "daily_digest_enabled";
    /**
     * The calendar day-of-year for which the last digest was sent.
     * Prevents the worker from sending duplicates.
     */
    public static final String KEY_LAST_DIGEST_DAY = "last_digest_day_of_year";

    // ── Biometric Lock ──────────────────────────────────────────────────────────
    /** When true, require biometric authentication on every app resume. */
    public static final String KEY_BIOMETRIC_LOCK = "biometric_lock_enabled";
}

