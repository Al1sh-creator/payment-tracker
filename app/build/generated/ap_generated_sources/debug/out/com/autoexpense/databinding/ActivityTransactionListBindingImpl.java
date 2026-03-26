package com.autoexpense.databinding;
import com.autoexpense.R;
import com.autoexpense.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityTransactionListBindingImpl extends ActivityTransactionListBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.toolbar, 1);
        sViewsWithIds.put(R.id.searchView, 2);
        sViewsWithIds.put(R.id.spinnerType, 3);
        sViewsWithIds.put(R.id.chipGroupCategoryFilter, 4);
        sViewsWithIds.put(R.id.spinnerDateRange, 5);
        sViewsWithIds.put(R.id.btnClearFilters, 6);
        sViewsWithIds.put(R.id.chipGroupActiveFilters, 7);
        sViewsWithIds.put(R.id.tvTransactionCount, 8);
        sViewsWithIds.put(R.id.recyclerView, 9);
        sViewsWithIds.put(R.id.layoutEmpty, 10);
        sViewsWithIds.put(R.id.lottieEmpty, 11);
    }
    // views
    @NonNull
    private final android.widget.LinearLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityTransactionListBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 12, sIncludes, sViewsWithIds));
    }
    private ActivityTransactionListBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (com.google.android.material.button.MaterialButton) bindings[6]
            , (com.google.android.material.chip.ChipGroup) bindings[7]
            , (com.google.android.material.chip.ChipGroup) bindings[4]
            , (android.widget.LinearLayout) bindings[10]
            , (com.airbnb.lottie.LottieAnimationView) bindings[11]
            , (androidx.recyclerview.widget.RecyclerView) bindings[9]
            , (com.google.android.material.textfield.TextInputEditText) bindings[2]
            , (android.widget.AutoCompleteTextView) bindings[5]
            , (android.widget.AutoCompleteTextView) bindings[3]
            , (com.google.android.material.appbar.MaterialToolbar) bindings[1]
            , (android.widget.TextView) bindings[8]
            );
        this.mboundView0 = (android.widget.LinearLayout) bindings[0];
        this.mboundView0.setTag(null);
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