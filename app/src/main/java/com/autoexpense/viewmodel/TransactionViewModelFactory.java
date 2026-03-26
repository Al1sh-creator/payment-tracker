package com.autoexpense.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory for creating TransactionViewModel with Application context.
 * Required because TransactionViewModel extends AndroidViewModel (needs
 * Application).
 *
 * Usage:
 * TransactionViewModelFactory factory = new
 * TransactionViewModelFactory(getApplication());
 * viewModel = new ViewModelProvider(this,
 * factory).get(TransactionViewModel.class);
 */
public class TransactionViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public TransactionViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TransactionViewModel.class)) {
            return (T) new TransactionViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
