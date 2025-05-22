package com.polyscievent.tracker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.polyscievent.tracker.R;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.User;
import com.polyscievent.tracker.util.UserSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private TextInputLayout mFullNameLayout;
    private TextInputLayout mCurrentPasswordLayout;
    private TextInputLayout mNewPasswordLayout;
    private TextInputLayout mConfirmPasswordLayout;
    private EditText mFullNameInput;
    private EditText mEmailInput;
    private EditText mCurrentPasswordInput;
    private EditText mNewPasswordInput;
    private EditText mConfirmPasswordInput;
    private Button mUpdateButton;
    
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    private UserSession mUserSession;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }

        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        mUserSession = new UserSession(this);
        mCurrentUser = mUserSession.getUserDetails();

        initializeViews();
        loadUserData();
        mUpdateButton.setOnClickListener(v -> validateAndUpdateProfile());
    }

    private void initializeViews() {
        mFullNameLayout = findViewById(R.id.text_input_layout_full_name);
        mCurrentPasswordLayout = findViewById(R.id.text_input_layout_current_password);
        mNewPasswordLayout = findViewById(R.id.text_input_layout_new_password);
        mConfirmPasswordLayout = findViewById(R.id.text_input_layout_confirm_password);

        mFullNameInput = findViewById(R.id.edit_text_full_name);
        mEmailInput = findViewById(R.id.edit_text_email);
        mCurrentPasswordInput = findViewById(R.id.edit_text_current_password);
        mNewPasswordInput = findViewById(R.id.edit_text_new_password);
        mConfirmPasswordInput = findViewById(R.id.edit_text_confirm_password);
        mUpdateButton = findViewById(R.id.button_update_profile);
    }

    private void loadUserData() {
        if (mCurrentUser != null) {
            mFullNameInput.setText(mCurrentUser.getFullName());
            mEmailInput.setText(mCurrentUser.getEmail());
            mEmailInput.setEnabled(false);
        }
    }

    private void validateAndUpdateProfile() {
        mFullNameLayout.setError(null);
        mCurrentPasswordLayout.setError(null);
        mNewPasswordLayout.setError(null);
        mConfirmPasswordLayout.setError(null);

        String fullName = mFullNameInput.getText().toString().trim();
        String currentPassword = mCurrentPasswordInput.getText().toString();
        String newPassword = mNewPasswordInput.getText().toString();
        String confirmPassword = mConfirmPasswordInput.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            mFullNameLayout.setError(getString(R.string.error_empty_name));
            return;
        }

        boolean changingPassword = !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmPassword);
        if (changingPassword) {
            if (TextUtils.isEmpty(currentPassword)) {
                mCurrentPasswordLayout.setError(getString(R.string.error_current_password_required));
                return;
            }
            if (TextUtils.isEmpty(newPassword)) {
                mNewPasswordLayout.setError(getString(R.string.error_new_password_required));
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                mConfirmPasswordLayout.setError(getString(R.string.error_confirm_password_required));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                mConfirmPasswordLayout.setError(getString(R.string.error_password_mismatch));
                return;
            }
        }

        updateProfile(fullName, currentPassword, newPassword, changingPassword);
    }

    private void updateProfile(String fullName, String currentPassword, String newPassword, boolean changingPassword) {
        mUpdateButton.setEnabled(false);

        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            boolean success = false;
            String errorMessage = null;

            if (changingPassword) {
                User user = dbHelper.authenticateUser(mCurrentUser.getEmail(), currentPassword);
                if (user == null) {
                    errorMessage = getString(R.string.error_incorrect_password);
                } else {
                    mCurrentUser.setFullName(fullName);
                    mCurrentUser.setPassword(newPassword);
                    success = dbHelper.updateUser(mCurrentUser) > 0;
                }
            } else {
                mCurrentUser.setFullName(fullName);
                success = dbHelper.updateUser(mCurrentUser) > 0;
            }

            final String finalErrorMessage = errorMessage;
            final boolean finalSuccess = success;
            mMainHandler.post(() -> {
                mUpdateButton.setEnabled(true);
                if (finalSuccess) {
                    mUserSession.updateUserDetails(mCurrentUser);
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    mCurrentPasswordInput.setText("");
                    mNewPasswordInput.setText("");
                    mConfirmPasswordInput.setText("");
                } else if (finalErrorMessage != null) {
                    Toast.makeText(this, finalErrorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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