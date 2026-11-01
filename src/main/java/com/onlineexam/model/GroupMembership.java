package com.onlineexam.model;

/**
 * Model for GroupMembership.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
import java.time.LocalDateTime;

public class GroupMembership {
    private int groupId;
    private int userId;
    private LocalDateTime joinedAt;

    public GroupMembership() {
    }

    public GroupMembership(int groupId, int userId,
                           LocalDateTime joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
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