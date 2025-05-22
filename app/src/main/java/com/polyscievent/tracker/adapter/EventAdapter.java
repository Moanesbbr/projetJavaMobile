package com.polyscievent.tracker.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.polyscievent.tracker.R;
import com.polyscievent.tracker.activity.AddEditEventActivity;
import com.polyscievent.tracker.activity.MainActivity;
import com.polyscievent.tracker.model.Event;
import com.polyscievent.tracker.util.Constants;
import com.polyscievent.tracker.util.DateUtils;
import com.polyscievent.tracker.util.UserSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying scientific events in a RecyclerView
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    
    private final Context mContext;
    private List<Event> mEvents;
    private final EventItemClickListener mListener;
    private final long mCurrentUserId;
    
    /**
     * Interface for handling event item clicks
     */
    public interface EventItemClickListener {
        void onEventItemClick(Event event);
        void onEventDeleteClick(Event event);
    }
    
    /**
     * Constructor
     * @param context The context
     * @param listener Click listener for event items
     */
    public EventAdapter(Context context, EventItemClickListener listener) {
        mContext = context;
        mEvents = new ArrayList<>();
        mListener = listener;
        // Get current user ID from UserSession
        UserSession userSession = new UserSession(context);
        mCurrentUserId = userSession.getUserDetails().getId();
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        return new EventViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mEvents.get(position);
        
        // Bind event data to the ViewHolder
        holder.nameTextView.setText(event.getName());
        holder.locationTextView.setText(event.getLocation());
        holder.dateTextView.setText(DateUtils.formatDate(event.getEventDate()));
        holder.organizerTextView.setText(event.getOrganizer());
        holder.themeTextView.setText(event.getTheme());
        holder.deadlineTextView.setText(DateUtils.formatDate(event.getSubmissionDeadline()));
        
        // Set the deadline status text
        holder.daysUntilDeadlineTextView.setText(
                DateUtils.getDeadlineText(mContext, event.getSubmissionDeadline()));
        
        // Set the deadline indicator color based on the deadline status
        int indicatorColor;
        if (DateUtils.isDeadlinePassed(event.getSubmissionDeadline())) {
            indicatorColor = R.color.deadlinePassed;
        } else if (DateUtils.isDeadlineUrgent(event.getSubmissionDeadline())) {
            indicatorColor = R.color.deadlineImmediate;
        } else if (DateUtils.isDeadlineApproaching(event.getSubmissionDeadline())) {
            indicatorColor = R.color.deadlineApproaching;
        } else {
            indicatorColor = R.color.deadlineNormal;
        }
        holder.deadlineIndicatorView.setBackgroundColor(
                ContextCompat.getColor(mContext, indicatorColor));
        
        // Handle item click to view/edit the event
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onEventItemClick(event);
            }
        });
        
        // Only show edit and delete buttons for events owned by the current user
        boolean isCurrentUserEvent = event.getUserId() == mCurrentUserId;
        holder.editButton.setVisibility(isCurrentUserEvent ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(isCurrentUserEvent ? View.VISIBLE : View.GONE);
        
        // Handle edit button click
        holder.editButton.setOnClickListener(v -> {
            if (isCurrentUserEvent) {
                Intent intent = new Intent(mContext, AddEditEventActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
                intent.putExtra(Constants.EXTRA_USER_ID, event.getUserId());
                ((MainActivity) mContext).startActivityForResult(intent, Constants.REQUEST_EDIT_EVENT);
            }
        });
        
        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            if (isCurrentUserEvent && mListener != null) {
                mListener.onEventDeleteClick(event);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return mEvents.size();
    }
    
    /**
     * Update the events list and refresh the adapter
     * @param events New list of events
     */
    public void setEvents(List<Event> events) {
        mEvents = events;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder class for event items
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView locationTextView;
        final TextView dateTextView;
        final TextView organizerTextView;
        final TextView themeTextView;
        final TextView deadlineTextView;
        final TextView daysUntilDeadlineTextView;
        final View deadlineIndicatorView;
        final Button editButton;
        final Button deleteButton;
        
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_event_name);
            locationTextView = itemView.findViewById(R.id.text_view_location);
            dateTextView = itemView.findViewById(R.id.text_view_date);
            organizerTextView = itemView.findViewById(R.id.text_view_organizer);
            themeTextView = itemView.findViewById(R.id.text_view_theme);
            deadlineTextView = itemView.findViewById(R.id.text_view_deadline);
            daysUntilDeadlineTextView = itemView.findViewById(R.id.text_view_days_until_deadline);
            deadlineIndicatorView = itemView.findViewById(R.id.deadline_indicator);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
} 