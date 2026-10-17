package com.example.ishaan_student_app.data.model;

public class UserResource {
    private final long id;
    private final String title;
    private final String mimeType;
    private final String uri;
    private final String subjectName;

    public UserResource(long id, String title, String mimeType, String uri) {
        this(id, title, mimeType, uri, "General");
    }

    public UserResource(long id, String title, String mimeType, String uri, String subjectName) {
        this.id = id;
        this.title = title == null ? "" : title.trim();
        this.mimeType = mimeType == null ? "" : mimeType.trim();
        this.uri = uri == null ? "" : uri.trim();
        this.subjectName = subjectName == null || subjectName.trim().isEmpty() ? "General" : subjectName.trim();
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getMimeType() { return mimeType; }
    public String getUri() { return uri; }
    public String getSubjectName() { return subjectName; }
}
