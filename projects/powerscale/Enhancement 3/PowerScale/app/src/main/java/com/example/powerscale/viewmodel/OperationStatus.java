package com.example.powerscale.viewmodel;

/**
 * OperationStatus
 * Defines the standardized outcome types returned to the UI.
 *
 * These values are used across screen flows so the UI can consistently
 * react to successful, failed, or warning-based operations.
 */
public enum OperationStatus {
    SUCCESS,
    ERROR,
    WARNING
}