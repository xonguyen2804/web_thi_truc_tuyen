package com.onlineexam.model;

/**
 * Model for Group.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
public class Group {
    private int groupId;
    private int classId;
    private String groupName;
    private String description;

    public Group() {
    }

    public Group(int groupId, int classId,
                 String groupName, String description) {
        this.groupId = groupId;
        this.classId = classId;
        this.groupName = groupName;
        this.description = description;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}