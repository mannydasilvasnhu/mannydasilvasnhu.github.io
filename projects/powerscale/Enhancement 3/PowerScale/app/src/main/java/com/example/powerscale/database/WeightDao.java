package com.example.powerscale.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * WeightDao
 * Handles all database operations related to weight entries and goal weight data.
 *
 * This class is responsible for:
 * - Inserting weight entries
 * - Querying weight entries for a user
 * - Updating weight entries
 * - Deleting weight entries
 * - Inserting or updating goal weight
 * - Reading goal weight
 */
public class WeightDao {

    // Shared database helper used for SQLite access
    private final AppDatabaseHelper dbHelper;

    /**
     * WeightDao(Context context)
     * Initializes the DAO with database access.
     *
     * @param context - The application context used for database access.
     */
    public WeightDao(Context context) {
        this.dbHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * insertWeight(String username, String entryDate, double weightLbs, String notes)
     * Inserts a new weight entry for a specific user.
     *
     * @param username - The username associated with the entry.
     * @param entryDate - The date of entry (MM/DD/YYYY).
     * @param weightLbs - The weight value in pounds.
     * @param notes - The optional notes.
     * @return long - The row ID of inserted record.
     */
    public long insertWeight(String username, String entryDate, double weightLbs, String notes) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_WEIGHT_USERNAME, username);
        values.put(AppDatabaseHelper.COL_WEIGHT_DATE, entryDate);
        values.put(AppDatabaseHelper.COL_WEIGHT_SORT_DATE, normalizeDateForStorage(entryDate));
        values.put(AppDatabaseHelper.COL_WEIGHT_VALUE, weightLbs);
        values.put(AppDatabaseHelper.COL_WEIGHT_NOTES, notes);
        values.put(AppDatabaseHelper.COL_WEIGHT_UPDATED_AT, System.currentTimeMillis());

        return db.insert(AppDatabaseHelper.TABLE_WEIGHTS, null, values);
    }

    /**
     * getAllWeightsForUser(String username)
     * Gets all weight entries for a specific user ordered by date descending.
     *
     * @param username - The username to query.
     * @return Cursor - The cursor containing weight entries.
     */
    public Cursor getAllWeightsForUser(String username) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(
                AppDatabaseHelper.TABLE_WEIGHTS,
                null,
                AppDatabaseHelper.COL_WEIGHT_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                AppDatabaseHelper.COL_WEIGHT_SORT_DATE + " DESC"
        );
    }

    /**
     * updateWeight(long id, String entryDate, double weightLbs, String notes)
     * Updates an existing weight entry.
     *
     * @param id - The ID of the weight record.
     * @param entryDate - The updated date.
     * @param weightLbs - The updated weight value.
     * @param notes - The optional updated notes.
     * @return boolean - True if update was successful.
     */
    public boolean updateWeight(long id, String entryDate, double weightLbs, String notes) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_WEIGHT_DATE, entryDate);
        values.put(AppDatabaseHelper.COL_WEIGHT_SORT_DATE, normalizeDateForStorage(entryDate));
        values.put(AppDatabaseHelper.COL_WEIGHT_VALUE, weightLbs);
        values.put(AppDatabaseHelper.COL_WEIGHT_NOTES, notes);
        values.put(AppDatabaseHelper.COL_WEIGHT_UPDATED_AT, System.currentTimeMillis());

        int rows = db.update(
                AppDatabaseHelper.TABLE_WEIGHTS,
                values,
                AppDatabaseHelper.COL_WEIGHT_ID + " = ?",
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rows = db.delete(
                AppDatabaseHelper.TABLE_WEIGHTS,
                AppDatabaseHelper.COL_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return rows > 0;
    }

    /**
     * hasWeightEntryForDate(String username, String entryDate)
     * Checks whether a user already has a weight entry stored for the provided date.
     *
     * @param username - The username to query.
     * @param entryDate - The display date entered by the user.
     * @return boolean - True if a matching date already exists for that user.
     */
    public boolean hasWeightEntryForDate(String username, String entryDate) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String normalizedDate = normalizeDateForStorage(entryDate);

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_WEIGHTS,
                new String[]{AppDatabaseHelper.COL_WEIGHT_ID},
                AppDatabaseHelper.COL_WEIGHT_USERNAME + " = ? AND "
                        + AppDatabaseHelper.COL_WEIGHT_SORT_DATE + " = ?",
                new String[]{username, normalizedDate},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    /**
     * hasWeightEntryForDateExcludingId(String username, String entryDate, long excludedId)
     * Checks whether another row already exists for the provided user/date combination.
     *
     * @param username - The username to query.
     * @param entryDate - The display date entered by the user.
     * @param excludedId - The current row being updated.
     * @return boolean - True if another entry already exists for that date.
     */
    public boolean hasWeightEntryForDateExcludingId(String username, String entryDate, long excludedId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String normalizedDate = normalizeDateForStorage(entryDate);

        try (Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_WEIGHTS,
                new String[]{AppDatabaseHelper.COL_WEIGHT_ID},
                AppDatabaseHelper.COL_WEIGHT_USERNAME + " = ? AND "
                        + AppDatabaseHelper.COL_WEIGHT_SORT_DATE + " = ? AND "
                        + AppDatabaseHelper.COL_WEIGHT_ID + " != ?",
                new String[]{username, normalizedDate, String.valueOf(excludedId)},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    /**
     * updateOrInsertGoalWeight(String username, double goal)
     * Inserts or updates the goal weight for a user.
     *
     * @param username - The username associated with the goal.
     * @param goal - The goal weight value.
     * @return boolean - True if operation was successful.
     */
    public boolean updateOrInsertGoalWeight(String username, double goal) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_GOAL_USERNAME, username);
        values.put(AppDatabaseHelper.COL_GOAL_VALUE, goal);

        long result = db.insertWithOnConflict(
                AppDatabaseHelper.TABLE_GOAL,
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

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor c = db.query(
                AppDatabaseHelper.TABLE_GOAL,
                new String[]{AppDatabaseHelper.COL_GOAL_VALUE},
                AppDatabaseHelper.COL_GOAL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (!c.moveToFirst()) {
                return null;
            }

            return c.getDouble(c.getColumnIndexOrThrow(AppDatabaseHelper.COL_GOAL_VALUE));
        }
    }

    /**
     * normalizeDateForStorage(String entryDate)
     * Converts a user-facing date into a normalized YYYY-MM-DD value used for
     * sorting and uniqueness checks in the database.
     *
     * @param entryDate - The date string entered in the UI.
     * @return String - The normalized date used internally by the database.
     */
    private String normalizeDateForStorage(String entryDate) {
        if (entryDate == null) {
            return "";
        }

        String trimmedDate = entryDate.trim();
        if (trimmedDate.isEmpty()) {
            return "";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);
        inputFormat.setLenient(false);

        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            Date parsedDate = inputFormat.parse(trimmedDate);
            if (parsedDate == null) {
                return trimmedDate;
            }

            return outputFormat.format(parsedDate);

        } catch (ParseException e) {
            return trimmedDate;
        }
    }
}