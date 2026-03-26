package com.autoexpense.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.R;
import com.autoexpense.databinding.ActivityTransactionListBinding;
import com.autoexpense.ui.adapters.TransactionAdapter;
import com.autoexpense.utils.CategoryUtils;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;

import android.text.TextUtils;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Displays all transactions in a RecyclerView.
 * Supports search by merchant name and filtering by type and category.
 * Swipe-to-delete is enabled.
 */
public class TransactionListActivity extends AppCompatActivity {

    private ActivityTransactionListBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    // Current filter state
    private String currentType = "All";
    private String currentCategory = "All";
    private String currentSearch = "";
    private String currentDateRange = "All";

    private final List<TransactionEntity> latestTransactions = new ArrayList<>();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ViewModel
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupDateRangeAndClear();
        setupSearch();
        observeAllTransactionsOnce();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionEntity transaction) {
                showTransactionDetailsBottomSheet(transaction);
            }

            @Override
            public void onSplitClick(TransactionEntity transaction) {
                Intent intent = new Intent(TransactionListActivity.this, SplitBillActivity.class);
                intent.putExtra("EXTRA_TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Swipe-to-delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView rv,
                    @androidx.annotation.NonNull RecyclerView.ViewHolder vh,
                    @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TransactionEntity t = adapter.getTransactionAt(position);
                viewModel.delete(t);
                Toast.makeText(TransactionListActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerView);
    }

    private void setupFilters() {
        // Type dropdown (M3 ExposedDropdownMenu / AutoCompleteTextView)
        List<String> types = new ArrayList<>();
        types.add("All");
        types.add("Debit");
        types.add("Credit");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, types);
        binding.spinnerType.setAdapter(typeAdapter);
        binding.spinnerType.setText(types.get(0), false);
        binding.spinnerType.setOnItemClickListener((parent, view, position, id) -> {
            currentType = types.get(position);
            applyFilters();
        });

        // Category Chips
        setupCategoryChips();
    }

    private void setupCategoryChips() {
        binding.chipGroupCategoryFilter.removeAllViews();

        // "All" chip
        Chip allChip = buildFilterChip("All");
        allChip.setId(View.generateViewId());
        allChip.setChecked(true);
        binding.chipGroupCategoryFilter.addView(allChip);

        for (String cat : CategoryUtils.ALL_CATEGORIES) {
            Chip chip = buildFilterChip(cat);
            chip.setId(View.generateViewId());
            binding.chipGroupCategoryFilter.addView(chip);
        }

        binding.chipGroupCategoryFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip selected = findViewById(checkedIds.get(0));
                currentCategory = selected.getText().toString();
            }
            applyFilters();
        });
    }

    private Chip buildFilterChip(String label) {
        Chip chip = new Chip(this, null, com.google.android.material.R.attr.chipStyle);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setFocusable(true);
        return chip;
    }

    private void setupSearch() {
        // searchView is now a TextInputEditText — use TextWatcher
        binding.searchView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s != null ? s.toString() : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observeAllTransactionsOnce() {
        viewModel.getAllTransactions().observe(this, transactions -> {
            latestTransactions.clear();
            if (transactions != null) {
                latestTransactions.addAll(transactions);
            }
            applyFilters();
        });
    }

    /**
     * Apply all active filters (type, category, search) to the transaction list.
     */
    private void applyFilters() {
        // In-memory filtering to avoid stacking LiveData observers.
        List<TransactionEntity> filtered = new ArrayList<>();

        long[] range = getStartEndForCurrentDateRange();
        boolean hasDateRange = range != null;
        long start = hasDateRange ? range[0] : 0;
        long end = hasDateRange ? range[1] : 0;

        for (TransactionEntity t : latestTransactions) {
            boolean typeMatch = "All".equals(currentType) || currentType.equals(t.getTransactionType());
            boolean catMatch = "All".equals(currentCategory) || currentCategory.equals(t.getCategory());

            boolean searchMatch;
            if (TextUtils.isEmpty(currentSearch)) {
                searchMatch = true;
            } else {
                String merchant = t.getMerchantName();
                String category = t.getCategory();
                String query = currentSearch.toLowerCase(Locale.getDefault());
                searchMatch = (merchant != null && merchant.toLowerCase(Locale.getDefault()).contains(query))
                        || (category != null && category.toLowerCase(Locale.getDefault()).contains(query));
            }

            boolean dateMatch = true;
            if (hasDateRange) {
                dateMatch = t.getDate() >= start && t.getDate() <= end;
            }

            if (typeMatch && catMatch && searchMatch && dateMatch) {
                filtered.add(t);
            }
        }

        adapter.submitList(filtered);
        binding.tvTransactionCount.setText(filtered.size() + " transactions");

        updateActiveFilterChips();

        // Show/hide empty state
        if (filtered.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void setupDateRangeAndClear() {
        // Date range dropdown (M3 ExposedDropdownMenu)
        List<String> ranges = new ArrayList<>();
        ranges.add("All");
        ranges.add("Today");
        ranges.add("Last 7 Days");
        ranges.add("Last 30 Days");
        ranges.add("This Month");
        ranges.add("This Year");

        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, ranges);
        binding.spinnerDateRange.setAdapter(rangeAdapter);
        binding.spinnerDateRange.setText(ranges.get(0), false);

        binding.spinnerDateRange.setOnItemClickListener((parent, view, position, id) -> {
            currentDateRange = ranges.get(position);
            applyFilters();
        });

        // Clear filters
        binding.btnClearFilters.setOnClickListener(v -> {
            currentType = "All";
            currentCategory = "All";
            currentSearch = "";
            currentDateRange = "All";

            // Reset M3 dropdown text fields
            binding.searchView.setText("");
            binding.searchView.clearFocus();
            binding.spinnerType.setText("All", false);
            binding.chipGroupCategoryFilter.clearCheck();
            binding.spinnerDateRange.setText("All", false);

            applyFilters();
        });

        updateActiveFilterChips();
    }

    private void updateActiveFilterChips() {
        ChipGroup chipGroup = binding.chipGroupActiveFilters;
        chipGroup.removeAllViews();

        if (!"All".equals(currentType)) {
            chipGroup.addView(buildChip("Type: " + currentType));
        }
        if (!"All".equals(currentCategory)) {
            chipGroup.addView(buildChip("Category: " + currentCategory));
        }
        if (!"All".equals(currentDateRange)) {
            chipGroup.addView(buildChip("Range: " + currentDateRange));
        }
        if (!TextUtils.isEmpty(currentSearch)) {
            chipGroup.addView(buildChip("Search: " + currentSearch));
        }

        if (chipGroup.getChildCount() == 0) {
            chipGroup.setVisibility(View.GONE);
        } else {
            chipGroup.setVisibility(View.VISIBLE);
        }
    }

    private Chip buildChip(String label) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCloseIconVisible(false);
        chip.setClickable(false);
        chip.setFocusable(false);
        return chip;
    }

    private long[] getStartEndForCurrentDateRange() {
        if ("All".equals(currentDateRange)) {
            return null;
        }

        long now = System.currentTimeMillis();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);

        switch (currentDateRange) {
            case "Today": {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long start = cal.getTimeInMillis();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                long end = cal.getTimeInMillis();
                return new long[] { start, end };
            }
            case "Last 7 Days": {
                long start = now - (7L * 24L * 60L * 60L * 1000L);
                return new long[] { start, now };
            }
            case "Last 30 Days": {
                long start = now - (30L * 24L * 60L * 60L * 1000L);
                return new long[] { start, now };
            }
            case "This Month": {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long start = cal.getTimeInMillis();
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                long end = cal.getTimeInMillis();
                return new long[] { start, end };
            }
            case "This Year": {
                cal.set(java.util.Calendar.MONTH, 0);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long start = cal.getTimeInMillis();
                cal.set(java.util.Calendar.MONTH, 11);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 31);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                long end = cal.getTimeInMillis();
                return new long[] { start, end };
            }
            default:
                return null;
        }
    }

    private void showTransactionDetailsBottomSheet(TransactionEntity transaction) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(
                R.layout.bottom_sheet_transaction_details, null, false);

        android.widget.TextView tvMerchant = view.findViewById(R.id.tvDetailMerchantName);
        android.widget.TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        android.widget.TextView tvPaymentMethod = view.findViewById(R.id.tvDetailPaymentMethod);
        android.widget.TextView tvDate = view.findViewById(R.id.tvDetailDate);
        android.widget.TextView tvType = view.findViewById(R.id.tvDetailType);
        android.widget.TextView tvAmount = view.findViewById(R.id.tvDetailAmount);

        boolean isDebit = "Debit".equals(transaction.getTransactionType());
        int typeColor = isDebit ? com.autoexpense.R.color.debit_red : com.autoexpense.R.color.credit_green;

        tvMerchant.setText(transaction.getMerchantName());
        tvCategory.setText(transaction.getCategory());
        tvPaymentMethod.setText(transaction.getPaymentMethod());
        tvDate.setText(DATE_FORMAT.format(new Date(transaction.getDate())));
        tvType.setText(transaction.getTransactionType());

        String sign = isDebit ? "- " : "+ ";
        tvAmount.setText(sign + String.format(Locale.getDefault(), "₹%.2f", transaction.getAmount()));
        tvAmount.setTextColor(ContextCompat.getColor(this, typeColor));

        view.findViewById(R.id.btnEditTransaction).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(TransactionListActivity.this, AddTransactionActivity.class);
            intent.putExtra("EXTRA_TRANSACTION_ID", transaction.getId());
            startActivity(intent);
        });

        dialog.setContentView(view);
        dialog.show();
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
