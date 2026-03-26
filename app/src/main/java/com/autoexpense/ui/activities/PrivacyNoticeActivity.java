package com.autoexpense.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.autoexpense.PreferenceKeys;
import com.autoexpense.databinding.ActivityPrivacyNoticeBinding;

/**
 * Shown on the very first app launch.
 * Displays the privacy policy and requires user acceptance before proceeding.
 * Once accepted, the flag is stored in SharedPreferences and this screen is
 * never shown again.
 */
public class PrivacyNoticeActivity extends AppCompatActivity {

    private ActivityPrivacyNoticeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if privacy notice was already accepted
        SharedPreferences prefs = getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(PreferenceKeys.KEY_PRIVACY_ACCEPTED, false)) {
            // Skip directly to main screen
            navigateToMain();
            return;
        }

        binding = ActivityPrivacyNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAccept.setOnClickListener(v -> {
            // Mark privacy as accepted
            prefs.edit().putBoolean(PreferenceKeys.KEY_PRIVACY_ACCEPTED, true).apply();
            navigateToMain();
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Remove this activity from back stack
    }
}
