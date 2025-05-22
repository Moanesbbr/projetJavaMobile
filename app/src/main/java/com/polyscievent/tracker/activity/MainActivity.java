package com.polyscievent.tracker.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.polyscievent.tracker.R;
import com.polyscievent.tracker.adapter.EventAdapter;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.model.User;
import com.polyscievent.tracker.util.Constants;
import com.polyscievent.tracker.util.ExportUtils;
import com.polyscievent.tracker.util.UserSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity that displays the list of scientific events
 */
public class MainActivity extends AppCompatActivity implements EventAdapter.EventItemClickListener {
    
    private static final int REQUEST_EXPORT_FILE = 3;
    private static final String CSV_MIME_TYPE = "text/csv";
    private static final String JSON_MIME_TYPE = "application/json";
    
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private TextView mEmptyTextView;
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    private BottomNavigationView mBottomNav;
    
    private String mSelectedExportFormat;
    private UserSession mUserSession;
    private User mCurrentUser;
    private boolean mShowingAllEvents = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize user session
        mUserSession = new UserSession(this);
        
        // Check if user is logged in
        if (!mUserSession.isLoggedIn()) {
            // Redirect to login activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        
        // Get current user details
        mCurrentUser = mUserSession.getUserDetails();
        if (mCurrentUser != null) {
            setTitle("Evento");
        }
        
        // Initialize threading components for background operations
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        
        // Set up views
        mRecyclerView = findViewById(R.id.recycler_view_events);
        mEmptyTextView = findViewById(R.id.text_view_empty);
        FloatingActionButton fab = findViewById(R.id.fab_add_event);
        mBottomNav = findViewById(R.id.bottom_navigation);
        
        // Set up RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EventAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        
        // Set up bottom navigation
        mBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                if (!mShowingAllEvents) {
                    mShowingAllEvents = true;
                    loadEvents();
                    Toast.makeText(this, R.string.showing_all_events, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.navigation_my_events) {
                if (mShowingAllEvents) {
                    mShowingAllEvents = false;
                    loadEvents();
                    Toast.makeText(this, R.string.showing_my_events, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.navigation_settings) {
                // TODO: Implement settings screen
                Toast.makeText(this, R.string.settings, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        // Set up FAB click listener to add a new event
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditEventActivity.class);
            intent.putExtra(Constants.EXTRA_USER_ID, mCurrentUser.getId());
            startActivityForResult(intent, Constants.REQUEST_ADD_EVENT);
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_wishlist) {
            startActivity(new Intent(this, WishlistActivity.class));
            return true;
        } else if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Show dialog to select export format
     */
    private void showExportFormatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.export_format_prompt)
                .setItems(new CharSequence[]{
                        getString(R.string.export_format_csv),
                        getString(R.string.export_format_json)
                }, (dialog, which) -> {
                    if (which == 0) {
                        mSelectedExportFormat = "csv";
                        startExportProcess(CSV_MIME_TYPE, ".csv");
                    } else {
                        mSelectedExportFormat = "json";
                        startExportProcess(JSON_MIME_TYPE, ".json");
                    }
                })
                .show();
    }
    
    /**
     * Start the export process by creating a file in the selected format
     */
    private void startExportProcess(String mimeType, String extension) {
        // Check if we have events to export
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            final List<Event> events;
            if (mShowingAllEvents) {
                events = dbHelper.getAllEvents();
            } else {
                events = dbHelper.getEventsByUserId(mCurrentUser.getId());
            }
            
            mMainHandler.post(() -> {
                if (events.isEmpty()) {
                    Toast.makeText(this, R.string.export_no_events, Toast.LENGTH_SHORT).show();
                } else {
                    // Create intent to create a new document
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mimeType);
                    
                    // Set default file name (current date + extension)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    String fileName = (mShowingAllEvents ? "all_events_" : "my_events_") + 
                            sdf.format(new Date()) + extension;
                    intent.putExtra(Intent.EXTRA_TITLE, fileName);
                    
                    startActivityForResult(intent, REQUEST_EXPORT_FILE);
                }
            });
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service when the activity is destroyed
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
    }
    
    /**
     * Load events from the database in a background thread
     */
    private void loadEvents() {
        if (mCurrentUser == null) {
            return;
        }
        
        mExecutorService.execute(() -> {
            // Get the database helper instance
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(MainActivity.this);
            
            // Get events based on current view mode
            final List<Event> events;
            if (mShowingAllEvents) {
                events = dbHelper.getAllEvents();
            } else {
                events = dbHelper.getEventsByUserId(mCurrentUser.getId());
            }
            
            // Update the UI on the main thread
            mMainHandler.post(() -> {
                if (events.isEmpty()) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mEmptyTextView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.setEvents(events);
                }
            });
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_ADD_EVENT) {
                // Refresh the events list after adding an event
                Toast.makeText(this, R.string.event_saved, Toast.LENGTH_SHORT).show();
                loadEvents();
            } else if (requestCode == Constants.REQUEST_EDIT_EVENT) {
                // Refresh the events list after editing an event
                Toast.makeText(this, R.string.event_updated, Toast.LENGTH_SHORT).show();
                loadEvents();
            } else if (requestCode == REQUEST_EXPORT_FILE && data != null) {
                // Handle export file creation result
                Uri uri = data.getData();
                if (uri != null) {
                    exportDataToUri(uri);
                }
            }
        }
    }
    
    /**
     * Export event data to the selected URI
     */
    private void exportDataToUri(Uri uri) {
        ExportUtils.ExportTask exportTask = new ExportUtils.ExportTask(
                this,
                uri,
                mSelectedExportFormat,
                new ExportUtils.ExportCallback() {
                    @Override
                    public void onExportSuccess() {
                        Toast.makeText(MainActivity.this, 
                                R.string.export_success, Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onExportError(String error) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.export_error, error), 
                                Toast.LENGTH_LONG).show();
                    }
                });
        exportTask.execute();
    }
    
    @Override
    public void onEventItemClick(Event event) {
        // Launch the view event activity
        Intent intent = new Intent(this, ViewEventActivity.class);
        intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }
    
    @Override
    public void onEventDeleteClick(Event event) {
        // Show confirmation dialog before deletion
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_event)
                .setMessage(R.string.delete_event_confirmation)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Delete the event in a background thread
                    mExecutorService.execute(() -> {
                        // Get database helper instance
                        DatabaseHelper dbHelper = DatabaseHelper.getInstance(MainActivity.this);
                        // Delete the event
                        dbHelper.deleteEvent(event.getId());
                        // Refresh the events list on the main thread
                        mMainHandler.post(() -> {
                            loadEvents();
                            Toast.makeText(MainActivity.this, 
                                    R.string.event_deleted, Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    /**
     * Sign out the current user and redirect to sign in screen
     */
    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user session
                    mUserSession.logoutUser();
                    
                    // Redirect to sign in screen
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Load wishlisted events from the database
     */
    private void loadWishlistedEvents() {
        if (mCurrentUser == null) {
            return;
        }
        
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(MainActivity.this);
            final List<Event> events = dbHelper.getWishlistedEvents(mCurrentUser.getId());
            
            mMainHandler.post(() -> {
                if (events.isEmpty()) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mEmptyTextView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.setEvents(events);
                }
                Toast.makeText(this, R.string.showing_wishlist, Toast.LENGTH_SHORT).show();
            });
        });
    }
} 