package com.autoexpense.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.autoexpense.R;
import com.autoexpense.data.entity.TransactionEntity;
import com.autoexpense.databinding.ItemTransactionBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying TransactionEntity items.
 * Uses ListAdapter with DiffUtil for efficient updates.
 *
 * Features:
 * - Color-coded type indicator bar (red=Debit, green=Credit)
 * - Formatted amount with ₹ symbol
 * - Human-readable date
 * - Debit/Credit badge with background color
 */
public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a",
            Locale.getDefault());

    private final OnTransactionClickListener clickListener;

    /** Callback interface for item click events */
    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionEntity transaction);
        void onSplitClick(TransactionEntity transaction);
    }

    // ─── DiffUtil Callback ───────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<TransactionEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionEntity oldItem,
                @NonNull TransactionEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionEntity oldItem,
                @NonNull TransactionEntity newItem) {
            return oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getMerchantName().equals(newItem.getMerchantName())
                    && oldItem.getTransactionType().equals(newItem.getTransactionType())
                    && oldItem.getCategory().equals(newItem.getCategory())
                    && oldItem.getPaymentMethod().equals(newItem.getPaymentMethod())
                    && oldItem.getDate() == newItem.getDate();
        }
    };

    public TransactionAdapter() {
        this(null);
    }

    public TransactionAdapter(OnTransactionClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Get the transaction at a given position (used for swipe-to-delete).
     */
    public TransactionEntity getTransactionAt(int position) {
        return getItem(position);
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────────

    class TransactionViewHolder extends RecyclerView.ViewHolder {

        private final ItemTransactionBinding binding;

        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransactionEntity transaction) {
            Context ctx = binding.getRoot().getContext();

            // Merchant name
            binding.tvMerchantName.setText(transaction.getMerchantName());

            // Category
            binding.tvCategory.setText(transaction.getCategory());

            // Payment method/source
            binding.tvPaymentMethod.setText(transaction.getPaymentMethod());

            // Date
            binding.tvDate.setText(DATE_FORMAT.format(new Date(transaction.getDate())));

            // Amount
            boolean isDebit = "Debit".equals(transaction.getTransactionType());
            String sign = isDebit ? "- " : "+ ";
            binding.tvAmount.setText(
                    sign + String.format(Locale.getDefault(), "₹%.2f", transaction.getAmount()));

            // Type badge and color
            int typeColor = isDebit
                    ? ContextCompat.getColor(ctx, R.color.debit_red)
                    : ContextCompat.getColor(ctx, R.color.credit_green);

            int backgroundColor = isDebit
                    ? ContextCompat.getColor(ctx, R.color.card_debit)
                    : ContextCompat.getColor(ctx, R.color.card_credit);

            binding.tvType.setText(transaction.getTransactionType());
            binding.tvType.getBackground().setTint(typeColor);
            binding.tvAmount.setTextColor(typeColor);
            binding.viewTypeIndicator.setBackgroundColor(typeColor);
            
            // Set card background for a premium feel
            binding.cardTransaction.setCardBackgroundColor(backgroundColor);

            // Split button logic
            if (isDebit && !"Income".equals(transaction.getCategory())) {
                binding.btnSplit.setVisibility(android.view.View.VISIBLE);
                binding.btnSplit.setOnClickListener(v -> {
                    if (clickListener != null) clickListener.onSplitClick(transaction);
                });
            } else {
                binding.btnSplit.setVisibility(android.view.View.GONE);
            }

            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null)
                    clickListener.onTransactionClick(transaction);
            });
        }
    }
}
