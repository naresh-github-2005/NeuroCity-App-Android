package com.example.neurocity;

public class CivicIssue {
    private String image_url;
    private String issue_type;
    private double latitude;
    private double longitude;
    private String timestamp;
    private String user_id;
    private String description;
    private String status;
    private String docId;


    // Empty constructor required for Firestore
    public CivicIssue() {
    }

    // Full constructor including description
    public CivicIssue(String image_url, String issue_type, double latitude, double longitude,
                      String timestamp, String user_id, String description, String status) {
        this.image_url = image_url;
        this.issue_type = issue_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.description = description;
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public String getStatus() { return status; }

    public String getDocId() {
        return docId;
    }

    // Setters
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setIssue_type(String issue_type) {
        this.issue_type = issue_type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) { this.status = status; }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
