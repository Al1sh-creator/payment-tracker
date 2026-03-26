package com.autoexpense.utils;

import com.autoexpense.data.entity.TransactionEntity;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing UPI/bank transaction SMS messages.
 *
 * Supports formats from major Indian banks:
 * - SBI: "Your A/c XX1234 debited Rs.500.00 on 18-02-26 to VPA swiggy@upi"
 * - HDFC: "Rs.1000 debited from HDFC Bank A/c XX5678 to Uber on 18-02-26"
 * - ICICI: "ICICI Bank: INR 250.00 credited to your A/c from Amazon"
 * - Paytm: "Rs.200 paid to Zomato via UPI"
 *
 * Returns null for OTP, promotional, or unrecognized SMS.
 */
public class SmsParser {

    // ─── Regex Patterns ──────────────────────────────────────────────────────────

    /**
     * Matches currency amounts like: ₹500, Rs.500, Rs 500, INR 500, INR500.00
     * Group 2 captures the numeric amount.
     */
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(₹|Rs\\.?|INR)\\s?([0-9,]+\\.?[0-9]*)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Detects debit transactions.
     */
    private static final Pattern DEBIT_PATTERN = Pattern.compile(
            "\\b(debited|debit|spent|paid|payment|withdrawn|deducted|sent|debiting|transferred out|money sent|debited rs|paid rs)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Detects credit transactions.
     */
    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "\\b(credited|credit|received|added|deposited|refund|reversed|money received|credited rs)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Extracts merchant name after "to", "at", "from", or "via" keywords.
     * Group 1 = keyword, Group 2 = merchant candidate.
     * Terminates on common bankwords like "on", "via", "upi", "a/c", etc.
     */
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "\\b(to|at|from|via|towards)\\s+([A-Za-z0-9@._\\-\\s]{2,30}?)(?:\\s+(?:on|via|using|ref|upi|a/c|account|bank)|$)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Keywords that indicate OTP or promotional SMS — these should be ignored.
     */
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "\\b(otp|one.?time.?password|offer|discount|cashback|expires?|valid|promo|deal|win|congratulations|lucky|reward|click|link|http|www)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Keywords that confirm this is a bank/UPI transaction SMS.
     */
    private static final Pattern BANK_SMS_PATTERN = Pattern.compile(
            "\\b(debited|credited|upi|neft|imps|rtgs|a/c|account|bank|transaction|txn|balance|avl bal|"
                    + "you paid|money sent|sent rs|paid rs|gpay|google pay|phonepe|paytm|"
                    + "transfer|towards|debit|credit|debiting|credited rs|debited rs)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * UPI VPA handle like: swiggy@upi, amazon@okhdfcbank.
     * Group 1 = full VPA.
     */
    private static final Pattern VPA_PATTERN = Pattern.compile(
            "\\b([A-Za-z0-9._\\-]{2,}@[A-Za-z0-9._\\-]{2,})\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches "VPA name@handle" (with or without the literal word VPA).
     * Only matches when the local-part starts with a letter (not a phone number).
     * Group 1 = the readable name before @.
     */
    private static final Pattern NAMED_VPA_PATTERN = Pattern.compile(
            "(?:VPA\\s+)?([A-Za-z][A-Za-z0-9._\\-]{2,30})@[A-Za-z0-9._\\-]{2,}",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches ICICI-style recipient name in debit SMS:
     * "Mr SUJAL RAKESH credited" or "SUJAL RAKESH credited" (Mr/Mrs optional)
     * Requires at least two words so single words like "Amount credited" are NOT matched.
     * Group 1 = the person's name.
     */
    private static final Pattern PERSON_PATTERN = Pattern.compile(
            "(?:\\b(?:Mr|Mrs|Ms|Dr)\\.?\\s+)?([A-Za-z]{2,}(?:\\s+[A-Za-z]{2,})+)\\s+credited\\b",
            Pattern.CASE_INSENSITIVE);

    // ─── Public API ──────────────────────────────────────────────────────────────

    /**
     * Parse SMS using receive time as transaction time and {@code UPI} as payment
     * method.
     */
    public static TransactionEntity parse(String smsBody) {
        long now = System.currentTimeMillis();
        return parse(smsBody, now, "UPI");
    }

    /**
     * Parse bank/UPI-style text (SMS or notification).
     */
    public static TransactionEntity parse(String smsBody, long transactionDateMs, String paymentMethod) {
        if (smsBody == null || smsBody.trim().isEmpty())
            return null;

        // Step 1: Filter out OTP and promotional messages
        if (IGNORE_PATTERN.matcher(smsBody).find())
            return null;

        // Step 2: Confirm it's a bank/UPI SMS
        if (!BANK_SMS_PATTERN.matcher(smsBody).find())
            return null;

        // Step 3: Extract amount
        double amount = extractAmount(smsBody);
        if (amount <= 0)
            return null;

        // Step 4: Determine transaction type
        String transactionType = extractTransactionType(smsBody);
        if (transactionType == null)
            return null;

        // Step 5: Extract merchant name
        // First try: get the human-readable part from a named VPA like swiggy@upi
        String merchantName = extractNamedVpa(smsBody);

        // Second try: look for a titled person name like "Mr SUJAL RAKESH credited"
        if (merchantName == null || merchantName.trim().isEmpty()) {
            merchantName = extractPersonName(smsBody);
        }

        // Third try: keyword-based extraction (to/from/at merchant)
        if (merchantName == null || merchantName.trim().isEmpty()) {
            merchantName = extractMerchant(smsBody, transactionType);
        }

        if (merchantName == null || merchantName.trim().isEmpty()) {
            merchantName = "Unknown";
        }
        merchantName = cleanMerchantName(merchantName);

        // Fallback: if still unknown or a raw numeric UPI transfer, try full VPA
        // local-part extraction
        if (merchantName.equals("Unknown") || merchantName.startsWith("UPI Transfer")
                || merchantName.matches("\\d{8,}")) {
            String vpaLocal = extractVpaLocalPart(smsBody);
            if (vpaLocal != null && !vpaLocal.trim().isEmpty()) {
                merchantName = cleanMerchantName(vpaLocal);
            }
        }

        // Step 6: Auto-detect category
        String category = CategoryUtils.getCategory(merchantName);

        // Step 7: Build entity
        long createdAt = System.currentTimeMillis();
        String method = paymentMethod != null && !paymentMethod.isEmpty() ? paymentMethod : "UPI";
        return new TransactionEntity(
                amount,
                merchantName,
                transactionType,
                category,
                transactionDateMs,
                method,
                createdAt);
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    private static double extractAmount(String sms) {
        Matcher matcher = AMOUNT_PATTERN.matcher(sms);
        if (matcher.find()) {
            try {
                String amountStr = matcher.group(2).replace(",", "");
                return Double.parseDouble(amountStr);
            } catch (NumberFormatException | NullPointerException e) {
                return 0;
            }
        }
        return 0;
    }

    private static String extractTransactionType(String sms) {
        if (DEBIT_PATTERN.matcher(sms).find())
            return "Debit";
        if (CREDIT_PATTERN.matcher(sms).find())
            return "Credit";
        return null;
    }

    /**
     * Extracts the readable name from a named VPA like "swiggy@upi" → "swiggy".
     * Ignores numeric VPAs like "9876543210@upi" (those are phone numbers, not
     * names).
     */
    private static String extractNamedVpa(String sms) {
        if (sms == null)
            return null;
        Matcher m = NAMED_VPA_PATTERN.matcher(sms);
        while (m.find()) {
            String name = m.group(1);
            if (name != null && name.length() > 2 && !name.matches("\\d+")) {
                // Skip generic bank/UPI infrastructure handles
                String lower = name.toLowerCase(Locale.ROOT);
                if (!lower.equals("upi") && !lower.equals("neft") && !lower.equals("imps")
                        && !lower.equals("paytm") && !lower.equals("gpay") && !lower.equals("phonepe")
                        && !lower.startsWith("ok") && !lower.equals("ybl") && !lower.equals("ibl")
                        && !lower.equals("axl") && !lower.equals("sbi") && !lower.equals("hdfc")) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Extracts a person name from ICICI-style debit SMS:
     * "Mr SUJAL RAKESH credited" or "SUJAL RAKESH credited" → "Sujal Rakesh"
     * Only matches when the name is 2+ words (prevents false positives like "Amount credited").
     */
    private static String extractPersonName(String sms) {
        if (sms == null) return null;
        Matcher m = PERSON_PATTERN.matcher(sms);
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.trim().isEmpty()) continue;
            String lower = name.trim().toLowerCase(Locale.ROOT);
            // Skip if name contains common banking terms (false positive guard)
            if (lower.contains("bank") || lower.contains("amount") || lower.contains("balance")
                    || lower.contains("acct") || lower.contains("account") || lower.contains("upi")
                    || lower.contains(" rs") || lower.contains("inr")) {
                continue;
            }
            return name.trim();
        }
        return null;
    }

    private static String extractMerchant(String sms, String transactionType) {
        Matcher matcher = MERCHANT_PATTERN.matcher(sms);

        String best = null;
        int bestScore = Integer.MIN_VALUE;

        while (matcher.find()) {
            String keyword = matcher.group(1).toLowerCase(Locale.ROOT);
            String candidate = matcher.group(2);

            int score = 0;
            if ("Debit".equals(transactionType)) {
                if ("to".equals(keyword))
                    score = 30;
                else if ("at".equals(keyword))
                    score = 20;
                else if ("via".equals(keyword))
                    score = 10;
                else if ("from".equals(keyword))
                    score = 0;
            } else {
                if ("from".equals(keyword))
                    score = 30;
                else if ("at".equals(keyword))
                    score = 20;
                else if ("to".equals(keyword))
                    score = 10;
                else if ("via".equals(keyword))
                    score = 0;
            }

            boolean hasLetters = candidate != null && candidate.matches(".*[A-Za-z].*");
            boolean isNumericOnly = candidate != null && candidate.matches("\\d{5,}");

            if (hasLetters)
                score += 5;
            if (isNumericOnly)
                score -= 20;

            // Skip useless tokens
            if (candidate != null) {
                String cl = candidate.trim().toLowerCase(Locale.ROOT);
                if (cl.equals("your") || cl.equals("the") || cl.equals("vpa")
                        || cl.equals("sms") || cl.equals("block") || cl.equals("dispute")
                        || cl.isEmpty()) {
                    continue;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Cleans up extracted merchant names.
     */
    private static String cleanMerchantName(String raw) {
        if (raw == null)
            return "Unknown";
        // Remove VPA handle (e.g. swiggy@upi → swiggy)
        raw = raw.replaceAll("@[a-zA-Z0-9]+", "").trim();
        // Remove literal "VPA" prefix
        raw = raw.replaceAll("(?i)^vpa\\b", "").trim();
        // Remove trailing punctuation
        raw = raw.replaceAll("[.,;:]+$", "").trim();

        // Phone number → shorter label
        if (raw.matches("\\d{8,}")) {
            String last4 = raw.substring(raw.length() - 4);
            return "UPI Transfer (ending " + last4 + ")";
        }

        if (raw.isEmpty())
            return "Unknown";

        // Title case
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.getDefault()))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static String extractVpaLocalPart(String sms) {
        if (sms == null || sms.trim().isEmpty())
            return null;
        Matcher m = VPA_PATTERN.matcher(sms);
        if (m.find()) {
            String vpa = m.group(1);
            if (vpa != null) {
                int at = vpa.indexOf('@');
                if (at > 0)
                    return vpa.substring(0, at);
                return vpa;
            }
        }
        return null;
    }
}
