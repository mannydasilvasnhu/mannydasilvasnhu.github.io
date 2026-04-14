package com.example.powerscale.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.repository.UserRepository;

/**
 * LoginViewModel
 * This ViewModel manages authentication and account creation logic.
 *
 * This class:
 * - Validates login credentials entered by the user
 * - Communicates with the UserRepository for authentication
 * - Handles new account creation
 * - Exposes authentication state and messages to the UI
 */
public class LoginViewModel extends AndroidViewModel {

    // Repository used for user authentication and account management
    private final UserRepository repository;

    // Message LiveData used to send validation and result messages to the UI
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // Indicates whether a login attempt succeeded
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);

    // Indicates whether a new account was successfully created
    private final MutableLiveData<Boolean> accountCreated = new MutableLiveData<>(false);

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
     * getMessage()
     * Returns the observable status and validation messages for the UI.
     *
     * @return LiveData<String> - The message text for UI feedback.
     */
    public LiveData<String> getMessage() {
        return message;
    }

    /**
     * getLoginSuccess()
     * Returns the login success state observed by the UI.
     *
     * @return LiveData<Boolean> - True if login succeeded.
     */
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    /**
     * getAccountCreated()
     * Returns the account creation success state observed by the UI.
     *
     * @return LiveData<Boolean> - True if account creation succeeded.
     */
    public LiveData<Boolean> getAccountCreated() {
        return accountCreated;
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

        // Reset login success state before attempting authentication
        loginSuccess.setValue(false);

        // Username is required
        if (username == null || username.trim().isEmpty()) {
            message.setValue("Username is required");
            return;
        }

        // Password is required
        if (password == null || password.trim().isEmpty()) {
            message.setValue("Password is required");
            return;
        }

        // Check credentials against stored user data
        boolean valid = repository.checkLogin(username.trim(), password);

        if (valid) {

            // Notify UI that login was successful
            message.setValue("LOGIN_SUCCESS");
            loginSuccess.setValue(true);

        } else {

            // Invalid credentials
            message.setValue("Login failed. Check your username/password.");
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

        // Reset account creation state before attempting creation
        accountCreated.setValue(false);

        // Username must be provided
        if (username == null || username.trim().isEmpty()) {
            message.setValue("Username is required");
            return;
        }

        // Password must be provided
        if (password == null || password.trim().isEmpty()) {
            message.setValue("Password is required");
            return;
        }

        // Remove whitespace from username
        String trimmedUsername = username.trim();

        // Check if username already exists
        if (repository.userExists(trimmedUsername)) {
            message.setValue("That username already exists. Try logging in.");
            return;
        }

        // Attempt to create the new user account
        boolean created = repository.createUser(trimmedUsername, password);

        if (created) {

            // Notify UI that account creation succeeded
            message.setValue("ACCOUNT_CREATED");
            accountCreated.setValue(true);

        } else {

            // Repository failed to create the account
            message.setValue("Could not create account. Try a different username.");
        }
    }
}