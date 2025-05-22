package com.polyscievent.tracker.data;

import android.provider.BaseColumns;

/**
 * Contract class that defines the users table schema
 */
public final class UserContract {
    
    // Private constructor to prevent instantiation
    private UserContract() {}
    
    /**
     * Inner class that defines the table contents
     */
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        
        // Column names
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_FULL_NAME = "full_name";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        
        // SQL statements
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_EMAIL + " TEXT UNIQUE NOT NULL," +
                COLUMN_NAME_FULL_NAME + " TEXT NOT NULL," +
                COLUMN_NAME_PASSWORD + " TEXT NOT NULL," +
                COLUMN_NAME_CREATED_AT + " INTEGER NOT NULL)";
        
        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
                
        public static final String SQL_SELECT_BY_EMAIL =
                "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME_EMAIL + " = ?";
    }
} 