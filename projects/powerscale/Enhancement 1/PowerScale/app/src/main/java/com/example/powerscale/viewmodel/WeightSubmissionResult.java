package com.example.powerscale.viewmodel;

/**
 * WeightSubmissionResult
 * Represents the final outcome of a successful add or update operation.
 *
 * This object gives the UI everything it needs in one place, which includes:
 * - The success message to display.
 * - Whether the submitted weight reached the user's goal.
 * - Whether the action was an update.
 */
public class WeightSubmissionResult {

    private final String message;
    private final boolean goalReached;
    private final boolean update;

    /**
     * WeightSubmissionResult(String message, boolean goalReached, boolean update)
     * Creates a submission result used by the UI after a successful save/update.
     *
     * @param message - The success message to display.
     * @param goalReached - True if the submitted weight met the user's goal.
     * @param update - True if the operation was an update rather than an add.
     */
    public WeightSubmissionResult(String message, boolean goalReached, boolean update) {
        this.message = message;
        this.goalReached = goalReached;
        this.update = update;
    }

    /**
     * getMessage()
     *
     * @return String - The success message for the UI.
     */
    public String getMessage() {
        return message;
    }

    /**
     * isGoalReached()
     *
     * @return boolean - True if the submitted weight met the goal.
     */
    public boolean isGoalReached() {
        return goalReached;
    }

    /**
     * isUpdate()
     *
     * @return boolean - True if the completed action was an update.
     */
    public boolean isUpdate() {
        return update;
    }
}