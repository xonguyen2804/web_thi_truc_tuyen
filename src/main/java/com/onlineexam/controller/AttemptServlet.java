package com.onlineexam.controller;

import com.onlineexam.dao.AttemptDAO;
import com.onlineexam.dao.ExamSessionDAO;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.User;
import com.onlineexam.service.GradingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet({ "/student/exams", "/student/check-pass", "/student/take-exam", "/student/submit-exam" })
public class AttemptServlet extends HttpServlet {

    private final AttemptDAO attemptDAO = new AttemptDAO();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAO();
    private final GradingService gradingService = new GradingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Kiểm tra quyền STUDENT
        String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "";
        if (!"STUDENT".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này");
            return;
        }

        String servletPath = request.getServletPath();
        if ("/student/take-exam".equals(servletPath)) {
            handleTakeExamPage(request, response);
        } else {
            handleAvailableExamsPage(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String servletPath = request.getServletPath();

        if ("/student/check-pass".equals(servletPath)) {
            handleVerifyPassAndStart(request, response);
        } else if ("/student/submit-exam".equals(servletPath)) {
            handleSubmitExam(request, response);
        }
    }

    // 1. Hiển thị danh sách ca thi khả dụng
    private void handleAvailableExamsPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<AttemptDAO.SessionViewDTO> sessions = attemptDAO.findAvailableSessions();
            request.setAttribute("sessions", sessions);
            request.getRequestDispatcher("/WEB-INF/views/student/available-exams.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi lấy danh sách ca thi: " + e.getMessage());
        }
    }

    // 2. Xác thực mật khẩu phòng thi và tạo/lấy lượt làm bài (Attempt)
    private void handleVerifyPassAndStart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession sessionHttp = request.getSession(false);
        User user = (sessionHttp != null) ? (User) sessionHttp.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        int sessionId = Integer.parseInt(request.getParameter("sessionId"));
        String inputPassword = request.getParameter("sessionPassword");

        try {
            ExamSession session = sessionDAO.findById(sessionId);
            if (session == null || !"OPEN".equalsIgnoreCase(session.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/student/exams?error=Ca+thi+chua+duoc+giao+vien+mo+phong!");
                return;
            }

            // Kiểm tra mật khẩu nếu ca thi yêu cầu
            if (session.getSessionPassword() != null && !session.getSessionPassword().isBlank()) {
                if (inputPassword == null || !session.getSessionPassword().trim().equals(inputPassword.trim())) {
                    response.sendRedirect(request.getContextPath()
                            + "/student/exams?error=Mat+khau+phong+thi+khong+dung!&invalidSessionId=" + sessionId);
                    return;
                }
            }

            // Tạo hoặc lấy bài làm IN_PROGRESS hiện tại
            int attemptId = attemptDAO.getOrCreateActiveAttempt(sessionId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/student/take-exam?attemptId=" + attemptId);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
        }
    }

    // 3. Tải câu hỏi hiển thị lên màn hình làm bài
    private void handleTakeExamPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String attemptIdParam = request.getParameter("attemptId");
        if (attemptIdParam == null || attemptIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        int attemptId;
        try {
            attemptId = Integer.parseInt(attemptIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        try {
            AttemptDAO.TakeExamDetailDTO detail = attemptDAO.getTakeExamDetail(attemptId);

            if (detail == null) {
                response.sendRedirect(request.getContextPath() + "/student/exams");
                return;
            }

            request.setAttribute("attemptId", detail.getAttemptId());
            request.setAttribute("examTitle", detail.getExamTitle());
            request.setAttribute("sessionName", detail.getSessionName());
            request.setAttribute("durationMinutes", detail.getDurationMinutes());
            request.setAttribute("questions", detail.getQuestions());

            request.getRequestDispatcher("/WEB-INF/views/student/take-exam.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải bài thi: " + e.getMessage());
        }
    }

    // 4. Học sinh nộp bài: Lưu đáp án và tự động chấm điểm
    private void handleSubmitExam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int attemptId = Integer.parseInt(request.getParameter("attemptId"));

        try {
            // Đẩy toàn bộ parameter sang DAO để lưu câu trả lời bằng Transaction
            attemptDAO.submitAttemptAnswers(attemptId, request.getParameterMap());

            // Tự động chấm điểm
            gradingService.gradeAttempt(attemptId);

            response.sendRedirect(request.getContextPath() + "/student/results?attemptId=" + attemptId);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
        }
    }
}