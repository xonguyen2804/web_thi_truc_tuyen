package com.onlineexam.dao;

import com.onlineexam.model.ClassMembership;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassMembershipDAO {

    // DTO dùng để hiển thị thông tin học sinh trong lớp
    public static class MemberDTO {
        private int userId;
        private String username;
        private String fullName;
        private String email;

        public MemberDTO(int userId, String username,
                         String fullName, String email) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }
    }

    // Lấy danh sách học sinh trong lớp
    public List<MemberDTO> findMembers(int classId)
            throws SQLException {

        List<MemberDTO> members = new ArrayList<>();

        String sql = """
            SELECT u.user_id,
                   u.username,
                   u.full_name,
                   u.email
            FROM Class_Memberships cm
            JOIN users u ON cm.user_id = u.user_id
            JOIN user_roles ur ON ur.user_id = u.user_id
            JOIN roles r ON r.role_id = ur.role_id
            WHERE cm.class_id = ?
              AND r.role_name = 'STUDENT'
            ORDER BY u.full_name ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new MemberDTO(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email")
                    ));
                }
            }
        }

        return members;
    }

    // Thêm học sinh vào lớp bằng username
    // Kết quả: ADDED, NOT_FOUND, NOT_STUDENT,
    // INACTIVE, ALREADY_MEMBER
    public String addStudent(int classId, String username)
            throws SQLException {

        String findSql = """
            SELECT u.user_id, u.status,
                   CASE WHEN EXISTS (
                       SELECT 1
                       FROM user_roles ur
                       JOIN roles r ON r.role_id = ur.role_id
                       WHERE ur.user_id = u.user_id
                         AND r.role_name = 'STUDENT'
                   ) THEN 1 ELSE 0 END AS is_student
            FROM users u
            WHERE u.username = ?
            """;

        try (Connection conn = DBConnection.getConnection()) {

            int userId;
            String status;
            boolean isStudent;

            try (PreparedStatement ps =
                         conn.prepareStatement(findSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "NOT_FOUND";
                    }

                    userId = rs.getInt("user_id");
                    status = rs.getString("status");
                    isStudent = rs.getInt("is_student") == 1;
                }
            }

            if (!isStudent) {
                return "NOT_STUDENT";
            }

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                return "INACTIVE";
            }

            if (exists(classId, userId)) {
                return "ALREADY_MEMBER";
            }

            String insertSql = """
                INSERT INTO Class_Memberships (class_id, user_id)
                VALUES (?, ?)
                """;

            try (PreparedStatement ps =
                         conn.prepareStatement(insertSql)) {

                ps.setInt(1, classId);
                ps.setInt(2, userId);

                return ps.executeUpdate() > 0
                        ? "ADDED" : "ERROR";
            }
        }
    }

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