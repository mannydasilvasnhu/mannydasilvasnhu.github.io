package com.example.powerscale.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.powerscale.R;
import com.example.powerscale.viewmodel.GoalWeightResult;
import com.example.powerscale.viewmodel.GoalWeightViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * GoalWeightActivity
 * This activity allows the user to view and save a goal weight.
 *
 * This screen renders a standardized GoalWeightResult returned by the ViewModel.
 * The current goal weight is also observed from the ViewModel so the UI updates
 * from observable state rather than a manual reload triggered by the Activity after save.
 */
public class GoalWeightActivity extends AppCompatActivity {

    // ViewModel used for goal weight flow
    private GoalWeightViewModel viewModel;

    // UI input container
    private TextInputLayout tilGoal;

    // Input field
    private TextInputEditText editGoal;

    // Displays the currently saved goal weight
    private TextView textCurrentGoal;

    // Save button
    private MaterialButton buttonSaveGoal;

    // Logged-in user
    private String currentUsername;

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the layout, loads the current session,
     * connects UI components, and wires observers to the ViewModel.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        // Get logged-in username from session preferences
        currentUsername = getSharedPreferences("session", MODE_PRIVATE).getString("username", null);

        // If no valid session exists, return to previous screen
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.login_again, Toast.LENGTH_SHORT).show();
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

        // Observe current goal weight display text
        viewModel.getCurrentGoalDisplay().observe(this, goalDisplay -> {
            if (goalDisplay != null) {
                textCurrentGoal.setText(goalDisplay);
            }
        });

        // Observe one-time goal weight results from the ViewModel
        viewModel.getGoalResult().observe(this, event -> {
            if (event == null) {
                return;
            }

            GoalWeightResult result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            // Clear field errors before applying new state
            clearErrors();

            // Apply field-level validation error when present
            if (result.getGoalError() != null) {
                tilGoal.setError(result.getGoalError());
                return;
            }

            // Update goal display text when the result provides one
            if (result.getGoalDisplayValue() != null && !result.getGoalDisplayValue().isEmpty()) {
                textCurrentGoal.setText(result.getGoalDisplayValue());
            }

            // Show user-facing message when present
            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Save button behavior
        buttonSaveGoal.setOnClickListener(v -> saveGoalWeight());

        // Initial goal weight load
        viewModel.loadGoalWeight(currentUsername);
    }

    /**
     * saveGoalWeight()
     * Reads the entered goal value and passes it to the ViewModel.
     */
    private void saveGoalWeight() {
        clearErrors();

        String goalText = editGoal.getText() == null ? "" : editGoal.getText().toString().trim();

        viewModel.saveGoalWeight(currentUsername, goalText);
    }

    /**
     * clearErrors()
     * Clears validation errors currently displayed on the input field.
     */
    private void clearErrors() {
        tilGoal.setError(null);
    }
}