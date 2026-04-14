package com.example.powerscale.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powerscale.R;
import com.example.powerscale.model.WeightEntry;
import com.example.powerscale.viewmodel.WeightLogViewModel;
import com.example.powerscale.viewmodel.WeightSubmissionResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * WeightLogActivity
 * This activity displays the user's daily weight entries and supports CRUD actions.
 *
 * This activity reacts to one-time submission events from the ViewModel for add and update
 * operations, and it sends an SMS alert when a successful submission meets the user's goal
 * weight, but only if the user has enabled SMS notifications, granted permission,
 * and saved a phone number.
 */
public class WeightLogActivity extends AppCompatActivity {

    // ViewModel used for data flow and validation
    private WeightLogViewModel viewModel;

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
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weight_log);

        // Get logged-in username from session preferences
        currentUsername = getSharedPreferences("session", MODE_PRIVATE).getString("username", null);

        // If no valid session exists, return to login
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            Toast.makeText(this, R.string.login_again, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WeightLogViewModel.class);

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
             * Deletes the entry through the ViewModel.
             */
            @Override
            public void onDeleteClicked(WeightEntry entry) {
                if (editingId != null && editingId.equals(entry.id)) {
                    resetToAddMode();
                }

                viewModel.deleteWeight(currentUsername, entry.id);
            }
        });

        recycler.setAdapter(adapter);

        // Observe updated entry list
        viewModel.getWeightEntries().observe(this, updatedEntries -> {
            entries.clear();

            if (updatedEntries != null) {
                entries.addAll(updatedEntries);
            }

            adapter.notifyDataSetChanged();

            findViewById(R.id.textFooter).setVisibility(
                    entries.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE
            );
        });

        // Observe validation, delete, and failure messages from the ViewModel
        viewModel.getMessage().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) {
                return;
            }

            if (msg.equals("Please enter a date.")) {
                tilDate.setError("Date is required");
                return;
            }

            if (msg.equals("Please enter a weight.")) {
                tilWeight.setError("Weight is required");
                return;
            }

            if (msg.equals("Please enter a valid numeric weight.")) {
                tilWeight.setError("Weight must be a number");
                return;
            }

            if (msg.equals("Weight must be greater than 0.")) {
                tilWeight.setError("Weight must be greater than 0");
                return;
            }

            // Delete and failure messages should always be shown regardless of SMS state
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Observe successful add/update submissions as one-time events
        viewModel.getSubmissionResult().observe(this, event -> {
            if (event == null) {
                return;
            }

            WeightSubmissionResult result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            if (result.isUpdate()) {
                resetToAddMode();
            } else {
                clearInputs();
            }

            boolean shouldSendGoalSms = result.isGoalReached() && isGoalSmsEnabledAndConfigured();

            String toastMessage;
            if (shouldSendGoalSms) {
                toastMessage = result.getMessage();
            } else {
                toastMessage = result.isUpdate()
                        ? "Weight updated successfully."
                        : "Weight added successfully.";
            }

            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

            if (shouldSendGoalSms) {
                sendGoalReachedSmsIfEnabled();
            }
        });

        // Handle Add/Update button click
        buttonAdd.setOnClickListener(v -> handleAddOrUpdate());

        // Initial data load
        viewModel.loadWeights(currentUsername);
    }

    /**
     * setupDatePicker()
     * Sets a DatePicker dialog on the date field so user input
     * stays in MM/DD/YYYY format.
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
                        String formattedDate = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear
                        );

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
     * Reads user input and sends it to the ViewModel.
     */
    private void handleAddOrUpdate() {
        clearErrors();

        String date = editDate.getText() == null ? "" : editDate.getText().toString().trim();
        String weightText = editWeight.getText() == null ? "" : editWeight.getText().toString().trim();

        if (editingId == null) {
            viewModel.saveWeight(currentUsername, date, weightText);
        } else {
            viewModel.updateWeight(currentUsername, editingId, date, weightText);
        }
    }

    /**
     * clearInputs()
     * Clears the input fields after a successful add.
     */
    private void clearInputs() {
        editDate.setText("");
        editWeight.setText("");
    }

    /**
     * resetToAddMode()
     * Resets the screen from edit mode back to add mode.
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * sendGoalReachedSmsIfEnabled()
     * Sends a goal-reached SMS only if SMS notifications are enabled,
     * permission is granted, and a phone number has been saved.
     */
    private void sendGoalReachedSmsIfEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        if (!enabled) {
            return;
        }

        boolean hasPermission = hasSmsPermission();
        if (!hasPermission) {
            return;
        }

        String phone = prefs.getString(KEY_SMS_PHONE, "");
        if (phone.trim().isEmpty()) {
            return;
        }

        try {
            SmsManager smsManager = getSystemService(SmsManager.class);
            if (smsManager == null) {
                return;
            }

            String smsMessage = "PowerScale: Congrats! You hit your goal weight.";
            smsManager.sendTextMessage(phone.trim(), null, smsMessage, null, null);

        } catch (Exception ignored) {
            // SMS failures should not interrupt the user workflow
        }
    }

    /**
     * isGoalSmsEnabledAndConfigured()
     * Returns true only when SMS goal notifications are actually usable.
     *
     * @return boolean - True if goal SMS behavior should be active.
     */
    private boolean isGoalSmsEnabledAndConfigured() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SMS, MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        if (!enabled) {
            return false;
        }

        boolean hasPermission = hasSmsPermission();
        if (!hasPermission) {
            return false;
        }

        String phone = prefs.getString(KEY_SMS_PHONE, "").trim();
        return !phone.isEmpty();
    }
}