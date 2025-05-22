package com.polyscievent.tracker.data;

import android.provider.BaseColumns;

/**
 * Contract class to define the database schema
 */
public final class EventContract {
    
    // Private constructor to prevent instantiation
    private EventContract() {}
    
    /**
     * Inner class that defines the table contents
     */
    public static class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "events";
        
        // Column names
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LOCATION = "location"; 
        public static final String COLUMN_NAME_EVENT_DATE = "event_date";
        public static final String COLUMN_NAME_ORGANIZER = "organizer";
        public static final String COLUMN_NAME_THEME = "theme";
        public static final String COLUMN_NAME_SUBMISSION_DEADLINE = "submission_deadline";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_IMAGE_URI = "image_uri";
        
        // SQL statements
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_NAME + " TEXT NOT NULL," +
                COLUMN_NAME_LOCATION + " TEXT NOT NULL," +
                COLUMN_NAME_EVENT_DATE + " INTEGER NOT NULL," +
                COLUMN_NAME_ORGANIZER + " TEXT NOT NULL," +
                COLUMN_NAME_THEME + " TEXT NOT NULL," +
                COLUMN_NAME_SUBMISSION_DEADLINE + " INTEGER NOT NULL," +
                COLUMN_NAME_USER_ID + " INTEGER NOT NULL," +
                COLUMN_NAME_IMAGE_URI + " TEXT" +
                ")";
        
        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
                
        public static final String SQL_SELECT_ALL_ORDERED =
                "SELECT * FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_NAME_SUBMISSION_DEADLINE + " ASC";
                
        public static final String SQL_SELECT_BY_USER_ID =
                "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME_USER_ID + " = ? " +
                " ORDER BY " + COLUMN_NAME_SUBMISSION_DEADLINE + " ASC";
    }
} 