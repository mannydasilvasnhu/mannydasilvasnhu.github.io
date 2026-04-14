package com.example.powerscale.viewmodel;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.powerscale.R;
import com.example.powerscale.repository.SmsSettingsRepository;
import com.example.powerscale.utils.Event;

/**
 * SmsSettingsViewModel
 * This ViewModel manages SMS settings and related business logic.
 *
 * This class:
 * - Loads the saved SMS settings state for the logged-in user
 * - Validates the phone number input
 * - Updates SMS enabled preference in the database
 * - Builds standardized UI result objects
 * - Sends a test SMS when conditions are valid
 */
public class SmsSettingsViewModel extends AndroidViewModel {

    // Repository used for SMS settings persistence
    private final SmsSettingsRepository repository;

    // The currently logged-in username retrieved from session
    private final String username;

    // Observable saved phone value displayed in the UI field
    private final MutableLiveData<String> savedPhone = new MutableLiveData<>();

    // One-time result event consumed by the UI
    private final MutableLiveData<Event<SmsSettingsResult>> smsResult = new MutableLiveData<>();

    /**
     * SmsSettingsViewModel(Application application)
     * Initializes the ViewModel and repository instance.
     *
     * @param application - The application context required for repository initialization.
     */
    public SmsSettingsViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository used for database operations
        repository = new SmsSettingsRepository(application);

        // Retrieve the logged-in username from the session
        username = application.getSharedPreferences("session", Application.MODE_PRIVATE).getString("username", "");
    }

    /**
     * getSavedPhone()
     *
     * @return LiveData<String> - The saved phone number text shown in the UI.
     */
    public LiveData<String> getSavedPhone() {
        return savedPhone;
    }

    /**
     * getSmsResult()
     *
     * @return LiveData<Event<SmsSettingsResult>> - The one-time SMS settings result event.
     */
    public LiveData<Event<SmsSettingsResult>> getSmsResult() {
        return smsResult;
    }

    /**
     * loadSmsSettings()
     * Loads the saved SMS settings for the logged-in user and emits the current UI state.
     */
    public void loadSmsSettings() {
        boolean hasPermission = hasSmsPermission();
        boolean enabledPref = repository.isSmsEnabledPreference(username);
        String phone = repository.getSavedPhone(username).trim();

        // If permission is not granted, force the stored enabled state off
        if (!hasPermission && enabledPref) {
            repository.setSmsEnabledPreference(username, false);
            enabledPref = false;
        }

        savedPhone.setValue(phone);

        smsResult.setValue(new Event<>(buildStateResult(enabledPref, hasPermission, phone, null, null, OperationStatus.SUCCESS)));
    }

    /**
     * handleToggleChanged(boolean wantsEnabled)
     * Handles toggle changes initiated by the UI.
     *
     * @param wantsEnabled - True if the user wants SMS enabled.
     */
    public void handleToggleChanged(boolean wantsEnabled) {
        boolean hasPermission = hasSmsPermission();
        String phone = repository.getSavedPhone(username).trim();

        if (wantsEnabled) {
            // The Activity should request permission before enabling if permission is missing
            if (!hasPermission) {
                smsResult.setValue(new Event<>(buildStateResult(false, false, phone, null, null, OperationStatus.ERROR)));
                return;
            }

            repository.setSmsEnabledPreference(username, true);

            smsResult.setValue(new Event<>(buildStateResult(true, true, phone, getApplication().getString(R.string.sms_enabled_toast), null, OperationStatus.SUCCESS)));
            return;
        }

        repository.setSmsEnabledPreference(username, false);

        smsResult.setValue(new Event<>(buildStateResult(false, hasPermission, phone, getApplication().getString(R.string.sms_disabled_toast), null, OperationStatus.SUCCESS)));
    }

    /**
     * handlePermissionResult(boolean granted)
     * Applies the result of the SEND_SMS permission request.
     *
     * @param granted - True if permission was granted.
     */
    public void handlePermissionResult(boolean granted) {
        String phone = repository.getSavedPhone(username).trim();

        if (granted) {
            repository.setSmsEnabledPreference(username, true);

            smsResult.setValue(new Event<>(buildStateResult(true, true, phone, getApplication().getString(R.string.sms_permission_granted), null, OperationStatus.SUCCESS)));
            return;
        }

        repository.setSmsEnabledPreference(username, false);

        smsResult.setValue(new Event<>(buildStateResult(false, false, phone, getApplication().getString(R.string.sms_permission_denied), null, OperationStatus.ERROR)));
    }

    /**
     * savePhone(String phoneText)
     * Validates and stores the SMS phone number for the logged-in user.
     *
     * @param phoneText - The entered phone number text.
     */
    public void savePhone(String phoneText) {
        String phone = phoneText == null ? "" : phoneText.trim();
        boolean hasPermission = hasSmsPermission();
        boolean enabledPref = repository.isSmsEnabledPreference(username);

        if (phone.isEmpty()) {
            smsResult.setValue(new Event<>(buildStateResult(enabledPref, hasPermission, repository.getSavedPhone(username).trim(), null, getApplication().getString(R.string.phone_required), OperationStatus.ERROR)));
            return;
        }

        repository.savePhone(username, phone);
        savedPhone.setValue(phone);

        smsResult.setValue(new Event<>(buildStateResult(enabledPref, hasPermission, phone, getApplication().getString(R.string.phone_saved), null, OperationStatus.SUCCESS)));
    }

    /**
     * sendTestSms(String phoneText)
     * Validates current state and attempts to send a test SMS.
     *
     * @param phoneText - The phone number currently shown in the field.
     */
    public void sendTestSms(String phoneText) {
        String phone = phoneText == null ? "" : phoneText.trim();
        boolean hasPermission = hasSmsPermission();
        boolean enabledPref = repository.isSmsEnabledPreference(username);

        if (!enabledPref) {
            smsResult.setValue(new Event<>(buildStateResult(false, hasPermission, repository.getSavedPhone(username).trim(), getApplication().getString(R.string.sms_enable_first), null, OperationStatus.ERROR)));
            return;
        }

        if (!hasPermission) {
            repository.setSmsEnabledPreference(username, false);

            smsResult.setValue(new Event<>(buildStateResult(false, false, repository.getSavedPhone(username).trim(), getApplication().getString(R.string.sms_permission_required_toggle), null, OperationStatus.ERROR)));
            return;
        }

        if (phone.isEmpty()) {
            smsResult.setValue(new Event<>(buildStateResult(true, true, repository.getSavedPhone(username).trim(), null, getApplication().getString(R.string.phone_required), OperationStatus.ERROR)));
            return;
        }

        try {
            SmsManager smsManager = getApplication().getSystemService(SmsManager.class);

            if (smsManager == null) {
                smsResult.setValue(new Event<>(buildStateResult(true, true, phone, getApplication().getString(R.string.sms_test_failed), null, OperationStatus.ERROR)));
                return;
            }

            smsManager.sendTextMessage(phone, null, "PowerScale test SMS: notifications are working.", null, null);

            smsResult.setValue(new Event<>(buildStateResult(true, true, phone, getApplication().getString(R.string.sms_test_sent), null, OperationStatus.SUCCESS)));

        } catch (Exception e) {
            smsResult.setValue(new Event<>(buildStateResult(true, true, phone, getApplication().getString(R.string.sms_test_failed), null, OperationStatus.ERROR)));
        }
    }

    /**
     * buildStateResult(boolean enabledPref, boolean hasPermission, String phone,
     * String message, String phoneError, OperationStatus status)
     * Builds the full UI state returned to the Activity.
     *
     * @param enabledPref - True if SMS is enabled in the database.
     * @param hasPermission - True if SEND_SMS permission is granted.
     * @param phone - The stored or entered phone number.
     * @param message - The optional user-facing message.
     * @param phoneError - The optional phone field validation error.
     * @param status - The standardized result status.
     * @return SmsSettingsResult - The result used by the UI.
     */
    private SmsSettingsResult buildStateResult(boolean enabledPref, boolean hasPermission, String phone, String message, String phoneError, OperationStatus status) {
        boolean smsReady = enabledPref && hasPermission && !phone.isEmpty();

        String statusText;

        if (!hasPermission && enabledPref) {
            statusText = getApplication().getString(R.string.sms_status_disabled_permission);
        } else if (enabledPref && phone.isEmpty()) {
            statusText = getApplication().getString(R.string.sms_status_disabled_no_phone);
        } else if (smsReady) {
            statusText = getApplication().getString(R.string.sms_status_enabled);
        } else {
            statusText = getApplication().getString(R.string.sms_status_disabled);
        }

        return SmsSettingsResult.state(status, message, phoneError, statusText, enabledPref && hasPermission, smsReady);
    }

    /**
     * hasSmsPermission()
     * Checks whether the application currently has SEND_SMS permission.
     *
     * @return boolean - True if SEND_SMS permission is granted.
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
}
