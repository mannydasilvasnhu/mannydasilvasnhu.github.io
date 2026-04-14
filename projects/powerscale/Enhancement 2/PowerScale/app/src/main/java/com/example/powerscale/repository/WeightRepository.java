package com.example.powerscale.repository;

import android.content.Context;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.database.AppDatabaseHelper;
import com.example.powerscale.database.WeightDao;
import com.example.powerscale.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * WeightRepository
 * Repository layer responsible for weight tracking data operations.
 *
 * This class acts as the single access point for weight-related
 * data including weight entries and goal weight values.
 * It communicates with the WeightDao and converts database
 * results into application model objects.
 *
 * This repository also owns the observable weight list for the
 * currently active user.
 */
public class WeightRepository {

    // DAO used for weight-related database operations
    private final WeightDao weightDao;

    // Observable list of weight entries for the currently active user
    private final MutableLiveData<List<WeightEntry>> observableWeights = new MutableLiveData<>();

    // Tracks the current user whose entries are being observed
    private String activeUsername;

    /**
     * WeightRepository(Context context)
     * Initializes repository dependencies.
     *
     * @param context - The application context used for database access.
     */
    public WeightRepository(Context context) {
        this.weightDao = new WeightDao(context.getApplicationContext());
        observableWeights.setValue(new ArrayList<>());
    }

    /**
     * observeWeights(String username)
     * Starts observing weight data for the specified user.
     *
     * @param username - The username associated with the observed entries.
     * @return LiveData<List<WeightEntry>> - The observable list of entries.
     */
    public LiveData<List<WeightEntry>> observeWeights(String username) {
        activeUsername = username;
        refreshObservedWeights();
        return observableWeights;
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
        long result = weightDao.insertWeight(username, entryDate, weightLbs, null);

        if (result != -1 && username != null && username.equals(activeUsername)) {
            refreshObservedWeights();
        }

        return result;
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
        boolean success = weightDao.updateWeight(id, entryDate, weightLbs, null);

        if (success) {
            refreshObservedWeights();
        }

        return success;
    }

    /**
     * deleteWeight(long id)
     * Deletes a weight entry from the database.
     *
     * @param id - The database ID of the entry.
     * @return boolean - True if deletion was successful.
     */
    public boolean deleteWeight(long id) {
        boolean success = weightDao.deleteWeight(id);

        if (success) {
            refreshObservedWeights();
        }

        return success;
    }

    /**
     * hasWeightEntryForDate(String username, String entryDate)
     * Checks whether the specified user already has a stored entry for the given date.
     *
     * @param username - The username associated with the query.
     * @param entryDate - The entered display date.
     * @return boolean - True if an entry already exists for that date.
     */
    public boolean hasWeightEntryForDate(String username, String entryDate) {
        return weightDao.hasWeightEntryForDate(username, entryDate);
    }

    /**
     * hasWeightEntryForDateExcludingId(String username, String entryDate, long excludedId)
     * Checks whether another entry already exists for the same user and date,
     * excluding the current record being updated.
     *
     * @param username - The username associated with the query.
     * @param entryDate - The entered display date.
     * @param excludedId - The current record being updated.
     * @return boolean - True if another row already uses that date.
     */
    public boolean hasWeightEntryForDateExcludingId(String username, String entryDate, long excludedId) {
        return weightDao.hasWeightEntryForDateExcludingId(username, entryDate, excludedId);
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
        return weightDao.updateOrInsertGoalWeight(username, goalWeight);
    }

    /**
     * getGoalWeight(String username)
     * Gets the goal weight for a user.
     *
     * @param username - The username to query.
     * @return Double - The stored goal weight or null if none exists.
     */
    public Double getGoalWeight(String username) {
        return weightDao.getGoalWeight(username);
    }

    /**
     * getLatestWeightEntry(String username)
     * Gets the most recent weight entry for a user.
     *
     * @param username - The username to query.
     * @return WeightEntry - The most recent weight entry or null if none exists.
     */
    public WeightEntry getLatestWeightEntry(String username) {

        try (Cursor cursor = weightDao.getAllWeightsForUser(username)) {

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

    /**
     * refreshObservedWeights()
     * Reloads the current user's weight entries and pushes the updated list
     * into the observable LiveData.
     */
    private void refreshObservedWeights() {
        if (activeUsername == null || activeUsername.trim().isEmpty()) {
            observableWeights.setValue(new ArrayList<>());
            return;
        }

        observableWeights.setValue(queryWeightsForUser(activeUsername));
    }

    /**
     * queryWeightsForUser(String username)
     * Gets all weight entries for a user and converts
     * database records into WeightEntry model objects.
     *
     * @param username - The username to query.
     * @return List<WeightEntry> - The list of weight entries.
     */
    private List<WeightEntry> queryWeightsForUser(String username) {

        List<WeightEntry> entries = new ArrayList<>();

        try (Cursor cursor = weightDao.getAllWeightsForUser(username)) {

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
}