package com.example.mannydasilvaweighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * AppDatabaseHelper
 * This class manages the local SQLite database for the PowerScale application.
 *
 * This class is responsible for:
 * - Creating the database tables
 * - Upgrading the database when schema changes
 * - Handling CRUD operations for users, weights, and the goal weight
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DB_NAME = "powerscale.db";
    private static final int DB_VERSION = 1;

    // =========================
    // Users Table
    // =========================
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASS_SALT = "password_salt";
    public static final String COL_PASS_HASH = "password_hash";
    public static final String COL_CREATED_AT = "created_at";

    // =========================
    // Weights Table
    // =========================
    public static final String TABLE_WEIGHTS = "weights";
    public static final String COL_WEIGHT_ID = "_id";
    public static final String COL_WEIGHT_USERNAME = "username";
    public static final String COL_WEIGHT_DATE = "entry_date";
    public static final String COL_WEIGHT_VALUE = "weight_lbs";
    public static final String COL_WEIGHT_NOTES = "notes";
    public static final String COL_WEIGHT_UPDATED_AT = "updated_at";

    // =========================
    // Goal Weight Table
    // =========================
    public static final String TABLE_GOAL = "goal_weight";
    public static final String COL_GOAL_USERNAME = "username";
    public static final String COL_GOAL_VALUE = "goal_lbs";

    /**
     * AppDatabaseHelper(Context context)
     * Constructor that initializes the SQLiteOpenHelper.
     *
     * @param context - The application context used to access the database.
     * @return void
     */
    public AppDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * onCreate(SQLiteDatabase db)
     * Called when the database is first created.
     * This method creates all required tables for the application.
     *
     * @param db - The SQLiteDatabase instance.
     * @return void
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Users table
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " ("
                        + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                        + COL_PASS_SALT + " TEXT NOT NULL, "
                        + COL_PASS_HASH + " TEXT NOT NULL, "
                        + COL_CREATED_AT + " INTEGER NOT NULL"
                        + ")";
        db.execSQL(createUsers);

        // Create Weights table
        String createWeights =
                "CREATE TABLE " + TABLE_WEIGHTS + " ("
                        + COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_WEIGHT_USERNAME + " TEXT NOT NULL, "
                        + COL_WEIGHT_DATE + " TEXT NOT NULL, "
                        + COL_WEIGHT_VALUE + " REAL NOT NULL, "
                        + COL_WEIGHT_NOTES + " TEXT, "
                        + COL_WEIGHT_UPDATED_AT + " INTEGER NOT NULL, "
                        + "FOREIGN KEY(" + COL_WEIGHT_USERNAME + ") REFERENCES "
                        + TABLE_USERS + "(" + COL_USERNAME + ")"
                        + ")";
        db.execSQL(createWeights);

        // Create Goal Weight table
        String createGoal =
                "CREATE TABLE " + TABLE_GOAL + " ("
                        + COL_GOAL_USERNAME + " TEXT PRIMARY KEY, "
                        + COL_GOAL_VALUE + " REAL NOT NULL, "
                        + "FOREIGN KEY(" + COL_GOAL_USERNAME + ") REFERENCES "
                        + TABLE_USERS + "(" + COL_USERNAME + ")"
                        + ")";
        db.execSQL(createGoal);
    }

    /**
     * onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     * Called when the database version is incremented.
     * Drops existing tables and recreates them.
     *
     * @param db - The SQLiteDatabase instance.
     * @param oldVersion - The previous database version.
     * @param newVersion - The new database version.
     * @return void
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate tables
        onCreate(db);
    }

    /**
     * userExists(String username)
     * Checks whether a username already exists in the Users table.
     *
     * @param username - The username to check.
     * @return boolean - True if the username exists, false otherwise.
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + " = ?",
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

        if (userExists(username)) return false;

        String salt = PasswordUtils.generateSaltBase64();
        String hash = PasswordUtils.hashPasswordBase64(plainPassword, salt);

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASS_SALT, salt);
        values.put(COL_PASS_HASH, hash);
        values.put(COL_CREATED_AT, System.currentTimeMillis());

        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert(TABLE_USERS, null, values);

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

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_PASS_SALT, COL_PASS_HASH},
                COL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {

            if (!cursor.moveToFirst()) return false;

            String salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASS_SALT));
            String expectedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASS_HASH));

            return PasswordUtils.verifyPassword(plainPassword, salt, expectedHash);
        }
    }

    /**
     * insertWeight(String username, String entryDate, double weightLbs, String notes)
     * Inserts a new weight entry for a specific user.
     *
     * @param username - The username associated with entry.
     * @param entryDate - The date of entry (MM/DD/YYYY).
     * @param weightLbs - The weight value in pounds.
     * @param notes - Any optional notes.
     * @return long - The row ID of inserted record.
     */
    public long insertWeight(String username, String entryDate, double weightLbs, String notes) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_USERNAME, username);
        values.put(COL_WEIGHT_DATE, entryDate);
        values.put(COL_WEIGHT_VALUE, weightLbs);
        values.put(COL_WEIGHT_NOTES, notes);
        values.put(COL_WEIGHT_UPDATED_AT, System.currentTimeMillis());

        return db.insert(TABLE_WEIGHTS, null, values);
    }

    /**
     * getAllWeightsForUser(String username)
     * Gets all weight entries for a specific user ordered by date descending.
     *
     * @param username - The username to query.
     * @return Cursor - The cursor containing weight entries.
     */
    public Cursor getAllWeightsForUser(String username) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_WEIGHTS,
                null,
                COL_WEIGHT_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                COL_WEIGHT_DATE + " DESC"
        );
    }

    /**
     * updateWeight(long id, String entryDate, double weightLbs, String notes)
     * Updates an existing weight entry.
     *
     * @param id - The ID of the weight record.
     * @param entryDate - The updated date.
     * @param weightLbs - The updated weight value.
     * @param notes - The updated notes.
     * @return boolean - True if update was successful.
     */
    public boolean updateWeight(long id, String entryDate, double weightLbs, String notes) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_DATE, entryDate);
        values.put(COL_WEIGHT_VALUE, weightLbs);
        values.put(COL_WEIGHT_NOTES, notes);
        values.put(COL_WEIGHT_UPDATED_AT, System.currentTimeMillis());

        int rows = db.update(
                TABLE_WEIGHTS,
                values,
                COL_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return rows > 0;
    }

    /**
     * deleteWeight(long id)
     * Deletes a weight entry from the database.
     *
     * @param id - The ID of the weight record.
     * @return boolean - True if deletion was successful.
     */
    public boolean deleteWeight(long id) {

        SQLiteDatabase db = getWritableDatabase();

        int rows = db.delete(
                TABLE_WEIGHTS,
                COL_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return rows > 0;
    }

    /**
     * updateOrInsertGoalWeight(String username, double goal)
     * Inserts or updates the goal weight for a user.
     *
     * @param username - The username associated with goal.
     * @param goal - The goal weight value.
     * @return boolean - True if operation was successful.
     */
    public boolean updateOrInsertGoalWeight(String username, double goal) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_GOAL_USERNAME, username);
        values.put(COL_GOAL_VALUE, goal);

        long result = db.insertWithOnConflict(
                TABLE_GOAL,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        return result != -1;
    }

    /**
     * getGoalWeight(String username)
     * Gets the stored goal weight for a user.
     *
     * @param username - The username to query.
     * @return Double - The goal weight or null if none exists.
     */
    public Double getGoalWeight(String username) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.query(
                TABLE_GOAL,
                new String[]{COL_GOAL_VALUE},
                COL_GOAL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (!c.moveToFirst()) return null;
            return c.getDouble(c.getColumnIndexOrThrow(COL_GOAL_VALUE));
        }
    }
}