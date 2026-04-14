package com.example.powerscale.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * AppDatabaseHelper
 * SQLiteOpenHelper responsible for database creation, upgrades, and configuration.
 *
 * This helper class is responsible for:
 * - Defining schema constants
 * - Creating database tables with enforced foreign key relationships
 * - Creating indexes for query performance optimization
 * - Enforcing data integrity rules through unique constraints
 * - Upgrading the database safely using migration strategies that preserve user data
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    // Tag used for logging database operations
    private static final String TAG = "AppDatabaseHelper";

    // Database name
    private static final String DB_NAME = "powerscale.db";

    // Database version
    private static final int DB_VERSION = 2;

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

    // =========================
    // Settings Table
    // =========================
    public static final String TABLE_SETTINGS = "settings";
    public static final String COL_SETTINGS_USERNAME = "username";
    public static final String COL_SETTINGS_SMS_ENABLED = "sms_enabled";
    public static final String COL_SETTINGS_SMS_PHONE = "sms_phone";

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
     * onConfigure(SQLiteDatabase db)
     * Called when the database connection is being configured.
     * Enables SQLite foreign key constraint enforcement so that
     * invalid child rows cannot exist in the database.
     *
     * @param db - The SQLiteDatabase instance being configured.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        // Enable foreign key constraint enforcement
        db.setForeignKeyConstraintsEnabled(true);

        Log.d(TAG, "Foreign key enforcement enabled");
    }

    /**
     * onCreate(SQLiteDatabase db)
     * Called when the database is first created.
     * Creates all required tables with enforced foreign key relationships,
     * unique constraints for data integrity, and indexes for query performance.
     *
     * @param db - The SQLiteDatabase instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Users table with unique username
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " ("
                        + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                        + COL_PASS_SALT + " TEXT NOT NULL, "
                        + COL_PASS_HASH + " TEXT NOT NULL, "
                        + COL_CREATED_AT + " INTEGER NOT NULL"
                        + ")";
        db.execSQL(createUsers);

        // Create Weights table with FK to users(username) ON DELETE CASCADE
        // UNIQUE constraint prevents duplicate daily weight entries for the same user on the same date
        // This is being coupled with the existing UI validation for duplicate daily weight entries
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
                        + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE, "
                        + "UNIQUE(" + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_SORT_DATE + ")"
                        + ")";
        db.execSQL(createWeights);

        // Create Goal Weight table with FK to users(username) ON DELETE CASCADE
        String createGoal =
                "CREATE TABLE " + TABLE_GOAL + " ("
                        + COL_GOAL_USERNAME + " TEXT PRIMARY KEY, "
                        + COL_GOAL_VALUE + " REAL NOT NULL, "
                        + "FOREIGN KEY(" + COL_GOAL_USERNAME + ") REFERENCES "
                        + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                        + ")";
        db.execSQL(createGoal);

        // Create Settings table with FK to users(username) ON DELETE CASCADE
        String createSettings =
                "CREATE TABLE " + TABLE_SETTINGS + " ("
                        + COL_SETTINGS_USERNAME + " TEXT PRIMARY KEY, "
                        + COL_SETTINGS_SMS_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                        + COL_SETTINGS_SMS_PHONE + " TEXT, "
                        + "FOREIGN KEY(" + COL_SETTINGS_USERNAME + ") REFERENCES "
                        + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                        + ")";
        db.execSQL(createSettings);

        // Create indexes
        createIndexes(db);

        Log.d(TAG, "Database created with version " + DB_VERSION);
    }

    /**
     * createIndexes(SQLiteDatabase db)
     * Creates indexes on columns.
     *
     * @param db - The SQLiteDatabase instance.
     */
    private void createIndexes(SQLiteDatabase db) {

        String createWeightSortIndex =
                "CREATE INDEX IF NOT EXISTS idx_weights_user_sort_date ON "
                        + TABLE_WEIGHTS + " ("
                        + COL_WEIGHT_USERNAME + ", "
                        + COL_WEIGHT_SORT_DATE + " DESC"
                        + ")";
        db.execSQL(createWeightSortIndex);

        String createGoalIndex =
                "CREATE INDEX IF NOT EXISTS idx_goal_username ON "
                        + TABLE_GOAL + " ("
                        + COL_GOAL_USERNAME
                        + ")";
        db.execSQL(createGoalIndex);

        String createSettingsIndex =
                "CREATE INDEX IF NOT EXISTS idx_settings_username ON "
                        + TABLE_SETTINGS + " ("
                        + COL_SETTINGS_USERNAME
                        + ")";
        db.execSQL(createSettingsIndex);

        Log.d(TAG, "Indexes created for weights, goal, and settings tables");
    }

    /**
     * onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     * Called when the database version is incremented.
     *
     * @param db - The SQLiteDatabase instance.
     * @param oldVersion - The previous database version.
     * @param newVersion - The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            migrateToVersion2(db);
        }
    }

    /**
     * migrateToVersion2(SQLiteDatabase db)
     * Performs the version 1 to version 2 migration.
     *
     * @param db - The SQLiteDatabase instance.
     */
    private void migrateToVersion2(SQLiteDatabase db) {

        Log.d(TAG, "Starting migration to version 2");

        db.beginTransaction();

        try {

            // -------------------------------------------------------
            // Migrate the weights table
            // -------------------------------------------------------
            if (tableExists(db, TABLE_WEIGHTS)) {

                // Rename the existing weights table to a temporary backup
                db.execSQL("ALTER TABLE " + TABLE_WEIGHTS + " RENAME TO weights_old");

                // Create the new weights table with ON DELETE CASCADE and UNIQUE constraint
                String createNewWeights =
                        "CREATE TABLE " + TABLE_WEIGHTS + " ("
                                + COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + COL_WEIGHT_USERNAME + " TEXT NOT NULL, "
                                + COL_WEIGHT_DATE + " TEXT NOT NULL, "
                                + COL_WEIGHT_SORT_DATE + " TEXT NOT NULL, "
                                + COL_WEIGHT_VALUE + " REAL NOT NULL, "
                                + COL_WEIGHT_NOTES + " TEXT, "
                                + COL_WEIGHT_UPDATED_AT + " INTEGER NOT NULL, "
                                + "FOREIGN KEY(" + COL_WEIGHT_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE, "
                                + "UNIQUE(" + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_SORT_DATE + ")"
                                + ")";
                db.execSQL(createNewWeights);

                /*
                 * Copy weight data into the new table while removing any duplicate daily entries.
                 */
                db.execSQL("INSERT INTO " + TABLE_WEIGHTS + " ("
                        + COL_WEIGHT_ID + ", "
                        + COL_WEIGHT_USERNAME + ", "
                        + COL_WEIGHT_DATE + ", "
                        + COL_WEIGHT_SORT_DATE + ", "
                        + COL_WEIGHT_VALUE + ", "
                        + COL_WEIGHT_NOTES + ", "
                        + COL_WEIGHT_UPDATED_AT
                        + ") "
                        + "SELECT "
                        + COL_WEIGHT_ID + ", "
                        + COL_WEIGHT_USERNAME + ", "
                        + COL_WEIGHT_DATE + ", "
                        + COL_WEIGHT_SORT_DATE + ", "
                        + COL_WEIGHT_VALUE + ", "
                        + COL_WEIGHT_NOTES + ", "
                        + COL_WEIGHT_UPDATED_AT
                        + " FROM weights_old "
                        + "WHERE " + COL_WEIGHT_ID + " IN ("
                        + "SELECT MAX(" + COL_WEIGHT_ID + ") "
                        + "FROM weights_old "
                        + "GROUP BY " + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_SORT_DATE
                        + ")");

                // Drop the temporary backup table after data is preserved
                db.execSQL("DROP TABLE weights_old");

                Log.d(TAG, "Weights table migrated successfully");
            } else {
                Log.d(TAG, "Weights table did not exist. Creating fresh table.");
                String createNewWeights =
                        "CREATE TABLE " + TABLE_WEIGHTS + " ("
                                + COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + COL_WEIGHT_USERNAME + " TEXT NOT NULL, "
                                + COL_WEIGHT_DATE + " TEXT NOT NULL, "
                                + COL_WEIGHT_SORT_DATE + " TEXT NOT NULL, "
                                + COL_WEIGHT_VALUE + " REAL NOT NULL, "
                                + COL_WEIGHT_NOTES + " TEXT, "
                                + COL_WEIGHT_UPDATED_AT + " INTEGER NOT NULL, "
                                + "FOREIGN KEY(" + COL_WEIGHT_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE, "
                                + "UNIQUE(" + COL_WEIGHT_USERNAME + ", " + COL_WEIGHT_SORT_DATE + ")"
                                + ")";
                db.execSQL(createNewWeights);
            }

            // -------------------------------------------------------
            // Migrate the goal weight table
            // -------------------------------------------------------
            if (tableExists(db, TABLE_GOAL)) {

                // Rename the existing goal table to a temporary backup
                db.execSQL("ALTER TABLE " + TABLE_GOAL + " RENAME TO goal_weight_old");

                // Create the new goal table with ON DELETE CASCADE
                String createNewGoal =
                        "CREATE TABLE " + TABLE_GOAL + " ("
                                + COL_GOAL_USERNAME + " TEXT PRIMARY KEY, "
                                + COL_GOAL_VALUE + " REAL NOT NULL, "
                                + "FOREIGN KEY(" + COL_GOAL_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                                + ")";
                db.execSQL(createNewGoal);

                // Copy all existing goal data from the backup into the new table
                db.execSQL("INSERT INTO " + TABLE_GOAL + " ("
                        + COL_GOAL_USERNAME + ", "
                        + COL_GOAL_VALUE
                        + ") SELECT "
                        + COL_GOAL_USERNAME + ", "
                        + COL_GOAL_VALUE
                        + " FROM goal_weight_old");

                // Drop the temporary backup table after data is preserved
                db.execSQL("DROP TABLE goal_weight_old");

                Log.d(TAG, "Goal weight table migrated successfully");
            } else {
                Log.d(TAG, "Goal table did not exist. Creating fresh table.");
                String createNewGoal =
                        "CREATE TABLE " + TABLE_GOAL + " ("
                                + COL_GOAL_USERNAME + " TEXT PRIMARY KEY, "
                                + COL_GOAL_VALUE + " REAL NOT NULL, "
                                + "FOREIGN KEY(" + COL_GOAL_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                                + ")";
                db.execSQL(createNewGoal);
            }

            // -------------------------------------------------------
            // Migrate the settings table
            // -------------------------------------------------------
            if (tableExists(db, TABLE_SETTINGS)) {

                // Rename the existing settings table to a temporary backup
                db.execSQL("ALTER TABLE " + TABLE_SETTINGS + " RENAME TO settings_old");

                // Create the new settings table with foreign key ON DELETE CASCADE
                String createNewSettings =
                        "CREATE TABLE " + TABLE_SETTINGS + " ("
                                + COL_SETTINGS_USERNAME + " TEXT PRIMARY KEY, "
                                + COL_SETTINGS_SMS_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                                + COL_SETTINGS_SMS_PHONE + " TEXT, "
                                + "FOREIGN KEY(" + COL_SETTINGS_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                                + ")";
                db.execSQL(createNewSettings);

                // Copy all existing settings data into the new table
                db.execSQL("INSERT INTO " + TABLE_SETTINGS + " ("
                        + COL_SETTINGS_USERNAME + ", "
                        + COL_SETTINGS_SMS_ENABLED + ", "
                        + COL_SETTINGS_SMS_PHONE
                        + ") SELECT "
                        + COL_SETTINGS_USERNAME + ", "
                        + COL_SETTINGS_SMS_ENABLED + ", "
                        + COL_SETTINGS_SMS_PHONE
                        + " FROM settings_old");

                // Drop the temporary backup table after data is preserved
                db.execSQL("DROP TABLE settings_old");

                Log.d(TAG, "Settings table migrated successfully");
            } else {
                Log.d(TAG, "Settings table did not exist. Creating fresh table.");
                String createNewSettings =
                        "CREATE TABLE " + TABLE_SETTINGS + " ("
                                + COL_SETTINGS_USERNAME + " TEXT PRIMARY KEY, "
                                + COL_SETTINGS_SMS_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                                + COL_SETTINGS_SMS_PHONE + " TEXT, "
                                + "FOREIGN KEY(" + COL_SETTINGS_USERNAME + ") REFERENCES "
                                + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE"
                                + ")";
                db.execSQL(createNewSettings);

                Log.d(TAG, "Settings table created successfully");
            }

            // -------------------------------------------------------
            // Create indexes on the migrated tables
            // -------------------------------------------------------
            createIndexes(db);

            // Mark the transaction as successful so changes are committed
            db.setTransactionSuccessful();

            Log.d(TAG, "Migration to version 2 completed successfully");

        } catch (Exception e) {

            // If any error occurs, the transaction will roll back automatically and no data will be lost
            Log.e(TAG, "Migration to version 2 failed: " + e.getMessage(), e);

        } finally {

            // End the transaction
            db.endTransaction();
        }
    }

    /**
     * tableExists(SQLiteDatabase db, String tableName)
     * Checks whether a table exists before attempting to migrate it.
     *
     * @param db - The SQLiteDatabase instance.
     * @param tableName - The table name to check.
     * @return true if the table exists, otherwise false.
     */
    private boolean tableExists(SQLiteDatabase db, String tableName) {

        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        }
    }
}