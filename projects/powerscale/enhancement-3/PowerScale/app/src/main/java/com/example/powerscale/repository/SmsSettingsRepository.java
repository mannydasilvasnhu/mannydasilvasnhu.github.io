package com.example.powerscale.repository;

import android.content.Context;

import com.example.powerscale.database.SettingsDao;

/**
 * SmsSettingsRepository
 * Repository layer responsible for storing and retrieving SMS settings.
 *
 * This class acts as the single access point for:
 * - Whether SMS notifications are enabled for a user
 * - The stored phone number used for SMS for a user
 */
public class SmsSettingsRepository {

    // SettingsDao used for database operations on the settings table
    private final SettingsDao settingsDao;

    /**
     * SmsSettingsRepository(Context context)
     * Initializes repository dependencies.
     *
     * @param context - The application context used for database access.
     */
    public SmsSettingsRepository(Context context) {
        settingsDao = new SettingsDao(context.getApplicationContext());
    }

    /**
     * isSmsEnabledPreference(String username)
     * Reads the SMS enabled setting for a specific user from the database.
     *
     * @param username - The username to query settings for.
     * @return boolean - The stored SMS enabled preference.
     */
    public boolean isSmsEnabledPreference(String username) {
        return settingsDao.isSmsEnabled(username);
    }

    /**
     * setSmsEnabledPreference(String username, boolean enabled)
     * Stores the SMS enabled preference for a specific user in the database.
     *
     * @param username - The username to update settings for.
     * @param enabled - True if SMS should be enabled.
     */
    public void setSmsEnabledPreference(String username, boolean enabled) {
        settingsDao.setSmsEnabled(username, enabled);
    }

    /**
     * getSavedPhone(String username)
     * Reads the stored phone number for a specific user from the database.
     *
     * @param username - The username to query settings for.
     * @return String - The stored phone number, or an empty string if none exists.
     */
    public String getSavedPhone(String username) {
        return settingsDao.getSmsPhone(username);
    }

    /**
     * savePhone(String username, String phone)
     * Stores the phone number used for SMS for a specific user in the database.
     *
     * @param username - The username to update settings for.
     * @param phone - The phone number to save.
     */
    public void savePhone(String username, String phone) {
        settingsDao.setSmsPhone(username, phone);
    }

    /**
     * initializeDefaultSettings(String username)
     * Creates a default settings row for a new user account.
     *
     * @param username - The username to initialize settings for.
     */
    public void initializeDefaultSettings(String username) {
        settingsDao.insertDefaultSettings(username);
    }
}
