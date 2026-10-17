package com.example.ishaan_student_app.data.model;

/**
 * Model class representing a Student's Personal Academic Period / Semester.
 */
public class AcademicPeriod {
    private long id;
    private String ownerAccountId;
    private String academicYear; // e.g. "2026-27"
    private String semesterLabel; // e.g. "Semester 1", "Semester 6"
    private String startDate; // optional ISO yyyy-MM-dd
    private String endDate;   // optional ISO yyyy-MM-dd
    private boolean isActive;
    private boolean isArchived;
    private long createdAt;
    private long updatedAt;

    public AcademicPeriod() {
    }

    public AcademicPeriod(long id, String ownerAccountId, String academicYear, String semesterLabel, String startDate, String endDate, boolean isActive, boolean isArchived, long createdAt, long updatedAt) {
        this.id = id;
        this.ownerAccountId = ownerAccountId != null ? ownerAccountId.trim() : "";
        this.academicYear = academicYear != null ? academicYear.trim() : "";
        this.semesterLabel = semesterLabel != null ? semesterLabel.trim() : "";
        this.startDate = startDate != null ? startDate.trim() : "";
        this.endDate = endDate != null ? endDate.trim() : "";
        this.isActive = isActive;
        this.isArchived = isArchived;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemesterLabel() {
        return semesterLabel;
    }

    public void setSemesterLabel(String semesterLabel) {
        this.semesterLabel = semesterLabel;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
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

    public String getDisplayName() {
        if (!academicYear.isEmpty()) {
            return semesterLabel + " (" + academicYear + ")";
        }
        return semesterLabel;
    }
}
