package com.onlineexam.model;

/**
 * Model for Option.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
public class Option {
    private int optionId;
    private int questionId;
    private String optionText;
    private boolean isCorrect;
    private int displayOrder;

    public Option() {
    }

    public Option(int optionId, int questionId,
                  String optionText, boolean isCorrect,
                  int displayOrder) {
        this.optionId = optionId;
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.displayOrder = displayOrder;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}