package com.polyscievent.tracker.data;

import android.provider.BaseColumns;

/**
 * Contract class for the wishlist table
 */
public final class WishlistContract {
    
    private WishlistContract() {}
    
    public static class WishlistEntry implements BaseColumns {
        public static final String TABLE_NAME = "wishlist";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_EVENT_ID = "event_id";
        public static final String COLUMN_NAME_ADDED_AT = "added_at";
        
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_USER_ID + " INTEGER NOT NULL," +
                COLUMN_NAME_EVENT_ID + " INTEGER NOT NULL," +
                COLUMN_NAME_ADDED_AT + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + COLUMN_NAME_USER_ID + ") REFERENCES " +
                UserContract.UserEntry.TABLE_NAME + "(" + UserContract.UserEntry._ID + ") " +
                "ON DELETE CASCADE," +
                "FOREIGN KEY (" + COLUMN_NAME_EVENT_ID + ") REFERENCES " +
                EventContract.EventEntry.TABLE_NAME + "(" + EventContract.EventEntry._ID + ") " +
                "ON DELETE CASCADE," +
                "UNIQUE(" + COLUMN_NAME_USER_ID + ", " + COLUMN_NAME_EVENT_ID + ")" +
                ")";
        
        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
} 