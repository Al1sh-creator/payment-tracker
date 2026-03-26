package com.autoexpense.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.autoexpense.data.entity.TransactionEntity;

import java.util.List;

/**
 * Data Access Object for TransactionEntity.
 * Provides all database operations needed by the app.
 */
@Dao
public interface TransactionDao {

    /**
     * Insert a new transaction. If a conflict occurs, replace the existing row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(TransactionEntity transaction);

    /**
     * Delete a specific transaction.
     */
    @Delete
    void deleteTransaction(TransactionEntity transaction);

    /**
     * Delete ALL transactions from the database.
     */
    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    /**
     * Fetch all transactions ordered by date descending (newest first).
     * Returns LiveData so the UI auto-updates on changes.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getAllTransactions();

    /**
     * Fetch transactions within a specific date range.
     *
     * @param startDate Unix timestamp (ms) for range start
     * @param endDate   Unix timestamp (ms) for range end
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getTransactionsByDateRange(long startDate, long endDate);

    /**
     * Get the sum of all transactions of a given type (Debit/Credit) within a date
     * range.
     * Used for monthly totals on the dashboard.
     *
     * @param type      "Debit" or "Credit"
     * @param startDate Unix timestamp (ms) for range start
     * @param endDate   Unix timestamp (ms) for range end
     * @return Total amount as double (0.0 if no records found)
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE transaction_type = :type AND date BETWEEN :startDate AND :endDate")
    double getMonthlyTotal(String type, long startDate, long endDate);

    /**
     * Fetch all transactions of a specific type (Debit/Credit).
     */
    @Query("SELECT * FROM transactions WHERE transaction_type = :type ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getTransactionsByType(String type);

    /**
     * Fetch all transactions in a specific category.
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getTransactionsByCategory(String category);

    /**
     * Search transactions by merchant name (case-insensitive partial match).
     */
    @Query("SELECT * FROM transactions WHERE merchant_name LIKE '%' || :query || '%' ORDER BY date DESC")
    LiveData<List<TransactionEntity>> searchByMerchant(String query);

    /**
     * Get all transactions as a plain list (not LiveData) — used for CSV export.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactionsSync();

    /**
     * Count rows matching amount, type, and date window — used to skip duplicate SMS + notification
     * captures of the same payment.
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE "
            + "ROUND(amount, 2) = ROUND(:amount, 2) "
            + "AND transaction_type = :type AND date BETWEEN :startDate AND :endDate")
    int countSimilarInDateRange(double amount, String type, long startDate, long endDate);

    /**
     * Fetch a single transaction by ID.
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getTransactionById(int id);
}
