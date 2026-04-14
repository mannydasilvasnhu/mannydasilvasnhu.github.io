package com.example.mannydasilvaweighttrackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * WeightLogAdapter
 * This class represents the RecyclerView adapter responsible for displaying weight entries
 * in a grid/list format.
 *
 * Each row supports:
 * - Tapping the row to edit
 * - Pressing delete to remove the entry
 */
public class WeightLogAdapter extends RecyclerView.Adapter<WeightLogAdapter.VH> {

    /**
     * RowActions
     * Interface used to communicate row click events
     * back to the hosting activity (WeightLogActivity).
     */
    public interface RowActions {

        /**
         * Called when a row is tapped.
         * Used to trigger update/edit behavior.
         *
         * @param entry - The weight entry associated with the tapped row.
         * @return void
         */
        void onRowClicked(WeightEntry entry);

        /**
         * Called when the delete button is pressed.
         * Used to remove the entry from the database.
         *
         * @param entry - The weight entry to delete.
         * @return void
         */
        void onDeleteClicked(WeightEntry entry);
    }

    // List of weight entries displayed in the RecyclerView
    private final List<WeightEntry> entries;

    // Callback interface for row interaction events
    private final RowActions actions;

    /**
     * WeightLogAdapter(List<WeightEntry> entries, RowActions actions)
     * Constructor used to initialize the adapter.
     *
     * @param entries - The list of weight entries to display.
     * @param actions - The callback interface for row click events.
     * @return void
     */
    public WeightLogAdapter(List<WeightEntry> entries, RowActions actions) {
        this.entries = entries;
        this.actions = actions;
    }

    /**
     * onCreateViewHolder(ViewGroup parent, int viewType)
     * Inflates the layout for a single weight entry row.
     *
     * @param parent - The parent view group.
     * @param viewType - The type of view.
     * @return VH - ViewHolder instance for the row.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate row layout from XML
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight_row, parent, false);

        return new VH(v);
    }

    /**
     * onBindViewHolder(VH holder, int position)
     * Binds a WeightEntry object to a specific row in the RecyclerView.
     *
     * @param holder - The ViewHolder containing row UI references.
     * @param position - The position of the item in the list.
     * @return void
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        // Get weight entry at this position
        WeightEntry entry = entries.get(position);

        // Display formatted date
        holder.textDate.setText(entry.date);

        // Display formatted weight
        holder.textWeight.setText(String.format(Locale.US, "%.1f", entry.weight));

        // Delete button removes this entry
        holder.buttonDelete.setOnClickListener(v -> actions.onDeleteClicked(entry));

        // Tapping the entire row triggers update/edit mode
        holder.itemView.setOnClickListener(v -> actions.onRowClicked(entry));
    }

    /**
     * getItemCount()
     * Returns the total number of weight entries displayed.
     *
     * @return int - The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        return entries.size();
    }

    /**
     * VH (ViewHolder)
     * Holds references to UI elements for a single row.
     */
    public static class VH extends RecyclerView.ViewHolder {

        // TextView displaying entry date
        TextView textDate;

        // TextView displaying entry weight
        TextView textWeight;

        // Delete button for this row
        MaterialButton buttonDelete;

        /**
         * VH(View itemView)
         * Constructor that binds row UI elements to Java references.
         *
         * @param itemView - The inflated row view.
         * @return void
         */
        public VH(@NonNull View itemView) {
            super(itemView);

            // Connect layout elements
            textDate = itemView.findViewById(R.id.textDate);
            textWeight = itemView.findViewById(R.id.textWeight);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}