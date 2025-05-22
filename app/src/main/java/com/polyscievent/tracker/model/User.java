package com.polyscievent.tracker.model;

/**
 * Data model class for a user
 */
public class User {
    private long id;
    private String email;
    private String fullName;
    private String password; // In a real app, you'd never store plain text passwords
    private long createdAt;
    
    // Empty constructor required for creating a new instance
    public User() { }
    
    // Constructor for creating a user with all fields except id
    public User(String email, String fullName, String password, long createdAt) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 