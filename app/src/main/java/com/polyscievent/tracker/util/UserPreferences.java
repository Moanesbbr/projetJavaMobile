package com.polyscievent.tracker.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to manage user preferences and login state
 */
public class UserPreferences {
    private static final String PREF_NAME = "eventii07_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    /**
     * Save user information to SharedPreferences
     *
     * @param context The context
     * @param email   User's email
     * @param name    User's name
     */
    public static void saveUser(Context context, String email, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Check if user is logged in
     *
     * @param context The context
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user's email
     *
     * @param context The context
     * @return User's email or empty string if not logged in
     */
    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get logged in user's name
     *
     * @param context The context
     * @return User's name or empty string if not logged in
     */
    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "");
    }

    /**
     * Clear user session (logout)
     *
     * @param context The context
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
} 