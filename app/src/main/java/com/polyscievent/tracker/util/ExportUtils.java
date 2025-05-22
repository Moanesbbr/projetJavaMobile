package com.polyscievent.tracker.util;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting events data to different formats (JSON, CSV)
 */
public class ExportUtils {
    private static final String TAG = "ExportUtils";
    
    /**
     * Interface for export operation callback
     */
    public interface ExportCallback {
        void onExportSuccess();
        void onExportError(String error);
    }
    
    /**
     * AsyncTask for exporting events in the background
     */
    public static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;
        private final Uri mDestinationUri;
        private final String mExportFormat;
        private final ExportCallback mCallback;
        private String mErrorMessage;
        
        public ExportTask(Context context, Uri destinationUri, String exportFormat, ExportCallback callback) {
            mContext = context;
            mDestinationUri = destinationUri;
            mExportFormat = exportFormat;
            mCallback = callback;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
                List<Event> events = dbHelper.getAllEvents();
                
                if (events.isEmpty()) {
                    mErrorMessage = "No events to export";
                    return false;
                }
                
                String exportContent;
                if (mExportFormat.equals("json")) {
                    exportContent = convertToJson(events);
                } else {
                    exportContent = convertToCsv(events);
                }
                
                writeToUri(mContext, mDestinationUri, exportContent);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Export error: " + e.getMessage());
                mErrorMessage = "Export failed: " + e.getMessage();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mCallback.onExportSuccess();
            } else {
                mCallback.onExportError(mErrorMessage);
            }
        }
    }
    
    /**
     * Convert a list of events to JSON format
     */
    public static String convertToJson(List<Event> events) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (Event event : events) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", event.getId());
            jsonObject.put("name", event.getName());
            jsonObject.put("location", event.getLocation());
            jsonObject.put("eventDate", sdf.format(new Date(event.getEventDate())));
            jsonObject.put("organizer", event.getOrganizer());
            jsonObject.put("theme", event.getTheme());
            jsonObject.put("submissionDeadline", sdf.format(new Date(event.getSubmissionDeadline())));
            jsonArray.put(jsonObject);
        }
        
        JSONObject root = new JSONObject();
        root.put("events", jsonArray);
        return root.toString(2); // pretty print with 2-space indentation
    }
    
    /**
     * Convert a list of events to CSV format
     */
    public static String convertToCsv(List<Event> events) {
        StringBuilder csv = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Add CSV header
        csv.append("ID,Name,Location,Event Date,Organizer,Theme,Submission Deadline\n");
        
        // Add event data
        for (Event event : events) {
            csv.append(event.getId()).append(",");
            csv.append(escapeCsvField(event.getName())).append(",");
            csv.append(escapeCsvField(event.getLocation())).append(",");
            csv.append(sdf.format(new Date(event.getEventDate()))).append(",");
            csv.append(escapeCsvField(event.getOrganizer())).append(",");
            csv.append(escapeCsvField(event.getTheme())).append(",");
            csv.append(sdf.format(new Date(event.getSubmissionDeadline()))).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Escape CSV field according to RFC 4180
     */
    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // If the field contains commas, quotes, or newlines, wrap it in quotes
        // and escape any quotes within it by doubling them
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
    
    /**
     * Write content to a URI
     */
    private static void writeToUri(Context context, Uri uri, String content) throws IOException {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                outputStream.flush();
            } else {
                throw new IOException("Could not open output stream");
            }
        }
    }
} 