package com.autoexpense.utils;

import android.graphics.Color;

import com.autoexpense.data.entity.TransactionEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpendingPersonality {

    public static class PersonalityResult {
        public final String name;
        public final String emoji;
        public final String description;
        public final int accentColor;

        public PersonalityResult(String name, String emoji, String description, int accentColor) {
            this.name = name;
            this.emoji = emoji;
            this.description = description;
            this.accentColor = accentColor;
        }
    }

    public static PersonalityResult compute(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new PersonalityResult("New Explorer", "🚀", "Start spending to discover your personality!", Color.parseColor("#9E9E9E"));
        }

        double totalSpend = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        for (TransactionEntity t : transactions) {
            if ("Debit".equalsIgnoreCase(t.getTransactionType())) {
                totalSpend += t.getAmount();
                String cat = t.getCategory();
                if (cat == null) cat = "Misc";
                categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + t.getAmount());
            }
        }

        if (totalSpend == 0) {
            return new PersonalityResult("Saver", "💰", "You haven't spent anything yet!", Color.parseColor("#4CAF50"));
        }

        if (totalSpend > 0 && totalSpend < 3000) {
            return new PersonalityResult("Frugal Master", "💎", "Master of saving and mindful spending.", Color.parseColor("#00BCD4"));
        }

        double foodPercent = categoryTotals.getOrDefault("Food & Dining", 0.0) / totalSpend;
        if (foodPercent == 0.0) { // Fallback if category name is just "Food"
            foodPercent = categoryTotals.getOrDefault("Food", 0.0) / totalSpend;
        }

        double travelPercent = categoryTotals.getOrDefault("Travel", 0.0) / totalSpend;
        double shoppingPercent = categoryTotals.getOrDefault("Shopping", 0.0) / totalSpend;
        double entertainmentPercent = categoryTotals.getOrDefault("Entertainment", 0.0) / totalSpend;
        double utilitiesPercent = categoryTotals.getOrDefault("Utilities", 0.0) / totalSpend;
        double educationPercent = categoryTotals.getOrDefault("Education", 0.0) / totalSpend;

        if (foodPercent > 0.40) {
            return new PersonalityResult("Foodie", "🍕", "You spend a good chunk of your money on delicious food.", Color.parseColor("#FF5722"));
        }
        if (utilitiesPercent > 0.40) {
            return new PersonalityResult("Bill Boss", "💡", "You are responsible and stay on top of your utility bills.", Color.parseColor("#607D8B"));
        }
        if (shoppingPercent > 0.35) {
            return new PersonalityResult("Shopaholic", "🛍️", "You love treating yourself to new things.", Color.parseColor("#E91E63"));
        }
        if (travelPercent > 0.30) {
            return new PersonalityResult("Nomad", "✈️", "Always on the move and exploring new places.", Color.parseColor("#3F51B5"));
        }
        if (entertainmentPercent > 0.30) {
            return new PersonalityResult("Entertainment Junkie", "🎬", "You know how to have a good time.", Color.parseColor("#9C27B0"));
        }
        if (educationPercent > 0.25) {
            return new PersonalityResult("Knowledge Seeker", "📚", "Investing heavily in your own learning.", Color.parseColor("#009688"));
        }

        return new PersonalityResult("Balanced Spender", "🌀", "A well-rounded spender.", Color.parseColor("#673AB7"));
    }
}
