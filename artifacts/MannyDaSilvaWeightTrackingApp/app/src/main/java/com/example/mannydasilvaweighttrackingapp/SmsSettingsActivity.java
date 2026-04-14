package com.example.mannydasilvaweighttrackingapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * SmsSettingsActivity
 * This class allows the user to enable/disable SMS notifications and save a phone number.
 *
 * Permission is ONLY requested when the user toggles SMS ON.
 */
public class SmsSettingsActivity extends AppCompatActivity {

    // Request code used when asking the user for SEND_SMS permission
    private static final int REQ_SEND_SMS = 1001;

    // SharedPreferences constants used to save the user's SMS settings
    private static final String PREFS_SMS = "sms_settings";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";

    // UI references
    private TextView textSmsStatus;
    private SwitchMaterial switchSmsEnabled;
    private TextInputEditText editPhone;
    private MaterialButton buttonSavePhone;
    private MaterialButton buttonSendTest;

    // Stored settings for SMS
    private SharedPreferences smsPrefs;

    // Tracks whether user tried to enable SMS before granting permission
    private boolean pendingEnableToggle = false;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the UI loads saved SMS settings,
     * and sets up permission-based toggle behavior.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms_settings);

        // Load SharedPreferences used for SMS settings
        smsPrefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);

        // Connect UI elements
        textSmsStatus = findViewById(R.id.textSmsStatus);
        switchSmsEnabled = findViewById(R.id.switchSmsEnabled);
        editPhone = findViewById(R.id.editPhone);
        buttonSavePhone = findViewById(R.id.buttonSavePhone);
        buttonSendTest = findViewById(R.id.buttonSendTest);

        // Load saved settings
        boolean enabledPref = smsPrefs.getBoolean(KEY_SMS_ENABLED, false);
        String savedPhone = smsPrefs.getString(KEY_SMS_PHONE, "");

        // Populate phone field with stored number (if any)
        editPhone.setText(savedPhone);

        // If permission is not granted, force setting off even if it was previously enabled
        if (!hasSmsPermission()) {
            enabledPref = false;
            smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, false).apply();
        }

        // Apply the saved toggle value (after permission check)
        switchSmsEnabled.setChecked(enabledPref);

        // Update UI based on current state
        updateSmsUi();

        // Request permission only when user flips ON
        switchSmsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // User wants to enable SMS
            if (isChecked) {

                // If permission is already granted, enable
                if (hasSmsPermission()) {
                    smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, true).apply();
                    Toast.makeText(this, R.string.sms_enabled_toast, Toast.LENGTH_SHORT).show();
                    updateSmsUi();
                } else {
                    // Otherwise request permission
                    pendingEnableToggle = true;
                    requestSmsPermission();
                }

            } else {
                // User disabled SMS notifications
                smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, false).apply();
                Toast.makeText(this, R.string.sms_disabled_toast, Toast.LENGTH_SHORT).show();
                updateSmsUi();
            }
        });

        // Save phone number button
        buttonSavePhone.setOnClickListener(v -> {
            String phone = editPhone.getText() == null ? "" : editPhone.getText().toString().trim();

            // Validate phone input
            if (phone.isEmpty()) {
                Toast.makeText(this, R.string.phone_required, Toast.LENGTH_SHORT).show();
                return;
            }

            // Save phone number to preferences
            smsPrefs.edit().putString(KEY_SMS_PHONE, phone).apply();
            Toast.makeText(this, R.string.phone_saved, Toast.LENGTH_SHORT).show();
            updateSmsUi();
        });

        // Send test SMS button
        buttonSendTest.setOnClickListener(v -> {

            // User must have SMS enabled
            if (!smsPrefs.getBoolean(KEY_SMS_ENABLED, false)) {
                Toast.makeText(this, R.string.sms_enable_first, Toast.LENGTH_SHORT).show();
                return;
            }

            // Permission must be granted
            if (!hasSmsPermission()) {
                Toast.makeText(this, R.string.sms_permission_required_toggle, Toast.LENGTH_SHORT).show();
                updateSmsUi();
                return;
            }

            // Attempt to send a test message
            sendTestSmsIfValid();
        });
    }

    /**
     * onResume()
     * Refreshes SMS status every time the user returns to this screen.
     *
     * @return void
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateSmsUi();
    }


    /**
     * hasSmsPermission()
     * Checks whether the application currently has SEND_SMS permission.
     *
     * @return boolean - True if permission is granted.
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * requestSmsPermission()
     * Prompts the user to grant SEND_SMS permission.
     * This is triggered when the user turns the toggle ON.
     *
     * @return void
     */
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQ_SEND_SMS);
    }

    /**
     * updateSmsUi()
     * Updates the UI to reflect the true SMS state when the user enters the screen.
     * This ensures the toggle and status match stored settings and permissions.
     *
     * @return void
     */
    private void updateSmsUi() {
        boolean granted = hasSmsPermission();

        boolean enabledPref = smsPrefs.getBoolean(KEY_SMS_ENABLED, false);

        String phone = smsPrefs.getString(KEY_SMS_PHONE, "");
        phone = phone.trim();

        // SMS is only enabled if toggle is on, permission granted, and phone exists
        boolean enabled = enabledPref && granted && !phone.isEmpty();

        // Status text explaining why it might be disabled
        if (!granted && enabledPref) {
            textSmsStatus.setText(R.string.sms_status_disabled_permission);
        } else if (enabledPref && phone.isEmpty()) {
            textSmsStatus.setText(R.string.sms_status_disabled_no_phone);
        } else if (enabled) {
            textSmsStatus.setText(R.string.sms_status_enabled);
        } else {
            textSmsStatus.setText(R.string.sms_status_disabled);
        }

        // Test button only usable when SMS conditions are met
        buttonSendTest.setEnabled(enabled);

        // If permission was revoked outside the app, force toggle off and pref off
        if (!granted && switchSmsEnabled.isChecked()) {
            smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, false).apply();
            switchSmsEnabled.setChecked(false);
        }
    }

    /**
     * sendTestSmsIfValid()
     * Attempts to send a test SMS to the saved phone number.
     * Only sends if a phone number exists.
     *
     * @return void
     */
    private void sendTestSmsIfValid() {
        String phone = editPhone.getText() == null ? "" : editPhone.getText().toString().trim();

        // Validate phone input
        if (phone.isEmpty()) {
            Toast.makeText(this, R.string.phone_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Use system SMS manager to send a message
            SmsManager smsManager = getSystemService(SmsManager.class);

            // Send a test SMS
            smsManager.sendTextMessage(phone, null, "PowerScale test SMS: notifications are working.", null, null);

            Toast.makeText(this, R.string.sms_test_sent, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, R.string.sms_test_failed, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
     * Called after the user responds to the SMS permission request prompt.
     * This method applies the user decision and updates the toggle/controls.
     *
     * @param requestCode - The request code that was used.
     * @param permissions - The requested permissions.
     * @param grantResults - Results of the permission request.
     * @return void
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Only handle SEND_SMS requests
        if (requestCode == REQ_SEND_SMS) {

            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            // If permission was granted, enable SMS if user turned it on
            if (granted) {
                Toast.makeText(this, R.string.sms_permission_granted, Toast.LENGTH_SHORT).show();

                if (pendingEnableToggle) {
                    pendingEnableToggle = false;
                    smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, true).apply();
                    switchSmsEnabled.setChecked(true);
                }

            } else {
                // If permission denied, disable notifications and toggle
                Toast.makeText(this, R.string.sms_permission_denied, Toast.LENGTH_LONG).show();
                smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, false).apply();
                pendingEnableToggle = false;
                switchSmsEnabled.setChecked(false);
            }

            // Refresh UI after permission decision
            updateSmsUi();
        }
    }
}