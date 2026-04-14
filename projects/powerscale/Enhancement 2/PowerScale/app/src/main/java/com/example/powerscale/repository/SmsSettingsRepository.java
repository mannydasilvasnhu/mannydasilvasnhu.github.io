package com.example.powerscale.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SmsSettingsRepository
 * Repository layer responsible for storing and retrieving SMS settings.
 *
 * This class acts as the single access point for:
 * - Whether SMS notifications are enabled
 * - The stored phone number used for SMS
 */
public class SmsSettingsRepository {

    // SharedPreferences constants used to save SMS settings
    public static final String PREFS_SMS = "sms_settings";
    public static final String KEY_SMS_ENABLED = "sms_enabled";
    public static final String KEY_SMS_PHONE = "sms_phone";

    // SharedPreferences instance used for persistence
    private final SharedPreferences smsPrefs;

    /**
     * SmsSettingsRepository(Context context)
     * Initializes repository dependencies.
     *
     * @param context - The application context used for database access.
     */
    public SmsSettingsRepository(Context context) {
        smsPrefs = context.getApplicationContext().getSharedPreferences(PREFS_SMS, Context.MODE_PRIVATE);
    }

    /**
     * isSmsEnabledPreference()
     *
     * @return boolean - The stored SMS enabled preference.
     */
    public boolean isSmsEnabledPreference() {
        return smsPrefs.getBoolean(KEY_SMS_ENABLED, false);
    }

    /**
     * setSmsEnabledPreference(boolean enabled)
     * Stores the SMS enabled preference.
     *
     * @param enabled - True if SMS should be enabled in preferences.
     */
    public void setSmsEnabledPreference(boolean enabled) {
        smsPrefs.edit().putBoolean(KEY_SMS_ENABLED, enabled).apply();
    }

    /**
     * getSavedPhone()
     *
     * @return String - The stored phone number, or an empty string if none exists.
     */
    public String getSavedPhone() {
        return smsPrefs.getString(KEY_SMS_PHONE, "");
    }

    /**
     * savePhone(String phone)
     * Stores the phone number used for SMS.
     *
     * @param phone - The phone number to save.
     */
    public void savePhone(String phone) {
        smsPrefs.edit().putString(KEY_SMS_PHONE, phone).apply();
    }
}