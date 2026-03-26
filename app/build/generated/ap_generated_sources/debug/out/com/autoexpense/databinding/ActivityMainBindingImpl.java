package com.autoexpense.databinding;
import com.autoexpense.R;
import com.autoexpense.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityMainBindingImpl extends ActivityMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.appBarLayout, 1);
        sViewsWithIds.put(R.id.toolbar, 2);
        sViewsWithIds.put(R.id.nestedScrollView, 3);
        sViewsWithIds.put(R.id.spinnerDashboardRange, 4);
        sViewsWithIds.put(R.id.cardBudget, 5);
        sViewsWithIds.put(R.id.tvExpenseLabel, 6);
        sViewsWithIds.put(R.id.tvMonthlyExpense, 7);
        sViewsWithIds.put(R.id.budgetProgress, 8);
        sViewsWithIds.put(R.id.tvRemainingBudget, 9);
        sViewsWithIds.put(R.id.tvIncomeLabel, 10);
        sViewsWithIds.put(R.id.tvMonthlyIncome, 11);
        sViewsWithIds.put(R.id.cardTotalBalance, 12);
        sViewsWithIds.put(R.id.tvBalanceLabel, 13);
        sViewsWithIds.put(R.id.tvBalance, 14);
        sViewsWithIds.put(R.id.cardInsights, 15);
        sViewsWithIds.put(R.id.btnViewSubscriptions, 16);
        sViewsWithIds.put(R.id.tvWeeklyInsight, 17);
        sViewsWithIds.put(R.id.pieChart, 18);
        sViewsWithIds.put(R.id.barChart, 19);
        sViewsWithIds.put(R.id.fabScanReceipt, 20);
        sViewsWithIds.put(R.id.fabAddTransaction, 21);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 22, sIncludes, sViewsWithIds));
    }
    private ActivityMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (com.google.android.material.appbar.AppBarLayout) bindings[1]
            , (com.github.mikephil.charting.charts.BarChart) bindings[19]
            , (com.google.android.material.button.MaterialButton) bindings[16]
            , (com.google.android.material.progressindicator.LinearProgressIndicator) bindings[8]
            , (com.google.android.material.card.MaterialCardView) bindings[5]
            , (com.google.android.material.card.MaterialCardView) bindings[15]
            , (com.google.android.material.card.MaterialCardView) bindings[12]
            , (com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton) bindings[21]
            , (com.google.android.material.floatingactionbutton.FloatingActionButton) bindings[20]
            , (androidx.constraintlayout.motion.widget.MotionLayout) bindings[0]
            , (androidx.core.widget.NestedScrollView) bindings[3]
            , (com.github.mikephil.charting.charts.PieChart) bindings[18]
            , (android.widget.AutoCompleteTextView) bindings[4]
            , (com.google.android.material.appbar.MaterialToolbar) bindings[2]
            , (android.widget.TextView) bindings[14]
            , (android.widget.TextView) bindings[13]
            , (android.widget.TextView) bindings[6]
            , (android.widget.TextView) bindings[10]
            , (android.widget.TextView) bindings[7]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[9]
            , (android.widget.TextView) bindings[17]
            );
        this.motionLayout.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}