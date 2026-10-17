package com.example.ishaan_student_app.data.model;

/**
 * Model class representing a Student's Personal Subject in a selected semester.
 */
public class PersonalSubject {
    private long id;
    private String ownerAccountId;
    private long academicPeriodId;
    private String subjectName;
    private String subjectCode;
    private boolean isArchived;
    private long createdAt;
    private long updatedAt;

    public PersonalSubject() {
    }

    public PersonalSubject(long id, String ownerAccountId, long academicPeriodId, String subjectName, String subjectCode, boolean isArchived, long createdAt, long updatedAt) {
        this.id = id;
        this.ownerAccountId = ownerAccountId != null ? ownerAccountId.trim() : "";
        this.academicPeriodId = academicPeriodId;
        this.subjectName = subjectName != null ? subjectName.trim() : "";
        this.subjectCode = subjectCode != null ? subjectCode.trim() : "";
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

    public long getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(long academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
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
        if (subjectCode != null && !subjectCode.isEmpty()) {
            return subjectName + " (" + subjectCode + ")";
        }
        return subjectName;
    }
}
