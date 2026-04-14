package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.repository.UserRepository;
import com.example.powerscale.utils.Event;

/**
 * LoginViewModel
 * This ViewModel manages authentication and account creation logic.
 *
 * This class:
 * - Validates login credentials entered by the user
 * - Communicates with the UserRepository for authentication
 * - Handles new account creation
 * - Exposes standardized authentication results to the UI
 */
public class LoginViewModel extends AndroidViewModel {

    // Repository used for user authentication and account management
    private final UserRepository repository;

    // One-time authentication result observed by the UI
    private final MutableLiveData<Event<AuthResult>> authResult = new MutableLiveData<>();

    /**
     * LoginViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context required for repository initialization.
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new UserRepository(application);
    }

    /**
     * getAuthResult()
     * Returns the one-time authentication result observed by the UI.
     *
     * @return LiveData<Event<AuthResult>> - The authentication event for the UI.
     */
    public LiveData<Event<AuthResult>> getAuthResult() {
        return authResult;
    }

    /**
     * attemptLogin(String username, String password)
     * Validates user input and attempts to authenticate the user
     * through the UserRepository.
     *
     * @param username - The username entered by the user.
     * @param password - The password entered by the user.
     */
    public void attemptLogin(String username, String password) {

        // Username is required
        if (username == null || username.trim().isEmpty()) {
            authResult.setValue(new Event<>(AuthResult.validationError("Username is required.", "Username is required", null)));
            return;
        }

        // Password is required
        if (password == null || password.trim().isEmpty()) {
            authResult.setValue(new Event<>(AuthResult.validationError("Password is required.", null, "Password is required")));
            return;
        }

        // Remove whitespace from username before checking credentials
        String trimmedUsername = username.trim();

        // Check credentials against stored user data
        boolean valid = repository.checkLogin(trimmedUsername, password);

        if (valid) {
            authResult.setValue(new Event<>(AuthResult.successLogin("Welcome back, " + trimmedUsername + "!")));
        } else {
            authResult.setValue(new Event<>(AuthResult.error("Login failed. Check your username/password.")));
        }
    }

    /**
     * attemptCreateAccount(String username, String password)
     * Validates input and attempts to create a new user account.
     *
     * @param username - The desired username entered by the user.
     * @param password - The password entered by the user.
     */
    public void attemptCreateAccount(String username, String password) {

        // Username must be provided
        if (username == null || username.trim().isEmpty()) {
            authResult.setValue(new Event<>(
                    AuthResult.validationError("Username is required.", "Username is required", null)));
            return;
        }

        // Password must be provided
        if (password == null || password.trim().isEmpty()) {
            authResult.setValue(new Event<>(AuthResult.validationError("Password is required.", null, "Password is required")));
            return;
        }

        // Remove whitespace from username
        String trimmedUsername = username.trim();

        // Check if username already exists
        if (repository.userExists(trimmedUsername)) {
            authResult.setValue(new Event<>(AuthResult.error("That username already exists. Try logging in.")));
            return;
        }

        // Attempt to create the new user account
        boolean created = repository.createUser(trimmedUsername, password);

        if (created) {
            authResult.setValue(new Event<>(AuthResult.successAccountCreated("Account created successfully.")));
        } else {
            authResult.setValue(new Event<>(AuthResult.error("Could not create account. Try a different username.")));
        }
    }
}