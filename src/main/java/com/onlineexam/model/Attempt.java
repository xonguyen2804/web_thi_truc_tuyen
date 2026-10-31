package com.onlineexam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model cho bang Attempts.
 */
public class Attempt {
    private int attemptId;
    private int sessionId;
    private int userId;
    private int attemptNumber;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private String status;
    private BigDecimal score;
    private Integer gradedBy;
    private LocalDateTime gradedAt;

    public Attempt() {
    }

    public Attempt(int attemptId, int sessionId, int userId, int attemptNumber,
                   LocalDateTime startedAt, LocalDateTime submittedAt, String status,
                   BigDecimal score, Integer gradedBy, LocalDateTime gradedAt) {
        this.attemptId = attemptId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.attemptNumber = attemptNumber;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
        this.status = status;
        this.score = score;
        this.gradedBy = gradedBy;
        this.gradedAt = gradedAt;
    }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    /** IN_PROGRESS, SUBMITTED hoac GRADED theo schema SQL. */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getGradedBy() { return gradedBy; }
    public void setGradedBy(Integer gradedBy) { this.gradedBy = gradedBy; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
