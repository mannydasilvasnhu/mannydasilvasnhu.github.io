package com.example.powerscale.viewmodel;

/**
 * WeightSubmissionResult
 * Represents the final outcome of a weight log operation.
 *
 * This object gives the UI everything it needs in one place, which includes:
 * - The standardized result status
 * - The main message to display
 * - Whether the action was an update
 * - Whether the weight save itself succeeded
 * - Optional field-level validation errors
 * - An optional warning message for non-blocking issues
 */
public class WeightSubmissionResult implements UiResult {

    private final OperationStatus status;
    private final String message;
    private final boolean update;
    private final boolean weightSaved;
    private final String dateError;
    private final String weightError;
    private final String warningMessage;

    /**
     * WeightSubmissionResult(OperationStatus status, String message, boolean update,
     * boolean weightSaved, String dateError, String weightError, String warningMessage)
     * Creates a submission result used by the UI after a weight log action completes.
     *
     * @param status - The standardized result status.
     * @param message - The main message to display.
     * @param update - True if the operation was an update rather than an add.
     * @param weightSaved - True if the weight entry was successfully saved.
     * @param dateError - The optional date field error.
     * @param weightError - The optional weight field error.
     * @param warningMessage - The optional warning message for non-blocking issues.
     */
    public WeightSubmissionResult(OperationStatus status, String message, boolean update, boolean weightSaved, String dateError, String weightError, String warningMessage) {
        this.status = status;
        this.message = message;
        this.update = update;
        this.weightSaved = weightSaved;
        this.dateError = dateError;
        this.weightError = weightError;
        this.warningMessage = warningMessage;
    }

    /**
     * success(String message, boolean update)
     * Creates a successful add or update result.
     *
     * @param message - The success message to display.
     * @param update - True if the operation was an update.
     * @return WeightSubmissionResult - The successful result.
     */
    public static WeightSubmissionResult success(String message, boolean update) {
        return new WeightSubmissionResult(OperationStatus.SUCCESS, message, update, true, null, null, null);
    }

    /**
     * warning(String message, boolean update, String warningMessage)
     * Creates a result where the weight operation succeeded, but a non-blocking warning occurred.
     *
     * @param message - The main success message to display.
     * @param update - True if the operation was an update.
     * @param warningMessage - The warning message to expose to the UI.
     * @return WeightSubmissionResult - The warning-based result.
     */
    public static WeightSubmissionResult warning(String message, boolean update, String warningMessage) {
        return new WeightSubmissionResult(OperationStatus.WARNING, message, update, true, null, null, warningMessage);
    }

    /**
     * error(String message, boolean update)
     * Creates a failed add or update result.
     *
     * @param message - The error message to display.
     * @param update - True if the failed operation was an update.
     * @return WeightSubmissionResult - The failed result.
     */
    public static WeightSubmissionResult error(String message, boolean update) {
        return new WeightSubmissionResult(OperationStatus.ERROR, message, update, false, null, null, null);
    }

    /**
     * validationError(String dateError, String weightError)
     * Creates a validation error result for form input.
     *
     * @param dateError - The optional date field error.
     * @param weightError - The optional weight field error.
     * @return WeightSubmissionResult - The validation result.
     */
    public static WeightSubmissionResult validationError(String dateError, String weightError) {
        return new WeightSubmissionResult(OperationStatus.ERROR, null, false, false, dateError, weightError, null);
    }

    /**
     * deleteResult(String message, boolean success)
     * Creates a result for a delete operation.
     *
     * @param message - The message to display.
     * @param success - True if the delete succeeded.
     * @return WeightSubmissionResult - The delete result.
     */
    public static WeightSubmissionResult deleteResult(String message, boolean success) {
        return new WeightSubmissionResult(success ? OperationStatus.SUCCESS : OperationStatus.ERROR, message, false, false, null, null, null);
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
     * @return String - The main user-facing message for the UI.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * isUpdate()
     *
     * @return boolean - True if the completed action was an update.
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * isWeightSaved()
     *
     * @return boolean - True if the weight entry was saved successfully.
     */
    public boolean isWeightSaved() {
        return weightSaved;
    }

    /**
     * getDateError()
     *
     * @return String - The optional date field error.
     */
    public String getDateError() {
        return dateError;
    }

    /**
     * getWeightError()
     *
     * @return String - The optional weight field error.
     */
    public String getWeightError() {
        return weightError;
    }

    /**
     * getWarningMessage()
     *
     * @return String - The optional warning message for non-blocking issues.
     */
    public String getWarningMessage() {
        return warningMessage;
    }
}