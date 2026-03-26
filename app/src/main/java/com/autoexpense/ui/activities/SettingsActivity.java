package com.autoexpense.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.autoexpense.PreferenceKeys;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ActivitySettingsBinding;
import com.autoexpense.utils.CsvExporter;
import com.autoexpense.utils.SmsInboxScanner;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Settings screen.
 * - Toggle SMS auto-detection
 * - Toggle dark mode
 * - Export transactions to CSV
 * - Delete all transactions
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int SMS_IMPORT_PERMISSION_REQUEST = 101;
    private static final int SMS_RECONCILE_LAST_24H_PERMISSION_REQUEST = 102;

    private ActivitySettingsBinding binding;
    private TransactionViewModel viewModel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);

        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        setupSmsToggle();
        setupNotificationCapture();
        setupSmsImport();
        setupDarkModeToggle();
        setupExportButton();
        setupDeleteButton();
        setupBudget();
    }

    private void setupBudget() {
        float currentBudget = prefs.getFloat(PreferenceKeys.KEY_MONTHLY_BUDGET, 0f);
        if (currentBudget > 0) {
            binding.etMonthlyBudget.setText(String.valueOf(currentBudget));
        }

        binding.btnSaveBudget.setOnClickListener(v -> {
            String budgetStr = binding.etMonthlyBudget.getText().toString().trim();
            if (budgetStr.isEmpty()) {
                prefs.edit().putFloat(PreferenceKeys.KEY_MONTHLY_BUDGET, 0f).apply();
                Toast.makeText(this, "Budget cleared", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    float budget = Float.parseFloat(budgetStr);
                    prefs.edit().putFloat(PreferenceKeys.KEY_MONTHLY_BUDGET, budget).apply();
                    Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSmsToggle() {
        boolean smsEnabled = prefs.getBoolean(PreferenceKeys.KEY_SMS_DETECTION, true);
        binding.switchSmsDetection.setChecked(smsEnabled);
        binding.switchSmsDetection.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(PreferenceKeys.KEY_SMS_DETECTION, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "SMS detection enabled" : "SMS detection disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNotificationCapture() {
        boolean enabled = prefs.getBoolean(PreferenceKeys.KEY_NOTIFICATION_CAPTURE, false);
        binding.switchNotificationCapture.setChecked(enabled);
        binding.switchNotificationCapture.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(PreferenceKeys.KEY_NOTIFICATION_CAPTURE, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Enable AutoExpense in notification access for this to work"
                            : "Bank notification capture disabled",
                    Toast.LENGTH_LONG).show();
        });
        binding.btnNotificationAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });
    }

    private void setupSmsImport() {
        binding.btnImportRecentSms.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_SMS },
                        SMS_IMPORT_PERMISSION_REQUEST);
                return;
            }
            runSmsImportRecent();
        });

        binding.btnReconcileLast24hSms.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_SMS },
                        SMS_RECONCILE_LAST_24H_PERMISSION_REQUEST);
                return;
            }
            runSmsReconcileLast24h();
        });
    }

    private void runSmsImportRecent() {
        Toast.makeText(this, "Scanning inbox…", Toast.LENGTH_SHORT).show();
        final android.content.Context appCtx = getApplication();
        Executors.newSingleThreadExecutor().execute(() -> {
            int added = SmsInboxScanner.scanRecent(appCtx, 50);
            runOnUiThread(() -> Toast.makeText(this,
                    added > 0 ? "Imported " + added + " transaction(s)" : "No new transactions found",
                    Toast.LENGTH_LONG).show());
        });
    }

    private void runSmsReconcileLast24h() {
        Toast.makeText(this, "Re-scanning last 24h…", Toast.LENGTH_SHORT).show();
        final android.content.Context appCtx = getApplication();
        final long sinceMs = System.currentTimeMillis() - (24L * 60L * 60L * 1000L);
        Executors.newSingleThreadExecutor().execute(() -> {
            int added = SmsInboxScanner.scanSince(appCtx, sinceMs, 300);
            runOnUiThread(() -> Toast.makeText(this,
                    added > 0 ? "Reconciled " + added + " transaction(s)" : "No missing payments found",
                    Toast.LENGTH_LONG).show());
        });
    }

    private void setupDarkModeToggle() {
        boolean darkMode = prefs.getBoolean(PreferenceKeys.KEY_DARK_MODE, false);
        binding.switchDarkMode.setChecked(darkMode);
        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(PreferenceKeys.KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void setupExportButton() {
        binding.btnExportCsv.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting...", Toast.LENGTH_SHORT).show();
            Executors.newSingleThreadExecutor().execute(() -> {
                List<TransactionEntity> transactions = viewModel.getAllTransactionsSync();
                File csvFile = CsvExporter.exportToCsv(this, transactions);
                runOnUiThread(() -> {
                    if (csvFile != null) {
                        Toast.makeText(this,
                                "Exported to Downloads/" + csvFile.getName(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Export failed. No transactions or permission denied.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void setupDeleteButton() {
        binding.btnDeleteAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Transactions")
                    .setMessage("This will permanently delete all transaction records. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteAll();
                        Toast.makeText(this, "All transactions deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "READ_SMS is required to scan the inbox", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == SMS_IMPORT_PERMISSION_REQUEST) {
            runSmsImportRecent();
        } else if (requestCode == SMS_RECONCILE_LAST_24H_PERMISSION_REQUEST) {
            runSmsReconcileLast24h();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
