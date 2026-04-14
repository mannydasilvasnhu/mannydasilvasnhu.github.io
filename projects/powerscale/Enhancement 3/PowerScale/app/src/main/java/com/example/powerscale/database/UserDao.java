package com.example.powerscale.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.powerscale.utils.PasswordUtils;

/**
 * UserDao
 * Handles all database operations related to user accounts and authentication.
 *
 * This class is responsible for:
 * - Checking whether a user exists
 * - Creating a new user account
 * - Validating login credentials
 */
public class UserDao {

    // Shared database helper used for SQLite access
    private final AppDatabaseHelper dbHelper;

    /**
     * UserDao(Context context)
     * Initializes the DAO with database access.
     *
     * @param context - The application context used to access the database.
     */
    public UserDao(Context context) {
        this.dbHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * userExists(String username)
     * Checks whether a username already exists in the Users table.
     *
     * @param username - The username to check.
     * @return boolean - True if the username exists.
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_USERS,
                new String[]{AppDatabaseHelper.COL_USER_ID},
                AppDatabaseHelper.COL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    /**
     * createUser(String username, String plainPassword)
     * Creates a new user account and stores a hashed password.
     *
     * @param username - The username to create.
     * @param plainPassword - The plain text password to hash.
     * @return boolean - True if user was created successfully.
     */
    public boolean createUser(String username, String plainPassword) {

        if (userExists(username)) {
            return false;
        }

        // Generate a unique salt and hash the password before storing it
        String salt = PasswordUtils.generateSaltBase64();
        String hash = PasswordUtils.hashPasswordBase64(plainPassword, salt);

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_USERNAME, username);
        values.put(AppDatabaseHelper.COL_PASS_SALT, salt);
        values.put(AppDatabaseHelper.COL_PASS_HASH, hash);
        values.put(AppDatabaseHelper.COL_CREATED_AT, System.currentTimeMillis());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(AppDatabaseHelper.TABLE_USERS, null, values);

        return rowId != -1;
    }

    /**
     * checkLogin(String username, String plainPassword)
     * Validates a login attempt by comparing hashed passwords.
     *
     * @param username - The username entered.
     * @param plainPassword - The password entered.
     * @return boolean - True if login credentials are valid.
     */
    public boolean checkLogin(String username, String plainPassword) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_USERS,
                new String[]{
                        AppDatabaseHelper.COL_PASS_SALT,
                        AppDatabaseHelper.COL_PASS_HASH
                },
                AppDatabaseHelper.COL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {

            if (!cursor.moveToFirst()) {
                return false;
            }

            String salt = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PASS_SALT));
            String expectedHash = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PASS_HASH));

            // Verify the entered password against the stored salted hash
            return PasswordUtils.verifyPassword(plainPassword, salt, expectedHash);
        }
    }
}