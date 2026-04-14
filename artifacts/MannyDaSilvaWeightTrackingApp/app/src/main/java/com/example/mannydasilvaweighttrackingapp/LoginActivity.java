package com.example.mannydasilvaweighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity
 * This class handles user authentication and account creation.
 *
 * This activity allows users to:
 * - Log in using existing credentials
 * - Create a new account if they do not already have one
 * - Save the current session using SharedPreferences
 */
public class LoginActivity extends AppCompatActivity {

    // Database helper used for user authentication
    private AppDatabaseHelper db;

    // UI input containers (used for validation)
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;

    // Input fields
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the layout, connects UI components,
     * and assigns button listeners.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Initialize database helper
        db = new AppDatabaseHelper(this);

        // Connect buttons
        MaterialButton buttonLogin = findViewById(R.id.buttonLogin);
        MaterialButton buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Connect input fields
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        // Assign button click behavior
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonCreateAccount.setOnClickListener(v -> attemptCreateAccount());
    }

    /**
     * readCredentials()
     * Gets username and password input from the UI.
     * Trims whitespace from username.
     *
     * @return String[] - The Array containing username and password.
     */
    private String[] readCredentials() {
        String username = editUsername.getText() == null ? "" : editUsername.getText().toString().trim();

        String password = editPassword.getText() == null ? "" : editPassword.getText().toString();

        return new String[]{username, password};
    }

    /**
     * clearErrors()
     * Clears any validation errors currently displayed on the input fields.
     *
     * @return void
     */
    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    /**
     * attemptLogin()
     * Validates user input and attempts to log in using database credentials.
     * If successful, stores the session and navigates to the Home screen.
     *
     * @return void
     */
    private void attemptLogin() {

        clearErrors();

        String[] creds = readCredentials();
        String username = creds[0];
        String password = creds[1];

        boolean hasError = false;

        // Validate username
        if (username.isEmpty()) {
            tilUsername.setError("Username is required");
            hasError = true;
        }

        // Validate password
        if (password.trim().isEmpty()) {
            tilPassword.setError("Password is required");
            hasError = true;
        }

        if (hasError) return;

        // Check credentials against database
        if (db.checkLogin(username, password)) {

            // Save session username
            getSharedPreferences("session", MODE_PRIVATE).edit().putString("username", username).apply();

            Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();

            goToHome();

        } else {
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * attemptCreateAccount()
     * Validates input and creates a new user account if the username is available.
     * On success, stores the session and navigates to the Home screen.
     *
     * @return void
     */
    private void attemptCreateAccount() {

        clearErrors();

        String[] creds = readCredentials();
        String username = creds[0];
        String password = creds[1];

        boolean hasError = false;

        // Validate username
        if (username.isEmpty()) {
            tilUsername.setError("Username is required");
            hasError = true;
        }

        // Validate password
        if (password.trim().isEmpty()) {
            tilPassword.setError("Password is required");
            hasError = true;
        }

        if (hasError) return;

        // Prevent duplicate usernames
        if (db.userExists(username)) {
            Toast.makeText(this, R.string.username_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new user in database
        boolean created = db.createUser(username, password);

        if (created) {

            // Save session username
            getSharedPreferences("session", MODE_PRIVATE).edit().putString("username", username).apply();

            resetSmsSettingsForNewAccount(username);

            Toast.makeText(this, R.string.account_creation_successful, Toast.LENGTH_SHORT).show();
            goToHome();

        } else {
            Toast.makeText(this, R.string.account_creation_unsuccessful, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Forces SMS settings to be disabled when a user creates an account for the first time.
     *
     * @param username newly created username
     * @return void
     */
    private void resetSmsSettingsForNewAccount(String username) {
        getSharedPreferences("sms_settings", MODE_PRIVATE).edit().putBoolean("sms_enabled", false).remove("sms_phone").remove("notified_goal_" + username).apply();
    }

    /**
     * goToHome()
     * Navigates the user to the HomeActivity and applies a fade transition.
     *
     * @return void
     */
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

        // Apply fade transition animation
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    /**
     * finish()
     * Called when this activity is closed.
     * Applies a fade animation when exiting the screen.
     *
     * @return void
     */
    @Override
    public void finish() {
        super.finish();

        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out);
    }
}