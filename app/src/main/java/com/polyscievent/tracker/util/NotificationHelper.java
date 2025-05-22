package com.polyscievent.tracker.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.polyscievent.tracker.R;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

public class NotificationHelper {
    public static final String CHANNEL_ID = "polyscievent_channel";
    private static final String CHANNEL_NAME = "Event Deadline Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for upcoming scientific event deadlines";
    
    // Time constants for notifications (in milliseconds)
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final long ONE_WEEK = 7 * ONE_DAY;
    
    public static void createNotificationChannel(Context context) {
        // Only needed for API 26+ (Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public static void scheduleNotification(Context context, Event event) {
        // Cancel any existing notifications for this event
        cancelNotification(context, event.getId());
        
        // Only schedule if deadline is in the future
        Date now = new Date();
        long deadlineTime = event.getSubmissionDeadline();
        if (deadlineTime < now.getTime()) {
            return; // Don't schedule notifications for past deadlines
        }
        
        // Schedule "day before" notification if deadline is more than a day away
        long dayBeforeTime = deadlineTime - ONE_DAY;
        if (dayBeforeTime > now.getTime()) {
            scheduleNotificationAt(context, event, dayBeforeTime, "tomorrow");
        }
        
        // Schedule "week before" notification if deadline is more than a week away
        long weekBeforeTime = deadlineTime - ONE_WEEK;
        if (weekBeforeTime > now.getTime()) {
            scheduleNotificationAt(context, event, weekBeforeTime, "in a week");
        }
    }
    
    private static void scheduleNotificationAt(Context context, Event event, long triggerTime, String timeFrame) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getName());
        intent.putExtra("timeFrame", timeFrame);
        
        // Create a unique request code based on event ID and time frame
        int requestCode = (int) (event.getId() * 100);
        if (timeFrame.equals("tomorrow")) {
            requestCode += 1;
        } else if (timeFrame.equals("in a week")) {
            requestCode += 2;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // Set exact alarm for API level < 19, and setExactAndAllowWhileIdle for API level >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
        
        String formattedDate = DateUtils.formatDate(triggerTime);
        String eventName = event.getName();
        if (eventName != null) {
            String logMessage = "Notification scheduled for event " + eventName + " at " + formattedDate;
            android.util.Log.d("NotificationHelper", logMessage);
        }
    }
    
    public static void cancelNotification(Context context, long eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        
        // Cancel "day before" notification
        int dayBeforeRequestCode = (int) (eventId * 100) + 1;
        Intent dayBeforeIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent dayBeforePendingIntent = PendingIntent.getBroadcast(
                context, 
                dayBeforeRequestCode, 
                dayBeforeIntent, 
                PendingIntent.FLAG_NO_CREATE | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        if (dayBeforePendingIntent != null) {
            alarmManager.cancel(dayBeforePendingIntent);
            dayBeforePendingIntent.cancel();
        }
        
        // Cancel "week before" notification
        int weekBeforeRequestCode = (int) (eventId * 100) + 2;
        Intent weekBeforeIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent weekBeforePendingIntent = PendingIntent.getBroadcast(
                context, 
                weekBeforeRequestCode, 
                weekBeforeIntent, 
                PendingIntent.FLAG_NO_CREATE | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        if (weekBeforePendingIntent != null) {
            alarmManager.cancel(weekBeforePendingIntent);
            weekBeforePendingIntent.cancel();
        }
    }
} 