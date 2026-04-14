package com.example.powerscale.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity
 * This activity handles user authentication and account creation.
 *
 * This screen allows users to:
 * - Log in using existing credentials
 * - Create a new account if they do not already have one
 * - Save the current session using SharedPreferences
 */
public class LoginActivity extends AppCompatActivity {

    // ViewModel used for login and account creation flow
    private LoginViewModel viewModel;

    // UI input containers
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;

    // Input fields
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the layout, connects UI components,
     * sets up button listeners, and wires observers to the ViewModel.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Connect buttons
        MaterialButton buttonLogin = findViewById(R.id.buttonLogin);
        MaterialButton buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Connect input fields
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        // Observe messages from the ViewModel
        viewModel.getMessage().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) {
                return;
            }

            if (msg.equals("Username is required")) {
                tilUsername.setError("Username is required");
                return;
            }

            if (msg.equals("Password is required")) {
                tilPassword.setError("Password is required");
                return;
            }

            if (msg.equals("Login failed. Check your username/password.")) {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            if (msg.equals("That username already exists. Try logging in.")) {
                Toast.makeText(this, R.string.username_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            if (msg.equals("Could not create account. Try a different username.")) {
                Toast.makeText(this, R.string.account_creation_unsuccessful, Toast.LENGTH_SHORT).show();
                return;
            }
        });

        // Observe successful login
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                String username = getEnteredUsername();

                getSharedPreferences("session", MODE_PRIVATE)
                        .edit()
                        .putString("username", username)
                        .apply();

                Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                goToHome();
            }
        });

        // Observe successful account creation
        viewModel.getAccountCreated().observe(this, created -> {
            if (Boolean.TRUE.equals(created)) {
                String username = getEnteredUsername();

                getSharedPreferences("session", MODE_PRIVATE)
                        .edit()
                        .putString("username", username)
                        .apply();

                resetSmsSettingsForNewAccount(username);

                Toast.makeText(this, R.string.account_creation_successful, Toast.LENGTH_SHORT).show();
                goToHome();
            }
        });

        // Assign button click behavior
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonCreateAccount.setOnClickListener(v -> attemptCreateAccount());
    }

    /**
     * readCredentials()
     * Gets username and password input from the UI.
     *
     * @return String[] - The array containing username and password.
     */
    private String[] readCredentials() {
        String username = editUsername.getText() == null
                ? ""
                : editUsername.getText().toString().trim();

        String password = editPassword.getText() == null
                ? ""
                : editPassword.getText().toString();

        return new String[]{username, password};
    }

    /**
     * getEnteredUsername()
     * Returns the current username field as trimmed text.
     *
     * @return String - The entered username.
     */
    private String getEnteredUsername() {
        return editUsername.getText() == null
                ? ""
                : editUsername.getText().toString().trim();
    }

    /**
     * clearErrors()
     * Clears any validation errors currently displayed on the input fields.
     */
    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    /**
     * attemptLogin()
     * Reads credentials and passes them to the ViewModel.
     */
    private void attemptLogin() {
        clearErrors();

        String[] creds = readCredentials();
        viewModel.attemptLogin(creds[0], creds[1]);
    }

    /**
     * attemptCreateAccount()
     * Reads credentials and passes them to the ViewModel.
     */
    private void attemptCreateAccount() {
        clearErrors();

        String[] creds = readCredentials();
        viewModel.attemptCreateAccount(creds[0], creds[1]);
    }

    /**
     * resetSmsSettingsForNewAccount(String username)
     * Forces SMS settings to be disabled when a user creates an account for the first time.
     *
     * @param username - The newly created username.
     */
    private void resetSmsSettingsForNewAccount(String username) {
        getSharedPreferences("sms_settings", MODE_PRIVATE)
                .edit()
                .putBoolean("sms_enabled", false)
                .remove("sms_phone")
                .remove("notified_goal_" + username)
                .apply();
    }

    /**
     * goToHome()
     * Navigates the user to the HomeActivity and applies a fade transition.
     */
    private void goToHome() {
        // Navigate to the home screen after successful authentication
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

        overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        finish();
    }

    /**
     * finish()
     * Called when this activity is closed.
     * Applies a fade animation when exiting the screen.
     */
    @Override
    public void finish() {
        super.finish();

        overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
    }
}