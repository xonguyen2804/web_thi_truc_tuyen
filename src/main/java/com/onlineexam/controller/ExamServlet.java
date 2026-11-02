package com.onlineexam.controller;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@WebServlet({ "/teacher/exams", "/exams" })
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,      // Tối đa 5MB cho 1 file
        maxRequestSize = 6 * 1024 * 1024     // Tối đa 6MB cho toàn bộ request
)
public class ExamServlet extends HttpServlet {

    private final ExamDAO examDAO = new ExamDAO();

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

        // 2. Kiểm tra quyền TEACHER hoặc ADMIN
        String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "";
        if (!"TEACHER".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền quản lý đề thi");
            return;
        }

        try {
            List<Exam> exams = examDAO.findAll();
            request.setAttribute("exams", exams);
            request.getRequestDispatcher("/WEB-INF/views/teacher/exams.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp danh sách đề thi: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            String title = request.getParameter("examTitle");
            String description = request.getParameter("description");
            int durationMinutes = Integer.parseInt(request.getParameter("durationMinutes"));
            BigDecimal passScore = new BigDecimal(request.getParameter("passScore"));
            String status = request.getParameter("status");

            Exam exam = new Exam();
            exam.setCreatedBy(user.getUserId());
            exam.setExamTitle(title);
            exam.setDescription(description);
            exam.setDurationMinutes(durationMinutes);
            exam.setPassScore(passScore);
            exam.setStatus(status != null ? status : "PUBLISHED");

            // 1. Lưu thông tin đề thi qua DAO
            examDAO.insert(exam);

            // 2. Xử lý file đề thi upload qua DAO (nếu có)
            Part filePart = request.getPart("examFile");
            if (filePart != null && filePart.getSize() > 0) {
                examDAO.importQuestionsFromStream(filePart.getInputStream(), exam.getExamId(), exam.getCreatedBy());
            }

            response.sendRedirect(request.getContextPath() + "/teacher/exams?success=true");

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Lỗi tạo đề thi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/teacher/exams?error=true");
        }
    }
}