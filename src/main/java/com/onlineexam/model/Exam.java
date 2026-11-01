package com.onlineexam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model cho bang Exams trong database OnlineExam.
 */
public class Exam {
    private int examId;
    private int createdBy;
    private String examTitle;
    private String description;
    private int durationMinutes;
    private BigDecimal passScore;
    private String status;
    private LocalDateTime createdAt;

    public Exam() {
    }

    public Exam(int examId, int createdBy, String examTitle, String description,
                int durationMinutes, BigDecimal passScore, String status,
                LocalDateTime createdAt) {
        this.examId = examId;
        this.createdBy = createdBy;
        this.examTitle = examTitle;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.passScore = passScore;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getPassScore() { return passScore; }
    public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }

    /** DRAFT, PUBLISHED hoặc ARCHIVED theo schema SQL. */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
