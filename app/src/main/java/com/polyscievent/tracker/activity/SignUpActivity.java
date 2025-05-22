package com.polyscievent.tracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.polyscievent.tracker.R;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.User;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editTextName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonSignUp;
    private TextView textViewSignIn;
    
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        
        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        editTextName = findViewById(R.id.edit_text_name);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        buttonSignUp = findViewById(R.id.button_sign_up);
        textViewSignIn = findViewById(R.id.text_view_sign_in);

        // Set up listeners
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to sign in
            }
        });
    }

    private void attemptRegistration() {
        // Reset errors
        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

        // Get values
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Check for valid input
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            editTextName.setError(getString(R.string.empty_fields));
            focusView = editTextName;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.empty_fields));
            focusView = editTextEmail;
            cancel = true;
        } else if (!email.contains("@")) {
            editTextEmail.setError("Please enter a valid email");
            focusView = editTextEmail;
            cancel = true;
        } else if (dbHelper.isEmailRegistered(email)) {
            editTextEmail.setError("This email is already registered");
            focusView = editTextEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.empty_fields));
            focusView = editTextPassword;
            cancel = true;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            focusView = editTextPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.empty_fields));
            focusView = editTextConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.password_mismatch));
            focusView = editTextConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Perform the sign up
            performSignUp(name, email, password);
        }
    }

    private void performSignUp(String name, String email, String password) {
        // Create a new user
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(System.currentTimeMillis());
        
        // Add user to database
        long userId = dbHelper.addUser(user);
        
        if (userId != -1) {
            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
} 