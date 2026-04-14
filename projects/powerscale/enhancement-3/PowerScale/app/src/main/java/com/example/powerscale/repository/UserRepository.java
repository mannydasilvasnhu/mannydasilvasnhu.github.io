package com.example.powerscale.repository;

import android.content.Context;

import com.example.powerscale.database.UserDao;

/**
 * UserRepository
 * Repository layer responsible for user authentication and account management.
 *
 * This class acts as the single access point for user-related operations
 * and delegates persistence work to the UserDao.
 */
public class UserRepository {

    // DAO used for user-related database operations
    private final UserDao userDao;

    /**
     * UserRepository(Context context)
     * Initializes repository dependencies.
     *
     * @param context - The application context used for database access.
     */
    public UserRepository(Context context) {
        this.userDao = new UserDao(context.getApplicationContext());
    }

    /**
     * userExists(String username)
     * Checks whether a username already exists.
     *
     * @param username - The username to check.
     * @return boolean - True if the username exists.
     */
    public boolean userExists(String username) {
        return userDao.userExists(username);
    }

    /**
     * createUser(String username, String password)
     * Creates a new user account.
     *
     * @param username - The username to create.
     * @param password - The password to store securely.
     * @return boolean - True if account creation succeeded.
     */
    public boolean createUser(String username, String password) {
        return userDao.createUser(username, password);
    }

    /**
     * checkLogin(String username, String password)
     * Validates login credentials.
     *
     * @param username - The entered username.
     * @param password - The entered password.
     * @return boolean - True if credentials are valid.
     */
    public boolean checkLogin(String username, String password) {
        return userDao.checkLogin(username, password);
    }
}