package com.example.powerscale.repository;

import android.content.Context;

import com.example.powerscale.database.AppDatabaseHelper;

/**
 * UserRepository
 * Repository layer responsible for user-related data operations.
 *
 * This class acts as an abstraction between the ViewModel layer
 * and the database helper. This allows authentication and user
 * creation logic to be accessed without the UI interacting
 * with the database.
 */
public class UserRepository {

    // Database helper used for user related database operations
    private final AppDatabaseHelper dbHelper;

    /**
     * UserRepository(Context context)
     * Initializes the repository and database helper.
     *
     * @param context - The application context used to access the database.
     */
    public UserRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context.getApplicationContext());
    }

    /**
     * userExists(String username)
     * Checks if a username already exists in the database.
     *
     * @param username - The username to check.
     * @return boolean - True if the username already exists.
     */
    public boolean userExists(String username) {
        return dbHelper.userExists(username);
    }

    /**
     * createUser(String username, String password)
     * Creates a new user account with the given credentials.
     *
     * @param username - The username for the new account.
     * @param password - The plain text password entered by the user.
     * @return boolean - True if the user was successfully created.
     */
    public boolean createUser(String username, String password) {
        return dbHelper.createUser(username, password);
    }

    /**
     * checkLogin(String username, String password)
     * Validates a login attempt using stored credentials.
     *
     * @param username - The username entered.
     * @param password - The password entered.
     * @return boolean - True if login credentials are valid.
     */
    public boolean checkLogin(String username, String password) {
        return dbHelper.checkLogin(username, password);
    }
}