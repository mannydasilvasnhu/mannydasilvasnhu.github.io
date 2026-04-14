package com.example.mannydasilvaweighttrackingapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * GoalWeightActivity
 * This class allows the user to set or update their goal weight.
 *
 * This screen:
 * - Displays the currently saved goal weight
 * - Validates user input
 * - Saves the goal weight to the database
 * - Resets SMS notification lock when goal changes
 */
public class GoalWeightActivity extends AppCompatActivity {

    // Database helper used for storing and getting goal weight
    private AppDatabaseHelper db;

    // Currently logged-in username from SharedPreferences
    private String currentUsername;

    // UI references
    private TextView textCurrentGoal;
    private TextInputLayout tilGoal;
    private TextInputEditText editGoal;
    private MaterialButton buttonSaveGoal;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the UI, gets the logged-in user,
     * loads the current goal weight, and sets up button listeners.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        // Initialize database helper
        db = new AppDatabaseHelper(this);

        // Get the currently logged-in username
        currentUsername = getSharedPreferences("session", MODE_PRIVATE).getString("username", null);

        // If no valid user session exists, return to login
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.goal_login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Connect UI elements
        textCurrentGoal = findViewById(R.id.textCurrentGoal);
        tilGoal = findViewById(R.id.tilGoal);
        editGoal = findViewById(R.id.editGoal);
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);

        // Display current goal (if one exists)
        loadCurrentGoal();

        // Handle goal save button click
        buttonSaveGoal.setOnClickListener(v -> saveGoalWeight());
    }

    /**
     * saveGoalWeight()
     * Validates the user input and saves the goal weight to the database.
     * If the goal changes, previously triggered SMS locks are cleared.
     *
     * @return void
     */
    private void saveGoalWeight() {

        // Clear any previous validation error
        tilGoal.setError(null);

        // Get user input
        String gStr = editGoal.getText() == null ? "" : editGoal.getText().toString().trim();

        // Validate required input
        if (gStr.isEmpty()) {
            tilGoal.setError(getString(R.string.goal_required));
            return;
        }

        double goal;

        // Validate numeric input
        try {
            goal = Double.parseDouble(gStr);
        } catch (Exception e) {
            tilGoal.setError(getString(R.string.goal_must_be_number));
            return;
        }

        // Make sure goal is positive
        if (goal <= 0) {
            tilGoal.setError(getString(R.string.goal_must_be_positive));
            return;
        }

        // Save or update goal in database
        boolean ok = db.updateOrInsertGoalWeight(currentUsername, goal);

        if (ok) {

            // Reset SMS goal notification lock if goal changes
            getSharedPreferences("sms_settings", MODE_PRIVATE).edit().remove("notified_goal_" + currentUsername).apply();

            Toast.makeText(this, R.string.goal_saved, Toast.LENGTH_SHORT).show();

            // Clear input field after successful save
            editGoal.setText("");

            // Refresh displayed goal
            loadCurrentGoal();

        } else {
            Toast.makeText(this, R.string.goal_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * loadCurrentGoal()
     * Gets the current goal weight from the database
     * and updates the UI display.
     *
     * @return void
     */
    private void loadCurrentGoal() {

        Double goal = db.getGoalWeight(currentUsername);

        if (goal == null) {
            textCurrentGoal.setText(getString(R.string.current_goal_none));
        } else {
            textCurrentGoal.setText(String.format(Locale.US, "Current Goal: %.1f lbs", goal)
            );
        }
    }
}