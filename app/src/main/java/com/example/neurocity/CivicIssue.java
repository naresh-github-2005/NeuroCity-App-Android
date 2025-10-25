package com.example.neurocity;

public class CivicIssue {
    private String image_url;
    private String issue_type;
    private double latitude;
    private double longitude;
    private String timestamp;
    private String user_id;

    // Empty constructor required for Firestore
    public CivicIssue() {
    }

    // Full constructor
    public CivicIssue(String image_url, String issue_type, double latitude, double longitude, String timestamp, String user_id) {
        this.image_url = image_url;
        this.issue_type = issue_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.user_id = user_id;
    }

    // Getters
    public String getImage_url() {
        return image_url;
    }

    public String getIssue_type() {
        return issue_type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUser_id() {
        return user_id;
    }
}
