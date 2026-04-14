package com.example.powerscale.viewmodel;

/**
 * AuthResult
 * Represents the final outcome of a login or account creation attempt.
 *
 * This object gives the UI everything it needs in one place, which includes:
 * - The standardized result status
 * - The message to display
 * - Whether login succeeded
 * - Whether account creation succeeded
 * - Optional field-level errors for username or password
 */
public class AuthResult implements UiResult {

    private final OperationStatus status;
    private final String message;
    private final boolean loginSuccess;
    private final boolean accountCreated;
    private final String usernameError;
    private final String passwordError;

    /**
     * AuthResult(OperationStatus status, String message, boolean loginSuccess,
     * boolean accountCreated, String usernameError, String passwordError)
     * Creates a result used by the UI after login or account creation logic completes.
     *
     * @param status - The standardized result status.
     * @param message - The message to display to the user.
     * @param loginSuccess - True if login succeeded.
     * @param accountCreated - True if account creation succeeded.
     * @param usernameError - The optional username field error.
     * @param passwordError - The optional password field error.
     */
    public AuthResult(OperationStatus status, String message, boolean loginSuccess, boolean accountCreated, String usernameError, String passwordError) {
        this.status = status;
        this.message = message;
        this.loginSuccess = loginSuccess;
        this.accountCreated = accountCreated;
        this.usernameError = usernameError;
        this.passwordError = passwordError;
    }

    /**
     * successLogin(String message)
     * Creates a successful login result.
     *
     * @param message - The success message to display.
     * @return AuthResult - The successful login result object.
     */
    public static AuthResult successLogin(String message) {
        return new AuthResult(OperationStatus.SUCCESS, message, true, false, null, null);
    }

    /**
     * successAccountCreated(String message)
     * Creates a successful account creation result.
     *
     * @param message - The success message to display.
     * @return AuthResult - The successful account creation result object.
     */
    public static AuthResult successAccountCreated(String message) {
        return new AuthResult(OperationStatus.SUCCESS, message, false, true, null, null);
    }

    /**
     * error(String message)
     * Creates a general authentication error result.
     *
     * @param message - The message to display.
     * @return AuthResult - The general error result object.
     */
    public static AuthResult error(String message) {
        return new AuthResult(OperationStatus.ERROR, message, false, false, null, null);
    }

    /**
     * validationError(String message, String usernameError, String passwordError)
     * Creates an authentication result with optional field-level validation errors.
     *
     * @param message - The message associated with the validation issue.
     * @param usernameError - The optional username field error.
     * @param passwordError - The optional password field error.
     * @return AuthResult - The validation error result object.
     */
    public static AuthResult validationError(String message, String usernameError, String passwordError) {
        return new AuthResult(OperationStatus.ERROR, message, false, false, usernameError, passwordError);
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
     * isLoginSuccess()
     *
     * @return boolean - True if login succeeded.
     */
    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    /**
     * isAccountCreated()
     *
     * @return boolean - True if account creation succeeded.
     */
    public boolean isAccountCreated() {
        return accountCreated;
    }

    /**
     * getUsernameError()
     *
     * @return String - The optional username field error.
     */
    public String getUsernameError() {
        return usernameError;
    }

    /**
     * getPasswordError()
     *
     * @return String - The optional password field error.
     */
    public String getPasswordError() {
        return passwordError;
    }
}