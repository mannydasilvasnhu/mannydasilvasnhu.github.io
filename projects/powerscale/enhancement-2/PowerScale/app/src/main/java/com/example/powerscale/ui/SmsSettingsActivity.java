package com.example.powerscale.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.viewmodel.SmsSettingsResult;
import com.example.powerscale.viewmodel.SmsSettingsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * SmsSettingsActivity
 * This activity allows the user to enable or disable SMS notifications,
 * save a phone number, and send a test message.
 *
 * This screen renders a standardized SmsSettingsResult returned by the ViewModel.
 */
public class SmsSettingsActivity extends AppCompatActivity {

    // Request code used when asking the user for SEND_SMS permission
    private static final int REQ_SEND_SMS = 1001;

    // ViewModel used for SMS settings flow
    private SmsSettingsViewModel viewModel;

    // UI references
    private TextView textSmsStatus;
    private SwitchMaterial switchSmsEnabled;
    private TextInputEditText editPhone;
    private MaterialButton buttonSavePhone;
    private MaterialButton buttonSendTest;

    // Tracks whether user tried to enable SMS before granting permission
    private boolean pendingEnableToggle = false;

    // Prevents listener re-entry when UI state updates toggle programmatically
    private boolean suppressToggleCallback = false;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the UI, loads SMS settings state,
     * and wires observers to the ViewModel.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SmsSettingsViewModel.class);

        // Connect UI elements
        textSmsStatus = findViewById(R.id.textSmsStatus);
        switchSmsEnabled = findViewById(R.id.switchSmsEnabled);
        editPhone = findViewById(R.id.editPhone);
        buttonSavePhone = findViewById(R.id.buttonSavePhone);
        buttonSendTest = findViewById(R.id.buttonSendTest);

        // Observe saved phone value displayed in the input field
        viewModel.getSavedPhone().observe(this, phone -> {
            if (phone != null && !phone.equals(getCurrentPhoneText())) {
                editPhone.setText(phone);
            }
        });

        // Observe one-time SMS settings results from the ViewModel
        viewModel.getSmsResult().observe(this, event -> {
            if (event == null) {
                return;
            }

            SmsSettingsResult result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            applySmsState(result);

            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Request permission only when user flips ON
        switchSmsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressToggleCallback) {
                return;
            }

            if (isChecked) {
                // If permission already exists, enable immediately
                if (hasSmsPermission()) {
                    pendingEnableToggle = false;
                    viewModel.handleToggleChanged(true);
                } else {
                    // Otherwise trigger the Android permission popup
                    pendingEnableToggle = true;
                    requestSmsPermission();

                    // Keep the switch visually off until the user responds
                    suppressToggleCallback = true;
                    switchSmsEnabled.setChecked(false);
                    suppressToggleCallback = false;
                }
            } else {
                pendingEnableToggle = false;
                viewModel.handleToggleChanged(false);
            }
        });

        // Save phone number button
        buttonSavePhone.setOnClickListener(v ->
                viewModel.savePhone(getCurrentPhoneText())
        );

        // Send test SMS button
        buttonSendTest.setOnClickListener(v ->
                viewModel.sendTestSms(getCurrentPhoneText())
        );

        // Initial settings load
        viewModel.loadSmsSettings();
    }

    /**
     * onResume()
     * Refreshes SMS settings state every time the user returns to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadSmsSettings();
    }

    /**
     * getCurrentPhoneText()
     *
     * @return String - The current phone text entered in the UI.
     */
    private String getCurrentPhoneText() {
        return editPhone.getText() == null ? "" : editPhone.getText().toString().trim();
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
     */
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQ_SEND_SMS);
    }

    /**
     * applySmsState(SmsSettingsResult result)
     * Applies the SMS settings state returned from the ViewModel to the UI.
     *
     * @param result - The result containing UI state and messages.
     */
    private void applySmsState(SmsSettingsResult result) {
        editPhone.setError(result.getPhoneError());
        textSmsStatus.setText(result.getStatusText());
        buttonSendTest.setEnabled(result.isTestButtonEnabled());

        suppressToggleCallback = true;
        switchSmsEnabled.setChecked(result.isToggleChecked());
        suppressToggleCallback = false;
    }

    /**
     * onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
     * Called after the user responds to the SMS permission request prompt.
     *
     * @param requestCode - The request code that was used.
     * @param permissions - The requested permissions.
     * @param grantResults - The results of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_SEND_SMS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!pendingEnableToggle) {
                viewModel.loadSmsSettings();
                return;
            }

            pendingEnableToggle = false;
            viewModel.handlePermissionResult(granted);
        }
    }
}