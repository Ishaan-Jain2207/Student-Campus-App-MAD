package com.example.ishaan_student_app.data.model;

public class StudentEvent {
    private final long id;
    private final String title;
    private final String date;
    private final String venue;
    private final String details;
    private final String imageUri;

    public StudentEvent(long id, String title, String date, String venue, String details, String imageUri) {
        this.id = id;
        this.title = title == null ? "" : title.trim();
        this.date = date == null ? "" : date.trim();
        this.venue = venue == null ? "" : venue.trim();
        this.details = details == null ? "" : details.trim();
        this.imageUri = imageUri == null ? "" : imageUri.trim();
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getVenue() { return venue; }
    public String getDetails() { return details; }
    public String getImageUri() { return imageUri; }
}
