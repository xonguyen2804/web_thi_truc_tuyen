package com.onlineexam.model;

/**
 * Model for SchoolClass.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
public class SchoolClass {
    private int classId;
    private String className;
    private String description;
    private int createdBy;

    public SchoolClass() {
    }

    public SchoolClass(int classId, String className,
                       String description, int createdBy) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.createdBy = createdBy;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}