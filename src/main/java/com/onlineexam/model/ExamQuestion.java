package com.onlineexam.model;

import java.math.BigDecimal;

/**
 * Model cho bang Exam_Questions.
 * Khoa chinh ghep: examId + questionId.
 */
public class ExamQuestion {
    private int examId;
    private int questionId;
    private BigDecimal points;
    private int displayOrder;

    public ExamQuestion() {
    }

    public ExamQuestion(int examId, int questionId, BigDecimal points, int displayOrder) {
        this.examId = examId;
        this.questionId = questionId;
        this.points = points;
        this.displayOrder = displayOrder;
    }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public BigDecimal getPoints() { return points; }
    public void setPoints(BigDecimal points) { this.points = points; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
