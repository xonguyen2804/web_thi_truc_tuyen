package com.onlineexam.dao;

import com.onlineexam.model.ExpectedAnswer;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpectedAnswerDAO {
    public int create(ExpectedAnswer answer) throws SQLException {
        String sql = "INSERT INTO Expected_Answers (question_id, answer_text) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, answer.getQuestionId());
            ps.setString(2, answer.getAnswerText());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating expected answer failed: no ID returned.");
    }

    public List<ExpectedAnswer> findByQuestion(int questionId) throws SQLException {
        String sql = "SELECT * FROM Expected_Answers WHERE question_id = ? ORDER BY expected_answer_id";
        List<ExpectedAnswer> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public boolean update(ExpectedAnswer answer) throws SQLException {
        String sql = "UPDATE Expected_Answers SET answer_text = ? WHERE expected_answer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, answer.getAnswerText());
            ps.setInt(2, answer.getExpectedAnswerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int expectedAnswerId) throws SQLException {
        String sql = "DELETE FROM Expected_Answers WHERE expected_answer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expectedAnswerId);
            return ps.executeUpdate() > 0;
        }
    }

    public int deleteByQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM Expected_Answers WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate();
        }
    }

    private ExpectedAnswer mapRow(ResultSet rs) throws SQLException {
        return new ExpectedAnswer(rs.getInt("expected_answer_id"),
                rs.getInt("question_id"), rs.getString("answer_text"));
    }
}