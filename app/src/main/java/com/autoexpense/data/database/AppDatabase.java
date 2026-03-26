package com.autoexpense.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.autoexpense.data.dao.TransactionDao;
import com.autoexpense.data.entity.TransactionEntity;

/**
 * Singleton Room database class.
 * Provides access to the TransactionDao.
 *
 * Usage:
 * AppDatabase db = AppDatabase.getInstance(context);
 * TransactionDao dao = db.transactionDao();
 */
@Database(entities = { TransactionEntity.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    /** Database file name stored on device */
    private static final String DATABASE_NAME = "autoexpense_db";

    /**
     * Returns the singleton AppDatabase instance.
     * Thread-safe via double-checked locking.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration() // For development; use migrations in production
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /** Abstract method to get the DAO */
    public abstract TransactionDao transactionDao();
}
