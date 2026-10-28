package com.onlineexam.model;

/**
 * Model for Question.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Question {
    private int questionId;
    private int createdBy;
    private String questionText;
    private String questionType;
    private BigDecimal defaultPoints;
    private String explanation;
    private String status;
    private LocalDateTime createdAt;

    public Question() {
    }

    public Question(int questionId, int createdBy,
                    String questionText, String questionType,
                    BigDecimal defaultPoints, String explanation,
                    String status, LocalDateTime createdAt) {
        this.questionId = questionId;
        this.createdBy = createdBy;
        this.questionText = questionText;
        this.questionType = questionType;
        this.defaultPoints = defaultPoints;
        this.explanation = explanation;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public BigDecimal getDefaultPoints() {
        return defaultPoints;
    }

    public void setDefaultPoints(BigDecimal defaultPoints) {
        this.defaultPoints = defaultPoints;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}