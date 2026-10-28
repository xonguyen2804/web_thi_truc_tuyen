package com.onlineexam.dao;

import com.onlineexam.model.Option;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OptionDAO {
    public int create(Option option) throws SQLException {
        String sql = "INSERT INTO Options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, option.getQuestionId());
            ps.setString(2, option.getOptionText());
            ps.setBoolean(3, option.isCorrect());
            ps.setInt(4, option.getDisplayOrder());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating option failed: no ID returned.");
    }

    public List<Option> findByQuestion(int questionId) throws SQLException {
        String sql = "SELECT * FROM Options WHERE question_id = ? ORDER BY display_order";
        List<Option> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public boolean update(Option option) throws SQLException {
        String sql = "UPDATE Options SET option_text = ?, is_correct = ?, display_order = ? WHERE option_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, option.getOptionText());
            ps.setBoolean(2, option.isCorrect());
            ps.setInt(3, option.getDisplayOrder());
            ps.setInt(4, option.getOptionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int optionId) throws SQLException {
        String sql = "DELETE FROM Options WHERE option_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, optionId);
            return ps.executeUpdate() > 0;
        }
    }

    public int deleteByQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM Options WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate();
        }
    }

    private Option mapRow(ResultSet rs) throws SQLException {
        return new Option(rs.getInt("option_id"), rs.getInt("question_id"),
                rs.getString("option_text"), rs.getBoolean("is_correct"),
                rs.getInt("display_order"));
    }
}
