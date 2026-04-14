package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.model.WeightEntry;
import com.example.powerscale.repository.WeightRepository;

/**
 * ProfileViewModel
 * This ViewModel manages the data displayed on the user's profile screen.
 *
 * This class:
 * - Gets the user's latest logged weight entry
 * - Gets the user's configured goal weight
 * - Exposes this data to the UI using LiveData
 */
public class ProfileViewModel extends AndroidViewModel {

    // Repository used for weight and goal-related data access
    private final WeightRepository repository;

    // LiveData holding the most recent weight entry for the user
    private final MutableLiveData<WeightEntry> latestWeightEntry = new MutableLiveData<>();

    // LiveData holding the user's goal weight
    private final MutableLiveData<Double> goalWeight = new MutableLiveData<>();

    /**
     * ProfileViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context used for repository initialization.
     */
    public ProfileViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new WeightRepository(application);
    }

    /**
     * getLatestWeightEntry()
     * Returns the most recently logged weight entry for the user.
     *
     * @return LiveData<WeightEntry> - The user's latest weight entry.
     */
    public LiveData<WeightEntry> getLatestWeightEntry() {
        return latestWeightEntry;
    }

    /**
     * getGoalWeight()
     * Returns the user's stored goal weight.
     *
     * @return LiveData<Double> - The user's goal weight.
     */
    public LiveData<Double> getGoalWeight() {
        return goalWeight;
    }

    /**
     * loadProfileData(String username)
     * Loads the profile data for the specified user.
     * This includes the most recent weight entry and the configured goal weight.
     *
     * @param username - The currently logged-in username.
     */
    public void loadProfileData(String username) {

        // Load the most recent weight entry from the repository
        latestWeightEntry.setValue(repository.getLatestWeightEntry(username));

        // Load the user's stored goal weight
        goalWeight.setValue(repository.getGoalWeight(username));
    }
}