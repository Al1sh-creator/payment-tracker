# AutoExpense

AutoExpense is an Android payment tracking application designed to automatically detect and log UPI transactions from SMS and bank app notifications. It provides a secure, locally-stored way to manage your expenses without sharing data with third-party servers.

## Features
- **Auto SMS Detection**: Automatically detects UPI transactions from SMS.
- **Notification Capture**: Parses payment alerts from bank notifications when SMS is missing.
- **Smart Insights**: Tracks category breakdowns and recurring subscriptions.
- **Dark Mode Support**: Seamlessly switch between light and dark themes.
- **CSV Export**: Export all your transaction data to a CSV file.
- **100% Private**: All data is stored locally on the device using Room Database. No personal information is uploaded to servers.

## Tech Stack
- Android SDK (Java)
- Material Design 3
- Room Database for local storage
- Lifecycle Architecture Components (ViewModel, LiveData)
- MPAndroidChart for visual representations
- CameraX & ML Kit Text Recognition

## Permissions
The app strictly uses `READ_SMS` and `RECEIVE_SMS` manually granted permissions, and notification listener access solely for the purpose of detecting payment alerts securely and processing them locally.
