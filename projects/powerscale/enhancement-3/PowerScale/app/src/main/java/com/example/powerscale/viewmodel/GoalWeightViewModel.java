package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.R;
import com.example.powerscale.repository.WeightRepository;
import com.example.powerscale.utils.Event;

import java.util.Locale;

/**
 * GoalWeightViewModel
 * This ViewModel manages the goal weight functionality of the application.
 *
 * This class:
 * - Loads the user's current goal weight
 * - Validates goal weight input from the UI
 * - Saves updated goal weight values through the repository
 * - Exposes goal weight state and standardized result objects to the UI
 */
public class GoalWeightViewModel extends AndroidViewModel {

    // Repository used for goal weight persistence
    private final WeightRepository repository;

    // Observable current goal weight text displayed by the UI
    private final MutableLiveData<String> currentGoalDisplay = new MutableLiveData<>();

    // One-time event used for goal weight results
    private final MutableLiveData<Event<GoalWeightResult>> goalResult = new MutableLiveData<>();

    /**
     * GoalWeightViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context required for repository initialization.
     */
    public GoalWeightViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new WeightRepository(application);
    }

    /**
     * getCurrentGoalDisplay()
     * Returns the observable goal weight display text shown in the UI.
     *
     * @return LiveData<String> - The current goal weight display text.
     */
    public LiveData<String> getCurrentGoalDisplay() {
        return currentGoalDisplay;
    }

    /**
     * getGoalResult()
     * Returns the one-time goal weight result observed by the UI.
     *
     * @return LiveData<Event<GoalWeightResult>> - The goal weight result event.
     */
    public LiveData<Event<GoalWeightResult>> getGoalResult() {
        return goalResult;
    }

    /**
     * loadGoalWeight(String username)
     * Loads the current goal weight for the specified user and updates the observable UI state.
     *
     * @param username - The currently logged-in username.
     */
    public void loadGoalWeight(String username) {

        // Ensure a valid user session exists
        if (username == null || username.trim().isEmpty()) {
            goalResult.setValue(new Event<>(GoalWeightResult.error("User session not found.")));
            return;
        }

        Double currentGoal = repository.getGoalWeight(username);

        if (currentGoal == null) {
            currentGoalDisplay.setValue(getApplication().getString(R.string.current_goal_none));
            goalResult.setValue(new Event<>(GoalWeightResult.loaded(getApplication().getString(R.string.current_goal_none))));
            return;
        }

        String displayValue = formatGoalWeight(currentGoal);
        currentGoalDisplay.setValue(displayValue);

        goalResult.setValue(new Event<>(GoalWeightResult.loaded(displayValue)));
    }

    /**
     * saveGoalWeight(String username, String goalText)
     * Validates user input and saves the goal weight through the repository.
     *
     * @param username - The currently logged-in username.
     * @param goalText - The entered goal weight value as text.
     */
    public void saveGoalWeight(String username, String goalText) {

        // Ensure a valid user session exists
        if (username == null || username.trim().isEmpty()) {
            goalResult.setValue(new Event<>(GoalWeightResult.error("User session not found.")));
            return;
        }

        // Goal field must not be empty
        if (goalText == null || goalText.trim().isEmpty()) {
            goalResult.setValue(new Event<>(GoalWeightResult.validationError("Goal weight is required")));
            return;
        }

        double goalValue;

        try {
            // Convert entered goal text into a numeric value
            goalValue = Double.parseDouble(goalText.trim());

        } catch (NumberFormatException e) {
            goalResult.setValue(new Event<>(GoalWeightResult.validationError("Goal weight must be a number")));
            return;
        }

        // Goal weight must be a positive number
        if (goalValue <= 0) {
            goalResult.setValue(new Event<>(GoalWeightResult.validationError("Goal weight must be greater than 0")));
            return;
        }

        // Save the validated goal weight through the repository
        boolean success = repository.saveGoalWeight(username, goalValue);

        if (success) {
            String displayValue = formatGoalWeight(goalValue);

            // Update observable UI state directly after save
            currentGoalDisplay.setValue(displayValue);

            goalResult.setValue(new Event<>(GoalWeightResult.success("Goal weight saved successfully.", displayValue)));

        } else {
            goalResult.setValue(new Event<>(GoalWeightResult.error("Failed to save goal weight.")));
        }
    }

    /**
     * formatGoalWeight(double goalWeight)
     * Formats the goal weight for display in the UI.
     *
     * @param goalWeight - The goal weight value.
     * @return String - The formatted goal weight string.
     */
    private String formatGoalWeight(double goalWeight) {
        return String.format(Locale.US, "Current Goal Power Level: %.1f lbs", goalWeight);
    }
}