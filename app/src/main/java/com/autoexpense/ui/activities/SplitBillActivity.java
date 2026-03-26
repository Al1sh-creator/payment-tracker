package com.autoexpense.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ActivitySplitBillBinding;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Split Bill Activity.
 * Allows users to divide an expense and generate a UPI payment intent.
 */
public class SplitBillActivity extends AppCompatActivity {

    private ActivitySplitBillBinding binding;
    private TransactionViewModel viewModel;
    private TransactionEntity transaction;
    private double currentSplitAmount = 0;
    
    private static final String PREFS_NAME = "AutoExpensePrefs";
    private static final String KEY_UPI_ID = "user_upi_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplitBillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int transactionId = getIntent().getIntExtra("EXTRA_TRANSACTION_ID", -1);
        if (transactionId == -1) {
            finish();
            return;
        }

        // Load saved UPI ID
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        binding.etUpiId.setText(prefs.getString(KEY_UPI_ID, ""));

        // Initialize ViewModel
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        // Fetch transaction details
        new Thread(() -> {
            transaction = viewModel.getTransactionById(transactionId);
            runOnUiThread(this::bindData);
        }).start();

        setupListeners();
    }

    private void bindData() {
        if (transaction == null) return;

        binding.tvMerchant.setText(transaction.getMerchantName());
        binding.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", transaction.getAmount()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        binding.tvDate.setText(sdf.format(new Date(transaction.getDate())));

        updateSplit();
    }

    private void setupListeners() {
        binding.sliderPeople.addOnChangeListener((slider, value, fromUser) -> {
            int people = (int) value;
            binding.tvPeopleCount.setText(people + " people");
            updateSplit();
        });

        binding.btnShareUpi.setOnClickListener(v -> shareUpiLink());
    }

    private void updateSplit() {
        if (transaction == null) return;
        int people = (int) binding.sliderPeople.getValue();
        currentSplitAmount = transaction.getAmount() / people;
        binding.tvSplitAmount.setText(String.format(Locale.getDefault(), "₹%.2f", currentSplitAmount));
    }

    private void shareUpiLink() {
        String upiId = binding.etUpiId.getText().toString().trim();
        if (upiId.isEmpty()) {
            Toast.makeText(this, "Please enter your UPI ID first", Toast.LENGTH_SHORT).show();
            binding.tilUpiId.setError("Required");
            return;
        }
        binding.tilUpiId.setError(null);

        // Save UPI ID for next time
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_UPI_ID, upiId).apply();

        // Build UPI Intent URI
        // Format: upi://pay?pa=address@upi&pn=Name&am=10.00&cu=INR&tn=Description
        String upiUri = String.format(Locale.getDefault(),
                "upi://pay?pa=%s&pn=AutoExpense User&am=%.2f&cu=INR&tn=Split for %s",
                upiId, currentSplitAmount, transaction.getMerchantName());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey, please pay your share for " + transaction.getMerchantName() + 
                " split using AutoExpense: " + upiUri);
        
        startActivity(Intent.createChooser(intent, "Share Payment Request"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
