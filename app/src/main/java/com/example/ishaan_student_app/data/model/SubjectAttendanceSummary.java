package com.example.ishaan_student_app.data.model;

import java.util.Locale;

/** UI summary of a student's manually recorded sessions for one personal subject. */
public class SubjectAttendanceSummary {
    private long id;
    private String subjectId;
    private String subjectName;
    private String subjectCode;
    private String department;
    private int year;
    private int totalConductedSessions;
    private int totalAttendedSessions;

    public SubjectAttendanceSummary() {
    }

    public SubjectAttendanceSummary(String subjectId, String subjectName, String subjectCode, String department, int year, int totalConductedSessions, int totalAttendedSessions) {
        this(0, subjectId, subjectName, subjectCode, department, year, totalConductedSessions, totalAttendedSessions);
    }

    public SubjectAttendanceSummary(long id, String subjectId, String subjectName, String subjectCode, String department, int year, int totalConductedSessions, int totalAttendedSessions) {
        this.id = id;
        this.subjectId = subjectId != null ? subjectId.trim() : "";
        this.subjectName = subjectName != null ? subjectName.trim() : "";
        this.subjectCode = subjectCode != null ? subjectCode.trim() : "";
        this.department = department != null ? department.trim().toUpperCase(Locale.ROOT) : "";
        this.year = year >= 1 && year <= 5 ? year : 0;
        this.totalConductedSessions = Math.max(0, totalConductedSessions);
        this.totalAttendedSessions = Math.max(0, Math.min(totalAttendedSessions, this.totalConductedSessions));
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department != null ? department.trim().toUpperCase(Locale.ROOT) : ""; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getTotalConductedSessions() { return totalConductedSessions; }
    public void setTotalConductedSessions(int totalConductedSessions) { this.totalConductedSessions = Math.max(0, totalConductedSessions); }
    public int getTotalAttendedSessions() { return totalAttendedSessions; }
    public void setTotalAttendedSessions(int totalAttendedSessions) { this.totalAttendedSessions = Math.max(0, Math.min(totalAttendedSessions, totalConductedSessions)); }

    public double getAttendancePercentage() {
        if (totalConductedSessions <= 0) return 0.0;
        return totalAttendedSessions * 100.0 / totalConductedSessions;
    }

    public double getDepartmentThreshold() {
        if ("SOL".equals(department)) return 70.0;
        if ("STME".equals(department) || "SOC".equals(department)) return 80.0;
        return 0.0;
    }

    public boolean isBelowDepartmentThreshold() {
        double threshold = getDepartmentThreshold();
        return totalConductedSessions > 0 && threshold > 0 && getAttendancePercentage() < threshold;
    }
}
