package com.onlineexam.controller;

import com.onlineexam.dao.DashboardDAO;
import com.onlineexam.dao.DashboardDAO.StudentDashboardDTO;
import com.onlineexam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập -> chuyển về trang login
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // Lấy tên học sinh hiển thị
        String studentName = user.getFullName();
        if (studentName == null || studentName.trim().isEmpty()) {
            studentName = user.getUsername();
        }

        try {
            // Gọi qua DAO lấy toàn bộ thông tin
            StudentDashboardDTO data = dashboardDAO.getStudentDashboardStats(user.getUserId());

            request.setAttribute("studentName", studentName);
            request.setAttribute("totalExamsDone", data.getTotalExamsDone());
            request.setAttribute("avgScore", data.getAvgScore());
            request.setAttribute("passedCount", data.getPassedCount());
            request.setAttribute("activeSessions", data.getActiveSessions());
            request.setAttribute("historyList", data.getHistoryList());

            request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi lấy dữ liệu trang học sinh");
        }
    }
}