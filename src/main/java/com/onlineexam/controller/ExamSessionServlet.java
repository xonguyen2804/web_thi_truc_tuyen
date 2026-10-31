package com.onlineexam.controller;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.ExamSessionDAO;
import com.onlineexam.dao.GroupDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.Group;
import com.onlineexam.model.SchoolClass;
import com.onlineexam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/teacher/exam-sessions")
public class ExamSessionServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final GroupDAO groupDAO = new GroupDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<ExamSession> sessions = sessionDAO.findAllForTeacher();
            List<Exam> exams = examDAO.findAll();
            List<SchoolClass> classes = classDAO.findAll();
            List<Group> groups = groupDAO.findAll();

            request.setAttribute("sessions", sessions);
            request.setAttribute("exams", exams);
            request.setAttribute("classes", classes);
            request.setAttribute("groups", groups);

            request.getRequestDispatcher("/WEB-INF/views/teacher/exam-sessions.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp dữ liệu ca thi: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // Giáo viên đang đăng nhập (hoặc mặc định id=1)
        User currentUser = (User) request.getSession().getAttribute("user");
        int teacherId = (currentUser != null) ? currentUser.getUserId() : 1;

        try {
            // 1. Thao tác Mở/Đóng phòng thi nhanh
            if ("toggle_status".equalsIgnoreCase(action)) {
                int sessionId = Integer.parseInt(request.getParameter("sessionId"));
                String targetStatus = request.getParameter("targetStatus"); // OPEN hoặc CLOSED
                sessionDAO.updateStatus(sessionId, targetStatus);
                response.sendRedirect(request.getContextPath() + "/teacher/exam-sessions?msg=status_updated");
                return;
            }

            // 2. Tạo ca thi mới
            int examId = Integer.parseInt(request.getParameter("examId"));
            String sessionName = request.getParameter("sessionName");
            String sessionPassword = request.getParameter("sessionPassword");
            String startStr = request.getParameter("startTime");
            String endStr = request.getParameter("endTime");
            String targetType = request.getParameter("targetType"); // CLASS hoặc GROUP
            String targetIdStr = request.getParameter("targetId");
            String initialStatus = request.getParameter("status"); // SCHEDULED hoặc OPEN

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime start = LocalDateTime.parse(startStr, formatter);
            LocalDateTime end = LocalDateTime.parse(endStr, formatter);

            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
            }

            ExamSession session = new ExamSession();
            session.setExamId(examId);
            session.setCreatedBy(teacherId);
            session.setSessionName(sessionName);
            session.setSessionPassword(sessionPassword != null && !sessionPassword.isBlank() ? sessionPassword.trim() : null);
            session.setStartTime(start);
            session.setEndTime(end);
            session.setStatus(initialStatus != null ? initialStatus : "SCHEDULED");

            if ("GROUP".equalsIgnoreCase(targetType) && targetIdStr != null && !targetIdStr.isBlank()) {
                session.setGroupId(Integer.parseInt(targetIdStr));
                session.setClassId(null);
            } else if (targetIdStr != null && !targetIdStr.isBlank()) {
                session.setClassId(Integer.parseInt(targetIdStr));
                session.setGroupId(null);
            } else {
                session.setClassId(2); // Mặc định lớp Khối 12 có sẵn trong DB
                session.setGroupId(null);
            }

            sessionDAO.insert(session);
            response.sendRedirect(request.getContextPath() + "/teacher/exam-sessions?msg=created_success");

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Lỗi tạo ca thi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/teacher/exam-sessions?error=true");
        }
    }
}