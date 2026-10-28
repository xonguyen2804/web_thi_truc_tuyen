package com.onlineexam.model;

/**
 * Model for ExpectedAnswer.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
public class ExpectedAnswer {
    private int expectedAnswerId;
    private int questionId;
    private String answerText;

    public ExpectedAnswer() {
    }

    public ExpectedAnswer(int expectedAnswerId, int questionId,
                          String answerText) {
        this.expectedAnswerId = expectedAnswerId;
        this.questionId = questionId;
        this.answerText = answerText;
    }

    public int getExpectedAnswerId() {
        return expectedAnswerId;
    }

    public void setExpectedAnswerId(int expectedAnswerId) {
        this.expectedAnswerId = expectedAnswerId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}