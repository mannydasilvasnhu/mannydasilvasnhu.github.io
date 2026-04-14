package com.example.powerscale.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.powerscale.R;
import com.google.android.material.button.MaterialButton;

/**
 * HomeActivity
 * This activity displays the main home screen after a user logs in.
 *
 * This activity acts as the main navigation hub of the application.
 * From this screen, the user can:
 * - View their weight log
 * - Set or modify their goal weight
 * - View their profile
 * - Configure SMS notification settings
 * - Log out of the application
 */
public class HomeActivity extends AppCompatActivity {

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the layout, connects UI buttons,
     * and assigns navigation logic for each action.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Connect buttons from layout to Java references
        MaterialButton buttonWeightLog = findViewById(R.id.buttonWeightLog);
        MaterialButton buttonGoalWeight = findViewById(R.id.buttonGoalWeight);
        MaterialButton buttonProfile = findViewById(R.id.buttonProfile);
        MaterialButton buttonSmsSettings = findViewById(R.id.buttonSmsSettings);
        MaterialButton buttonLogout = findViewById(R.id.buttonLogout);

        // Navigate to Weight Log screen
        buttonWeightLog.setOnClickListener(v -> openScreen(WeightLogActivity.class));

        // Navigate to Goal Weight screen
        buttonGoalWeight.setOnClickListener(v -> openScreen(GoalWeightActivity.class));

        // Navigate to Profile screen
        buttonProfile.setOnClickListener(v -> openScreen(ProfileActivity.class));

        // Navigate to SMS Settings screen
        buttonSmsSettings.setOnClickListener(v -> openScreen(SmsSettingsActivity.class));

        // Log out user and clear session data
        buttonLogout.setOnClickListener(v -> {

            // Remove stored session information
            getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();

            // Return to Login screen
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    /**
     * openScreen(Class<?> destination)
     * Starts a new activity and applies a slide transition animation.
     *
     * @param destination - The Activity class to navigate to.
     */
    private void openScreen(Class<?> destination) {

        // Start selected activity
        startActivity(new Intent(this, destination));

        // Apply slide animation when opening new screen
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    /**
     * finish()
     * Called when this activity is closed.
     * Applies a slide animation when returning to the previous screen.
     */
    @Override
    public void finish() {
        super.finish();

        // Apply slide animation when closing this screen
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}