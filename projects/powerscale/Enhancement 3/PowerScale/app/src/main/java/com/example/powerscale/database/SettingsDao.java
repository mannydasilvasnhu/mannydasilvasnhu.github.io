package com.example.powerscale.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * SettingsDao
 * Handles all database operations related to user settings.
 *
 * This class is responsible for:
 * - Inserting default settings for a new user
 * - Reading SMS enabled state for a user
 * - Updating SMS enabled state for a user
 * - Reading the stored SMS phone number for a user
 * - Updating the stored SMS phone number for a user
 */
public class SettingsDao {

    // Shared database helper used for SQLite access
    private final AppDatabaseHelper dbHelper;

    /**
     * SettingsDao(Context context)
     * Initializes the DAO with database access.
     *
     * @param context - The application context used for database access.
     */
    public SettingsDao(Context context) {
        this.dbHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * insertDefaultSettings(String username)
     * Inserts a default settings row for a new user.
     *
     * @param username - The username to create default settings for.
     */
    public void insertDefaultSettings(String username) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_SETTINGS_USERNAME, username);
        values.put(AppDatabaseHelper.COL_SETTINGS_SMS_ENABLED, 0);
        values.putNull(AppDatabaseHelper.COL_SETTINGS_SMS_PHONE);

        db.insertWithOnConflict(AppDatabaseHelper.TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * isSmsEnabled(String username)
     * Reads the SMS enabled setting for a specific user.
     *
     * @param username - The username to query.
     * @return boolean - True if SMS notifications are enabled for this user.
     */
    public boolean isSmsEnabled(String username) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_SETTINGS,
                new String[]{AppDatabaseHelper.COL_SETTINGS_SMS_ENABLED},
                AppDatabaseHelper.COL_SETTINGS_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) {
                return false;
            }

            return cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_SETTINGS_SMS_ENABLED)) == 1;
        }
    }

    /**
     * setSmsEnabled(String username, boolean enabled)
     * Updates the SMS enabled setting for a specific user.
     * If no settings row exists for the user, one is created first.
     *
     * @param username - The username to update.
     * @param enabled - True to enable SMS notifications.
     */
    public void setSmsEnabled(String username, boolean enabled) {

        // Ensure a settings row exists before updating
        insertDefaultSettings(username);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_SETTINGS_SMS_ENABLED, enabled ? 1 : 0);

        db.update(
                AppDatabaseHelper.TABLE_SETTINGS,
                values,
                AppDatabaseHelper.COL_SETTINGS_USERNAME + " = ?",
                new String[]{username}
        );
    }

    /**
     * getSmsPhone(String username)
     * Reads the stored SMS phone number for a specific user.
     *
     * @param username - The username to query.
     * @return String - The stored phone number, or an empty string if none exists.
     */
    public String getSmsPhone(String username) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_SETTINGS,
                new String[]{AppDatabaseHelper.COL_SETTINGS_SMS_PHONE},
                AppDatabaseHelper.COL_SETTINGS_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) {
                return "";
            }

            int colIndex = cursor.getColumnIndexOrThrow(
                    AppDatabaseHelper.COL_SETTINGS_SMS_PHONE);

            if (cursor.isNull(colIndex)) {
                return "";
            }

            return cursor.getString(colIndex);
        }
    }

    /**
     * setSmsPhone(String username, String phone)
     * Updates the stored SMS phone number for a specific user.
     * If no settings row exists for the user, one is created first.
     *
     * @param username - The username to update.
     * @param phone - The phone number to store.
     */
    public void setSmsPhone(String username, String phone) {

        // Ensure a settings row exists before updating
        insertDefaultSettings(username);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_SETTINGS_SMS_PHONE, phone);

        db.update(
                AppDatabaseHelper.TABLE_SETTINGS,
                values,
                AppDatabaseHelper.COL_SETTINGS_USERNAME + " = ?",
                new String[]{username}
        );
    }
}
