package com.example.neurocity;

public class CivicIssue {

    private String resolved_image_url;
    private String image_url;
    private String issue_type;
    private double latitude;
    private double longitude;
    private String timestamp;
    private String user_id;
    private String description;
    private String status;
    private String docId;

    // 🔹 New fields for department assignment
    private String department;
    private String assigned_worker_id;
    private String assigned_worker_name;

    // Empty constructor required for Firestore
    public CivicIssue() {
    }

    // Full constructor including description and new fields
    public CivicIssue(String image_url, String issue_type, double latitude, double longitude,
                      String timestamp, String user_id, String description, String status,
                      String department, String assigned_worker_id, String assigned_worker_name, String resolved_image_url) {
        this.image_url = image_url;
        this.issue_type = issue_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.description = description;
        this.status = status;
        this.department = department;
        this.assigned_worker_id = assigned_worker_id;
        this.assigned_worker_name = assigned_worker_name;
        this.resolved_image_url = resolved_image_url;
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

    public String getStatus() {
        return status;
    }

    public String getDocId() {
        return docId;
    }

    public String getDepartment() {
        return department;
    }

    public String getAssigned_worker_id() {
        return assigned_worker_id;
    }

    public String getAssigned_worker_name() {
        return assigned_worker_name;
    }

    public String getResolved_image_url() {
        return resolved_image_url;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setAssigned_worker_id(String assigned_worker_id) {
        this.assigned_worker_id = assigned_worker_id;
    }

    public void setAssigned_worker_name(String assigned_worker_name) {
        this.assigned_worker_name = assigned_worker_name;
    }

    public void setResolved_image_url(String resolvedImageUrl) {
        this.resolved_image_url = resolvedImageUrl;
    }
}
