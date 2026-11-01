
package com.onlineexam.controller;

import com.onlineexam.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet({ "/teacher/dashboard", "/teacher/home" })
public class TeacherDashboardServlet extends HttpServlet {

    // DTO hiển thị các ca thi gần nhất
    public static class RecentSessionDTO {
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String timeRange;
        public String targetName;
        public String status;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra phiên đăng nhập
        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Kiểm tra quyền TEACHER
        if (!"TEACHER".equals(session.getAttribute("role"))) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập trang giáo viên");
            return;
        }

        // 3. Xác thực ID người dùng trong session
        int teacherId;

        try {
            teacherId = Integer.parseInt(
                    session.getAttribute("userId").toString());

            if (teacherId <= 0) {
                throw new NumberFormatException(
                        "Invalid teacher ID");
            }

        } catch (NumberFormatException e) {

            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        // 4. Khởi tạo dữ liệu dashboard
        int totalExams = 0;
        int publishedExams = 0;
        int totalSessions = 0;
        int activeSessions = 0;
        int totalStudents = 0;
        double avgScore = 0.0;

        List<RecentSessionDTO> recentSessions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 5. Thống kê đề thi toàn hệ thống
            String sqlExams = """
                    SELECT
                        COUNT(*) AS total,
                        COALESCE(
                            SUM(
                                CASE
                                    WHEN status = 'PUBLISHED'
                                    THEN 1
                                    ELSE 0
                                END
                            ), 0
                        ) AS published
                    FROM exams
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sqlExams);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalExams = rs.getInt("total");

                    publishedExams = rs.getInt("published");
                }
            }

            // 6. Thống kê ca thi toàn hệ thống
            String sqlSessions = """
                    SELECT
                        COUNT(*) AS total,
                        COALESCE(
                            SUM(
                                CASE
                                    WHEN status = 'OPEN'
                                    THEN 1
                                    ELSE 0
                                END
                            ), 0
                        ) AS active
                    FROM exam_sessions
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sqlSessions);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalSessions = rs.getInt("total");

                    activeSessions = rs.getInt("active");
                }
            }

            // 7. Đếm học sinh theo vai trò STUDENT
            String sqlStudents = """
                    SELECT COUNT(DISTINCT u.user_id) AS total
                    FROM users u
                    JOIN user_roles ur
                        ON u.user_id = ur.user_id
                    JOIN roles r
                        ON ur.role_id = r.role_id
                    WHERE r.role_name = 'STUDENT'
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sqlStudents);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalStudents = rs.getInt("total");
                }
            }

            // 8. Tính điểm trung bình các bài đã có điểm
            String sqlAvgScore = """
                    SELECT
                        COALESCE(AVG(score), 0) AS avg_score
                    FROM attempts
                    WHERE score IS NOT NULL
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sqlAvgScore);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    avgScore = rs.getDouble("avg_score");
                }
            }

            // 9. Lấy 5 ca thi mới nhất
            String sqlRecent = """
                    SELECT
                        es.session_name,
                        e.exam_title,
                        e.duration_minutes,
                        DATE_FORMAT(
                            es.start_time,
                            '%H:%i %d/%m'
                        ) AS start_f,
                        DATE_FORMAT(
                            es.end_time,
                            '%H:%i %d/%m'
                        ) AS end_f,
                        COALESCE(
                            c.class_name,
                            g.group_name,
                            'Tất cả'
                        ) AS target_name,
                        es.status
                    FROM exam_sessions es
                    JOIN exams e
                        ON es.exam_id = e.exam_id
                    LEFT JOIN classes c
                        ON es.class_id = c.class_id
                    LEFT JOIN `groups` g
                        ON es.group_id = g.group_id
                    ORDER BY es.created_at DESC
                    LIMIT 5
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sqlRecent);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    RecentSessionDTO dto = new RecentSessionDTO();

                    dto.sessionName = rs.getString("session_name");

                    dto.examTitle = rs.getString("exam_title");

                    dto.durationMinutes = rs.getInt("duration_minutes");

                    String start = rs.getString("start_f");

                    String end = rs.getString("end_f");

                    dto.timeRange = (start != null ? start : "-")
                            + " - " +
                            (end != null ? end : "-");

                    dto.targetName = rs.getString("target_name");

                    dto.status = rs.getString("status");

                    recentSessions.add(dto);
                }
            }

        } catch (SQLException e) {

            // Ghi lỗi chi tiết vào log Tomcat
            log(
                    "Lỗi tải Teacher Dashboard, teacherId="
                            + teacherId,
                    e);

            // Không hiển thị số liệu mặc định như dữ liệu thật
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải dữ liệu dashboard giáo viên.");
            return;
        }

        // 10. Truyền dữ liệu sang JSP
        request.setAttribute("totalExams", totalExams);

        request.setAttribute(
                "publishedExams", publishedExams);

        request.setAttribute(
                "totalSessions", totalSessions);

        request.setAttribute(
                "activeSessions", activeSessions);

        request.setAttribute(
                "totalStudents", totalStudents);

        request.setAttribute(
                "avgScore",
                String.format(Locale.US, "%.2f", avgScore));

        request.setAttribute(
                "recentSessions", recentSessions);

        // 11. Hiển thị dashboard giáo viên
        request.getRequestDispatcher(
                "/WEB-INF/views/teacher/dashboard.jsp").forward(request, response);
    }
}
