package com.onlineexam.service;

import com.onlineexam.dao.AttemptDAO;
import com.onlineexam.dao.AttemptAnswerDAO;
import com.onlineexam.model.Attempt;
import com.onlineexam.model.AttemptAnswer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp xử lý nghiệp vụ cho Lượt làm bài (Attempt).
 */
public class AttemptService {

    // 1. Thuộc tính DAO và Service chấm điểm
    private AttemptDAO attemptDAO;
    private AttemptAnswerDAO attemptAnswerDAO;
    private GradingService gradingService;

    // 2. Constructors
    public AttemptService() {
        this.attemptDAO = new AttemptDAO();
        this.attemptAnswerDAO = new AttemptAnswerDAO();
        this.gradingService = new GradingService();
    }

    public AttemptService(AttemptDAO attemptDAO, AttemptAnswerDAO attemptAnswerDAO, GradingService gradingService) {
        this.attemptDAO = attemptDAO;
        this.attemptAnswerDAO = attemptAnswerDAO;
        this.gradingService = gradingService;
    }

    // 3. Getter và Setter
    public AttemptDAO getAttemptDAO() {
        return attemptDAO;
    }

    public void setAttemptDAO(AttemptDAO attemptDAO) {
        this.attemptDAO = attemptDAO;
    }

    public AttemptAnswerDAO getAttemptAnswerDAO() {
        return attemptAnswerDAO;
    }

    public void setAttemptAnswerDAO(AttemptAnswerDAO attemptAnswerDAO) {
        this.attemptAnswerDAO = attemptAnswerDAO;
    }

    public GradingService getGradingService() {
        return gradingService;
    }

    public void setGradingService(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    // 4. Các phương thức nghiệp vụ

    /**
     * Bắt đầu một lượt làm bài mới cho sinh viên trong phiên thi.
     */
    public Attempt startAttempt(int sessionId, int userId) {
        // 1. Xác định số lần thi hiện tại (attempt_number)
        // int nextAttemptNumber = attemptDAO.countUserAttempts(sessionId, userId) + 1;
        int nextAttemptNumber = 1;

        Attempt attempt = new Attempt();
        attempt.setSessionId(sessionId);
        attempt.setUserId(userId);
        attempt.setAttemptNumber(nextAttemptNumber);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus("IN_PROGRESS");

        // attemptDAO.insert(attempt);
        return attempt;
    }

    /**
     * Lưu câu trả lời của từng câu hỏi trong quá trình làm bài.
     */
    public void saveAnswer(AttemptAnswer answer) {
        if (answer == null || answer.getAttemptId() <= 0 || answer.getQuestionId() <= 0) {
            throw new IllegalArgumentException("Dữ liệu câu trả lời không hợp lệ");
        }
        // attemptAnswerDAO.saveOrUpdate(answer);
    }

    /**
     * Nộp bài thi:
     * - Cập nhật submittedAt = now()
     * - Đổi status = SUBMITTED
     * - Tự động gọi GradingService để chấm điểm trắc nghiệm ngay lập tức.
     */
    public boolean submitAttempt(int attemptId, List<AttemptAnswer> answers) {
        // 1. Lưu toàn bộ câu trả lời cuối cùng
        if (answers != null) {
            for (AttemptAnswer ans : answers) {
                saveAnswer(ans);
            }
        }

        // 2. Chấm điểm tự động qua GradingService
        gradingService.gradeAttempt(attemptId);
        return true;
    }
}