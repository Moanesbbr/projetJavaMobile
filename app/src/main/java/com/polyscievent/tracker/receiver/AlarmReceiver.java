package com.polyscievent.tracker.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polyscievent.tracker.R;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.activity.MainActivity;
import com.polyscievent.tracker.util.DateUtils;

/**
 * Broadcast receiver for handling deadline alarm notifications
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final String EXTRA_EVENT_ID = "event_id";
    
    // Notification channel constants
    private static final String CHANNEL_ID = "event_deadline_channel";
    private static final String CHANNEL_NAME = "Event Deadlines";
    private static final String CHANNEL_DESC = "Notifications for upcoming event deadlines";
    private static final int NOTIFICATION_ID = 1000;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get event details from the intent
        long eventId = intent.getLongExtra("event_id", -1);
        String eventTitle = intent.getStringExtra("event_title");
        String timeFrame = intent.getStringExtra("time_frame");
        
        if (eventId == -1 || eventTitle == null || timeFrame == null) {
            return;
        }
        
        // Create notification
        showNotification(context, eventId, eventTitle, timeFrame);
    }
    
    /**
     * Shows a notification for an upcoming event deadline.
     *
     * @param context The application context
     * @param eventId The ID of the event
     * @param eventTitle The title of the event
     * @param timeFrame The time frame of the event
     */
    private void showNotification(Context context, long eventId, String eventTitle, String timeFrame) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return;
        }

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to open the app when notification is tapped
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("event_id", eventId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Event Deadline Reminder")
                .setContentText(eventTitle + " - " + timeFrame)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.BLUE);

        // Show the notification with a unique ID based on the event ID
        int notificationId = (int) eventId;
        notificationManager.notify(notificationId, builder.build());
        
        Log.d(TAG, "Notification shown for event: " + eventTitle);
    }
} 