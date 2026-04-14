package com.example.mannydasilvaweighttrackingapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

/**
 * ProfileActivity
 * This class displays a simple profile screen using ONLY existing app data.
 *
 * Shows username, goal weight, latest weight and date, and SMS settings info.
 */
public class ProfileActivity extends AppCompatActivity {

    // SharedPreferences file names and keys used elsewhere in the app
    private static final String PREFS_SESSION = "session";
    private static final String KEY_SESSION_USERNAME = "username";
    private static final String PREFS_SMS = "sms_settings";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";

    // Database helper and references
    private AppDatabaseHelper db;
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
     * This method initializes the user interface, loads existing data from the database,
     * and SharedPreferences, and displays it on screen.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // Initialize database helper
        db = new AppDatabaseHelper(this);

        // Read current user from session prefs
        currentUsername = getSharedPreferences(PREFS_SESSION, MODE_PRIVATE).getString(KEY_SESSION_USERNAME, null);

        // If no user is logged in, force user back
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI references
        textProfileUsername = findViewById(R.id.textProfileUsername);
        textProfileGoal = findViewById(R.id.textProfileGoal);
        textProfileLatestWeight = findViewById(R.id.textProfileLatestWeight);
        textProfileLastLogged = findViewById(R.id.textProfileLastLogged);
        textProfileSmsStatus = findViewById(R.id.textProfileSmsStatus);
        textProfileAlertNumber = findViewById(R.id.textProfileAlertNumber);

        // Load and display everything
        loadProfileData();
    }

    /**
     * loadProfileData()
     * Loads existing data for the logged-in user and updates the UI fields.
     * Uses the following data:
     * - session SharedPreferences for username
     * - goal table for goal weight
     * - weights table for latest weight and date
     * - sms_settings SharedPreferences for toggle and phone
     *
     * @return void
     */
    private void loadProfileData() {
        // Username
        textProfileUsername.setText(currentUsername);

        // Goal weight
        Double goal = db.getGoalWeight(currentUsername);
        if (goal == null) {
            textProfileGoal.setText(R.string.profile_not_set);
        } else {
            textProfileGoal.setText(String.format(Locale.US, "%.1f lbs", goal));
        }

        // Latest weight and last logged date
        String latestDate = null;
        Double latestWeight = null;

        try (Cursor c = db.getAllWeightsForUser(currentUsername)) {
            if (c != null && c.moveToFirst()) {
                int dateCol = c.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_DATE);
                int valCol = c.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_VALUE);

                latestDate = c.getString(dateCol);
                latestWeight = c.getDouble(valCol);
            }
        }

        if (latestWeight == null || latestDate == null) {
            textProfileLatestWeight.setText(R.string.profile_no_entries);
            textProfileLastLogged.setText(R.string.profile_no_entries);
        } else {
            textProfileLatestWeight.setText(String.format(Locale.US, "%.1f lbs", latestWeight));
            textProfileLastLogged.setText(latestDate);
        }

        // SMS status and alert number
        SharedPreferences smsPrefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);
        boolean enabledPref = smsPrefs.getBoolean(KEY_SMS_ENABLED, false);
        String phone = smsPrefs.getString(KEY_SMS_PHONE, "").trim();

        boolean hasPermission = hasSmsPermission();

        // Determine ready state
        boolean smsReady = enabledPref && hasPermission && !phone.isEmpty();

        textProfileSmsStatus.setText(smsReady ? R.string.profile_sms_enabled : R.string.profile_sms_disabled);

        if (phone.isEmpty()) {
            textProfileAlertNumber.setText(R.string.profile_alert_not_set);
        } else {
            textProfileAlertNumber.setText(maskPhone(phone));
        }
    }

    /**
     * hasSmsPermission()
     * Checks whether SEND_SMS permission has been granted.
     *
     * @return boolean - True if granted, false otherwise.
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