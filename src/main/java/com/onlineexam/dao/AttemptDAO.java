package com.onlineexam.dao;

import com.onlineexam.model.Attempt;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho bảng Attempts (bài thi của thí sinh).
 */
public class AttemptDAO {

    private Connection connection;

    public AttemptDAO() {
    }

    public AttemptDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        }
        return DBConnection.getConnection();
    }

    public boolean insert(Attempt attempt) throws SQLException {
        String sql = "INSERT INTO Attempts (session_id, user_id, attempt_number, started_at, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, attempt.getSessionId());
            ps.setInt(2, attempt.getUserId());
            ps.setInt(3, attempt.getAttemptNumber());
            ps.setTimestamp(4, Timestamp.valueOf(attempt.getStartedAt()));
            ps.setString(5, attempt.getStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        attempt.setAttemptId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Attempt findById(int attemptId) throws SQLException {
        String sql = "SELECT * FROM Attempts WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        }
        return null;
    }

    public int countUserAttempts(int sessionId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Attempts WHERE session_id = ? AND user_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean updateSubmission(Attempt attempt) throws SQLException {
        String sql = "UPDATE Attempts SET submitted_at = ?, status = ? WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(attempt.getSubmittedAt()));
            ps.setString(2, attempt.getStatus());
            ps.setInt(3, attempt.getAttemptId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGrading(Attempt attempt) throws SQLException {
        String sql = "UPDATE Attempts SET score = ?, status = ?, graded_by = ?, graded_at = ? WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBigDecimal(1, attempt.getScore());
            ps.setString(2, attempt.getStatus());

            if (attempt.getGradedBy() != null) {
                ps.setInt(3, attempt.getGradedBy());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (attempt.getGradedAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(attempt.getGradedAt()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            ps.setInt(5, attempt.getAttemptId());

            return ps.executeUpdate() > 0;
        }
    }

    private Attempt mapResultSetToAttempt(ResultSet rs) throws SQLException {
        Attempt attempt = new Attempt();
        attempt.setAttemptId(rs.getInt("attempt_id"));
        attempt.setSessionId(rs.getInt("session_id"));
        attempt.setUserId(rs.getInt("user_id"));
        attempt.setAttemptNumber(rs.getInt("attempt_number"));

        Timestamp start = rs.getTimestamp("started_at");
        if (start != null) attempt.setStartedAt(start.toLocalDateTime());

        Timestamp submit = rs.getTimestamp("submitted_at");
        if (submit != null) attempt.setSubmittedAt(submit.toLocalDateTime());

        attempt.setStatus(rs.getString("status"));
        attempt.setScore(rs.getBigDecimal("score"));

        int gradedBy = rs.getInt("graded_by");
        attempt.setGradedBy(rs.wasNull() ? null : gradedBy);

        Timestamp gradedAt = rs.getTimestamp("graded_at");
        if (gradedAt != null) attempt.setGradedAt(gradedAt.toLocalDateTime());

        return attempt;
    }
}