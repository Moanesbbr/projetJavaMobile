package com.polyscievent.tracker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.polyscievent.tracker.R;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.util.Constants;
import com.polyscievent.tracker.util.DateUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewEventActivity extends AppCompatActivity {
    
    private ImageView mEventImageView;
    private TextView mNameTextView;
    private TextView mLocationTextView;
    private TextView mDateTextView;
    private TextView mOrganizerTextView;
    private TextView mThemeTextView;
    private TextView mDeadlineTextView;
    private FloatingActionButton mEditFab;
    
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    private long mEventId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        
        // Initialize threading components
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        
        // Set up the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.view_event);
        }
        
        // Initialize views
        initViews();
        
        // Get event ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.EXTRA_EVENT_ID)) {
            mEventId = intent.getLongExtra(Constants.EXTRA_EVENT_ID, -1);
            if (mEventId != -1) {
                loadEvent();
            } else {
                Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Error: Event ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Set up edit FAB
        mEditFab.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, AddEditEventActivity.class);
            editIntent.putExtra(Constants.EXTRA_EVENT_ID, mEventId);
            startActivity(editIntent);
        });
    }
    
    private void initViews() {
        mEventImageView = findViewById(R.id.image_view_event);
        mNameTextView = findViewById(R.id.text_view_name);
        mLocationTextView = findViewById(R.id.text_view_location);
        mDateTextView = findViewById(R.id.text_view_date);
        mOrganizerTextView = findViewById(R.id.text_view_organizer);
        mThemeTextView = findViewById(R.id.text_view_theme);
        mDeadlineTextView = findViewById(R.id.text_view_deadline);
        mEditFab = findViewById(R.id.fab_edit);
    }
    
    private void loadEvent() {
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            Event event = dbHelper.getEvent(mEventId);
            
            mMainHandler.post(() -> {
                if (event != null) {
                    // Set event image if available
                    if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
                        mEventImageView.setImageURI(Uri.parse(event.getImageUri()));
                    }
                    
                    // Set text fields
                    mNameTextView.setText(event.getName());
                    mLocationTextView.setText(event.getLocation());
                    mDateTextView.setText(DateUtils.formatDate(event.getEventDate()));
                    mOrganizerTextView.setText(event.getOrganizer());
                    mThemeTextView.setText(event.getTheme());
                    mDeadlineTextView.setText(DateUtils.formatDate(event.getSubmissionDeadline()));
                } else {
                    Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
    }
} 