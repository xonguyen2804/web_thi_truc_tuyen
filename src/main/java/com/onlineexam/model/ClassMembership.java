package com.onlineexam.model;

/**
 * Model for ClassMembership.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */

import java.time.LocalDateTime;

public class ClassMembership {
    private int classId;
    private int userId;
    private LocalDateTime joinedAt;

    public ClassMembership() {
    }

    public ClassMembership(int classId, int userId,
                           LocalDateTime joinedAt) {
        this.classId = classId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
