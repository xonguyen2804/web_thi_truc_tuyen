package com.onlineexam.dao;

import com.onlineexam.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    // DTO chứa dữ liệu phiên thi gần nhất
    public static class RecentSessionDTO {
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String timeRange;
        public String targetName;
        public String status;
        public RecentSessionDTO(String sessionName, String examTitle, int durationMinutes, String timeRange, String targetName, String status) {
            this.sessionName = sessionName;
            this.examTitle = examTitle;
            this.durationMinutes = durationMinutes;
            this.timeRange = timeRange;
            this.targetName = targetName;
            this.status = status;
        }
        public String getSessionName() { return sessionName; }
        public String getExamTitle() { return examTitle; }
        public int getDurationMinutes() { return durationMinutes; }
        public String getTimeRange() { return timeRange; }
        public String getTargetName() { return targetName; }
        public String getStatus() { return status; }
    }

    // DTO chứa số liệu thống kê Dashboard giáo viên
    public static class TeacherStatsDTO {
        private int totalExams;
        private int publishedExams;
        private int totalSessions;
        private int activeSessions;
        private int totalStudents;
        private String avgScore;
        private List<RecentSessionDTO> recentSessions;

        // Getters and Setters
        public int getTotalExams() { return totalExams; }
        public void setTotalExams(int totalExams) { this.totalExams = totalExams; }
        public int getPublishedExams() { return publishedExams; }
        public void setPublishedExams(int publishedExams) { this.publishedExams = publishedExams; }
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public int getActiveSessions() { return activeSessions; }
        public void setActiveSessions(int activeSessions) { this.activeSessions = activeSessions; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public String getAvgScore() { return avgScore; }
        public void setAvgScore(String avgScore) { this.avgScore = avgScore; }
        public List<RecentSessionDTO> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<RecentSessionDTO> recentSessions) { this.recentSessions = recentSessions; }
    }

    public TeacherStatsDTO getTeacherDashboardStats(int teacherId) throws SQLException {
        TeacherStatsDTO stats = new TeacherStatsDTO();

        // 1. Thống kê bài thi
        String sqlExams = """
            SELECT COUNT(*) AS total, 
                   SUM(CASE WHEN status = 'PUBLISHED' THEN 1 ELSE 0 END) AS published 
            FROM exams WHERE created_by = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlExams)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalExams(rs.getInt("total"));
                    stats.setPublishedExams(rs.getInt("published"));
                }
            }
        }

        // 2. Thống kê ca thi
        String sqlSessions = """
            SELECT COUNT(*) AS total,
                   SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS active
            FROM exam_sessions WHERE created_by = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSessions)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalSessions(rs.getInt("total"));
                    stats.setActiveSessions(rs.getInt("active"));
                }
            }
        }

        // 3. Thống kê học sinh
        String sqlStudents = """
            SELECT COUNT(DISTINCT cm.user_id) AS total_students
            FROM class_memberships cm
            JOIN classes c ON cm.class_id = c.class_id
            WHERE c.created_by = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalStudents(rs.getInt("total_students"));
                }
            }
        }

        // 4. Điểm trung bình
        String sqlAvgScore = """
            SELECT ROUND(AVG(a.score), 2) AS avg_score
            FROM attempts a
            JOIN exam_sessions s ON a.session_id = s.session_id
            WHERE s.created_by = ? AND a.score IS NOT NULL
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAvgScore)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_score");
                    stats.setAvgScore(rs.wasNull() ? "0.00" : String.format("%.2f", avg));
                }
            }
        }

                // 5. Danh sách ca thi gần đây
        List<RecentSessionDTO> list = new ArrayList<>();
        String sqlRecent = """
            SELECT s.session_name, e.exam_title, e.duration_minutes, 
                   s.start_time, s.end_time, s.status,
                   COALESCE(c.class_name, g.group_name, 'Tất cả') AS target_name
            FROM exam_sessions s
            JOIN exams e ON s.exam_id = e.exam_id
            LEFT JOIN classes c ON s.class_id = c.class_id
            LEFT JOIN `groups` g ON s.group_id = g.group_id
            WHERE s.created_by = ?
            ORDER BY s.session_id DESC LIMIT 5
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRecent)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String start = rs.getString("start_time");
                    String end = rs.getString("end_time");
                    String timeRange = (start != null ? start : "") + (end != null ? " - " + end : "");
                    if (timeRange.isBlank()) timeRange = "Không giới hạn";

                    String target = rs.getString("target_name");

                    list.add(new RecentSessionDTO(
                        rs.getString("session_name"),
                        rs.getString("exam_title"),
                        rs.getInt("duration_minutes"),
                        timeRange,
                        target,
                        rs.getString("status")
                    ));
                }
            }
        }
        stats.setRecentSessions(list);

        return stats;
    }

    // --- CÁC DTO DÀNH CHO HỌC SINH ---
    public static class ActiveSessionDTO {
        public int sessionId;
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String status;
        public String startTime;
        public boolean hasPassword;
        public int getSessionId() { return sessionId; }
        public String getSessionName() { return sessionName; }
        public String getExamTitle() { return examTitle; }
        public int getDurationMinutes() { return durationMinutes; }
        public String getStatus() { return status; }
        public String getStartTime() { return startTime; }
        public boolean isHasPassword() { return hasPassword; }
    }
    public static class AttemptHistoryDTO {
        public int attemptId;
        public String examTitle;
        public String sessionName;
        public java.math.BigDecimal score;
        public java.math.BigDecimal passScore;
        public String submittedAt;
        public boolean isPassed;
        public int getAttemptId() { return attemptId; }
        public String getExamTitle() { return examTitle; }
        public String getSessionName() { return sessionName; }
        public java.math.BigDecimal getScore() { return score; }
        public java.math.BigDecimal getPassScore() { return passScore; }
        public String getSubmittedAt() { return submittedAt; }
        public boolean isPassed() { return isPassed; }
    }
    public static class StudentDashboardDTO {
        private int totalExamsDone;
        private double avgScore;
        private int passedCount;
        private List<ActiveSessionDTO> activeSessions = new ArrayList<>();
        private List<AttemptHistoryDTO> historyList = new ArrayList<>();
        public int getTotalExamsDone() { return totalExamsDone; }
        public void setTotalExamsDone(int totalExamsDone) { this.totalExamsDone = totalExamsDone; }
        public double getAvgScore() { return avgScore; }
        public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
        public int getPassedCount() { return passedCount; }
        public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
        public List<ActiveSessionDTO> getActiveSessions() { return activeSessions; }
        public void setActiveSessions(List<ActiveSessionDTO> activeSessions) { this.activeSessions = activeSessions; }
        public List<AttemptHistoryDTO> getHistoryList() { return historyList; }
        public void setHistoryList(List<AttemptHistoryDTO> historyList) { this.historyList = historyList; }
    }
    // Phương thức truy vấn toàn bộ dữ liệu trang Dashboard của Học Sinh
    public StudentDashboardDTO getStudentDashboardStats(int studentId) throws SQLException {
        StudentDashboardDTO result = new StudentDashboardDTO();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Thống kê số bài thi đã làm, điểm trung bình, số bài đạt
            String sqlStats = """
                SELECT COUNT(*) AS total_done,
                       COALESCE(AVG(a.score), 0) AS avg_score,
                       SUM(CASE WHEN a.score >= e.pass_score THEN 1 ELSE 0 END) AS passed_count
                FROM attempts a
                JOIN exam_sessions es ON a.session_id = es.session_id
                JOIN exams e ON es.exam_id = e.exam_id
                WHERE a.user_id = ? AND a.status = 'GRADED'
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlStats)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result.setTotalExamsDone(rs.getInt("total_done"));
                        result.setAvgScore(Math.round(rs.getDouble("avg_score") * 10.0) / 10.0);
                        result.setPassedCount(rs.getInt("passed_count"));
                    }
                }
            }
            // 2. Lấy các ca thi đang mở (OPEN) hoặc sắp diễn ra
            String sqlActive = """
                SELECT es.*, e.exam_title AS exam_title, e.duration_minutes
                FROM exam_sessions es
                JOIN exams e ON es.exam_id = e.exam_id
                WHERE es.status IN ('OPEN', 'SCHEDULED')
                ORDER BY (es.status = 'OPEN') DESC, es.start_time ASC LIMIT 4
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlActive);
                 ResultSet rs = ps.executeQuery()) {
                List<ActiveSessionDTO> activeSessions = new ArrayList<>();
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
                result.setActiveSessions(activeSessions);
            }
            // 3. Lịch sử bài làm (Attempt History)
            String sqlHistory = """
                SELECT a.attempt_id, a.score, a.submitted_at, e.exam_title AS exam_title, e.pass_score, es.session_name
                FROM attempts a
                JOIN exam_sessions es ON a.session_id = es.session_id
                JOIN exams e ON es.exam_id = e.exam_id
                WHERE a.user_id = ? AND a.status = 'GRADED'
                ORDER BY a.submitted_at DESC LIMIT 10
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlHistory)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<AttemptHistoryDTO> historyList = new ArrayList<>();
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
                    result.setHistoryList(historyList);
                }
            }
        }
        return result;
    }
}