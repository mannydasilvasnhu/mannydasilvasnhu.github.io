package com.example.powerscale.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.powerscale.database.AppDatabaseHelper;
import com.example.powerscale.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * WeightRepository
 * Repository layer responsible for weight tracking data operations.
 *
 * This class acts as the single access point for weight-related
 * data including weight entries and goal weight values.
 * It communicates with the AppDatabaseHelper and converts
 * database results into application model objects.
 */
public class WeightRepository {

    // Database helper used for weight-related database operations
    private final AppDatabaseHelper dbHelper;

    /**
     * WeightRepository(Context context)
     * Initializes the repository and database helper.
     *
     * @param context - The application context used to access the database.
     */
    public WeightRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * insertWeight(String username, String entryDate, double weightLbs)
     * Inserts a new weight entry for a specific user.
     *
     * @param username - The username associated with the entry.
     * @param entryDate - The date of the weight entry.
     * @param weightLbs - The weight value in pounds.
     * @return long - The row ID of the inserted record.
     */
    public long insertWeight(String username, String entryDate, double weightLbs) {
        return dbHelper.insertWeight(username, entryDate, weightLbs, null);
    }

    /**
     * updateWeight(long id, String entryDate, double weightLbs)
     * Updates an existing weight entry.
     *
     * @param id - The database ID of the entry.
     * @param entryDate - The updated date.
     * @param weightLbs - The updated weight value.
     * @return boolean - True if update was successful.
     */
    public boolean updateWeight(long id, String entryDate, double weightLbs) {
        return dbHelper.updateWeight(id, entryDate, weightLbs, null);
    }

    /**
     * deleteWeight(long id)
     * Deletes a weight entry from the database.
     *
     * @param id - The database ID of the entry.
     * @return boolean - True if deletion was successful.
     */
    public boolean deleteWeight(long id) {
        return dbHelper.deleteWeight(id);
    }

    /**
     * saveGoalWeight(String username, double goalWeight)
     * Saves or updates the goal weight for a user.
     *
     * @param username - The username associated with the goal.
     * @param goalWeight - The goal weight value.
     * @return boolean - True if operation succeeded.
     */
    public boolean saveGoalWeight(String username, double goalWeight) {
        return dbHelper.updateOrInsertGoalWeight(username, goalWeight);
    }

    /**
     * getGoalWeight(String username)
     * Gets the goal weight for a user.
     *
     * @param username - The username to query.
     * @return Double - The stored goal weight or null if none exists.
     */
    public Double getGoalWeight(String username) {
        return dbHelper.getGoalWeight(username);
    }

    /**
     * getWeightsForUser(String username)
     * Gets all weight entries for a user and converts
     * database records into WeightEntry model objects.
     *
     * @param username - The username to query.
     * @return List<WeightEntry> - The list of weight entries.
     */
    public List<WeightEntry> getWeightsForUser(String username) {

        List<WeightEntry> entries = new ArrayList<>();

        try (Cursor cursor = dbHelper.getAllWeightsForUser(username)) {

            if (cursor != null) {

                int idCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_ID);
                int dateCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_DATE);
                int valueCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_VALUE);

                while (cursor.moveToNext()) {

                    long id = cursor.getLong(idCol);
                    String date = cursor.getString(dateCol);
                    double weight = cursor.getDouble(valueCol);

                    entries.add(new WeightEntry(id, date, weight));
                }
            }
        }

        return entries;
    }

    /**
     * getLatestWeightEntry(String username)
     * Gets the most recent weight entry for a user.
     *
     * @param username - The username to query.
     * @return WeightEntry - The most recent weight entry or null if none exists.
     */
    public WeightEntry getLatestWeightEntry(String username) {

        try (Cursor cursor = dbHelper.getAllWeightsForUser(username)) {

            if (cursor != null && cursor.moveToFirst()) {

                int idCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_ID);
                int dateCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_DATE);
                int valueCol = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_VALUE);

                long id = cursor.getLong(idCol);
                String date = cursor.getString(dateCol);
                double weight = cursor.getDouble(valueCol);

                return new WeightEntry(id, date, weight);
            }
        }

        return null;
    }
}