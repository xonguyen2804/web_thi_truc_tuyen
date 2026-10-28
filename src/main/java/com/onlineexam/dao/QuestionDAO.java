package com.onlineexam.dao;

import com.onlineexam.model.Question;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public int create(Question question) throws SQLException {
        String sql = "INSERT INTO Questions (created_by, question_text, question_type, default_points, explanation, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getCreatedBy());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getQuestionType());
            ps.setBigDecimal(4, question.getDefaultPoints());
            ps.setString(5, question.getExplanation());
            ps.setString(6, question.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating question failed: no ID returned.");
    }

    public Question findById(int questionId) throws SQLException {
        String sql = "SELECT * FROM Questions WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Question> findByCreator(int createdBy) throws SQLException {
        String sql = "SELECT * FROM Questions WHERE created_by = ? ORDER BY question_id DESC";
        List<Question> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Question> findAllActive() throws SQLException {
        String sql = "SELECT * FROM Questions WHERE status = 'ACTIVE' ORDER BY question_id DESC";
        List<Question> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public boolean update(Question question) throws SQLException {
        String sql = "UPDATE Questions SET question_text = ?, question_type = ?, default_points = ?, explanation = ?, status = ? WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getQuestionType());
            ps.setBigDecimal(3, question.getDefaultPoints());
            ps.setString(4, question.getExplanation());
            ps.setString(5, question.getStatus());
            ps.setInt(6, question.getQuestionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int questionId) throws SQLException {
        String sql = "UPDATE Questions SET status = 'INACTIVE' WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Question(rs.getInt("question_id"), rs.getInt("created_by"),
                rs.getString("question_text"), rs.getString("question_type"),
                rs.getBigDecimal("default_points"), rs.getString("explanation"),
                rs.getString("status"), ts == null ? null : ts.toLocalDateTime());
    }
}
