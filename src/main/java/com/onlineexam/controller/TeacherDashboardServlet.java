package com.onlineexam.controller;

import com.onlineexam.dao.DashboardDAO;
import com.onlineexam.dao.DashboardDAO.TeacherStatsDTO;
import com.onlineexam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/teacher/dashboard")
public class TeacherDashboardServlet extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            TeacherStatsDTO stats = dashboardDAO.getTeacherDashboardStats(user.getUserId());

            request.setAttribute("totalExams", stats.getTotalExams());
            request.setAttribute("publishedExams", stats.getPublishedExams());
            request.setAttribute("totalSessions", stats.getTotalSessions());
            request.setAttribute("activeSessions", stats.getActiveSessions());
            request.setAttribute("totalStudents", stats.getTotalStudents());
            request.setAttribute("avgScore", stats.getAvgScore());
            request.setAttribute("recentSessions", stats.getRecentSessions());

            request.getRequestDispatcher("/WEB-INF/views/teacher/dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải dữ liệu thống kê giáo viên");
        }
    }
}