package com.polyscievent.tracker.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.polyscievent.tracker.model.User;

/**
 * Manages the user session in the application
 */
public class UserSession {
    private static final String PREF_NAME = "eventii07_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    
    /**
     * Constructor
     * @param context Application context
     */
    public UserSession(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Create user login session
     * @param user User to create session for
     */
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.apply();
    }

    /**
     * Check if user is logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get stored user details
     * @return User with stored session data
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setId(prefs.getLong(KEY_USER_ID, -1));
        user.setEmail(prefs.getString(KEY_USER_EMAIL, ""));
        user.setFullName(prefs.getString(KEY_USER_NAME, ""));
        return user;
    }

    /**
     * Clear user data and log out
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    /**
     * Update user details in the session
     * @param user Updated user details
     */
    public void updateUserDetails(User user) {
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.apply();
    }
} 