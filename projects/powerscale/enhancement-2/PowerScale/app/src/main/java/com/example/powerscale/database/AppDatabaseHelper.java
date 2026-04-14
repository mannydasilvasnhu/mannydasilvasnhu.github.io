package com.example.powerscale.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * AppDatabaseHelper
 * SQLiteOpenHelper responsible for database creation and upgrades.
 *
 * This helper class is responsible for:
 * - Defining schema constants
 * - Creating database tables
 * - Upgrading the database when schema changes
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
    public static final String COL_WEIGHT_SORT_DATE = "entry_date_sort";
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
     * @param context - The application context used for database access.
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
                        + COL_WEIGHT_SORT_DATE + " TEXT NOT NULL, "
                        + COL_WEIGHT_VALUE + " REAL NOT NULL, "
                        + COL_WEIGHT_NOTES + " TEXT, "
                        + COL_WEIGHT_UPDATED_AT + " INTEGER NOT NULL, "
                        + "FOREIGN KEY(" + COL_WEIGHT_USERNAME + ") REFERENCES "
                        + TABLE_USERS + "(" + COL_USERNAME + "), "
                        + "UNIQUE(" + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_SORT_DATE + ")"
                        + ")";
        db.execSQL(createWeights);

        // Index used to support date-based retrieval
        String createWeightSortIndex =
                "CREATE INDEX idx_weights_user_sort_date ON "
                        + TABLE_WEIGHTS + " ("
                        + COL_WEIGHT_USERNAME + ", "
                        + COL_WEIGHT_SORT_DATE + " DESC"
                        + ")";
        db.execSQL(createWeightSortIndex);

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
}