package com.onlineexam.dao;

import com.onlineexam.model.AttemptAnswer;
import com.onlineexam.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho bảng Attempt_Answers và Attempt_Answer_Options.
 */
public class AttemptAnswerDAO {

    private Connection connection;

    public AttemptAnswerDAO() {
    }

    public AttemptAnswerDAO(Connection connection) {
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

    public boolean saveOrUpdate(AttemptAnswer answer) throws SQLException {
        // Sử dụng ON DUPLICATE KEY UPDATE theo schema UNIQUE (attempt_id, question_id)
        String sql = "INSERT INTO Attempt_Answers (attempt_id, question_id, answer_text, is_correct, points_awarded, feedback) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE answer_text = VALUES(answer_text), is_correct = VALUES(is_correct), " +
                     "points_awarded = VALUES(points_awarded), feedback = VALUES(feedback)";

        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, answer.getAttemptId());
            ps.setInt(2, answer.getQuestionId());
            ps.setString(3, answer.getAnswerText());

            if (answer.getIsCorrect() != null) {
                ps.setBoolean(4, answer.getIsCorrect());
            } else {
                ps.setNull(4, Types.BOOLEAN);
            }

            ps.setBigDecimal(5, answer.getPointsAwarded() != null ? answer.getPointsAwarded() : BigDecimal.ZERO);
            ps.setString(6, answer.getFeedback());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        answer.setAnswerId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<AttemptAnswer> findByAttemptId(int attemptId) throws SQLException {
        List<AttemptAnswer> list = new ArrayList<>();
        String sql = "SELECT * FROM Attempt_Answers WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttemptAnswer ans = new AttemptAnswer();
                    ans.setAnswerId(rs.getInt("answer_id"));
                    ans.setAttemptId(rs.getInt("attempt_id"));
                    ans.setQuestionId(rs.getInt("question_id"));
                    ans.setAnswerText(rs.getString("answer_text"));

                    boolean isCorrect = rs.getBoolean("is_correct");
                    ans.setIsCorrect(rs.wasNull() ? null : isCorrect);

                    ans.setPointsAwarded(rs.getBigDecimal("points_awarded"));
                    ans.setFeedback(rs.getString("feedback"));
                    list.add(ans);
                }
            }
        }
        return list;
    }

    public BigDecimal sumPointsByAttemptId(int attemptId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points_awarded), 0.00) FROM Attempt_Answers WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }
}