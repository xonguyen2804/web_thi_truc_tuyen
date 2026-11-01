package com.onlineexam.service;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.ExamQuestionDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.ExamQuestion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lớp xử lý nghiệp vụ cho Đề thi (Exam).
 */
public class ExamService {

    // 1. Thuộc tính DAO
    private ExamDAO examDAO;
    private ExamQuestionDAO examQuestionDAO;

    // 2. Constructors
    public ExamService() {
        this.examDAO = new ExamDAO();
        this.examQuestionDAO = new ExamQuestionDAO();
    }

    public ExamService(ExamDAO examDAO, ExamQuestionDAO examQuestionDAO) {
        this.examDAO = examDAO;
        this.examQuestionDAO = examQuestionDAO;
    }

    // 3. Getter và Setter
    public ExamDAO getExamDAO() {
        return examDAO;
    }

    public void setExamDAO(ExamDAO examDAO) {
        this.examDAO = examDAO;
    }

    public ExamQuestionDAO getExamQuestionDAO() {
        return examQuestionDAO;
    }

    public void setExamQuestionDAO(ExamQuestionDAO examQuestionDAO) {
        this.examQuestionDAO = examQuestionDAO;
    }

    // 4. Các phương thức nghiệp vụ

    /**
     * Xác thực và tạo mới đề thi.
     * Ràng buộc: durationMinutes > 0, passScore >= 0, title không được rỗng.
     */
    public boolean createExam(Exam exam) {
        validateExam(exam);
        if (exam.getStatus() == null || exam.getStatus().isBlank()) {
            exam.setStatus("DRAFT");
        }
        // Gọi DAO lưu đề thi (khi bạn viết ExamDAO)
        // return examDAO.insert(exam);
        return true;
    }

    /**
     * Chuyển trạng thái đề thi sang PUBLISHED để có thể gán vào ca thi.
     */
    public boolean publishExam(int examId) {
        // Exam exam = examDAO.findById(examId);
        // if (exam == null) {
        //     throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        // }
        // exam.setStatus("PUBLISHED");
        // return examDAO.update(exam);
        return true;
    }

    /**
     * Thêm câu hỏi vào đề thi với số điểm và thứ tự hiển thị.
     */
    public boolean addQuestionToExam(int examId, int questionId, BigDecimal points, int displayOrder) {
        if (points == null || points.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Điểm câu hỏi phải >= 0");
        }
        if (displayOrder <= 0) {
            throw new IllegalArgumentException("Thứ tự hiển thị câu hỏi phải > 0");
        }

        ExamQuestion eq = new ExamQuestion();
        eq.setExamId(examId);
        eq.setQuestionId(questionId);
        eq.setPoints(points);
        eq.setDisplayOrder(displayOrder);

        // return examQuestionDAO.insert(eq);
        return true;
    }

    /**
     * Kiểm tra hợp lệ dữ liệu đầu vào theo ràng buộc schema SQL.
     */
    private void validateExam(Exam exam) {
        if (exam == null) {
            throw new IllegalArgumentException("Dữ liệu đề thi không được null");
        }
        if (exam.getExamTitle() == null || exam.getExamTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề đề thi không được để trống");
        }
        if (exam.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Thời gian thi phải lớn hơn 0 phút (CHECK duration_minutes > 0)");
        }
        if (exam.getPassScore() == null || exam.getPassScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Điểm chuẩn đạt phải >= 0 (CHECK pass_score >= 0)");
        }
    }
}