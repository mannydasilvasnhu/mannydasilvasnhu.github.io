package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.model.WeightEntry;
import com.example.powerscale.repository.WeightRepository;
import com.example.powerscale.utils.Event;

import java.util.List;

/**
 * WeightLogViewModel
 * This ViewModel manages the weight logging functionality of the application.
 *
 * This class:
 * - Loads a user's stored weight entries
 * - Validates weight input from the UI
 * - Handles add, update, and delete operations through the repository
 * - Exposes weight data and status messages to the UI using LiveData
 * - Emits one-time submission events for successful add and update actions
 */
public class WeightLogViewModel extends AndroidViewModel {

    // Repository used for all weight-related data operations
    private final WeightRepository repository;

    // LiveData holding the list of weight entries for the logged-in user
    private final MutableLiveData<List<WeightEntry>> weightEntries = new MutableLiveData<>();

    // LiveData used to send validation, delete, and failure messages back to the UI
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // One-time event used for successful add/update operations
    private final MutableLiveData<Event<WeightSubmissionResult>> submissionResult =
            new MutableLiveData<>();

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
     * getMessage()
     * Returns the observable status and validation messages for the UI.
     *
     * @return LiveData<String> - The message text for UI feedback.
     */
    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * getSubmissionResult()
     * Returns a one-time event containing the final result of a successful
     * add or update action.
     *
     * @return LiveData<Event<WeightSubmissionResult>> - The event consumed by the UI.
     */
    public LiveData<Event<WeightSubmissionResult>> getSubmissionResult() {
        return submissionResult;
    }

    /**
     * loadWeights(String username)
     * Loads all stored weight entries for the specified user and updates
     * the observable LiveData values.
     *
     * @param username - The currently logged-in username.
     */
    public void loadWeights(String username) {

        // Get weight entries from the repository
        List<WeightEntry> entries = repository.getWeightsForUser(username);

        // Update observable entry list
        weightEntries.setValue(entries);
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

        // Ensure a valid user session exists
        if (username == null || username.trim().isEmpty()) {
            message.setValue("User session not found.");
            return;
        }

        // Date field must not be empty
        if (entryDate == null || entryDate.trim().isEmpty()) {
            message.setValue("Please enter a date.");
            return;
        }

        // Weight field must not be empty
        if (weightText == null || weightText.trim().isEmpty()) {
            message.setValue("Please enter a weight.");
            return;
        }

        double weightValue;

        try {
            // Convert entered weight text into a numeric value
            weightValue = Double.parseDouble(weightText.trim());

        } catch (NumberFormatException e) {
            message.setValue("Please enter a valid numeric weight.");
            return;
        }

        // Weight must be a positive number
        if (weightValue <= 0) {
            message.setValue("Weight must be greater than 0.");
            return;
        }

        // Insert the weight entry through the repository
        long result = repository.insertWeight(username, entryDate.trim(), weightValue);

        if (result != -1) {

            boolean didReachGoal = didSubmittedWeightReachGoal(username, weightValue);

            String successMessage = didReachGoal
                    ? "Goal weight reached and Weight added successfully."
                    : "Weight added successfully.";

            // Emit one-time submission event for the Activity
            submissionResult.setValue(
                    new Event<>(new WeightSubmissionResult(successMessage, didReachGoal, false))
            );

            // Reload entries so UI reflects the new data
            loadWeights(username);

        } else {

            // Database insert failed
            message.setValue("Failed to save weight.");
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

        // Ensure user session exists
        if (username == null || username.trim().isEmpty()) {
            message.setValue("User session not found.");
            return;
        }

        // Date validation
        if (entryDate == null || entryDate.trim().isEmpty()) {
            message.setValue("Please enter a date.");
            return;
        }

        // Weight validation
        if (weightText == null || weightText.trim().isEmpty()) {
            message.setValue("Please enter a weight.");
            return;
        }

        double weightValue;

        try {
            // Convert entered text into numeric weight
            weightValue = Double.parseDouble(weightText.trim());

        } catch (NumberFormatException e) {
            message.setValue("Please enter a valid numeric weight.");
            return;
        }

        // Weight must be greater than zero
        if (weightValue <= 0) {
            message.setValue("Weight must be greater than 0.");
            return;
        }

        // Update the entry through the repository
        boolean success = repository.updateWeight(id, entryDate.trim(), weightValue);

        if (success) {

            boolean didReachGoal = didSubmittedWeightReachGoal(username, weightValue);

            String successMessage = didReachGoal
                    ? "Goal weight reached and Weight updated successfully."
                    : "Weight updated successfully.";

            // Emit one-time submission event for the Activity
            submissionResult.setValue(
                    new Event<>(new WeightSubmissionResult(successMessage, didReachGoal, true))
            );

            // Reload data so UI reflects updated values
            loadWeights(username);

        } else {

            // Update operation failed
            message.setValue("Failed to update weight.");
        }
    }

    /**
     * deleteWeight(String username, long id)
     * Deletes an existing weight entry.
     *
     * @param username - The currently logged-in username.
     * @param id - The database ID of the entry to remove.
     */
    public void deleteWeight(String username, long id) {

        // Delete the entry through the repository
        boolean success = repository.deleteWeight(id);

        if (success) {

            // Notify UI that deletion succeeded
            message.setValue("Weight deleted successfully.");

            // Reload entries to refresh the UI
            loadWeights(username);

        } else {

            // Deletion failed
            message.setValue("Failed to delete weight.");
        }
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
}