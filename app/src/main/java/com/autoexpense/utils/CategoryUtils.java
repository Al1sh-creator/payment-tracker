package com.autoexpense.utils;

import java.util.Locale;

/**
 * Utility class for auto-detecting transaction categories based on merchant
 * name.
 *
 * Categories:
 * - Food : Swiggy, Zomato, McDonald's, KFC, Domino's, etc.
 * - Travel : Uber, Ola, Rapido, IRCTC, MakeMyTrip, etc.
 * - Shopping : Amazon, Flipkart, Myntra, Meesho, Ajio, etc.
 * - Utilities : Airtel, Jio, BSNL, electricity, water, gas
 * - Entertainment: Netflix, Spotify, Hotstar, YouTube, etc.
 * - Health : Pharmacy, hospital, doctor, Apollo, etc.
 * - Education : Udemy, Coursera, school, college, etc.
 * - Miscellaneous: Everything else
 */
public class CategoryUtils {

    public static final String FOOD = "Food";
    public static final String TRAVEL = "Travel";
    public static final String SHOPPING = "Shopping";
    public static final String UTILITIES = "Utilities";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String HEALTH = "Health";
    public static final String EDUCATION = "Education";
    public static final String SUBSCRIPTIONS = "Subscriptions";
    public static final String MISCELLANEOUS = "Miscellaneous";

    /** All available categories for spinners/filters */
    public static final String[] ALL_CATEGORIES = {
            FOOD, TRAVEL, SHOPPING, UTILITIES, ENTERTAINMENT, HEALTH, EDUCATION, SUBSCRIPTIONS, MISCELLANEOUS
    };

    /**
     * Determine the category for a given merchant name.
     *
     * @param merchantName The merchant name (case-insensitive)
     * @return Category string
     */
    public static String getCategory(String merchantName) {
        if (merchantName == null || merchantName.isEmpty())
            return MISCELLANEOUS;

        String lower = merchantName.toLowerCase(Locale.ROOT).trim();

        // ── Food ────────────────────────────────────────────────────────────────
        if (containsAny(lower, "swiggy", "zomato", "mcdonald", "kfc", "domino",
                "pizza", "burger", "restaurant", "cafe", "food", "biryani",
                "dunzo", "blinkit", "zepto", "instamart", "bigbasket")) {
            return FOOD;
        }

        // ── Travel ──────────────────────────────────────────────────────────────
        if (containsAny(lower, "uber", "ola", "rapido", "irctc", "makemytrip",
                "goibibo", "redbus", "yatra", "indigo", "spicejet", "airindia",
                "metro", "bus", "taxi", "auto", "cab", "flight", "train")) {
            return TRAVEL;
        }

        // ── Shopping ────────────────────────────────────────────────────────────
        if (containsAny(lower, "amazon", "flipkart", "myntra", "meesho", "ajio",
                "nykaa", "snapdeal", "shopclues", "tatacliq", "reliance",
                "dmart", "bigbazar", "mall", "store", "shop")) {
            return SHOPPING;
        }

        // ── Utilities ───────────────────────────────────────────────────────────
        if (containsAny(lower, "airtel", "jio", "bsnl", "vodafone", "vi",
                "electricity", "bescom", "msedcl", "tata power", "gas",
                "water", "broadband", "internet", "recharge", "bill")) {
            return UTILITIES;
        }

        // ── Entertainment ───────────────────────────────────────────────────────
        if (containsAny(lower, "netflix", "spotify", "hotstar", "youtube",
                "prime", "zee5", "sonyliv", "jiocinema", "bookmyshow",
                "pvr", "inox", "movie", "game", "steam")) {
            return ENTERTAINMENT;
        }

        // ── Health ──────────────────────────────────────────────────────────────
        if (containsAny(lower, "apollo", "medplus", "netmeds", "1mg", "pharmeasy",
                "pharmacy", "hospital", "clinic", "doctor", "health",
                "medical", "lab", "diagnostic", "dentist")) {
            return HEALTH;
        }

        // ── Education ───────────────────────────────────────────────────────────
        if (containsAny(lower, "udemy", "coursera", "byju", "unacademy", "vedantu",
                "school", "college", "university", "tuition", "education",
                "book", "stationery")) {
            return EDUCATION;
        }

        if (isSubscription(merchantName)) {
            return SUBSCRIPTIONS;
        }

        return MISCELLANEOUS;
    }

    /**
     * Check if a merchant name belongs to a known recurring subscription service.
     */
    public static boolean isSubscription(String merchantName) {
        if (merchantName == null || merchantName.isEmpty()) return false;
        String lower = merchantName.toLowerCase(Locale.ROOT).trim();
        return containsAny(lower, "netflix", "spotify", "hotstar", "youtube premium", 
                "prime video", "amazon prime", "zee5", "sonyliv", "jiocinema", 
                "apple music", "icloud", "google one", "microsoft 365", "adobe", 
                "canva", "chatgpt", "midjourney", "notion", "broadband", "rent");
    }

    /**
     * Helper: returns true if the source string contains any of the given keywords.
     */
    private static boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword))
                return true;
        }
        return false;
    }
}
