package com.onlineexam.controller;

import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/teacher/dashboard")
public class TeacherDashboardServlet extends HttpServlet {

    // DTO chứa dữ liệu hàng của bảng ca thi gần nhất
    public static class RecentSessionDTO {
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String timeRange;
        public String targetName; // Lớp hoặc Nhóm
        public String status;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int totalExams = 0;
        int publishedExams = 0;
        int totalSessions = 0;
        int activeSessions = 0;
        int totalStudents = 0;
        double avgScore = 0.0;
        List<RecentSessionDTO> recentSessions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Đếm tổng số đề thi và số đề đã PUBLISHED
            String sqlExams = "SELECT COUNT(*) AS total, " +
                              "SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END) AS published " +
                              "FROM Exams";
            try (PreparedStatement ps = conn.prepareStatement(sqlExams);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalExams = rs.getInt("total");
                    publishedExams = rs.getInt("published");
                }
            }

            // 2. Đếm số ca thi và số ca đang mở (OPEN)
            String sqlSessions = "SELECT COUNT(*) AS total, " +
                                 "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS active " +
                                 "FROM Exam_Sessions";
            try (PreparedStatement ps = conn.prepareStatement(sqlSessions);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalSessions = rs.getInt("total");
                    activeSessions = rs.getInt("active");
                }
            }

            // 3. Đếm số học sinh trong hệ thống
            String sqlStudents = "SELECT COUNT(DISTINCT u.user_id) FROM Users u " +
                                 "JOIN User_Roles ur ON u.user_id = ur.user_id " +
                                 "JOIN Roles r ON ur.role_id = r.role_id " +
                                 "WHERE r.role_name = 'STUDENT'";
            try (PreparedStatement ps = conn.prepareStatement(sqlStudents);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalStudents = rs.getInt(1);
                }
            }

            // 4. Tính điểm trung bình của các bài thi đã làm (bảng Attempts)
            String sqlAvgScore = "SELECT AVG(score) FROM Attempts WHERE score IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sqlAvgScore);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    avgScore = rs.getDouble(1);
                }
            }

            // 5. Lấy danh sách 5 ca thi mới nhất từ DB
            String sqlRecent = "SELECT es.session_name, e.exam_title, e.duration_minutes, " +
                               "DATE_FORMAT(es.start_time, '%H:%i %d/%m') AS start_f, " +
                               "DATE_FORMAT(es.end_time, '%H:%i %d/%m') AS end_f, " +
                               "COALESCE(c.class_name, g.group_name, 'Tất cả') AS target_name, " +
                               "es.status " +
                               "FROM Exam_Sessions es " +
                               "JOIN Exams e ON es.exam_id = e.exam_id " +
                               "LEFT JOIN Classes c ON es.class_id = c.class_id " +
                               "LEFT JOIN `Groups` g ON es.group_id = g.group_id " +
                               "ORDER BY es.created_at DESC LIMIT 5";
            try (PreparedStatement ps = conn.prepareStatement(sqlRecent);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecentSessionDTO dto = new RecentSessionDTO();
                    dto.sessionName = rs.getString("session_name");
                    dto.examTitle = rs.getString("exam_title");
                    dto.durationMinutes = rs.getInt("duration_minutes");
                    dto.timeRange = rs.getString("start_f") + " - " + rs.getString("end_f");
                    dto.targetName = rs.getString("target_name");
                    dto.status = rs.getString("status");
                    recentSessions.add(dto);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Gửi dữ liệu thực tế sang trang JSP
        request.setAttribute("totalExams", totalExams);
        request.setAttribute("publishedExams", publishedExams);
        request.setAttribute("totalSessions", totalSessions);
        request.setAttribute("activeSessions", activeSessions);
        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("avgScore", String.format("%.2f", avgScore));
        request.setAttribute("recentSessions", recentSessions);

        request.getRequestDispatcher("/WEB-INF/views/teacher/dashboard.jsp").forward(request, response);
    }
}