package com.onlineexam.service;

import com.onlineexam.dao.ExamSessionDAO;
import com.onlineexam.model.ExamSession;

import java.time.LocalDateTime;

/**
 * Lớp xử lý nghiệp vụ cho Phiên thi (ExamSession).
 */
public class ExamSessionService {

    // 1. Thuộc tính DAO
    private ExamSessionDAO examSessionDAO;

    // 2. Constructors
    public ExamSessionService() {
        this.examSessionDAO = new ExamSessionDAO();
    }

    public ExamSessionService(ExamSessionDAO examSessionDAO) {
        this.examSessionDAO = examSessionDAO;
    }

    // 3. Getter và Setter
    public ExamSessionDAO getExamSessionDAO() {
        return examSessionDAO;
    }

    public void setExamSessionDAO(ExamSessionDAO examSessionDAO) {
        this.examSessionDAO = examSessionDAO;
    }

    // 4. Các phương thức nghiệp vụ

    /**
     * Tạo một phiên thi mới và kiểm tra các ràng buộc:
     * - endTime > startTime
     * - Chỉ được gán cho Lớp HOẶC Nhóm (không đồng thời cả hai, không để trống cả hai).
     */
    public boolean scheduleSession(ExamSession session) {
        validateSession(session);
        if (session.getStatus() == null || session.getStatus().isBlank()) {
            session.setStatus("SCHEDULED");
        }
        // return examSessionDAO.insert(session);
        return true;
    }

    /**
     * Kiểm tra xem ca thi hiện tại có đang mở để sinh viên vào làm bài không.
     */
    public boolean isSessionOpen(ExamSession session) {
        if (session == null) return false;
        LocalDateTime now = LocalDateTime.now();
        boolean withinTime = now.isAfter(session.getStartTime()) && now.isBefore(session.getEndTime());
        boolean isOpenStatus = "OPEN".equalsIgnoreCase(session.getStatus()) || "SCHEDULED".equalsIgnoreCase(session.getStatus());
        return withinTime && isOpenStatus;
    }

    /**
     * Hủy ca thi (chuyển status thành CANCELLED).
     */
    public boolean cancelSession(int sessionId) {
        // ExamSession session = examSessionDAO.findById(sessionId);
        // if (session != null) {
        //     session.setStatus("CANCELLED");
        //     return examSessionDAO.update(session);
        // }
        return false;
    }

    /**
     * Xác thực các quy tắc nghiệp vụ theo schema:
     * CHECK (end_time > start_time)
     * CHECK ((class_id IS NOT NULL AND group_id IS NULL) OR (class_id IS NULL AND group_id IS NOT NULL))
     */
    private void validateSession(ExamSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Thông tin ca thi không được null");
        }
        if (session.getSessionName() == null || session.getSessionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phiên thi không được để trống");
        }
        if (session.getStartTime() == null || session.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        boolean hasClass = session.getClassId() != null && session.getClassId() > 0;
        boolean hasGroup = session.getGroupId() != null && session.getGroupId() > 0;

        if ((hasClass && hasGroup) || (!hasClass && !hasGroup)) {
            throw new IllegalArgumentException("Ca thi chỉ được gán cho đúng một đối tượng: Lớp học HOẶC Nhóm học.");
        }
    }
}