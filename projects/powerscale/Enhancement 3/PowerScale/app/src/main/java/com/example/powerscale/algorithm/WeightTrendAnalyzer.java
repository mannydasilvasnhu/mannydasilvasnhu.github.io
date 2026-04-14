package com.example.powerscale.algorithm;

import com.example.powerscale.model.WeightEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WeightTrendAnalyzer
 * This class provides the algorithm used to analyze weight-entry history
 * as time-series data.
 *
 * This class is responsible for:
 * - Converting stored weight entries into an ordered numeric sequence
 * - Computing a 7-day moving average using a single-pass sliding window
 * - Determining whether the latest trend is up, down, or stable
 */
public final class WeightTrendAnalyzer {

    // Rolling window size
    public static final int MOVING_AVERAGE_WINDOW = 7;

    // Threshold used to classify the difference between the latest averages
    public static final double TREND_THRESHOLD = 0.2;

    /**
     * WeightTrendAnalyzer()
     * Private constructor.
     */
    private WeightTrendAnalyzer() {
        // Utility class
    }

    /**
     * analyzeTrends(List<WeightEntry> entries)
     * Runs the full trend-analysis workflow.
     *
     * The workflow is:
     * - Reverse the list into chronological order
     * - Extract the weight values
     * - Compute all 7-day moving averages
     * - Compare the latest two averages to determine direction
     *
     * @param entries - The stored weight entries for the current user.
     * @return TrendAnalysisResult - The completed analysis result.
     */
    public static TrendAnalysisResult analyzeTrends(List<WeightEntry> entries) {

        List<Double> orderedWeights = extractWeightsInChronologicalOrder(entries);
        List<Double> movingAverages = calculateMovingAverages(orderedWeights, MOVING_AVERAGE_WINDOW);

        boolean hasMovingAverage = !movingAverages.isEmpty();
        Double latestMovingAverage = hasMovingAverage ? movingAverages.get(movingAverages.size() - 1) : null;

        TrendDirection trendDirection = determineTrendDirection(movingAverages);
        boolean hasTrendDirection = trendDirection != TrendDirection.NOT_ENOUGH_DATA;

        return new TrendAnalysisResult(movingAverages, latestMovingAverage, trendDirection, hasMovingAverage, hasTrendDirection);
    }

    /**
     * calculateMovingAverages(List<Double> weights, int windowSize)
     * Computes a moving-average sequence using a single-pass sliding window.
     *
     * The running sum is updated by:
     * - adding the new value entering the window
     * - subtracting the old value leaving the window
     *
     * Time complexity: O(n)
     *
     * @param weights - The chronological list of weight values.
     * @param windowSize - The moving-average window size.
     * @return List<Double> - The calculated moving-average values.
     */
    public static List<Double> calculateMovingAverages(List<Double> weights, int windowSize) {

        List<Double> result = new ArrayList<>();

        if (weights == null || weights.size() < windowSize || windowSize <= 0) {
            return result;
        }

        double sum = 0.0;

        // Build the first full window sum
        for (int i = 0; i < windowSize; i++) {
            sum += weights.get(i);
        }

        result.add(sum / windowSize);

        // Slide the window forward one value at a time
        for (int i = windowSize; i < weights.size(); i++) {
            sum = sum + weights.get(i) - weights.get(i - windowSize);
            result.add(sum / windowSize);
        }

        return result;
    }

    /**
     * determineTrendDirection(List<Double> movingAverages)
     * Compares the two most recent moving-average values to classify trend direction.
     *
     * Rules:
     * - diff > 0.2  -> UP
     * - diff < -0.2 -> DOWN
     * - otherwise -> STABLE
     *
     * @param movingAverages - The calculated moving-average values.
     * @return TrendDirection - The detected direction or NOT_ENOUGH_DATA.
     */
    public static TrendDirection determineTrendDirection(List<Double> movingAverages) {

        if (movingAverages == null || movingAverages.size() < 2) {
            return TrendDirection.NOT_ENOUGH_DATA;
        }

        double last = movingAverages.get(movingAverages.size() - 1);
        double previous = movingAverages.get(movingAverages.size() - 2);
        double diff = last - previous;

        if (diff > TREND_THRESHOLD) {
            return TrendDirection.UP;
        } else if (diff < -TREND_THRESHOLD) {
            return TrendDirection.DOWN;
        } else {
            return TrendDirection.STABLE;
        }
    }

    /**
     * extractWeightsInChronologicalOrder(List<WeightEntry> entries)
     * Converts the repository-provided entry list into a chronological
     * list of weight values from oldest to newest.
     *
     * @param entries - The stored weight-entry objects.
     * @return List<Double> - The chronological weight values only.
     */
    private static List<Double> extractWeightsInChronologicalOrder(List<WeightEntry> entries) {

        List<Double> orderedWeights = new ArrayList<>();

        if (entries == null || entries.isEmpty()) {
            return orderedWeights;
        }

        List<WeightEntry> chronologicalEntries = new ArrayList<>(entries);
        Collections.reverse(chronologicalEntries);

        for (WeightEntry entry : chronologicalEntries) {
            orderedWeights.add(entry.weight);
        }

        return orderedWeights;
    }
}