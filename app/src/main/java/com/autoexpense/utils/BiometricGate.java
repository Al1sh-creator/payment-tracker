package com.autoexpense.utils;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/**
 * Thin wrapper around {@link BiometricPrompt}.
 * <p>
 * Usage:
 * <pre>
 *     BiometricGate.authenticate(this,
 *         () -> { /* auth ok — show UI *\/ },
 *         () -> { /* auth failed — finish() or show error *\/ });
 * </pre>
 */
public final class BiometricGate {

    private BiometricGate() {}

    /**
     * Attempts biometric (or device-credential) authentication.
     * Falls back silently (calls {@code onSuccess}) on devices that have no enrolled biometrics
     * so users without hardware are never locked out.
     *
     * @param activity  the host FragmentActivity
     * @param onSuccess Runnable called on successful auth (or when biometrics unavailable)
     * @param onFailure Runnable called when the user cancels or auth fails
     */
    public static void authenticate(@NonNull FragmentActivity activity,
                                    @NonNull Runnable onSuccess,
                                    @NonNull Runnable onFailure) {

        BiometricManager bm = BiometricManager.from(activity);
        int canAuth = bm.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // No biometrics or no enrolled credentials — skip gate
            onSuccess.run();
            return;
        }

        BiometricPrompt.AuthenticationCallback callback =
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        onSuccess.run();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        // User's biometric didn't match — prompt stays open, not a fatal error
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        // User pressed Cancel / Back
                        onFailure.run();
                    }
                };

        BiometricPrompt prompt = new BiometricPrompt(activity,
                ContextCompat.getMainExecutor(activity), callback);

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AutoExpense")
                .setSubtitle("Authenticate to access your financial data")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                                | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        prompt.authenticate(info);
    }
}
