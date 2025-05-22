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
import com.polyscievent.tracker.util.UserSession;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonSignIn;
    private TextView textViewSignUp;
    private TextView textViewForgotPassword;
    
    private UserSession userSession;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize database and session manager
        dbHelper = DatabaseHelper.getInstance(this);
        userSession = new UserSession(this);
        
        // Check if user is already logged in
        if (userSession.isLoggedIn()) {
            startMainActivity();
            return;
        }

        // Initialize views
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonSignIn = findViewById(R.id.button_sign_in);
        textViewSignUp = findViewById(R.id.text_view_sign_up);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);

        // Set up listeners
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUpActivity();
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement forgot password functionality
                Toast.makeText(SignInActivity.this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        // Reset errors
        editTextEmail.setError(null);
        editTextPassword.setError(null);

        // Get values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check for valid input
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.empty_fields));
            focusView = editTextPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.empty_fields));
            focusView = editTextEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Perform the sign in
            performSignIn(email, password);
        }
    }

    private void performSignIn(String email, String password) {
        // Authenticate user with database
        User user = dbHelper.authenticateUser(email, password);
        
        if (user != null) {
            // Create session
            userSession.createLoginSession(user);
            startMainActivity();
        } else {
            Toast.makeText(this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
} 