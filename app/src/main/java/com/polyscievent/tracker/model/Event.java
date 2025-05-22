package com.polyscievent.tracker.model;

/**
 * POJO class for Scientific Event data
 */
public class Event {
    
    private long id;
    private String name;
    private String location;
    private long eventDate;
    private String organizer;
    private String theme;
    private long submissionDeadline;
    private long userId; // ID of the user who created this event
    private String imageUri; // URI string for the event image
    
    /**
     * Default constructor
     */
    public Event() {
    }
    
    /**
     * Constructor with all fields except ID (for creating new events)
     */
    public Event(String name, String location, long eventDate, String organizer, 
                String theme, long submissionDeadline, long userId, String imageUri) {
        this.name = name;
        this.location = location;
        this.eventDate = eventDate;
        this.organizer = organizer;
        this.theme = theme;
        this.submissionDeadline = submissionDeadline;
        this.userId = userId;
        this.imageUri = imageUri != null ? imageUri : ""; // Handle null image URI
    }
    
    /**
     * Constructor with all fields including ID (for retrieving from database)
     */
    public Event(long id, String name, String location, long eventDate, String organizer, 
                String theme, long submissionDeadline, long userId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.eventDate = eventDate;
        this.organizer = organizer;
        this.theme = theme;
        this.submissionDeadline = submissionDeadline;
        this.userId = userId;
        this.imageUri = ""; // Default empty image URI
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public long getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getOrganizer() {
        return organizer;
    }
    
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public long getSubmissionDeadline() {
        return submissionDeadline;
    }
    
    public void setSubmissionDeadline(long submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getImageUri() {
        return imageUri;
    }
    
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", eventDate=" + eventDate +
                ", organizer='" + organizer + '\'' +
                ", theme='" + theme + '\'' +
                ", submissionDeadline=" + submissionDeadline +
                ", userId=" + userId +
                ", imageUri='" + imageUri + '\'' +
                '}';
    }
} 