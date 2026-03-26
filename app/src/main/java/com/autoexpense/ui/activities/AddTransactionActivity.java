package com.autoexpense.ui.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ActivityAddTransactionBinding;
import com.autoexpense.utils.CategoryUtils;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for manually adding a transaction.
 * Fields: Amount, Merchant Name, Type (Debit/Credit), Category, Date.
 */
public class AddTransactionActivity extends AppCompatActivity {

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private int editId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check for edit mode
        editId = getIntent().getIntExtra("EXTRA_TRANSACTION_ID", -1);
        isEditMode = editId != -1;

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (isEditMode) {
                getSupportActionBar().setTitle("Edit Transaction");
            }
        }

        // ViewModel
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        setupCategorySpinner();
        setupDatePicker();
        setupSaveButton();

        // Handle scan results
        String scannedMerchant = getIntent().getStringExtra("EXTRA_SCAN_MERCHANT");
        double scannedAmount = getIntent().getDoubleExtra("EXTRA_SCAN_AMOUNT", -1.0);
        if (scannedMerchant != null) {
            binding.etMerchant.setText(scannedMerchant);
        }
        if (scannedAmount > 0) {
            binding.etAmount.setText(String.valueOf(scannedAmount));
        }

        if (isEditMode) {
            loadTransactionData();
        }
    }

    private void loadTransactionData() {
        com.autoexpense.repository.TransactionRepository repo = new com.autoexpense.repository.TransactionRepository(getApplicationContext());
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            TransactionEntity t = repo.getTransactionById(editId);
            if (t != null) {
                runOnUiThread(() -> {
                    binding.etAmount.setText(String.valueOf(t.getAmount()));
                    binding.etMerchant.setText(t.getMerchantName());
                    if ("Debit".equals(t.getTransactionType())) {
                        binding.rbDebit.setChecked(true);
                    } else {
                        binding.rbCredit.setChecked(true);
                    }
                    binding.spinnerCategory.setText(t.getCategory(), false);
                    selectedDate.setTimeInMillis(t.getDate());
                    binding.btnPickDate.setText(dateFormat.format(selectedDate.getTime()));
                    binding.btnSave.setText("Update Transaction");
                });
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CategoryUtils.ALL_CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        // Show today's date initially
        binding.btnPickDate.setText(dateFormat.format(selectedDate.getTime()));

        binding.btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        binding.btnPickDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            // Don't allow future dates
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            if (!validateInputs())
                return;

            String amountStr = binding.etAmount.getText().toString().trim();
            String merchant = binding.etMerchant.getText().toString().trim();
            String type = binding.rbDebit.isChecked() ? "Debit" : "Credit";
            String category = binding.spinnerCategory.getText().toString();
            long date = selectedDate.getTimeInMillis();
            long now = System.currentTimeMillis();

            double amount = Double.parseDouble(amountStr);

            TransactionEntity transaction = new TransactionEntity(
                    amount, merchant, type, category, date, "Manual", now);

            if (isEditMode) {
                transaction.setId(editId);
                viewModel.insert(transaction); // Room @Insert(onConflict = OnConflictStrategy.REPLACE) will update
                Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.insert(transaction);
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private boolean validateInputs() {
        String amountStr = binding.etAmount.getText() != null
                ? binding.etAmount.getText().toString().trim()
                : "";
        String merchant = binding.etMerchant.getText() != null
                ? binding.etMerchant.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(amountStr)) {
            binding.etAmount.setError("Amount is required");
            binding.etAmount.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.etAmount.setError("Amount must be greater than 0");
                binding.etAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etAmount.setError("Invalid amount");
            binding.etAmount.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(merchant)) {
            binding.etMerchant.setError("Merchant name is required");
            binding.etMerchant.requestFocus();
            return false;
        }

        return true;
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
