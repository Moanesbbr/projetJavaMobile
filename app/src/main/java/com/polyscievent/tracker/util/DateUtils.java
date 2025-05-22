package com.polyscievent.tracker.util;

import android.content.Context;

import com.polyscievent.tracker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date formatting and comparison
 */
public class DateUtils {

    private static final int DAYS_WARNING_THRESHOLD = 7; // Days before deadline to start showing warning color
    private static final int DAYS_URGENT_THRESHOLD = 2;  // Days before deadline to show urgent color

    /**
     * Format a timestamp into a readable date string
     * @param timestamp The timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format a timestamp into a readable date string using specified format
     * @param timestamp The timestamp in milliseconds
     * @param format The date format pattern
     * @return Formatted date string
     */
    public static String formatDate(long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Check if a deadline is approaching (within the warning threshold)
     * @param deadlineTimestamp The deadline timestamp in milliseconds
     * @return True if the deadline is approaching, false otherwise
     */
    public static boolean isDeadlineApproaching(long deadlineTimestamp) {
        long currentTime = System.currentTimeMillis();
        long daysUntilDeadline = getDaysUntilDeadline(currentTime, deadlineTimestamp);
        
        return daysUntilDeadline <= DAYS_WARNING_THRESHOLD && daysUntilDeadline > DAYS_URGENT_THRESHOLD;
    }
    
    /**
     * Check if a deadline is urgent (within the urgent threshold)
     * @param deadlineTimestamp The deadline timestamp in milliseconds
     * @return True if the deadline is urgent, false otherwise
     */
    public static boolean isDeadlineUrgent(long deadlineTimestamp) {
        long currentTime = System.currentTimeMillis();
        long daysUntilDeadline = getDaysUntilDeadline(currentTime, deadlineTimestamp);
        
        return daysUntilDeadline <= DAYS_URGENT_THRESHOLD && daysUntilDeadline >= 0;
    }
    
    /**
     * Check if a deadline has already passed
     * @param deadlineTimestamp The deadline timestamp in milliseconds
     * @return True if the deadline has passed, false otherwise
     */
    public static boolean isDeadlinePassed(long deadlineTimestamp) {
        return System.currentTimeMillis() > deadlineTimestamp;
    }
    
    /**
     * Calculate days until the deadline
     * @param currentTime Current time in milliseconds
     * @param deadlineTimestamp The deadline timestamp in milliseconds
     * @return Number of days until the deadline
     */
    public static long getDaysUntilDeadline(long currentTime, long deadlineTimestamp) {
        long diffMillis = deadlineTimestamp - currentTime;
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }
    
    /**
     * Get days until deadline text for display
     * @param context Android context for string resources
     * @param deadlineTimestamp The deadline timestamp in milliseconds
     * @return String describing days until deadline
     */
    public static String getDeadlineText(Context context, long deadlineTimestamp) {
        long currentTime = System.currentTimeMillis();
        long daysUntilDeadline = getDaysUntilDeadline(currentTime, deadlineTimestamp);
        
        if (daysUntilDeadline < 0) {
            return context.getString(R.string.deadline_passed);
        } else if (daysUntilDeadline == 0) {
            return context.getString(R.string.deadline_today);
        } else {
            return context.getString(R.string.days_until_deadline, daysUntilDeadline);
        }
    }
    
    /**
     * Create a Calendar instance set to start of day
     * @return Calendar set to start of today
     */
    public static Calendar getStartOfDay(Calendar calendar) {
        Calendar startOfDay = (Calendar) calendar.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        return startOfDay;
    }
} 