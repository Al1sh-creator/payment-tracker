package com.autoexpense.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.autoexpense.R;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ActivityMainBinding;
import com.autoexpense.utils.CategoryUtils;
import com.autoexpense.viewmodel.TransactionViewModel;
import com.autoexpense.viewmodel.TransactionViewModelFactory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main Dashboard Activity.
 * Shows monthly expense/income/balance summary cards and MPAndroidChart charts.
 */
public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private androidx.activity.result.ActivityResultLauncher<Intent> scannerLauncher;
    private TransactionViewModel viewModel;
    private final List<TransactionEntity> latestTransactions = new ArrayList<>();

    // Dashboard range selector
    private String currentDashboardRange = "This Month";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Scanner Result Launcher
        scannerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String merchant = result.getData().getStringExtra("SCAN_RESULT_MERCHANT");
                        double amount = result.getData().getDoubleExtra("SCAN_RESULT_AMOUNT", 0.0);

                        Intent intent = new Intent(this, AddTransactionActivity.class);
                        intent.putExtra("EXTRA_SCAN_MERCHANT", merchant);
                        intent.putExtra("EXTRA_SCAN_AMOUNT", amount);
                        startActivity(intent);
                    }
                }
        );

        setSupportActionBar(binding.toolbar);

        // Initialize ViewModel
        TransactionViewModelFactory factory = new TransactionViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        // Request SMS permissions
        requestSmsPermissions();

        // Setup FAB
        binding.fabAddTransaction
                .setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));

        // Setup Subscriptions Navigation
        binding.btnViewSubscriptions
                .setOnClickListener(v -> startActivity(new Intent(this, SubscriptionsActivity.class)));

        // Setup Scanner Navigation
        binding.fabScanReceipt
                .setOnClickListener(v -> scannerLauncher.launch(new Intent(this, ScannerActivity.class)));

        // Observe data
        observeViewModel();

        setupDashboardRangeSelector();
    }

    private void observeViewModel() {
        // Single observer: render everything from the currently selected date range.
        viewModel.getAllTransactions().observe(this, transactions -> {
            latestTransactions.clear();
            if (transactions != null) {
                latestTransactions.addAll(transactions);
            }
            renderDashboard();
        });
    }

    private void setupDashboardRangeSelector() {
        List<String> ranges = new ArrayList<>();
        ranges.add("This Month");
        ranges.add("Last 30 Days");
        ranges.add("This Year");
        ranges.add("All");

        // M3 ExposedDropdownMenu uses ArrayAdapter with simple_list_item_1
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, ranges);
        binding.spinnerDashboardRange.setAdapter(adapter);
        binding.spinnerDashboardRange.setText(ranges.get(0), false);

        binding.spinnerDashboardRange.setOnItemClickListener((parent, view, position, id) -> {
            currentDashboardRange = ranges.get(position);
            renderDashboard();
        });
    }

    private void renderDashboard() {
        long[] range = getStartEndForCurrentDashboardRange();
        boolean hasRange = range != null;
        long start = hasRange ? range[0] : 0;
        long end = hasRange ? range[1] : 0;

        List<TransactionEntity> filtered = new ArrayList<>();
        double debitTotal = 0;
        double creditTotal = 0;

        for (TransactionEntity t : latestTransactions) {
            // Apply date filter
            if (hasRange && (t.getDate() < start || t.getDate() > end)) {
                continue;
            }
            
            filtered.add(t);

            // Case-insensitive check to be safe with any legacy or SMS-captured data
            String type = t.getTransactionType();
            if (type != null) {
                if (type.equalsIgnoreCase("Debit")) {
                    debitTotal += t.getAmount();
                } else if (type.equalsIgnoreCase("Credit")) {
                    creditTotal += t.getAmount();
                }
            }
        }

        // Update box labels based on selected range
        String prefix = currentDashboardRange.equals("All") ? "Total" : 
                       (currentDashboardRange.equals("This Year") ? "Yearly" : "Monthly");
        
        binding.tvExpenseLabel.setText(prefix + " Expense");
        binding.tvIncomeLabel.setText(prefix + " Income");
        binding.tvBalanceLabel.setText(currentDashboardRange.equals("All") ? "Total Balance" : "Net Balance (" + prefix + ")");

        binding.tvMonthlyExpense.setText(String.format(Locale.getDefault(), "₹%.2f", debitTotal));
        binding.tvMonthlyIncome.setText(String.format(Locale.getDefault(), "₹%.2f", creditTotal));

        // Use total balance for the main card, or filtered balance? 
        // Let's stick to filtered balance to match the expense/income boxes, but label it better.
        double balance = creditTotal - debitTotal;
        binding.tvBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));
        
        // Color balance based on positivity
        int colorRes = balance >= 0 ? com.google.android.material.R.attr.colorTertiary : com.google.android.material.R.attr.colorError;
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(colorRes, typedValue, true);
        binding.tvBalance.setTextColor(typedValue.data);

        setupPieChart(filtered);
        setupBarChart(filtered);
        renderBudget(debitTotal);
        calculateWeeklyInsights();
        calculatePersonality(filtered);
    }

    private void calculatePersonality(List<TransactionEntity> filtered) {
        com.autoexpense.utils.SpendingPersonality.PersonalityResult result = 
            com.autoexpense.utils.SpendingPersonality.compute(filtered);
        
        binding.tvPersonalityEmoji.setText(result.emoji);
        binding.tvPersonalityName.setText(result.name);
        binding.tvPersonalityDesc.setText(result.description);
    }

    private void calculateWeeklyInsights() {
        // Run on background thread via ViewModel executor or simple calculation from latestTransactions
        long now = System.currentTimeMillis();
        long currentWeekStart = now - (7L * 24L * 60L * 60L * 1000L);
        long lastWeekStart = currentWeekStart - (7L * 24L * 60L * 60L * 1000L);

        double thisWeekTotal = 0;
        double lastWeekTotal = 0;

        for (TransactionEntity t : latestTransactions) {
            if ("Debit".equals(t.getTransactionType())) {
                if (t.getDate() >= currentWeekStart && t.getDate() <= now) {
                    thisWeekTotal += t.getAmount();
                } else if (t.getDate() >= lastWeekStart && t.getDate() < currentWeekStart) {
                    lastWeekTotal += t.getAmount();
                }
            }
        }

        if (latestTransactions.isEmpty()) {
            binding.tvWeeklyInsight.setText("Start adding transactions to see patterns.");
            return;
        }

        if (lastWeekTotal == 0) {
            binding.tvWeeklyInsight.setText(String.format(Locale.getDefault(), 
                "You've spent ₹%.2f this week. Keep tracking to see comparisons next week!", thisWeekTotal));
            return;
        }

        double diff = thisWeekTotal - lastWeekTotal;
        double percent = (Math.abs(diff) / lastWeekTotal) * 100;
        String direction = diff > 0 ? "higher" : "lower";
        String emoji = diff > 0 ? "📈" : "📉";

        binding.tvWeeklyInsight.setText(String.format(Locale.getDefault(),
                "%s Your spending hit ₹%.2f this week, which is %.0f%% %s than last week.",
                emoji, thisWeekTotal, percent, direction));
    }

    private int[] getCategoryColors() {
        return new int[] {
                ContextCompat.getColor(this, R.color.chart_food),
                ContextCompat.getColor(this, R.color.chart_travel),
                ContextCompat.getColor(this, R.color.chart_shopping),
                ContextCompat.getColor(this, R.color.chart_utilities),
                ContextCompat.getColor(this, R.color.chart_entertainment),
                ContextCompat.getColor(this, R.color.chart_health),
                ContextCompat.getColor(this, R.color.chart_education),
                ContextCompat.getColor(this, R.color.chart_misc)
        };
    }

    private void renderBudget(double debitTotal) {
        android.content.SharedPreferences prefs = getSharedPreferences(com.autoexpense.PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
        float budget = prefs.getFloat(com.autoexpense.PreferenceKeys.KEY_MONTHLY_BUDGET, 0f);

        if (budget > 0) {
            binding.budgetProgress.setVisibility(android.view.View.VISIBLE);
            binding.tvRemainingBudget.setVisibility(android.view.View.VISIBLE);

            int percent = (int) ((debitTotal / budget) * 100);
            binding.budgetProgress.setProgress(Math.min(percent, 100));

            double remaining = budget - debitTotal;
            if (remaining >= 0) {
                binding.tvRemainingBudget.setText(String.format(Locale.getDefault(), "₹%.2f remaining", remaining));
            } else {
                binding.tvRemainingBudget.setText(String.format(Locale.getDefault(), "₹%.2f over budget", Math.abs(remaining)));
            }
            
            // Check and fire budget alerts
            com.autoexpense.utils.BudgetAlertHelper.checkAndFireBudgetAlert(this, debitTotal, budget);
        } else {
            binding.budgetProgress.setVisibility(android.view.View.GONE);
            binding.tvRemainingBudget.setVisibility(android.view.View.GONE);
        }
    }

    private long[] getStartEndForCurrentDashboardRange() {
        if ("All".equals(currentDashboardRange)) {
            return null;
        }

        long now = System.currentTimeMillis();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);

        if ("Last 30 Days".equals(currentDashboardRange)) {
            long start = now - (30L * 24L * 60L * 60L * 1000L);
            return new long[] { start, now };
        }

        if ("This Year".equals(currentDashboardRange)) {
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

        // Default: This Month
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

    // ─── Pie Chart ───────────────────────────────────────────────────────────────

    private void setupPieChart(List<TransactionEntity> transactions) {
        PieChart chart = binding.pieChart;

        // Aggregate debit amounts by category
        Map<String, Float> categoryTotals = new HashMap<>();
        for (TransactionEntity t : transactions) {
            if ("Debit".equals(t.getTransactionType())) {
                String cat = t.getCategory();
                categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0f) + (float) t.getAmount());
            }
        }

        if (categoryTotals.isEmpty()) {
            chart.setNoDataText("No expense data yet");
            chart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getCategoryColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(40f);
        chart.setTransparentCircleRadius(45f);
        chart.setCenterText("Expenses");
        chart.setCenterTextSize(14f);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(10f);
        chart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chart.animateY(800);
        chart.invalidate();
    }

    // ─── Bar Chart ───────────────────────────────────────────────────────────────

    private void setupBarChart(List<TransactionEntity> transactions) {
        BarChart chart = binding.barChart;

        // Simple: last 6 months debit totals (index 0 = oldest)
        // For demo, we use category totals as bars
        Map<String, Float> categoryTotals = new HashMap<>();
        for (TransactionEntity t : transactions) {
            if ("Debit".equals(t.getTransactionType())) {
                String cat = t.getCategory();
                categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0f) + (float) t.getAmount());
            }
        }

        if (categoryTotals.isEmpty()) {
            chart.setNoDataText("No expense data yet");
            chart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Expenses by Category");
        dataSet.setColors(getCategoryColors());
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setFitBars(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    // ─── Permissions ─────────────────────────────────────────────────────────────

    private void requestSmsPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_SMS);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Permissions handled; SMS receiver will check at runtime
    }

    // ─── Menu ────────────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_transactions) {
            startActivity(new Intent(this, TransactionListActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh monthly totals when returning from other screens
        viewModel.refreshMonthlyTotals();
    }
}
