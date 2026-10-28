package com.onlineexam.dao;

import com.onlineexam.model.ClassMembership;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassMembershipDAO {
    public boolean add(int classId, int userId) throws SQLException {
        String sql = "INSERT INTO Class_Memberships (class_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ClassMembership> findByClass(int classId) throws SQLException {
        String sql = "SELECT class_id, user_id, joined_at FROM Class_Memberships WHERE class_id = ? ORDER BY joined_at";
        List<ClassMembership> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("joined_at");
                    result.add(new ClassMembership(rs.getInt("class_id"), rs.getInt("user_id"),
                            ts == null ? null : ts.toLocalDateTime()));
                }
            }
        }
        return result;
    }

    public boolean exists(int classId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM Class_Memberships WHERE class_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean remove(int classId, int userId) throws SQLException {
        String sql = "DELETE FROM Class_Memberships WHERE class_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
