package com.autoexpense.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ActivitySubscriptionsBinding;
import com.autoexpense.ui.adapters.TransactionAdapter;
import com.autoexpense.utils.CategoryUtils;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscription Tracker Screen.
 * Lists all transactions categorized as "Subscriptions" or detected as recurring.
 */
public class SubscriptionsActivity extends AppCompatActivity {

    private ActivitySubscriptionsBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize RecyclerView
        adapter = new TransactionAdapter();
        binding.rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubscriptions.setAdapter(adapter);

        // Initialize ViewModel
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        // Observe only Subscription category transactions
        viewModel.getTransactionsByCategory(CategoryUtils.SUBSCRIPTIONS).observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvSubscriptions.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvSubscriptions.setVisibility(View.VISIBLE);
                adapter.submitList(transactions);
            }
        });
    }
}
