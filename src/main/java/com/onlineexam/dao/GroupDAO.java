package com.onlineexam.dao;

import com.onlineexam.model.Group;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
    public int create(Group group) throws SQLException {
        String sql = "INSERT INTO `Groups` (class_id, group_name, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, group.getClassId());
            ps.setString(2, group.getGroupName());
            ps.setString(3, group.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating group failed: no ID returned.");
    }

    public Group findById(int groupId) throws SQLException {
        String sql = "SELECT group_id, class_id, group_name, description FROM `Groups` WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Group> findByClass(int classId) throws SQLException {
        String sql = "SELECT group_id, class_id, group_name, description FROM `Groups` WHERE class_id = ? ORDER BY group_id";
        List<Group> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public boolean update(Group group) throws SQLException {
        String sql = "UPDATE `Groups` SET group_name = ?, description = ? WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.getGroupName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.getGroupId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int groupId) throws SQLException {
        String sql = "DELETE FROM `Groups` WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            return ps.executeUpdate() > 0;
        }
    }

    private Group mapRow(ResultSet rs) throws SQLException {
        return new Group(rs.getInt("group_id"), rs.getInt("class_id"),
                rs.getString("group_name"), rs.getString("description"));
    }
}
