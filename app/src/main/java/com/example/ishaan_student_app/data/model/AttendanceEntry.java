package com.example.ishaan_student_app.data.model;

import java.util.Locale;

/**
 * Model class representing an individual Student Attendance Entry logged for a subject.
 */
public class AttendanceEntry {
    private long id;
    private String ownerAccountId;
    private long subjectId;
    private String classDate; // ISO yyyy-MM-dd
    private String classTime; // optional e.g. "10:00 AM"
    private String occurrenceLabel; // optional e.g. "Lecture 1", "Lab 2"
    private String status; // PRESENT, ABSENT, CANCELLED
    private String note;   // optional private note
    private long createdAt;
    private long updatedAt;

    public AttendanceEntry() {
    }

    public AttendanceEntry(long id, String ownerAccountId, long subjectId, String classDate, String classTime, String occurrenceLabel, String status, String note, long createdAt, long updatedAt) {
        this.id = id;
        this.ownerAccountId = ownerAccountId != null ? ownerAccountId.trim() : "";
        this.subjectId = subjectId;
        this.classDate = classDate != null ? classDate.trim() : "";
        this.classTime = classTime != null ? classTime.trim() : "";
        this.occurrenceLabel = occurrenceLabel != null ? occurrenceLabel.trim() : "";
        this.status = status != null ? status.trim().toUpperCase(Locale.ROOT) : "PRESENT";
        this.note = note != null ? note.trim() : "";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(String ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
        this.subjectId = subjectId;
    }

    public String getClassDate() {
        return classDate;
    }

    public void setClassDate(String classDate) {
        this.classDate = classDate;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    public String getOccurrenceLabel() {
        return occurrenceLabel;
    }

    public void setOccurrenceLabel(String occurrenceLabel) {
        this.occurrenceLabel = occurrenceLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPresent() {
        return "PRESENT".equalsIgnoreCase(status);
    }

    public boolean isAbsent() {
        return "ABSENT".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }
}
