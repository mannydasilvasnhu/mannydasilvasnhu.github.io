package com.example.powerscale.algorithm;

/**
 * TrendDirection
 * This class represents the possible outcomes of the weight trend analysis.
 *
 * These values are used after comparing the two most recent
 * moving-average values.
 */
public enum TrendDirection {
    UP,
    DOWN,
    STABLE,
    NOT_ENOUGH_DATA
}