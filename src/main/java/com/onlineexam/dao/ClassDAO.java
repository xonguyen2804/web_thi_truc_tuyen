package com.onlineexam.dao;

import com.onlineexam.model.SchoolClass;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {
    public int create(SchoolClass schoolClass) throws SQLException {
        String sql = "INSERT INTO Classes (class_name, description, created_by) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, schoolClass.getClassName());
            ps.setString(2, schoolClass.getDescription());
            ps.setInt(3, schoolClass.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating class failed: no ID returned.");
    }

    public SchoolClass findById(int classId) throws SQLException {
        String sql = "SELECT class_id, class_name, description, created_by FROM Classes WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<SchoolClass> findByTeacher(int teacherId) throws SQLException {
        String sql = "SELECT class_id, class_name, description, created_by FROM Classes WHERE created_by = ? ORDER BY class_id DESC";
        List<SchoolClass> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<SchoolClass> findAll() throws SQLException {
        String sql = "SELECT class_id, class_name, description, created_by FROM Classes ORDER BY class_id DESC";
        List<SchoolClass> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public boolean update(SchoolClass schoolClass) throws SQLException {
        String sql = "UPDATE Classes SET class_name = ?, description = ? WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schoolClass.getClassName());
            ps.setString(2, schoolClass.getDescription());
            ps.setInt(3, schoolClass.getClassId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int classId) throws SQLException {
        String sql = "DELETE FROM Classes WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            return ps.executeUpdate() > 0;
        }
    }

    private SchoolClass mapRow(ResultSet rs) throws SQLException {
        return new SchoolClass(rs.getInt("class_id"), rs.getString("class_name"),
                rs.getString("description"), rs.getInt("created_by"));
    }
}
