package com.onlineexam.dao;

import com.onlineexam.model.GroupMembership;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMembershipDAO {
    public boolean add(int groupId, int userId) throws SQLException {
        String sql = "INSERT INTO Group_Memberships (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<GroupMembership> findByGroup(int groupId) throws SQLException {
        String sql = "SELECT group_id, user_id, joined_at FROM Group_Memberships WHERE group_id = ? ORDER BY joined_at";
        List<GroupMembership> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("joined_at");
                    result.add(new GroupMembership(rs.getInt("group_id"), rs.getInt("user_id"),
                            ts == null ? null : ts.toLocalDateTime()));
                }
            }
        }
        return result;
    }

    public boolean remove(int groupId, int userId) throws SQLException {
        String sql = "DELETE FROM Group_Memberships WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}