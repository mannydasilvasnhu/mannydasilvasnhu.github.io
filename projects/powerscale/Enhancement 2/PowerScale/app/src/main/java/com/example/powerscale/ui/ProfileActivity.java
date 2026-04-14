package com.example.powerscale.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.viewmodel.ProfileViewModel;

import java.util.Locale;

/**
 * ProfileActivity
 * This activity displays a simple profile screen using existing app data.
 *
 * This screen shows:
 * - Username
 * - Goal weight
 * - Latest logged weight and date
 * - SMS notification status and saved phone number
 */
public class ProfileActivity extends AppCompatActivity {

    // SharedPreferences file names and keys used elsewhere in the app
    private static final String PREFS_SESSION = "session";
    private static final String KEY_SESSION_USERNAME = "username";
    private static final String PREFS_SMS = "sms_settings";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";

    // ViewModel and references
    private ProfileViewModel viewModel;
    private String currentUsername;

    // UI references
    private TextView textProfileUsername;
    private TextView textProfileGoal;
    private TextView textProfileLatestWeight;
    private TextView textProfileLastLogged;
    private TextView textProfileSmsStatus;
    private TextView textProfileAlertNumber;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the user interface, loads existing data from the ViewModel
     * and SharedPreferences, and displays it on screen.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // Read current user from session prefs
        currentUsername = getSharedPreferences(PREFS_SESSION, MODE_PRIVATE)
                .getString(KEY_SESSION_USERNAME, null);

        // If no user is logged in, force user back
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // UI references
        textProfileUsername = findViewById(R.id.textProfileUsername);
        textProfileGoal = findViewById(R.id.textProfileGoal);
        textProfileLatestWeight = findViewById(R.id.textProfileLatestWeight);
        textProfileLastLogged = findViewById(R.id.textProfileLastLogged);
        textProfileSmsStatus = findViewById(R.id.textProfileSmsStatus);
        textProfileAlertNumber = findViewById(R.id.textProfileAlertNumber);

        // Observe goal weight
        viewModel.getGoalWeight().observe(this, goal -> {
            if (goal == null) {
                textProfileGoal.setText(R.string.profile_not_set);
            } else {
                textProfileGoal.setText(String.format(Locale.US, "%.1f lbs", goal));
            }
        });

        // Observe latest weight entry
        viewModel.getLatestWeightEntry().observe(this, entry -> {
            if (entry == null) {
                textProfileLatestWeight.setText(R.string.profile_no_entries);
                textProfileLastLogged.setText(R.string.profile_no_entries);
            } else {
                textProfileLatestWeight.setText(String.format(Locale.US, "%.1f lbs", entry.weight));
                textProfileLastLogged.setText(entry.date);
            }
        });

        // Load and display everything
        loadProfileData();
    }

    /**
     * loadProfileData()
     * Loads existing data for the logged-in user and updates the UI fields.
     * Uses the following data:
     * - session SharedPreferences for username
     * - ViewModel for goal weight and latest weight/date
     * - sms_settings SharedPreferences for toggle and phone
     */
    private void loadProfileData() {
        // Username
        textProfileUsername.setText(currentUsername);

        // SMS status and alert number
        SharedPreferences smsPrefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);
        boolean enabledPref = smsPrefs.getBoolean(KEY_SMS_ENABLED, false);
        String phone = smsPrefs.getString(KEY_SMS_PHONE, "").trim();

        boolean hasPermission = hasSmsPermission();
        boolean smsReady = enabledPref && hasPermission && !phone.isEmpty();

        textProfileSmsStatus.setText(smsReady ? R.string.profile_sms_enabled : R.string.profile_sms_disabled);

        if (phone.isEmpty()) {
            textProfileAlertNumber.setText(R.string.profile_alert_not_set);
        } else {
            textProfileAlertNumber.setText(maskPhone(phone));
        }

        viewModel.loadProfileData(currentUsername);
    }

    /**
     * hasSmsPermission()
     * Checks whether SEND_SMS permission has been granted.
     *
     * @return boolean - True if granted.
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * maskPhone(String phone)
     * Masks a phone number for display.
     *
     * @param phone - The raw phone number string.
     * @return String - The masked phone number for UI display.
     */
    private String maskPhone(String phone) {
        // Keep only digits
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return "****";
        }

        String last4 = digits.substring(digits.length() - 4);
        return getString(R.string.profile_masked_number, last4);
    }
}