package com.polyscievent.tracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class for managing the SQLite database
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    
    // Database info
    private static final String DATABASE_NAME = "scievents.db";
    private static final int DATABASE_VERSION = 5;  // Increased version for wishlist table
    
    // Singleton instance
    private static DatabaseHelper sInstance;
    
    /**
     * Get singleton instance of DatabaseHelper
     * @param context Context to use for locating data
     * @return The singleton instance of DatabaseHelper
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
    
    /**
     * Private constructor to enforce singleton pattern
     * @param context Application context
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the events table
        db.execSQL(EventContract.EventEntry.SQL_CREATE_TABLE);
        // Create the users table
        db.execSQL(UserContract.UserEntry.SQL_CREATE_TABLE);
        // Create the wishlist table
        db.execSQL(WishlistContract.WishlistEntry.SQL_CREATE_TABLE);
        Log.d(TAG, "Database created with tables: events, users, wishlist");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add the users table if upgrading from version 1
            db.execSQL(UserContract.UserEntry.SQL_CREATE_TABLE);
            Log.d(TAG, "Added users table during upgrade from version " + oldVersion + " to " + newVersion);
        }
        
        if (oldVersion < 3) {
            // Need to migrate events table to include user_id
            try {
                // Create temporary table with new schema
                db.execSQL("CREATE TABLE events_temp (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "location TEXT NOT NULL," +
                        "event_date INTEGER NOT NULL," +
                        "organizer TEXT NOT NULL," +
                        "theme TEXT NOT NULL," +
                        "submission_deadline INTEGER NOT NULL," +
                        "user_id INTEGER NOT NULL DEFAULT 1," +
                        "image_uri TEXT)");
                
                // Copy data from old table to new one
                db.execSQL("INSERT INTO events_temp (_id, name, location, event_date, organizer, theme, submission_deadline, user_id) " +
                        "SELECT _id, name, location, event_date, organizer, theme, submission_deadline, user_id FROM events");
                        
                // Drop the old table
                db.execSQL("DROP TABLE events");
                
                // Rename the new table to the original name
                db.execSQL("ALTER TABLE events_temp RENAME TO events");
                
                Log.d(TAG, "Successfully migrated events table to include user_id and image_uri fields");
            } catch (Exception e) {
                // If migration fails, recreate the table (losing data)
                Log.e(TAG, "Error migrating events table: " + e.getMessage());
                db.execSQL(EventContract.EventEntry.SQL_DELETE_TABLE);
                db.execSQL(EventContract.EventEntry.SQL_CREATE_TABLE);
            }
        }

        if (oldVersion < 4) {
            try {
                // Add image_uri column if it doesn't exist
                db.execSQL("ALTER TABLE " + EventContract.EventEntry.TABLE_NAME + 
                        " ADD COLUMN " + EventContract.EventEntry.COLUMN_NAME_IMAGE_URI + " TEXT");
                Log.d(TAG, "Added image_uri column to events table");
            } catch (Exception e) {
                Log.e(TAG, "Error adding image_uri column: " + e.getMessage());
            }
        }

        if (oldVersion < 5) {
            // Add wishlist table
            db.execSQL(WishlistContract.WishlistEntry.SQL_CREATE_TABLE);
            Log.d(TAG, "Added wishlist table during upgrade");
        }
    }
    
    // --- USER MANAGEMENT METHODS ---
    
    /**
     * Add a new user to the database
     * @param user The user to add
     * @return The row ID of the newly inserted user, or -1 if an error occurred
     */
    public long addUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NAME_EMAIL, user.getEmail());
        values.put(UserContract.UserEntry.COLUMN_NAME_FULL_NAME, user.getFullName());
        values.put(UserContract.UserEntry.COLUMN_NAME_PASSWORD, user.getPassword());
        values.put(UserContract.UserEntry.COLUMN_NAME_CREATED_AT, user.getCreatedAt());
        
        long id = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
        
        if (id != -1) {
            Log.d(TAG, "User added with ID: " + id);
        } else {
            Log.e(TAG, "Error adding user: " + user.toString());
        }
        
        return id;
    }
    
    /**
     * Get a user by email
     * @param email The email of the user to retrieve
     * @return The user, or null if not found
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        
        String[] selectionArgs = { email };
        Cursor cursor = db.rawQuery(UserContract.UserEntry.SQL_SELECT_BY_EMAIL, selectionArgs);
        
        User user = null;
        
        try {
            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                
                int idIndex = cursor.getColumnIndex(UserContract.UserEntry._ID);
                int emailIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME_EMAIL);
                int fullNameIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME_FULL_NAME);
                int passwordIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME_PASSWORD);
                int createdAtIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME_CREATED_AT);
                
                // Safely get values (check for -1 column index)
                if (idIndex != -1) user.setId(cursor.getLong(idIndex));
                if (emailIndex != -1) user.setEmail(cursor.getString(emailIndex));
                if (fullNameIndex != -1) user.setFullName(cursor.getString(fullNameIndex));
                if (passwordIndex != -1) user.setPassword(cursor.getString(passwordIndex));
                if (createdAtIndex != -1) user.setCreatedAt(cursor.getLong(createdAtIndex));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        if (user != null) {
            Log.d(TAG, "Retrieved user with email: " + email);
        } else {
            Log.d(TAG, "No user found with email: " + email);
        }
        
        return user;
    }
    
    /**
     * Authenticate a user by email and password
     * @param email The user's email
     * @param password The user's password
     * @return The authenticated User if credentials match, null otherwise
     */
    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        
        if (user != null && user.getPassword().equals(password)) {
            // In a real app, you would use proper password hashing here
            Log.d(TAG, "User authenticated: " + email);
            return user;
        }
        
        Log.d(TAG, "Authentication failed for email: " + email);
        return null;
    }
    
    /**
     * Check if a user with the given email already exists
     * @param email Email to check
     * @return true if the email is already registered, false otherwise
     */
    public boolean isEmailRegistered(String email) {
        return getUserByEmail(email) != null;
    }
    
    /**
     * Update user details (name and password)
     * @param user User with updated information
     * @return The number of rows affected
     */
    public int updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NAME_FULL_NAME, user.getFullName());
        values.put(UserContract.UserEntry.COLUMN_NAME_PASSWORD, user.getPassword());
        
        String selection = UserContract.UserEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(user.getId()) };
        
        int count = db.update(
                UserContract.UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        
        if (count > 0) {
            Log.d(TAG, "Updated user ID: " + user.getId());
        } else {
            Log.e(TAG, "Error updating user ID: " + user.getId());
        }
        
        return count;
    }
    
    // --- EVENT MANAGEMENT METHODS ---
    
    /**
     * Add a new event to the database
     * @param event The event to add
     * @return The row ID of the newly inserted event, or -1 if an error occurred
     */
    public long addEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(EventContract.EventEntry.COLUMN_NAME_NAME, event.getName());
        values.put(EventContract.EventEntry.COLUMN_NAME_LOCATION, event.getLocation());
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DATE, event.getEventDate());
        values.put(EventContract.EventEntry.COLUMN_NAME_ORGANIZER, event.getOrganizer());
        values.put(EventContract.EventEntry.COLUMN_NAME_THEME, event.getTheme());
        values.put(EventContract.EventEntry.COLUMN_NAME_SUBMISSION_DEADLINE, event.getSubmissionDeadline());
        values.put(EventContract.EventEntry.COLUMN_NAME_USER_ID, event.getUserId());
        values.put(EventContract.EventEntry.COLUMN_NAME_IMAGE_URI, event.getImageUri());
        
        long id = db.insert(EventContract.EventEntry.TABLE_NAME, null, values);
        
        if (id != -1) {
            Log.d(TAG, "Event added with ID: " + id);
        } else {
            Log.e(TAG, "Error adding event: " + event.toString());
        }
        
        return id;
    }
    
    /**
     * Update only the submission deadline of an event
     * @param eventId ID of the event to update
     * @param newDeadline The new submission deadline timestamp
     * @return The number of rows affected
     */
    public int updateEventSubmissionDate(long eventId, long newDeadline) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(EventContract.EventEntry.COLUMN_NAME_SUBMISSION_DEADLINE, newDeadline);
        
        String selection = EventContract.EventEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(eventId) };
        
        int count = db.update(
                EventContract.EventEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        
        if (count > 0) {
            Log.d(TAG, "Updated submission deadline for event ID: " + eventId);
        } else {
            Log.e(TAG, "Error updating submission deadline for event ID: " + eventId);
        }
        
        return count;
    }
    
    /**
     * Update all fields of an event
     * @param event The event with updated fields
     * @return The number of rows affected
     */
    public int updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(EventContract.EventEntry.COLUMN_NAME_NAME, event.getName());
        values.put(EventContract.EventEntry.COLUMN_NAME_LOCATION, event.getLocation());
        values.put(EventContract.EventEntry.COLUMN_NAME_EVENT_DATE, event.getEventDate());
        values.put(EventContract.EventEntry.COLUMN_NAME_ORGANIZER, event.getOrganizer());
        values.put(EventContract.EventEntry.COLUMN_NAME_THEME, event.getTheme());
        values.put(EventContract.EventEntry.COLUMN_NAME_SUBMISSION_DEADLINE, event.getSubmissionDeadline());
        values.put(EventContract.EventEntry.COLUMN_NAME_USER_ID, event.getUserId());
        values.put(EventContract.EventEntry.COLUMN_NAME_IMAGE_URI, event.getImageUri());
        
        String selection = EventContract.EventEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(event.getId()) };
        
        int count = db.update(
                EventContract.EventEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        
        if (count > 0) {
            Log.d(TAG, "Updated event ID: " + event.getId());
        } else {
            Log.e(TAG, "Error updating event ID: " + event.getId());
        }
        
        return count;
    }
    
    /**
     * Get all events for a specific user, sorted by submission deadline (ascending)
     * @param userId The ID of the user whose events to retrieve
     * @return List of all events belonging to the user
     */
    public List<Event> getEventsByUserId(long userId) {
        List<Event> events = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        String[] selectionArgs = { String.valueOf(userId) };
        Cursor cursor = db.rawQuery(EventContract.EventEntry.SQL_SELECT_BY_USER_ID, selectionArgs);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Event event = createEventFromCursor(cursor);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        Log.d(TAG, "Retrieved " + events.size() + " events for user ID: " + userId);
        return events;
    }
    
    /**
     * Get all events, sorted by submission deadline (ascending)
     * @return List of all events
     */
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EventContract.EventEntry.SQL_SELECT_ALL_ORDERED, null);
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Event event = createEventFromCursor(cursor);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        Log.d(TAG, "Retrieved " + events.size() + " events from database");
        return events;
    }
    
    /**
     * Get a single event by ID
     * @param eventId The ID of the event to retrieve
     * @return The event, or null if not found
     */
    public Event getEvent(long eventId) {
        SQLiteDatabase db = getReadableDatabase();
        
        String selection = EventContract.EventEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(eventId) };
        
        Cursor cursor = db.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
        
        Event event = null;
        
        try {
            if (cursor != null && cursor.moveToFirst()) {
                event = createEventFromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        if (event != null) {
            Log.d(TAG, "Retrieved event with ID: " + eventId);
        } else {
            Log.d(TAG, "Event with ID: " + eventId + " not found");
        }
        
        return event;
    }
    
    /**
     * Helper method to create an Event object from a Cursor
     */
    private Event createEventFromCursor(Cursor cursor) {
        Event event = new Event();
        
        int idIndex = cursor.getColumnIndex(EventContract.EventEntry._ID);
        int nameIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_NAME);
        int locationIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_LOCATION);
        int eventDateIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_EVENT_DATE);
        int organizerIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_ORGANIZER);
        int themeIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_THEME);
        int deadlineIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_SUBMISSION_DEADLINE);
        int userIdIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_USER_ID);
        int imageUriIndex = cursor.getColumnIndex(EventContract.EventEntry.COLUMN_NAME_IMAGE_URI);
        
        // Safely get values (check for -1 column index)
        if (idIndex != -1) event.setId(cursor.getLong(idIndex));
        if (nameIndex != -1) event.setName(cursor.getString(nameIndex));
        if (locationIndex != -1) event.setLocation(cursor.getString(locationIndex));
        if (eventDateIndex != -1) event.setEventDate(cursor.getLong(eventDateIndex));
        if (organizerIndex != -1) event.setOrganizer(cursor.getString(organizerIndex));
        if (themeIndex != -1) event.setTheme(cursor.getString(themeIndex));
        if (deadlineIndex != -1) event.setSubmissionDeadline(cursor.getLong(deadlineIndex));
        if (userIdIndex != -1) event.setUserId(cursor.getLong(userIdIndex));
        if (imageUriIndex != -1) event.setImageUri(cursor.getString(imageUriIndex));
        else event.setImageUri(""); // Default image URI if not specified
        
        return event;
    }
    
    /**
     * Delete an event from the database
     * @param eventId The ID of the event to delete
     * @return The number of rows affected
     */
    public int deleteEvent(long eventId) {
        SQLiteDatabase db = getWritableDatabase();
        
        String selection = EventContract.EventEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(eventId) };
        
        int deletedRows = db.delete(
                EventContract.EventEntry.TABLE_NAME,
                selection,
                selectionArgs);
        
        if (deletedRows > 0) {
            Log.d(TAG, "Deleted event with ID: " + eventId);
        } else {
            Log.e(TAG, "Error deleting event with ID: " + eventId);
        }
        
        return deletedRows;
    }

    // --- WISHLIST MANAGEMENT METHODS ---
    
    /**
     * Add an event to user's wishlist
     * @param userId The user ID
     * @param eventId The event ID
     * @return true if added successfully, false otherwise
     */
    public boolean addToWishlist(long userId, long eventId) {
        // Check if event belongs to user
        Event event = getEvent(eventId);
        if (event != null && event.getUserId() == userId) {
            return false; // Can't wishlist own events
        }
        
        // Check if already in wishlist
        if (isInWishlist(userId, eventId)) {
            return false;
        }
        
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WishlistContract.WishlistEntry.COLUMN_NAME_USER_ID, userId);
        values.put(WishlistContract.WishlistEntry.COLUMN_NAME_EVENT_ID, eventId);
        values.put(WishlistContract.WishlistEntry.COLUMN_NAME_ADDED_AT, System.currentTimeMillis());
        
        long id = db.insert(WishlistContract.WishlistEntry.TABLE_NAME, null, values);
        return id != -1;
    }
    
    /**
     * Remove an event from user's wishlist
     * @param userId The user ID
     * @param eventId The event ID
     * @return true if removed successfully, false otherwise
     */
    public boolean removeFromWishlist(long userId, long eventId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = WishlistContract.WishlistEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                WishlistContract.WishlistEntry.COLUMN_NAME_EVENT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId), String.valueOf(eventId) };
        
        int count = db.delete(WishlistContract.WishlistEntry.TABLE_NAME, selection, selectionArgs);
        return count > 0;
    }
    
    /**
     * Check if an event is in user's wishlist
     * @param userId The user ID
     * @param eventId The event ID
     * @return true if in wishlist, false otherwise
     */
    public boolean isInWishlist(long userId, long eventId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = WishlistContract.WishlistEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                WishlistContract.WishlistEntry.COLUMN_NAME_EVENT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId), String.valueOf(eventId) };
        
        Cursor cursor = db.query(
                WishlistContract.WishlistEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }
    
    /**
     * Get all wishlisted events for a user
     * @param userId The user ID
     * @return List of wishlisted events
     */
    public List<Event> getWishlistedEvents(long userId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT e.* FROM " + EventContract.EventEntry.TABLE_NAME + " e " +
                "INNER JOIN " + WishlistContract.WishlistEntry.TABLE_NAME + " w " +
                "ON e." + EventContract.EventEntry._ID + " = w." + WishlistContract.WishlistEntry.COLUMN_NAME_EVENT_ID + " " +
                "WHERE w." + WishlistContract.WishlistEntry.COLUMN_NAME_USER_ID + " = ? " +
                "ORDER BY w." + WishlistContract.WishlistEntry.COLUMN_NAME_ADDED_AT + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(userId) });
        
        try {
            if (cursor.moveToFirst()) {
                do {
                    Event event = createEventFromCursor(cursor);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        
        return events;
    }
} 