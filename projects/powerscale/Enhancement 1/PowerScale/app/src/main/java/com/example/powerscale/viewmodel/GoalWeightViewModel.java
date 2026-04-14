package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.repository.WeightRepository;

/**
 * GoalWeightViewModel
 * This ViewModel is responsible for handling goal weight data and validation logic.
 *
 * This class:
 * - Loads the user's current goal weight from the repository
 * - Validates goal weight input from the UI
 * - Saves goal weight updates through the repository
 * - Exposes goal weight data and status messages to the UI
 */
public class GoalWeightViewModel extends AndroidViewModel {

    // Repository used for weight and goal-related data operations
    private final WeightRepository repository;

    // LiveData holding the user's current goal weight
    private final MutableLiveData<Double> goalWeight = new MutableLiveData<>();

    // LiveData used to send validation and result messages to the UI
    private final MutableLiveData<String> message = new MutableLiveData<>();

    /**
     * GoalWeightViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context used to initialize the repository.
     */
    public GoalWeightViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new WeightRepository(application);
    }

    /**
     * getGoalWeight()
     * Returns the observable goal weight value for the UI.
     *
     * @return LiveData<Double> - The user's current goal weight.
     */
    public LiveData<Double> getGoalWeight() {
        return goalWeight;
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
     * loadGoalWeight(String username)
     * Loads the current goal weight for the specified user
     * and updates the observable LiveData value.
     *
     * @param username - The username whose goal weight should be loaded.
     */
    public void loadGoalWeight(String username) {
        Double goal = repository.getGoalWeight(username);
        goalWeight.setValue(goal);
    }

    /**
     * saveGoalWeight(String username, String goalWeightText)
     * Validates the entered goal weight and saves it through the repository.
     * If validation fails, an error message is posted to the UI.
     *
     * @param username - The currently logged-in username.
     * @param goalWeightText - The raw goal weight text entered by the user.
     */
    public void saveGoalWeight(String username, String goalWeightText) {

        // Make sure a valid logged-in user session exists
        if (username == null || username.trim().isEmpty()) {
            message.setValue("User session not found.");
            return;
        }

        // Goal weight field is required
        if (goalWeightText == null || goalWeightText.trim().isEmpty()) {
            message.setValue("Please enter a goal weight.");
            return;
        }

        double goalValue;

        try {
            // Convert entered text into a numeric goal weight
            goalValue = Double.parseDouble(goalWeightText.trim());

        } catch (NumberFormatException e) {
            message.setValue("Please enter a valid numeric goal weight.");
            return;
        }

        // Goal weight must be a positive number
        if (goalValue <= 0) {
            message.setValue("Goal weight must be greater than 0.");
            return;
        }

        // Save the validated goal weight through the repository
        boolean success = repository.saveGoalWeight(username, goalValue);

        if (success) {
            message.setValue("Goal weight saved successfully.");

            // Refresh the LiveData so the UI shows the latest stored value
            loadGoalWeight(username);

        } else {
            message.setValue("Failed to save goal weight.");
        }
    }
}