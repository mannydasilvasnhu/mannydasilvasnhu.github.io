package com.example.powerscale.algorithm;

import java.util.List;

/**
 * TrendAnalysisResult
 * This class stores the final output of the weight trend analysis algorithm.
 *
 * This includes:
 * - The calculated moving averages
 * - The latest available 7-day moving average
 * - The final trend direction result
 * - Whether enough data existed to calculate averages
 * - Whether enough averages existed to determine direction
 */
public class TrendAnalysisResult {

    private final List<Double> movingAverages;
    private final Double latestMovingAverage;
    private final TrendDirection trendDirection;
    private final boolean hasMovingAverage;
    private final boolean hasTrendDirection;

    /**
     * TrendAnalysisResult(List<Double> movingAverages, Double latestMovingAverage,
     * TrendDirection trendDirection, boolean hasMovingAverage, boolean hasTrendDirection)
     * Creates an immutable result object for trend analysis.
     *
     * @param movingAverages - All calculated moving-average values.
     * @param latestMovingAverage - The most recent 7-day moving average, or null.
     * @param trendDirection - The final detected trend direction.
     * @param hasMovingAverage - True if at least one moving average was calculated.
     * @param hasTrendDirection - True if enough averages existed to determine direction.
     */
    public TrendAnalysisResult(List<Double> movingAverages, Double latestMovingAverage, TrendDirection trendDirection, boolean hasMovingAverage, boolean hasTrendDirection) {
        this.movingAverages = movingAverages;
        this.latestMovingAverage = latestMovingAverage;
        this.trendDirection = trendDirection;
        this.hasMovingAverage = hasMovingAverage;
        this.hasTrendDirection = hasTrendDirection;
    }

    /**
     * getMovingAverages()
     *
     * @return List<Double> - All calculated moving-average values.
     */
    public List<Double> getMovingAverages() {
        return movingAverages;
    }

    /**
     * getLatestMovingAverage()
     *
     * @return Double - The most recent 7-day moving average, or null.
     */
    public Double getLatestMovingAverage() {
        return latestMovingAverage;
    }

    /**
     * getTrendDirection()
     *
     * @return TrendDirection - The final trend direction result.
     */
    public TrendDirection getTrendDirection() {
        return trendDirection;
    }

    /**
     * hasMovingAverage()
     *
     * @return boolean - True if enough data existed to calculate at least one average.
     */
    public boolean hasMovingAverage() {
        return hasMovingAverage;
    }

    /**
     * hasTrendDirection()
     *
     * @return boolean - True if enough averages existed to determine trend direction.
     */
    public boolean hasTrendDirection() {
        return hasTrendDirection;
    }
}