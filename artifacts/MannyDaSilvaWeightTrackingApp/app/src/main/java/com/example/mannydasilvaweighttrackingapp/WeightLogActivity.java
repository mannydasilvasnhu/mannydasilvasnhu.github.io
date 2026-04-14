package com.example.mannydasilvaweighttrackingapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * WeightLogActivity
 * This class displays the user's daily weight entries and supports CRUD actions.
 *
 * This activity also triggers an SMS alert when a user logs a weight that meets their goal weight,
 * but only if the user has enabled SMS notifications, granted permission, and saved a phone number.
 */
public class WeightLogActivity extends AppCompatActivity {

    // Database helper used for CRUD operations
    private AppDatabaseHelper db;

    // UI input containers
    private TextInputLayout tilDate;
    private TextInputLayout tilWeight;

    // Text fields
    private TextInputEditText editDate;
    private TextInputEditText editWeight;

    // Add/Update button
    private MaterialButton buttonAdd;

    // RecyclerView grid for displaying weight entries
    private RecyclerView recycler;
    private WeightLogAdapter adapter;

    // List of entries shown in the adapter
    private final List<WeightEntry> entries = new ArrayList<>();

    // Logged-in user
    private String currentUsername;

    // If not null, the user is editing an existing record
    private Long editingId = null;

    // SMS prefs keys (shared with SmsSettingsActivity)
    private static final String PREFS_SMS = "sms_settings";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_SMS_PHONE = "sms_phone";

    /**
     * onCreate(Bundle savedInstanceState)
     * Called when the activity is first created.
     * This method initializes the UI, loads the logged-in user session,
     * sets up the RecyclerView grid, wires up input validation,
     * and loads the user’s stored weight entries.
     *
     * @param savedInstanceState - The saved state of the activity, if available.
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weight_log);

        // Initialize database helper
        db = new AppDatabaseHelper(this);

        // Get logged-in username from session preferences
        currentUsername = getSharedPreferences("session", MODE_PRIVATE).getString("username", null);

        // If no valid session exists, return to login
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Connect input UI elements
        tilDate = findViewById(R.id.tilDate);
        tilWeight = findViewById(R.id.tilWeight);
        editDate = findViewById(R.id.editDate);
        editWeight = findViewById(R.id.editWeight);
        buttonAdd = findViewById(R.id.buttonAdd);

        // Force date selection using DatePicker to ensure MM/DD/YYYY formatting
        setupDatePicker();

        // Connect RecyclerView and layout manager
        recycler = findViewById(R.id.recyclerWeightLog);
        recycler.setLayoutManager(new GridLayoutManager(this, 1));

        // Initialize adapter and row click actions
        adapter = new WeightLogAdapter(entries, new WeightLogAdapter.RowActions() {

            /**
             * Called when a row is tapped.
             * Loads the selected entry into input fields so the user can edit it.
             */
            @Override
            public void onRowClicked(WeightEntry entry) {
                editingId = entry.id;

                // Populate form fields with selected row values
                editDate.setText(entry.date);
                editWeight.setText(String.valueOf(entry.weight));

                // Switch button to update mode
                buttonAdd.setText(R.string.update_entry);

                Toast.makeText(WeightLogActivity.this, R.string.editing_entry_toast, Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when delete button on a row is pressed.
             * Deletes the entry from the database and refreshes the grid.
             */
            @Override
            public void onDeleteClicked(WeightEntry entry) {
                boolean ok = db.deleteWeight(entry.id);

                if (ok) {
                    // If the user deletes the entry being edited, reset back to add mode
                    if (editingId != null && editingId.equals(entry.id)) {
                        resetToAddMode();
                    }
                    loadWeights();
                } else {
                    Toast.makeText(WeightLogActivity.this, R.string.could_not_delete, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recycler.setAdapter(adapter);

        // Handle Add/Update button click
        buttonAdd.setOnClickListener(v -> handleAddOrUpdate());

        // Initial data load
        loadWeights();
    }

    /**
     * setupDatePicker()
     * Sets a DatePicker dialog on the date field so user input
     * stays in MM/DD/YYYY format.
     *
     * @return void
     */
    private void setupDatePicker() {

        editDate.setOnClickListener(v -> {

            // Default DatePicker to today's date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    WeightLogActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        // Format date as MM/DD/YYYY
                        String formattedDate = String.format(Locale.US, "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);

                        editDate.setText(formattedDate);

                        // Clear date field error once valid input exists
                        tilDate.setError(null);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
    }

    /**
     * handleAddOrUpdate()
     * Reads user input, validates it, then either inserts a new weight entry
     * or updates an existing entry depending on edit mode.
     *
     * @return void
     */
    private void handleAddOrUpdate() {

        clearErrors();

        // Read input values
        String date = editDate.getText() == null ? "" : editDate.getText().toString().trim();
        String wStr = editWeight.getText() == null ? "" : editWeight.getText().toString().trim();

        boolean hasError = false;

        // Validate date input
        if (date.isEmpty()) {
            tilDate.setError("Date is required");
            hasError = true;
        }

        // Validate weight input
        if (wStr.isEmpty()) {
            tilWeight.setError("Weight is required");
            hasError = true;
        }

        if (hasError) return;

        // Parse weight value
        double w;
        try {
            w = Double.parseDouble(wStr);
        } catch (Exception e) {
            tilWeight.setError("Weight must be a number");
            return;
        }

        // Decide between Create vs Update
        if (editingId == null) {
            createWeightEntry(date, w);
        } else {
            updateWeightEntry(date, w);
        }
    }

    /**
     * createWeightEntry(String date, double weight)
     * Inserts a new weight entry into the database and refreshes the grid.
     *
     * @param date - The date value (MM/DD/YYYY).
     * @param weight - The weight value.
     * @return void
     */
    private void createWeightEntry(String date, double weight) {

        long id = db.insertWeight(currentUsername, date, weight, null);

        if (id == -1) {
            Toast.makeText(this, R.string.could_not_add, Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear inputs after successful insert
        clearInputs();

        // Trigger goal check and SMS alert if needed
        sendGoalReachedSms(weight);

        // Refresh grid
        loadWeights();
    }

    /**
     * updateWeightEntry(String date, double weight)
     * Updates an existing entry in the database and refreshes the grid.
     *
     * @param date - The updated date value.
     * @param weight - The updated weight value.
     * @return void
     */
    private void updateWeightEntry(String date, double weight) {

        boolean ok = db.updateWeight(editingId, date, weight, null);

        if (!ok) {
            Toast.makeText(this, R.string.could_not_update, Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset form back to add mode after update
        resetToAddMode();

        // Trigger goal check and SMS alert if needed
        sendGoalReachedSms(weight);

        // Refresh grid
        loadWeights();
    }

    /**
     * loadWeights()
     * Loads all weight entries for the current user from the database
     * and updates the RecyclerView.
     *
     * @return void
     */
    private void loadWeights() {

        // Clear current list
        int oldSize = entries.size();
        entries.clear();

        // Notify adapter that old items were removed
        if (oldSize > 0) {
            adapter.notifyItemRangeRemoved(0, oldSize);
        }

        // Query database and repopulate list
        try (Cursor c = db.getAllWeightsForUser(currentUsername)) {

            int idCol = c.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_ID);
            int dateCol = c.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_DATE);
            int valCol = c.getColumnIndexOrThrow(AppDatabaseHelper.COL_WEIGHT_VALUE);

            while (c.moveToNext()) {
                long id = c.getLong(idCol);
                String date = c.getString(dateCol);
                double w = c.getDouble(valCol);

                entries.add(new WeightEntry(id, date, w));
            }
        }

        // Notify adapter that new items were inserted
        if (!entries.isEmpty()) {
            adapter.notifyItemRangeInserted(0, entries.size());
        }

        // Show footer hint only when list is empty
        findViewById(R.id.textFooter).setVisibility(entries.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    /**
     * clearInputs()
     * Clears the input fields after a successful add.
     *
     * @return void
     */
    private void clearInputs() {
        editDate.setText("");
        editWeight.setText("");
    }

    /**
     * resetToAddMode()
     * Resets the screen from edit mode back to add mode.
     *
     * @return void
     */
    private void resetToAddMode() {
        editingId = null;
        clearInputs();
        buttonAdd.setText(getString(R.string.add_to_log));
        clearErrors();
    }

    /**
     * clearErrors()
     * Clears validation errors on the input fields.
     *
     * @return void
     */
    private void clearErrors() {
        tilDate.setError(null);
        tilWeight.setError(null);
    }

    /**
     * hasSmsPermission()
     * Checks whether the SEND_SMS permission is currently granted.
     *
     * @return boolean - True if permission is granted.
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * sendGoalReachedSms(double newWeight)
     * Checks whether the new weight meets the user's goal.
     * If goal is met, shows a toast and sends an SMS if enabled and permitted.
     *
     * @param newWeight - The weight value that was just added or updated.
     * @return void
     */
    private void sendGoalReachedSms(double newWeight) {

        Double goal = db.getGoalWeight(currentUsername);
        if (goal == null) return;

        // Goal reached if weight is less than or equal to goal
        if (newWeight > goal) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);

        // Must be enabled in settings
        boolean enabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        if (!enabled) return;

        // Must have SMS permission
        if (!hasSmsPermission()) return;

        // Must have saved phone number
        String phone = prefs.getString(KEY_SMS_PHONE, "");
        if (phone.trim().isEmpty()) return;

        try {
            SmsManager smsManager = getSystemService(SmsManager.class);
            if (smsManager == null) return;

            String msg = "PowerScale: Congrats! You hit your goal weight of " + String.format(Locale.US, "%.1f", goal) + " lbs.";

            smsManager.sendTextMessage(phone, null, msg, null, null);

            // Only show popup if SMS is actually being sent
            Toast.makeText(this, "Goal reached! SMS alert sent.", Toast.LENGTH_LONG).show();

        } catch (Exception ignored) {
            // App continues to function as normal even if SMS fails
        }
    }
}