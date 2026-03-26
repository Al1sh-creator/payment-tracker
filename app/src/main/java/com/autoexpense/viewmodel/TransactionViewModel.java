package com.autoexpense.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for transaction data.
 * Survives configuration changes and exposes LiveData to the UI.
 * All business logic and data access goes through TransactionRepository.
 */
public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final LiveData<List<TransactionEntity>> allTransactions;

    /** LiveData for monthly debit total */
    private final MutableLiveData<Double> monthlyDebit = new MutableLiveData<>(0.0);

    /** LiveData for monthly credit total */
    private final MutableLiveData<Double> monthlyCredit = new MutableLiveData<>(0.0);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
        refreshMonthlyTotals();
    }

    // ─── Write Operations ────────────────────────────────────────────────────────

    /** Insert a new transaction */
    public void insert(TransactionEntity transaction) {
        repository.insert(transaction);
        refreshMonthlyTotals();
    }

    /** Delete a specific transaction */
    public void delete(TransactionEntity transaction) {
        repository.delete(transaction);
        refreshMonthlyTotals();
    }

    /** Delete all transactions */
    public void deleteAll() {
        repository.deleteAll();
        monthlyDebit.postValue(0.0);
        monthlyCredit.postValue(0.0);
    }

    // ─── Read Operations ─────────────────────────────────────────────────────────

    /** Get all transactions as LiveData */
    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return allTransactions;
    }

    /** Get transactions filtered by type */
    public LiveData<List<TransactionEntity>> getTransactionsByType(String type) {
        return repository.getTransactionsByType(type);
    }

    /** Get transactions filtered by category */
    public LiveData<List<TransactionEntity>> getTransactionsByCategory(String category) {
        return repository.getTransactionsByCategory(category);
    }

    /** Search transactions by merchant name */
    public LiveData<List<TransactionEntity>> searchByMerchant(String query) {
        return repository.searchByMerchant(query);
    }

    /** Get transactions within a date range */
    public LiveData<List<TransactionEntity>> getTransactionsByDateRange(long start, long end) {
        return repository.getTransactionsByDateRange(start, end);
    }

    /** LiveData for current month's total debit */
    public LiveData<Double> getMonthlyDebit() {
        return monthlyDebit;
    }

    /** LiveData for current month's total credit */
    public LiveData<Double> getMonthlyCredit() {
        return monthlyCredit;
    }

    /**
     * Recalculates monthly totals for the current calendar month.
     * Runs on a background thread and posts results to LiveData.
     */
    public void refreshMonthlyTotals() {
        executor.execute(() -> {
            // Get start and end of current month
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfMonth = cal.getTimeInMillis();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            long endOfMonth = cal.getTimeInMillis();

            double debit = repository.getMonthlyTotal("Debit", startOfMonth, endOfMonth);
            double credit = repository.getMonthlyTotal("Credit", startOfMonth, endOfMonth);

            monthlyDebit.postValue(debit);
            monthlyCredit.postValue(credit);
        });
    }

    /**
     * Get all transactions synchronously for CSV export.
     * Must be called from a background thread.
     */
    public List<TransactionEntity> getAllTransactionsSync() {
        return repository.getAllTransactionsSync();
    }

    /**
     * Get a single transaction by ID synchronously.
     * Must be called from a background thread.
     */
    public TransactionEntity getTransactionById(int id) {
        return repository.getTransactionById(id);
    }
}
