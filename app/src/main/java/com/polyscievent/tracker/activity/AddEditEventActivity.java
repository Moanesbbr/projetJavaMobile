package com.polyscievent.tracker.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.polyscievent.tracker.R;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.util.Constants;
import com.polyscievent.tracker.util.DateUtils;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for adding a new event or editing an existing event
 */
public class AddEditEventActivity extends AppCompatActivity {

    private static final String TAG = AddEditEventActivity.class.getSimpleName();
    
    private TextView mTitleTextView;
    private ImageView mEventImageView;
    private Button mPickImageButton;
    private TextInputEditText mNameEditText;
    private TextInputEditText mLocationEditText;
    private TextInputEditText mEventDateEditText;
    private TextInputEditText mOrganizerEditText;
    private TextInputEditText mThemeEditText;
    private TextInputEditText mSubmissionDeadlineEditText;
    private Button mSaveButton;
    private Button mCancelButton;
    
    private Calendar mEventDateCalendar;
    private Calendar mSubmissionDeadlineCalendar;
    private Uri mSelectedImageUri;
    
    private long mEventId = -1;
    private long mUserId = -1; // User ID of the current user
    private boolean mEditMode = false;
    
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    
    // Image picker launcher
    private final ActivityResultLauncher<Intent> mImagePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            // Take a persistent URI permission
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            
                            mSelectedImageUri = uri;
                            try {
                                mEventImageView.setImageURI(null); // Clear the old image
                                mEventImageView.setImageURI(uri);
                            } catch (Exception e) {
                                Log.e(TAG, "Error setting image: " + e.getMessage());
                                Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                            }
                        } catch (SecurityException e) {
                            Log.w(TAG, "Could not take URI permission: " + e.getMessage());
                            Toast.makeText(this, R.string.error_saving_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);
        
        // Initialize threading components for background operations
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize default calendar instances for date picking
        mEventDateCalendar = Calendar.getInstance();
        mSubmissionDeadlineCalendar = Calendar.getInstance();
        
        // Set up the action bar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        // Get user ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.EXTRA_USER_ID)) {
            mUserId = intent.getLongExtra(Constants.EXTRA_USER_ID, -1);
            
            if (mUserId == -1) {
                // If no valid user ID, finish activity
                Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            // If no user ID, finish activity
            Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Find and initialize UI components
        initViews();
        
        // Set up listeners for date fields and buttons
        setUpListeners();
        
        // Check if we're in edit mode and populate fields if so
        checkForEditMode();
    }
    
    private void initViews() {
        mTitleTextView = findViewById(R.id.text_view_title);
        mEventImageView = findViewById(R.id.image_view_event);
        mPickImageButton = findViewById(R.id.button_pick_image);
        mNameEditText = findViewById(R.id.edit_text_name);
        mLocationEditText = findViewById(R.id.edit_text_location);
        mEventDateEditText = findViewById(R.id.edit_text_event_date);
        mOrganizerEditText = findViewById(R.id.edit_text_organizer);
        mThemeEditText = findViewById(R.id.edit_text_theme);
        mSubmissionDeadlineEditText = findViewById(R.id.edit_text_submission_deadline);
        mSaveButton = findViewById(R.id.button_save);
        mCancelButton = findViewById(R.id.button_cancel);
    }
    
    private void setUpListeners() {
        // Set up image picker
        mPickImageButton.setOnClickListener(v -> launchImagePicker());
        
        // Set up event date picker
        mEventDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddEditEventActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        mEventDateCalendar.set(Calendar.YEAR, year);
                        mEventDateCalendar.set(Calendar.MONTH, month);
                        mEventDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mEventDateEditText.setText(DateUtils.formatDate(mEventDateCalendar.getTimeInMillis()));
                    },
                    mEventDateCalendar.get(Calendar.YEAR),
                    mEventDateCalendar.get(Calendar.MONTH),
                    mEventDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Set up submission deadline picker
        mSubmissionDeadlineEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddEditEventActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        mSubmissionDeadlineCalendar.set(Calendar.YEAR, year);
                        mSubmissionDeadlineCalendar.set(Calendar.MONTH, month);
                        mSubmissionDeadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mSubmissionDeadlineEditText.setText(DateUtils.formatDate(mSubmissionDeadlineCalendar.getTimeInMillis()));
                    },
                    mSubmissionDeadlineCalendar.get(Calendar.YEAR),
                    mSubmissionDeadlineCalendar.get(Calendar.MONTH),
                    mSubmissionDeadlineCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Set up save button click listener
        mSaveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveEvent();
            }
        });
        
        // Set up cancel button click listener
        mCancelButton.setOnClickListener(v -> finish());
    }
    
    private void checkForEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.EXTRA_EVENT_ID)) {
            mEventId = intent.getLongExtra(Constants.EXTRA_EVENT_ID, -1);
            mEditMode = mEventId != -1;
            
            if (mEditMode) {
                mTitleTextView.setText(R.string.edit_event);
                loadEvent();
            }
        }
    }
    
    private void loadEvent() {
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(AddEditEventActivity.this);
            Event event = dbHelper.getEvent(mEventId);
            
            mMainHandler.post(() -> {
                if (event != null) {
                    // Populate fields with event data
                    mNameEditText.setText(event.getName());
                    mLocationEditText.setText(event.getLocation());
                    mOrganizerEditText.setText(event.getOrganizer());
                    mThemeEditText.setText(event.getTheme());
                    
                    // Set up date fields
                    mEventDateCalendar.setTimeInMillis(event.getEventDate());
                    mEventDateEditText.setText(DateUtils.formatDate(event.getEventDate()));
                    
                    mSubmissionDeadlineCalendar.setTimeInMillis(event.getSubmissionDeadline());
                    mSubmissionDeadlineEditText.setText(DateUtils.formatDate(event.getSubmissionDeadline()));
                    
                    // Load image if available
                    String imageUri = event.getImageUri();
                    if (imageUri != null && !imageUri.isEmpty()) {
                        try {
                            mSelectedImageUri = Uri.parse(imageUri);
                            mEventImageView.setImageURI(null); // Clear the old image
                            mEventImageView.setImageURI(mSelectedImageUri);
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                            Toast.makeText(AddEditEventActivity.this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mEventImageView.setImageResource(R.drawable.ic_event_placeholder);
                    }
                } else {
                    Toast.makeText(AddEditEventActivity.this, "Error loading event", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validate name
        String name = mNameEditText.getText() != null ? mNameEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError(getString(R.string.error_empty_name));
            isValid = false;
        }
        
        // Validate location
        String location = mLocationEditText.getText() != null ? mLocationEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(location)) {
            mLocationEditText.setError(getString(R.string.error_empty_location));
            isValid = false;
        }
        
        // Validate event date
        String eventDateStr = mEventDateEditText.getText() != null ? mEventDateEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(eventDateStr)) {
            mEventDateEditText.setError(getString(R.string.error_empty_date));
            isValid = false;
        }
        
        // Validate organizer
        String organizer = mOrganizerEditText.getText() != null ? mOrganizerEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(organizer)) {
            mOrganizerEditText.setError(getString(R.string.error_empty_organizer));
            isValid = false;
        }
        
        // Validate theme
        String theme = mThemeEditText.getText() != null ? mThemeEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(theme)) {
            mThemeEditText.setError(getString(R.string.error_empty_theme));
            isValid = false;
        }
        
        // Validate submission deadline
        String submissionDeadlineStr = mSubmissionDeadlineEditText.getText() != null ? 
                mSubmissionDeadlineEditText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(submissionDeadlineStr)) {
            mSubmissionDeadlineEditText.setError(getString(R.string.error_empty_deadline));
            isValid = false;
        }
        
        return isValid;
    }
    
    private void saveEvent() {
        String name = mNameEditText.getText().toString().trim();
        String location = mLocationEditText.getText().toString().trim();
        String organizer = mOrganizerEditText.getText().toString().trim();
        String theme = mThemeEditText.getText().toString().trim();
        long eventDate = mEventDateCalendar.getTimeInMillis();
        long submissionDeadline = mSubmissionDeadlineCalendar.getTimeInMillis();
        
        // Get the current image URI or keep the existing one in edit mode
        String imageUri = "";
        if (mSelectedImageUri != null) {
            imageUri = mSelectedImageUri.toString();
            Log.d(TAG, "Saving event with image URI: " + imageUri);
        } else if (mEditMode) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            Event existingEvent = dbHelper.getEvent(mEventId);
            if (existingEvent != null && existingEvent.getImageUri() != null) {
                imageUri = existingEvent.getImageUri();
                Log.d(TAG, "Keeping existing image URI: " + imageUri);
            }
        }
        
        final Event event = new Event(name, location, eventDate, organizer, theme, submissionDeadline, mUserId, imageUri);
        
        if (mEditMode) {
            event.setId(mEventId);
        }
        
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(AddEditEventActivity.this);
            long result = mEditMode ? dbHelper.updateEvent(event) : dbHelper.addEvent(event);
            
            mMainHandler.post(() -> {
                if (result > 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddEditEventActivity.this, "Error saving event", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            mImagePicker.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching image picker: " + e.getMessage());
            Toast.makeText(this, R.string.error_picking_image, Toast.LENGTH_SHORT).show();
        }
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
        // Shutdown the executor service when the activity is destroyed
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
    }
} 