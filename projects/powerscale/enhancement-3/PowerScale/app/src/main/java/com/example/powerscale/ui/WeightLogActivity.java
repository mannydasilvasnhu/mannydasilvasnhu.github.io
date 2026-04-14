package com.example.powerscale.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powerscale.R;
import com.example.powerscale.model.WeightEntry;
import com.example.powerscale.viewmodel.OperationStatus;
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
 * This screen renders standardized submission results returned by the ViewModel.
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

    // Action buttons
    private MaterialButton buttonAdd;
    private MaterialButton buttonViewTrends;

    // RecyclerView grid for displaying weight entries
    private RecyclerView recycler;
    private WeightLogAdapter adapter;

    // List of entries shown in the adapter
    private final List<WeightEntry> entries = new ArrayList<>();

    // Logged-in user
    private String currentUsername;

    // If not null, the user is editing an existing record
    private Long editingId = null;

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

        // Begin observing repository-backed weight data for the logged-in user
        viewModel.startObservingWeights(currentUsername);

        // Connect input UI elements
        tilDate = findViewById(R.id.tilDate);
        tilWeight = findViewById(R.id.tilWeight);
        editDate = findViewById(R.id.editDate);
        editWeight = findViewById(R.id.editWeight);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonViewTrends = findViewById(R.id.buttonViewTrends);

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

                viewModel.deleteWeight(entry.id);
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

            View footer = findViewById(R.id.textFooter);
            footer.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe all weight log results as one-time events
        viewModel.getSubmissionResult().observe(this, event -> {
            if (event == null) {
                return;
            }

            WeightSubmissionResult result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            // Clear field errors before applying new result state
            clearErrors();

            // Apply field-level validation errors
            if (result.getDateError() != null) {
                tilDate.setError(result.getDateError());
            }

            if (result.getWeightError() != null) {
                tilWeight.setError(result.getWeightError());
            }

            // Stop here for validation-only results
            if (result.getDateError() != null || result.getWeightError() != null) {
                return;
            }

            // Reset form state only after successful add/update
            if (result.isWeightSaved()) {
                if (result.isUpdate()) {
                    resetToAddMode();
                } else {
                    clearInputs();
                }
            }

            // Show main result message
            if (result.getMessage() != null && !result.getMessage().isEmpty()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // Show warning message when present
            if (result.getStatus() == OperationStatus.WARNING && result.getWarningMessage() != null && !result.getWarningMessage().isEmpty()) {
                Toast.makeText(this, result.getWarningMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Add/Update button click
        buttonAdd.setOnClickListener(v -> handleAddOrUpdate());

        // Open the dedicated Trends screen
        buttonViewTrends.setOnClickListener(v -> openTrendsScreen());
    }

    /**
     * setupDatePicker()
     * Sets a DatePicker dialog on the date field so user input
     * stays in MM/DD/YYYY format.
     */
    private void setupDatePicker() {
        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    WeightLogActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear
                        );

                        editDate.setText(formattedDate);
                        tilDate.setError(null);
                    },
                    year,
                    month,
                    day
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
     * openTrendsScreen()
     * Opens the dedicated Trends screen where the user can view
     * the 7-day moving average and trend direction results.
     */
    private void openTrendsScreen() {
        Intent intent = new Intent(this, TrendsActivity.class);
        startActivity(intent);
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
}