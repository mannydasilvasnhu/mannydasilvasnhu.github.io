package com.example.powerscale.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.algorithm.TrendAnalysisResult;
import com.example.powerscale.algorithm.TrendDirection;
import com.example.powerscale.viewmodel.WeightLogViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * TrendsActivity
 * Displays the calculated weight trend information for the logged-in user.
 *
 * This screen shows:
 * - the latest 7-day moving average
 * - the current trend direction
 */
public class TrendsActivity extends AppCompatActivity {

    // ViewModel providing observed weight and trend-analysis data
    private WeightLogViewModel viewModel;

    // UI fields used to display trend results
    private TextView textMovingAverageValue;
    private TextView textTrendDirectionValue;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the Trends screen and binds trend data to the UI.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);

        // Connect UI elements
        textMovingAverageValue = findViewById(R.id.textMovingAverageValue);
        textTrendDirectionValue = findViewById(R.id.textTrendDirectionValue);
        MaterialButton buttonBackToLog = findViewById(R.id.buttonBackToLog);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WeightLogViewModel.class);

        String username = getLoggedInUsername();
        if (username == null || username.trim().isEmpty()) {
            finish();
            return;
        }

        viewModel.startObservingWeights(username);
        observeTrendAnalysis();

        buttonBackToLog.setOnClickListener(v -> finish());
    }

    /**
     * observeTrendAnalysis()
     * Observes the trend-analysis LiveData and updates the UI whenever
     * the calculated result changes.
     */
    private void observeTrendAnalysis() {
        viewModel.getTrendAnalysisResult().observe(this, this::renderTrendAnalysis);
    }

    /**
     * renderTrendAnalysis(TrendAnalysisResult result)
     * Displays the latest moving average and trend direction on screen.
     *
     * @param result - The latest trend-analysis result from the ViewModel.
     */
    private void renderTrendAnalysis(TrendAnalysisResult result) {
        if (result == null) {
            showNotEnoughData();
            return;
        }

        if (result.hasMovingAverage() && result.getLatestMovingAverage() != null) {
            String formattedAverage = getString(R.string.trend_average_format, result.getLatestMovingAverage());
            textMovingAverageValue.setText(formattedAverage);
        } else {
            textMovingAverageValue.setText(R.string.trend_not_enough_data);
        }

        if (result.hasTrendDirection()) {
            textTrendDirectionValue.setText(getTrendDirectionText(result.getTrendDirection()));
        } else {
            textTrendDirectionValue.setText(R.string.trend_not_enough_data);
        }
    }

    /**
     * showNotEnoughData()
     * Displays the fallback state used when trend analysis cannot be shown.
     */
    private void showNotEnoughData() {
        textMovingAverageValue.setText(R.string.trend_not_enough_data);
        textTrendDirectionValue.setText(R.string.trend_not_enough_data);
    }

    /**
     * getTrendDirectionText(TrendDirection direction)
     * Maps the trend-direction enum to the user-facing string resource.
     *
     * @param direction - The detected trend direction.
     * @return String - The display text shown to the user.
     */
    private String getTrendDirectionText(TrendDirection direction) {
        if (direction == null) {
            return getString(R.string.trend_not_enough_data);
        }

        switch (direction) {
            case UP:
                return getString(R.string.trend_direction_up);
            case DOWN:
                return getString(R.string.trend_direction_down);
            case STABLE:
                return getString(R.string.trend_direction_stable);
            case NOT_ENOUGH_DATA:
            default:
                return getString(R.string.trend_not_enough_data);
        }
    }

    /**
     * getLoggedInUsername()
     * Reads the currently logged-in username from SharedPreferences.
     *
     * @return String - The active username, or null if not found.
     */
    private String getLoggedInUsername() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        return prefs.getString("username", null);
    }
}