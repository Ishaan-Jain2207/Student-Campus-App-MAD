package com.example.ishaan_student_app.data.model;

import java.util.Locale;

/**
 * Model class representing an authenticated SVKM's NMIMS Chandigarh Student profile.
 */
public class Student {
    private long id;
    private String sapId;
    private String name;
    private String email;
    private String department; // STME, SOC, SOL
    private int year;           // 1 to 5
    private String photoUri;

    public Student() {
    }

    public Student(long id, String sapId, String name, String email, String department, int year, String photoUri) {
        this.id = id;
        this.sapId = sapId != null ? sapId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.department = department != null ? department.trim().toUpperCase(Locale.ROOT) : "";
        this.year = year >= 1 && year <= 5 ? year : 0;
        this.photoUri = photoUri != null ? photoUri.trim() : "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSapId() {
        return sapId;
    }

    public void setSapId(String sapId) {
        this.sapId = sapId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim().toUpperCase(Locale.ROOT) : "";
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
