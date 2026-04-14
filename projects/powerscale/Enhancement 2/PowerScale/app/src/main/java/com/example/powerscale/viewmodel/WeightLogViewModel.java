package com.example.powerscale.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.algorithm.TrendAnalysisResult;
import com.example.powerscale.algorithm.WeightTrendAnalyzer;
import com.example.powerscale.model.WeightEntry;
import com.example.powerscale.repository.WeightRepository;
import com.example.powerscale.utils.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * WeightLogViewModel
 * This ViewModel manages the weight logging functionality of the application.
 *
 * This class:
 * - Observes a user's stored weight entries through the repository
 * - Validates weight input from the UI
 * - Handles add, update, and delete operations through the repository
 * - Determines whether a submitted weight reached the user's goal
 * - Attempts goal-reached SMS notifications when applicable
 * - Runs weight trend analysis against the observed entries
 * - Exposes weight data, trend results, and standardized result objects to the UI using LiveData
 */
public class WeightLogViewModel extends AndroidViewModel {

    // SMS prefs keys shared with SmsSettingsActivity
    private static final String PREFS_SMS = "sms_settings";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";

    // Repository used for all weight-related data operations
    private final WeightRepository repository;

    // Observable list of weight entries for the logged-in user
    private final MediatorLiveData<List<WeightEntry>> weightEntries = new MediatorLiveData<>();

    // Observable trend-analysis result derived from the current weight list
    private final MutableLiveData<TrendAnalysisResult> trendAnalysisResult = new MutableLiveData<>();

    // One-time event used for all weight log results
    private final MutableLiveData<Event<WeightSubmissionResult>> submissionResult = new MutableLiveData<>();

    // Current repository-backed source being observed
    private LiveData<List<WeightEntry>> repositoryWeightSource;

    /**
     * WeightLogViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context used for repository initialization.
     */
    public WeightLogViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new WeightRepository(application);

        // Initialize empty observable state
        weightEntries.setValue(new ArrayList<>());
        trendAnalysisResult.setValue(WeightTrendAnalyzer.analyzeTrends(new ArrayList<>()));
    }

    /**
     * startObservingWeights(String username)
     * Starts observing repository-backed weight data for the specified user.
     *
     * @param username - The currently logged-in username.
     */
    public void startObservingWeights(String username) {

        if (repositoryWeightSource != null) {
            weightEntries.removeSource(repositoryWeightSource);
        }

        repositoryWeightSource = repository.observeWeights(username);

        // Seed the current snapshot
        List<WeightEntry> currentEntries = repositoryWeightSource.getValue();
        List<WeightEntry> safeCurrentEntries = currentEntries != null ? currentEntries : new ArrayList<>();

        weightEntries.setValue(safeCurrentEntries);
        trendAnalysisResult.setValue(WeightTrendAnalyzer.analyzeTrends(safeCurrentEntries));

        // Keep weightEntries and trends synchronized for screens that observe updates.
        weightEntries.addSource(repositoryWeightSource, entries -> {
            List<WeightEntry> safeEntries = entries != null ? entries : new ArrayList<>();
            weightEntries.setValue(safeEntries);
            trendAnalysisResult.setValue(WeightTrendAnalyzer.analyzeTrends(safeEntries));
        });
    }

    /**
     * getWeightEntries()
     * Returns the observable list of weight entries displayed in the UI.
     *
     * @return LiveData<List<WeightEntry>> - The user's stored weight entries.
     */
    public LiveData<List<WeightEntry>> getWeightEntries() {
        return weightEntries;
    }

    /**
     * getTrendAnalysisResult()
     * Returns the latest calculated trend-analysis result for the current user.
     *
     * @return LiveData<TrendAnalysisResult> - The observable trend result.
     */
    public LiveData<TrendAnalysisResult> getTrendAnalysisResult() {
        return trendAnalysisResult;
    }

    /**
     * getSubmissionResult()
     * Returns a one-time event containing the final result of a weight log action.
     *
     * @return LiveData<Event<WeightSubmissionResult>> - The event consumed by the UI.
     */
    public LiveData<Event<WeightSubmissionResult>> getSubmissionResult() {
        return submissionResult;
    }

    /**
     * saveWeight(String username, String entryDate, String weightText)
     * Validates user input and inserts a new weight entry.
     *
     * @param username - The currently logged-in username.
     * @param entryDate - The entered date of the weight entry.
     * @param weightText - The entered weight value as text.
     */
    public void saveWeight(String username, String entryDate, String weightText) {

        WeightSubmissionResult validationResult = validateWeightInputForSave(username, entryDate, weightText);
        if (validationResult != null) {
            submissionResult.setValue(new Event<>(validationResult));
            return;
        }

        double weightValue = Double.parseDouble(weightText.trim());

        // Insert the weight entry through the repository
        long result = repository.insertWeight(username, entryDate.trim(), weightValue);

        if (result != -1) {
            boolean didReachGoal = didSubmittedWeightReachGoal(username, weightValue);

            WeightSubmissionResult finalResult = buildSubmissionResult(didReachGoal, false);

            submissionResult.setValue(new Event<>(finalResult));

        } else {
            submissionResult.setValue(new Event<>(WeightSubmissionResult.error("Failed to save weight.", false)));
        }
    }

    /**
     * updateWeight(String username, long id, String entryDate, String weightText)
     * Validates input and updates an existing weight entry.
     *
     * @param username - The currently logged-in username.
     * @param id - The database ID of the entry being updated.
     * @param entryDate - The updated date value.
     * @param weightText - The updated weight value as text.
     */
    public void updateWeight(String username, long id, String entryDate, String weightText) {

        WeightSubmissionResult validationResult = validateWeightInputForUpdate(username, id, entryDate, weightText);
        if (validationResult != null) {
            submissionResult.setValue(new Event<>(validationResult));
            return;
        }

        double weightValue = Double.parseDouble(weightText.trim());

        // Update the entry through the repository
        boolean success = repository.updateWeight(id, entryDate.trim(), weightValue);

        if (success) {
            boolean didReachGoal = didSubmittedWeightReachGoal(username, weightValue);

            WeightSubmissionResult finalResult = buildSubmissionResult(didReachGoal, true);

            submissionResult.setValue(new Event<>(finalResult));

        } else {
            submissionResult.setValue(new Event<>(WeightSubmissionResult.error("Failed to update weight.", true)));
        }
    }

    /**
     * deleteWeight(long id)
     * Deletes an existing weight entry.
     *
     * @param id - The database ID of the entry to remove.
     */
    public void deleteWeight(long id) {
        boolean success = repository.deleteWeight(id);

        if (success) {
            submissionResult.setValue(new Event<>(WeightSubmissionResult.deleteResult("Weight deleted successfully.", true)));
        } else {
            submissionResult.setValue(new Event<>(WeightSubmissionResult.deleteResult("Failed to delete weight.", false)));
        }
    }

    /**
     * validateWeightInputForSave(String username, String entryDate, String weightText)
     * Validates user input before a save operation and enforces the one-entry-per-date rule.
     *
     * @param username - The currently logged-in username.
     * @param entryDate - The entered date value.
     * @param weightText - The entered weight value as text.
     * @return WeightSubmissionResult - The validation result if invalid, or null if valid.
     */
    private WeightSubmissionResult validateWeightInputForSave(String username, String entryDate, String weightText) {

        WeightSubmissionResult baseValidation = validateBaseWeightInput(username, entryDate, weightText);
        if (baseValidation != null) {
            return baseValidation;
        }

        if (repository.hasWeightEntryForDate(username, entryDate.trim())) {
            return WeightSubmissionResult.validationError("An entry already exists for this date", null);
        }

        return null;
    }

    /**
     * validateWeightInputForUpdate(String username, long id, String entryDate, String weightText)
     * Validates user input before an update operation and prevents changing a row
     * to a date that already belongs to another record.
     *
     * @param username - The currently logged-in username.
     * @param id - The current database row being updated.
     * @param entryDate - The entered date value.
     * @param weightText - The entered weight value as text.
     * @return WeightSubmissionResult - The validation result if invalid, or null if valid.
     */
    private WeightSubmissionResult validateWeightInputForUpdate(String username, long id, String entryDate, String weightText) {

        WeightSubmissionResult baseValidation = validateBaseWeightInput(username, entryDate, weightText);
        if (baseValidation != null) {
            return baseValidation;
        }

        if (repository.hasWeightEntryForDateExcludingId(username, entryDate.trim(), id)) {
            return WeightSubmissionResult.validationError("Another entry already exists for this date", null);
        }

        return null;
    }

    /**
     * validateBaseWeightInput(String username, String entryDate, String weightText)
     * Runs the shared validation used by both save and update operations.
     *
     * @param username - The currently logged-in username.
     * @param entryDate - The entered date value.
     * @param weightText - The entered weight value as text.
     * @return WeightSubmissionResult - The validation result if invalid, or null if valid.
     */
    private WeightSubmissionResult validateBaseWeightInput(String username, String entryDate, String weightText) {

        // Ensure a valid user session exists
        if (username == null || username.trim().isEmpty()) {
            return WeightSubmissionResult.error("User session not found.", false);
        }

        // Date field must not be empty
        if (entryDate == null || entryDate.trim().isEmpty()) {
            return WeightSubmissionResult.validationError("Date is required", null);
        }

        // Weight field must not be empty
        if (weightText == null || weightText.trim().isEmpty()) {
            return WeightSubmissionResult.validationError(null, "Weight is required");
        }

        double weightValue;

        try {
            weightValue = Double.parseDouble(weightText.trim());
        } catch (NumberFormatException e) {
            return WeightSubmissionResult.validationError(null, "Weight must be a number");
        }

        // Weight must be a positive number
        if (weightValue <= 0) {
            return WeightSubmissionResult.validationError(null, "Weight must be greater than 0");
        }

        return null;
    }

    /**
     * buildSubmissionResult(boolean didReachGoal, boolean isUpdate)
     * Builds the final result object returned to the UI after a successful save/update.
     *
     * @param didReachGoal - True if the submitted weight met the user's goal.
     * @param isUpdate - True if the operation was an update.
     * @return WeightSubmissionResult - The final submission result for the UI.
     */
    private WeightSubmissionResult buildSubmissionResult(boolean didReachGoal, boolean isUpdate) {

        String normalMessage = isUpdate ? "Weight updated successfully." : "Weight added successfully.";

        String goalMessage = isUpdate ? "Goal weight reached and weight updated successfully." : "Goal weight reached and weight added successfully.";

        // If goal was not reached, return the standard success result
        if (!didReachGoal) {
            return WeightSubmissionResult.success(normalMessage, isUpdate);
        }

        // If goal was reached but SMS settings are disabled or incomplete, keep the operation successful but do not return the goal message
        if (!isGoalSmsEnabledAndConfigured()) {
            return WeightSubmissionResult.success(normalMessage, isUpdate);
        }

        // Attempt SMS send and return either goal success or warning
        boolean smsSent = sendGoalReachedSms();

        if (smsSent) {
            return WeightSubmissionResult.success(goalMessage, isUpdate);
        }

        return WeightSubmissionResult.warning(normalMessage, isUpdate, "Weight was saved, but the goal notification text could not be sent.");
    }

    /**
     * didSubmittedWeightReachGoal(String username, double submittedWeight)
     * Determines whether the submitted weight meets the user's configured goal weight.
     *
     * @param username - The currently logged-in username.
     * @param submittedWeight - The submitted weight value from the form.
     * @return boolean - True if the submitted weight is less than or equal to the goal.
     */
    private boolean didSubmittedWeightReachGoal(String username, double submittedWeight) {
        Double goalWeight = repository.getGoalWeight(username);

        if (goalWeight == null) {
            return false;
        }

        return submittedWeight <= goalWeight;
    }

    /**
     * isGoalSmsEnabledAndConfigured()
     * Returns true only when SMS goal notifications are actually usable.
     *
     * @return boolean - True if goal SMS behavior should be active.
     */
    private boolean isGoalSmsEnabledAndConfigured() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_SMS, Application.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        if (!enabled) {
            return false;
        }

        String phone = prefs.getString(KEY_SMS_PHONE, "").trim();
        if (phone.isEmpty()) {
            return false;
        }

        return ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * sendGoalReachedSms()
     * Attempts to send the goal-reached SMS.
     *
     * @return boolean - True if the SMS send call completed successfully.
     */
    private boolean sendGoalReachedSms() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_SMS, Application.MODE_PRIVATE);

        String phone = prefs.getString(KEY_SMS_PHONE, "");
        if (phone.isEmpty()) {
            return false;
        }

        try {
            SmsManager smsManager = getApplication().getSystemService(SmsManager.class);
            if (smsManager == null) {
                return false;
            }

            String smsMessage = "PowerScale: Congrats! You hit your goal weight.";
            smsManager.sendTextMessage(phone.trim(), null, smsMessage, null, null);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}