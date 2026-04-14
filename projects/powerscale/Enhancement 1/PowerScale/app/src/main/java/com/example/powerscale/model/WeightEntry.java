package com.example.powerscale.model;

/**
 * WeightEntry
 * This class is a model class representing a single weight log entry.
 *
 * This object is used to store and transfer weight data
 * between the database and the RecyclerView adapter.
 */
public class WeightEntry {

    // Unique database ID for this entry
    public long id;

    // Date the weight was recorded (stored as MM/DD/YYYY)
    public String date;

    // Weight value in pounds
    public double weight;

    /**
     * WeightEntry(long id, String date, double weight)
     * Constructor used to create a new weight entry object.
     *
     * @param id - The unique database ID for the entry.
     * @param date - The date the weight was recorded.
     * @param weight - The weight value in pounds.
     */
    public WeightEntry(long id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }
}