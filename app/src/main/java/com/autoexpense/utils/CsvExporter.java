package com.autoexpense.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.autoexpense.data.entity.TransactionEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting transactions to a CSV file.
 * The file is saved to the public Downloads folder.
 *
 * NOTE: Must be called from a background thread.
 */
public class CsvExporter {

    private static final String CSV_HEADER = "ID,Amount,Merchant,Type,Category,Date,Payment Method,Created At\n";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
            Locale.getDefault());

    /**
     * Export a list of transactions to a CSV file in the Downloads directory.
     *
     * @param context      Application context
     * @param transactions List of transactions to export
     * @return The exported File, or null if export failed
     */
    public static File exportToCsv(Context context, List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty())
            return null;

        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "AutoExpense_" + timestamp + ".csv";

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDir, fileName);

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Write header
            writer.append(CSV_HEADER);

            // Write each transaction row
            for (TransactionEntity t : transactions) {
                writer.append(String.valueOf(t.getId())).append(",");
                writer.append(String.format(Locale.getDefault(), "%.2f", t.getAmount())).append(",");
                writer.append(escapeCsv(t.getMerchantName())).append(",");
                writer.append(escapeCsv(t.getTransactionType())).append(",");
                writer.append(escapeCsv(t.getCategory())).append(",");
                writer.append(DATE_FORMAT.format(new Date(t.getDate()))).append(",");
                writer.append(escapeCsv(t.getPaymentMethod())).append(",");
                writer.append(DATE_FORMAT.format(new Date(t.getCreatedAt()))).append("\n");
            }

            writer.flush();
            return csvFile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Escape a CSV field value: wrap in quotes if it contains commas or quotes.
     */
    private static String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
