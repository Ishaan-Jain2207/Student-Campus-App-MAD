package com.example.ishaan_student_app.data.model;

/**
 * Model class representing a Campus Resource loaded from runtime TXT/CSV files.
 */
public class CampusResource {
    private final String title;
    private final String category;
    private final String description;
    private final long id;
    private final String uri;
    private final String mimeType;
    private final boolean userAdded;

    public CampusResource(String title, String category, String description) {
        this(title, category, description, -1, "", "", false);
    }

    public CampusResource(String title, String category, String description, long id, String uri, String mimeType) {
        this(title, category, description, id, uri, mimeType, true);
    }

    private CampusResource(String title, String category, String description, long id, String uri, String mimeType, boolean userAdded) {
        this.title = title != null ? title.trim() : "";
        this.category = category != null ? category.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.id = id;
        this.uri = uri == null ? "" : uri;
        this.mimeType = mimeType == null ? "" : mimeType;
        this.userAdded = userAdded;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getId() { return id; }
    public String getUri() { return uri; }
    public String getMimeType() { return mimeType; }
    public boolean isUserAdded() { return userAdded; }
}
