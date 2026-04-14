package com.example.powerscale.viewmodel;

/**
 * SmsSettingsResult
 * Represents the final outcome of an SMS settings action.
 *
 * This object gives the UI everything it needs in one place, which includes:
 * - The standardized result status
 * - The message to display
 * - Optional field-level validation error for the phone number
 * - The current SMS status text for the screen
 * - Whether the test button should be enabled
 * - Whether the toggle should be checked
 */
public class SmsSettingsResult implements UiResult {

    private final OperationStatus status;
    private final String message;
    private final String phoneError;
    private final String statusText;
    private final boolean toggleChecked;
    private final boolean testButtonEnabled;

    /**
     * SmsSettingsResult(OperationStatus status, String message, String phoneError,
     * String statusText, boolean toggleChecked, boolean testButtonEnabled)
     * Creates a result used by the UI after an SMS settings action completes.
     *
     * @param status - The standardized result status.
     * @param message - The main user-facing message.
     * @param phoneError - The optional phone field validation error.
     * @param statusText - The SMS status text to display.
     * @param toggleChecked - True if the toggle should appear checked.
     * @param testButtonEnabled - True if the test button should be enabled.
     */
    public SmsSettingsResult(OperationStatus status, String message, String phoneError, String statusText, boolean toggleChecked, boolean testButtonEnabled) {
        this.status = status;
        this.message = message;
        this.phoneError = phoneError;
        this.statusText = statusText;
        this.toggleChecked = toggleChecked;
        this.testButtonEnabled = testButtonEnabled;
    }

    /**
     * state(OperationStatus status, String message, String phoneError,
     * String statusText, boolean toggleChecked, boolean testButtonEnabled)
     * Creates a result containing the current full SMS UI state.
     *
     * @param status - The standardized result status.
     * @param message - The optional user-facing message.
     * @param phoneError - The optional phone field validation error.
     * @param statusText - The SMS status text to display.
     * @param toggleChecked - True if the toggle should appear checked.
     * @param testButtonEnabled - True if the test button should be enabled.
     * @return SmsSettingsResult - The full UI state result.
     */
    public static SmsSettingsResult state(OperationStatus status, String message, String phoneError, String statusText, boolean toggleChecked, boolean testButtonEnabled) {
        return new SmsSettingsResult(status, message, phoneError, statusText, toggleChecked, testButtonEnabled);
    }

    /**
     * getStatus()
     *
     * @return OperationStatus - The standardized result status.
     */
    @Override
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * getMessage()
     *
     * @return String - The main user-facing message.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * getPhoneError()
     *
     * @return String - The optional phone number validation error.
     */
    public String getPhoneError() {
        return phoneError;
    }

    /**
     * getStatusText()
     *
     * @return String - The SMS status text for the screen.
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     * isToggleChecked()
     *
     * @return boolean - True if the toggle should appear checked.
     */
    public boolean isToggleChecked() {
        return toggleChecked;
    }

    /**
     * isTestButtonEnabled()
     *
     * @return boolean - True if the test button should be enabled.
     */
    public boolean isTestButtonEnabled() {
        return testButtonEnabled;
    }
}