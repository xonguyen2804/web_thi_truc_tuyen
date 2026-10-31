package com.onlineexam.model;

/**
 * Model cho bảng Expected_Answers trong cơ sở dữ liệu OnlineExam.
 */
public class ExpectedAnswer {
    private int expectedAnswerId;
    private int questionId;
    private String answerText;

    public ExpectedAnswer() {
    }

    public ExpectedAnswer(int expectedAnswerId, int questionId, String answerText) {
        this.expectedAnswerId = expectedAnswerId;
        this.questionId = questionId;
        this.answerText = answerText;
    }

    public ExpectedAnswer(int questionId, String answerText) {
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
