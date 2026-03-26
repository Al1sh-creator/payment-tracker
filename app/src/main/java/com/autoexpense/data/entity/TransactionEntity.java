package com.autoexpense.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a single financial transaction.
 * Each row in the "transactions" table corresponds to one TransactionEntity.
 */
@Entity(tableName = "transactions")
public class TransactionEntity {

    /** Auto-generated primary key */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /** Transaction amount in INR */
    @ColumnInfo(name = "amount")
    private double amount;

    /** Name of the merchant or sender/receiver */
    @ColumnInfo(name = "merchant_name")
    private String merchantName;

    /** "Debit" or "Credit" */
    @ColumnInfo(name = "transaction_type")
    private String transactionType;

    /** Auto-detected or user-selected category (Food, Travel, Shopping, etc.) */
    @ColumnInfo(name = "category")
    private String category;

    /** Unix timestamp (ms) of when the transaction occurred */
    @ColumnInfo(name = "date")
    private long date;

    /** Payment method, e.g. "UPI", "Manual" */
    @ColumnInfo(name = "payment_method")
    private String paymentMethod;

    /** Unix timestamp (ms) of when the record was created in the app */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ─── Constructor ────────────────────────────────────────────────────────────

    public TransactionEntity(double amount, String merchantName, String transactionType,
            String category, long date, String paymentMethod, long createdAt) {
        this.amount = amount;
        this.merchantName = merchantName;
        this.transactionType = transactionType;
        this.category = category;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
