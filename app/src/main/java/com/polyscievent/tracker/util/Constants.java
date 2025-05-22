package com.polyscievent.tracker.util;

/**
 * App-wide constants
 */
public class Constants {
    
    // Intent extras
    public static final String EXTRA_USER_ID = "com.polyscievent.tracker.EXTRA_USER_ID";
    public static final String EXTRA_EVENT_ID = "com.polyscievent.tracker.EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_NAME = "com.polyscievent.tracker.EXTRA_EVENT_NAME";
    public static final String EXTRA_EVENT_LOCATION = "com.polyscievent.tracker.EXTRA_EVENT_LOCATION";
    public static final String EXTRA_EVENT_DATE = "com.polyscievent.tracker.EXTRA_EVENT_DATE";
    public static final String EXTRA_EVENT_ORGANIZER = "com.polyscievent.tracker.EXTRA_EVENT_ORGANIZER";
    public static final String EXTRA_EVENT_THEME = "com.polyscievent.tracker.EXTRA_EVENT_THEME";
    public static final String EXTRA_SUBMISSION_DEADLINE = "com.polyscievent.tracker.EXTRA_SUBMISSION_DEADLINE";
    public static final String EXTRA_EDIT_DEADLINE_ONLY = "com.polyscievent.tracker.EXTRA_EDIT_DEADLINE_ONLY";
    
    // Request codes
    public static final int REQUEST_ADD_EVENT = 1;
    public static final int REQUEST_EDIT_EVENT = 2;
    
    // Result codes
    public static final int RESULT_EVENT_ADDED = 101;
    public static final int RESULT_EVENT_UPDATED = 102;
    public static final int RESULT_EVENT_DELETED = 103;
    
    // Deadline thresholds (in days)
    public static final int DEADLINE_APPROACHING_DAYS = 7;
    public static final int DEADLINE_URGENT_DAYS = 2;
} 