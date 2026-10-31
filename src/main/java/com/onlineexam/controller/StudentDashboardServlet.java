package com.onlineexam.controller;

import com.onlineexam.model.User;
import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/student/dashboard", "/student/home"})
public class StudentDashboardServlet extends HttpServlet {

    // DTO chứa thông tin ca thi sắp tới / đang mở
    public static class ActiveSessionDTO {
        public int sessionId;
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String startTime;
        public String status;
        public boolean hasPassword;
    }

    // DTO chứa thông tin lịch sử bài làm của học sinh
    public static class AttemptHistoryDTO {
        public int attemptId;
        public String examTitle;
        public String sessionName;
        public BigDecimal score;
        public BigDecimal passScore;
        public String submittedAt;
        public boolean isPassed;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User currentUser = (User) request.getSession().getAttribute("user");
        int studentId = (currentUser != null) ? currentUser.getUserId() : 2; // Mặc định ID 2 (Trần Thị B)
        String studentName = (currentUser != null) ? currentUser.getFullName() : "Em Trần Thị B";

        int totalExamsDone = 0;
        double avgScore = 0.0;
        int passedCount = 0;

        List<ActiveSessionDTO> activeSessions = new ArrayList<>();
        List<AttemptHistoryDTO> historyList = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Thống kê bài thi của học sinh
            String sqlStats = "SELECT COUNT(*) AS total_done, " +
                              "COALESCE(AVG(a.score), 0) AS avg_score, " +
                              "SUM(CASE WHEN a.score >= e.pass_score THEN 1 ELSE 0 END) AS passed_count " +
                              "FROM attempts a " +
                              "JOIN exam_sessions es ON a.session_id = es.session_id " +
                              "JOIN exams e ON es.exam_id = e.exam_id " +
                              "WHERE a.user_id = ? AND a.status = 'GRADED'";

            try (PreparedStatement ps = conn.prepareStatement(sqlStats)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalExamsDone = rs.getInt("total_done");
                        avgScore = Math.round(rs.getDouble("avg_score") * 10.0) / 10.0;
                        passedCount = rs.getInt("passed_count");
                    }
                }
            }

            // 2. Lấy các ca thi đang mở (OPEN) hoặc sắp diễn ra
            String sqlActive = "SELECT es.*, e.exam_title, e.duration_minutes " +
                               "FROM exam_sessions es " +
                               "JOIN exams e ON es.exam_id = e.exam_id " +
                               "WHERE es.status IN ('OPEN', 'SCHEDULED') " +
                               "ORDER BY (es.status = 'OPEN') DESC, es.start_time ASC LIMIT 4";

            try (PreparedStatement ps = conn.prepareStatement(sqlActive);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActiveSessionDTO s = new ActiveSessionDTO();
                    s.sessionId = rs.getInt("session_id");
                    s.sessionName = rs.getString("session_name");
                    s.examTitle = rs.getString("exam_title");
                    s.durationMinutes = rs.getInt("duration_minutes");
                    s.status = rs.getString("status");
                    String pass = rs.getString("session_password");
                    s.hasPassword = (pass != null && !pass.trim().isEmpty());

                    if (rs.getTimestamp("start_time") != null) {
                        s.startTime = rs.getTimestamp("start_time").toLocalDateTime().format(dtf);
                    }
                    activeSessions.add(s);
                }
            }

            // 3. Lấy lịch sử các bài đã làm (Attempt History)
            String sqlHistory = "SELECT a.attempt_id, a.score, a.submitted_at, e.exam_title, e.pass_score, es.session_name " +
                                "FROM attempts a " +
                                "JOIN exam_sessions es ON a.session_id = es.session_id " +
                                "JOIN exams e ON es.exam_id = e.exam_id " +
                                "WHERE a.user_id = ? AND a.status = 'GRADED' " +
                                "ORDER BY a.submitted_at DESC LIMIT 10";

            try (PreparedStatement ps = conn.prepareStatement(sqlHistory)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AttemptHistoryDTO h = new AttemptHistoryDTO();
                        h.attemptId = rs.getInt("attempt_id");
                        h.examTitle = rs.getString("exam_title");
                        h.sessionName = rs.getString("session_name");
                        h.score = rs.getBigDecimal("score");
                        h.passScore = rs.getBigDecimal("pass_score");
                        h.isPassed = (h.score != null && h.passScore != null && h.score.compareTo(h.passScore) >= 0);

                        if (rs.getTimestamp("submitted_at") != null) {
                            h.submittedAt = rs.getTimestamp("submitted_at").toLocalDateTime().format(dtf);
                        }
                        historyList.add(h);
                    }
                }
            }

            request.setAttribute("studentName", studentName);
            request.setAttribute("totalExamsDone", totalExamsDone);
            request.setAttribute("avgScore", avgScore);
            request.setAttribute("passedCount", passedCount);
            request.setAttribute("activeSessions", activeSessions);
            request.setAttribute("historyList", historyList);

            request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp dashboard: " + e.getMessage());
        }
    }
}