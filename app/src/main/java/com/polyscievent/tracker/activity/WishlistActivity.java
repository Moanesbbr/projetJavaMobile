package com.polyscievent.tracker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polyscievent.tracker.R;
import com.polyscievent.tracker.adapter.EventAdapter;
import com.polyscievent.tracker.data.DatabaseHelper;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.model.User;
import com.polyscievent.tracker.util.UserSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishlistActivity extends AppCompatActivity implements EventAdapter.EventItemClickListener {
    
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private TextView mEmptyTextView;
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    private User mCurrentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        
        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.wishlist);
        }
        
        // Get current user
        UserSession userSession = new UserSession(this);
        mCurrentUser = userSession.getUserDetails();
        
        // Initialize threading components
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        
        // Set up views
        mRecyclerView = findViewById(R.id.recycler_view_wishlist);
        mEmptyTextView = findViewById(R.id.text_view_empty_wishlist);
        
        // Set up RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new EventAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        
        // Load wishlisted events
        loadWishlistedEvents();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void loadWishlistedEvents() {
        if (mCurrentUser == null) return;
        
        mExecutorService.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
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
            });
        });
    }
    
    @Override
    public void onEventItemClick(Event event) {
        // Handle event click
    }
    
    @Override
    public void onEventDeleteClick(Event event) {
        // Not used in wishlist view
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null) {
            mExecutorService.shutdown();
        }
    }
} 