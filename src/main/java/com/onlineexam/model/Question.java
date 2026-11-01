package com.onlineexam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model cho bảng Questions trong cơ sở dữ liệu OnlineExam.
 */
public class Question {
    private int questionId;
    private int createdBy;
    private String questionText;
    private String questionType; // SINGLE, MULTIPLE, TRUE_FALSE, SHORT_ANSWER
    private BigDecimal defaultPoints;
    private String explanation;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;

    // Các trường hỗ trợ hiển thị dữ liệu đầy đủ
    private List<Option> options = new ArrayList<>();
    private String shortAnswer;
    private List<String> linkedExamTitles = new ArrayList<>();

    public Question() {
        this.defaultPoints = new BigDecimal("0.25");
        this.status = "ACTIVE";
    }

    public Question(int questionId, int createdBy, String questionText, String questionType,
                    BigDecimal defaultPoints, String explanation, String status, LocalDateTime createdAt) {
        this.questionId = questionId;
        this.createdBy = createdBy;
        this.questionText = questionText;
        this.questionType = questionType;
        this.defaultPoints = defaultPoints != null ? defaultPoints : new BigDecimal("0.25");
        this.explanation = explanation;
        this.status = status != null ? status : "ACTIVE";
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

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = (options != null) ? options : new ArrayList<>();
    }

    public String getShortAnswer() {
        return shortAnswer;
    }

    public void setShortAnswer(String shortAnswer) {
        this.shortAnswer = shortAnswer;
    }

    public List<String> getLinkedExamTitles() {
        return linkedExamTitles;
    }

    public void setLinkedExamTitles(List<String> linkedExamTitles) {
        this.linkedExamTitles = (linkedExamTitles != null) ? linkedExamTitles : new ArrayList<>();
    }

    /**
     * Tên loại câu hỏi thân thiện bằng tiếng Việt.
     */
    public String getQuestionTypeDisplay() {
        if ("SINGLE".equalsIgnoreCase(questionType)) {
            return "Trắc nghiệm 4 lựa chọn";
        } else if ("TRUE_FALSE".equalsIgnoreCase(questionType)) {
            return "Đúng / Sai (4 ý)";
        } else if ("SHORT_ANSWER".equalsIgnoreCase(questionType)) {
            return "Trả lời ngắn";
        } else if ("MULTIPLE".equalsIgnoreCase(questionType)) {
            return "Nhiều lựa chọn";
        }
        return questionType;
    }
}
