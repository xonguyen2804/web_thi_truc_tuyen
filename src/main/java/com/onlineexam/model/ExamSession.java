package com.onlineexam.model;

import java.time.LocalDateTime;

public class ExamSession {
    private int sessionId;
    private int examId;
    private int createdBy;
    private Integer classId;
    private Integer groupId;
    private String sessionName;
    private String sessionPassword; // Khớp tên cột session_password trong MySQL
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // SCHEDULED, OPEN, CLOSED, CANCELLED
    private LocalDateTime createdAt;

    // Các thuộc tính phụ trợ hiển thị giao diện
    private String examTitle;
    private int durationMinutes;
    private String targetName; // Hiển thị tên Lớp hoặc Nhóm được phân công

    public ExamSession() {
    }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public String getSessionPassword() { return sessionPassword; }
    public void setSessionPassword(String sessionPassword) { this.sessionPassword = sessionPassword; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
}