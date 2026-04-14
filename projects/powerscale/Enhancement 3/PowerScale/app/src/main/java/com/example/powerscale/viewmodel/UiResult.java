package com.example.powerscale.viewmodel;

/**
 * UiResult
 * Shared result objects returned from ViewModel operations.
 *
 * Any result class that is intended to be observed by the UI should expose:
 * - A standardized status
 * - A user-facing message
 */
public interface UiResult {

    /**
     * getStatus()
     *
     * @return OperationStatus - The standardized result status.
     */
    OperationStatus getStatus();

    /**
     * getMessage()
     *
     * @return String - The main user-facing message for the operation.
     */
    String getMessage();
}