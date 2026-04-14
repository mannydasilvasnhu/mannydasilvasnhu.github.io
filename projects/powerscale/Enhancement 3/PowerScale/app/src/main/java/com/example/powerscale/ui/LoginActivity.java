package com.example.powerscale.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.repository.SmsSettingsRepository;
import com.example.powerscale.viewmodel.AuthResult;
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

        // Observe one-time authentication results from the ViewModel
        viewModel.getAuthResult().observe(this, event -> {
            if (event == null) {
                return;
            }

            AuthResult result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            // Apply field-level errors returned by the ViewModel
            tilUsername.setError(result.getUsernameError());
            tilPassword.setError(result.getPasswordError());

            // Stop here for validation-only results
            if (result.getUsernameError() != null || result.getPasswordError() != null) {
                return;
            }

            // Persist session for successful login or account creation
            if (result.isLoginSuccess() || result.isAccountCreated()) {
                String username = getEnteredUsername();

                getSharedPreferences("session", MODE_PRIVATE).edit().putString("username", username).apply();

                if (result.isAccountCreated()) {
                    resetSmsSettingsForNewAccount(username);
                }

                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                goToHome();
                return;
            }

            // Show general authentication failure message
            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
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
        String username = editUsername.getText() == null ? "" : editUsername.getText().toString().trim();

        String password = editPassword.getText() == null ? "" : editPassword.getText().toString();

        return new String[]{username, password};
    }

    /**
     * getEnteredUsername()
     * Returns the current username field as trimmed text.
     *
     * @return String - The entered username.
     */
    private String getEnteredUsername() {
        return editUsername.getText() == null ? "" : editUsername.getText().toString().trim();
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
     * Initializes default SMS settings in the database when a user creates
     * an account for the first time.
     *
     * @param username - The newly created username.
     */
    private void resetSmsSettingsForNewAccount(String username) {
        SmsSettingsRepository smsSettingsRepository = new SmsSettingsRepository(this);
        smsSettingsRepository.initializeDefaultSettings(username);
    }

    /**
     * goToHome()
     * Navigates the user to the HomeActivity and applies a fade transition.
     */
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);

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

        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out);
    }
}