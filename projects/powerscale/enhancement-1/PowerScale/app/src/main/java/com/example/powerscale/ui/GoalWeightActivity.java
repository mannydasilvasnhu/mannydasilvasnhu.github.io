package com.example.powerscale.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.viewmodel.GoalWeightViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * GoalWeightActivity
 * This activity allows the user to set or update their goal weight.
 *
 * This screen:
 * - Displays the currently saved goal weight
 * - Validates user input
 * - Saves the goal weight through the ViewModel
 * - Resets SMS notification lock when goal changes
 */
public class GoalWeightActivity extends AppCompatActivity {

    // ViewModel used for goal weight operations
    private GoalWeightViewModel viewModel;

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
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        // Get the currently logged-in username
        currentUsername = getSharedPreferences("session", MODE_PRIVATE).getString("username", null);

        // If no valid user session exists, return to login
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.goal_login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(GoalWeightViewModel.class);

        // Connect UI elements
        textCurrentGoal = findViewById(R.id.textCurrentGoal);
        tilGoal = findViewById(R.id.tilGoal);
        editGoal = findViewById(R.id.editGoal);
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);

        // Observe goal value
        viewModel.getGoalWeight().observe(this, goal -> {
            if (goal == null) {
                textCurrentGoal.setText(getString(R.string.current_goal_none));
            } else {
                textCurrentGoal.setText(String.format(Locale.US, "Current Goal: %.1f lbs", goal));
            }
        });

        // Observe messages
        viewModel.getMessage().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) {
                return;
            }

            if (msg.equals("Please enter a goal weight.")) {
                tilGoal.setError(getString(R.string.goal_required));
                return;
            }

            if (msg.equals("Please enter a valid numeric goal weight.")) {
                tilGoal.setError(getString(R.string.goal_must_be_number));
                return;
            }

            if (msg.equals("Goal weight must be greater than 0.")) {
                tilGoal.setError(getString(R.string.goal_must_be_positive));
                return;
            }

            if (msg.equals("Goal weight saved successfully.")) {
                getSharedPreferences("sms_settings", MODE_PRIVATE)
                        .edit()
                        .remove("notified_goal_" + currentUsername)
                        .apply();

                editGoal.setText("");
            }

            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Load current goal
        viewModel.loadGoalWeight(currentUsername);

        // Handle goal save button click
        buttonSaveGoal.setOnClickListener(v -> saveGoalWeight());
    }

    /**
     * saveGoalWeight()
     * Sends the entered goal weight to the ViewModel.
     */
    private void saveGoalWeight() {
        // Clear any previous validation error before attempting to save
        tilGoal.setError(null);

        String goalText = editGoal.getText() == null ? "" : editGoal.getText().toString().trim();
        viewModel.saveGoalWeight(currentUsername, goalText);
    }
}