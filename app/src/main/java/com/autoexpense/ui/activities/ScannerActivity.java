package com.autoexpense.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.autoexpense.databinding.ActivityScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scanner Activity.
 * Uses CameraX to capture a receipt and ML Kit to extract text.
 */
public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final int CAMERA_PERMISSION_CODE = 200;
    
    private ActivityScannerBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        binding.btnCapture.setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);

        File photoFile = new File(getExternalCacheDir(), "scan_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                processImage(Uri.fromFile(photoFile));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
            }
        });
    }

    private void processImage(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        extractInfo(visionText);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "OCR Failed", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCapture.setEnabled(true);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void extractInfo(Text visionText) {
        String fullText = visionText.getText();
        String merchant = "";
        double amount = 0;

        // Simple heuristic for merchant: Usually the first line or highest block
        if (!visionText.getTextBlocks().isEmpty()) {
            merchant = visionText.getTextBlocks().get(0).getText().split("\n")[0];
        }

        // Regex for amount: Look for numbers after ₹ or "Total" or at end of lines
        Pattern amountPattern = Pattern.compile("(?:total|grand total|total amount|payable|₹|rs|inr)\\s*[:\\-]?\\s*([0-9,]+\\.[0-9]{2}|[0-9,]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = amountPattern.matcher(fullText);
        
        // Find the last occurrence which is often the final total
        while (matcher.find()) {
            try {
                String match = matcher.group(1).replace(",", "");
                amount = Double.parseDouble(match);
            } catch (Exception ignored) {}
        }

        binding.progressBar.setVisibility(View.GONE);
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SCAN_RESULT_MERCHANT", merchant);
        resultIntent.putExtra("SCAN_RESULT_AMOUNT", amount);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
