package com.onlineexam.model;

import java.math.BigDecimal;

/**
 * Model cho bang Attempt_Answers.
 * Lua chon duoc chon luu trong bang trung gian Attempt_Answer_Options.
 */
public class AttemptAnswer {
    private int answerId;
    private int attemptId;
    private int questionId;
    private String answerText;
    private Boolean isCorrect;
    private BigDecimal pointsAwarded;
    private String feedback;

    public AttemptAnswer() {
    }

    public AttemptAnswer(int answerId, int attemptId, int questionId, String answerText,
                         Boolean isCorrect, BigDecimal pointsAwarded, String feedback) {
        this.answerId = answerId;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
        this.pointsAwarded = pointsAwarded;
        this.feedback = feedback;
    }

    public int getAnswerId() { return answerId; }
    public void setAnswerId(int answerId) { this.answerId = answerId; }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public BigDecimal getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(BigDecimal pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
