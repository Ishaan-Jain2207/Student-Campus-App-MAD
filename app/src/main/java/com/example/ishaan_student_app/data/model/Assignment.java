package com.example.ishaan_student_app.data.model;

/**
 * Model class representing an Assignment in CampusMate.
 * Due dates are stored in DD-MM-YYYY format.
 * Priority can be LOW, MEDIUM, or HIGH.
 */
public class Assignment {
    private long id;
    private String title;
    private String subject;
    private String dueDate;
    private boolean isCompleted;
    private String priority; // LOW, MEDIUM, HIGH
    private long academicPeriodId;
    private long createdAt;
    private long updatedAt;

    public Assignment() {
        this.priority = "MEDIUM";
    }

    public Assignment(String title, String subject, String dueDate, boolean isCompleted) {
        this(title, subject, dueDate, isCompleted, "MEDIUM");
    }

    public Assignment(String title, String subject, String dueDate, boolean isCompleted, String priority) {
        long now = System.currentTimeMillis();
        this.id = 0;
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.priority = priority != null && !priority.trim().isEmpty() ? priority.trim().toUpperCase() : "MEDIUM";
        this.academicPeriodId = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Assignment(long id, String title, String subject, String dueDate, boolean isCompleted, long createdAt, long updatedAt) {
        this(id, title, subject, dueDate, isCompleted, "MEDIUM", 0, createdAt, updatedAt);
    }

    public Assignment(long id, String title, String subject, String dueDate, boolean isCompleted, String priority, long createdAt, long updatedAt) {
        this(id, title, subject, dueDate, isCompleted, priority, 0, createdAt, updatedAt);
    }

    public Assignment(long id, String title, String subject, String dueDate, boolean isCompleted, String priority, long academicPeriodId, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.priority = priority != null && !priority.trim().isEmpty() ? priority.trim().toUpperCase() : "MEDIUM";
        this.academicPeriodId = academicPeriodId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getPriority() {
        return priority != null ? priority : "MEDIUM";
    }

    public void setPriority(String priority) {
        this.priority = priority != null ? priority.trim().toUpperCase() : "MEDIUM";
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getAcademicPeriodId() { return academicPeriodId; }

    public void setAcademicPeriodId(long academicPeriodId) { this.academicPeriodId = academicPeriodId; }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
