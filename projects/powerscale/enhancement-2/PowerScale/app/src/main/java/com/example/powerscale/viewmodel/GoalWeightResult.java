package com.example.powerscale.viewmodel;

/**
 * GoalWeightResult
 * Represents the final outcome of a goal weight load or save operation.
 *
 * This object gives the UI everything it needs in one place, which includes:
 * - The standardized result status
 * - The message to display
 * - Optional field-level validation error for the goal weight input
 * - The currently loaded goal weight display text when applicable
 */
public class GoalWeightResult implements UiResult {

    private final OperationStatus status;
    private final String message;
    private final String goalError;
    private final String goalDisplayValue;

    /**
     * GoalWeightResult(OperationStatus status, String message, String goalError, String goalDisplayValue)
     * Creates a result used by the UI after loading or saving goal weight data.
     *
     * @param status - The standardized result status.
     * @param message - The main user-facing message.
     * @param goalError - The optional goal weight field error.
     * @param goalDisplayValue - The optional goal weight display value for the UI.
     */
    public GoalWeightResult(OperationStatus status, String message, String goalError, String goalDisplayValue) {
        this.status = status;
        this.message = message;
        this.goalError = goalError;
        this.goalDisplayValue = goalDisplayValue;
    }

    /**
     * success(String message, String goalDisplayValue)
     * Creates a successful goal weight result.
     *
     * @param message - The success message to display.
     * @param goalDisplayValue - The goal weight value to display in the UI.
     * @return GoalWeightResult - The successful result object.
     */
    public static GoalWeightResult success(String message, String goalDisplayValue) {
        return new GoalWeightResult(OperationStatus.SUCCESS, message, null, goalDisplayValue);
    }

    /**
     * error(String message)
     * Creates a general error result.
     *
     * @param message - The error message to display.
     * @return GoalWeightResult - The general error result object.
     */
    public static GoalWeightResult error(String message) {
        return new GoalWeightResult(OperationStatus.ERROR, message, null, null);
    }

    /**
     * validationError(String goalError)
     * Creates a validation error result for the goal input field.
     *
     * @param goalError - The validation error message for the input field.
     * @return GoalWeightResult - The validation error result object.
     */
    public static GoalWeightResult validationError(String goalError) {
        return new GoalWeightResult(OperationStatus.ERROR, null, goalError, null);
    }

    /**
     * loaded(String goalDisplayValue)
     * Creates a load result containing the current stored goal weight.
     *
     * @param goalDisplayValue - The goal weight value to display in the UI.
     * @return GoalWeightResult - The load result object.
     */
    public static GoalWeightResult loaded(String goalDisplayValue) {
        return new GoalWeightResult(OperationStatus.SUCCESS, null, null, goalDisplayValue);
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
     * getGoalError()
     *
     * @return String - The optional goal field validation error.
     */
    public String getGoalError() {
        return goalError;
    }

    /**
     * getGoalDisplayValue()
     *
     * @return String - The optional goal weight display value for the UI.
     */
    public String getGoalDisplayValue() {
        return goalDisplayValue;
    }
}