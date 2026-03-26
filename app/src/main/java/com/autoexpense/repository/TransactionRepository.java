package com.autoexpense.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.autoexpense.data.dao.TransactionDao;
import com.autoexpense.data.database.AppDatabase;
import com.autoexpense.data.entity.TransactionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that abstracts the data sources from the ViewModel.
 * All database write operations are executed on a background thread via
 * ExecutorService.
 */
public class TransactionRepository {

    private final TransactionDao transactionDao;

    /** Single-thread executor for background DB operations */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        transactionDao = db.transactionDao();
    }

    // ─── Write Operations (run on background thread) ─────────────────────────────

    /**
     * Insert a transaction into the database on a background thread.
     */
    public void insert(TransactionEntity transaction) {
        executorService.execute(() -> transactionDao.insertTransaction(transaction));
    }

    /**
     * Inserts an auto-captured transaction unless a very similar one already exists (same amount and
     * type within a short time window). Reduces duplicates when both SMS and a bank notification
     * arrive for one payment.
     */
    public void insertAutoCaptured(TransactionEntity transaction) {
        executorService.execute(() -> insertAutoCapturedBlocking(transaction));
    }

    /**
     * Same dedupe rules as {@link #insertAutoCaptured} but runs synchronously on the calling thread.
     * Must be called from a background thread (Room forbids main-thread queries).
     *
     * @return true if a new row was inserted
     */
    public boolean insertAutoCapturedBlocking(TransactionEntity transaction) {
        long d = transaction.getDate();
        // SMS and notification posts can be offset by a few minutes.
        long windowMs = 10 * 60 * 1000L;
        int existing = transactionDao.countSimilarInDateRange(
                transaction.getAmount(),
                transaction.getTransactionType(),
                d - windowMs,
                d + windowMs);
        if (existing > 0) {
            return false;
        }
        transactionDao.insertTransaction(transaction);
        return true;
    }

    /**
     * Delete a specific transaction on a background thread.
     */
    public void delete(TransactionEntity transaction) {
        executorService.execute(() -> transactionDao.deleteTransaction(transaction));
    }

    /**
     * Delete all transactions on a background thread.
     */
    public void deleteAll() {
        executorService.execute(transactionDao::deleteAllTransactions);
    }

    // ─── Read Operations (return LiveData, observed on main thread)
    // ───────────────

    /**
     * Get all transactions as LiveData (auto-updates UI on change).
     */
    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    /**
     * Get transactions within a date range.
     */
    public LiveData<List<TransactionEntity>> getTransactionsByDateRange(long start, long end) {
        return transactionDao.getTransactionsByDateRange(start, end);
    }

    /**
     * Get transactions filtered by type (Debit/Credit).
     */
    public LiveData<List<TransactionEntity>> getTransactionsByType(String type) {
        return transactionDao.getTransactionsByType(type);
    }

    /**
     * Get transactions filtered by category.
     */
    public LiveData<List<TransactionEntity>> getTransactionsByCategory(String category) {
        return transactionDao.getTransactionsByCategory(category);
    }

    /**
     * Search transactions by merchant name.
     */
    public LiveData<List<TransactionEntity>> searchByMerchant(String query) {
        return transactionDao.searchByMerchant(query);
    }

    /**
     * Get monthly total for a given type synchronously (called from background
     * thread in ViewModel).
     */
    public double getMonthlyTotal(String type, long startDate, long endDate) {
        return transactionDao.getMonthlyTotal(type, startDate, endDate);
    }

    /**
     * Get all transactions as a plain list for CSV export (must be called off main
     * thread).
     */
    public List<TransactionEntity> getAllTransactionsSync() {
        return transactionDao.getAllTransactionsSync();
    }

    /**
     * Get a single transaction by ID (must be called off main thread).
     */
    public TransactionEntity getTransactionById(int id) {
        return transactionDao.getTransactionById(id);
    }
}
