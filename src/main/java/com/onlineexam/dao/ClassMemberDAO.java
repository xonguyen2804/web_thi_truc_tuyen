
package com.onlineexam.dao;

import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassMemberDAO {

    // DTO hiển thị thông tin thành viên
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

    // 1. Lấy danh sách học sinh trong lớp
    public List<MemberDTO> findMembers(int classId)
            throws SQLException {

        List<MemberDTO> members = new ArrayList<>();

        String sql = """
            SELECT u.user_id,
                   u.username,
                   u.full_name,
                   u.email
            FROM class_memberships cm
            JOIN users u
                ON cm.user_id = u.user_id
            JOIN user_roles ur
                ON ur.user_id = u.user_id
            JOIN roles r
                ON r.role_id = ur.role_id
            WHERE cm.class_id = ?
              AND r.role_name = 'STUDENT'
            ORDER BY u.full_name ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    MemberDTO member = new MemberDTO(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email")
                    );

                    members.add(member);
                }
            }
        }

        return members;
    }

    // 2. Thêm học sinh vào lớp
    // Kết quả: ADDED, NOT_FOUND, NOT_STUDENT,
    // INACTIVE, ALREADY_MEMBER
    public String addStudent(int classId, String username)
            throws SQLException {

        String findSql = """
            SELECT u.user_id, u.status
            FROM users u
            WHERE u.username = ?
              AND EXISTS (
                  SELECT 1
                  FROM user_roles ur
                  JOIN roles r ON r.role_id = ur.role_id
                  WHERE ur.user_id = u.user_id
                    AND r.role_name = 'STUDENT'
              )
            """;

        try (Connection conn = DBConnection.getConnection()) {

            int userId;
            String status;

            // Tìm tài khoản có username và vai trò STUDENT
            try (PreparedStatement ps =
                     conn.prepareStatement(findSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return "NOT_FOUND";
                    }

                    userId = rs.getInt("user_id");
                    status = rs.getString("status");
                }
            }

            // Kiểm tra tài khoản có hoạt động không
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                return "INACTIVE";
            }

            // Kiểm tra thành viên đã tồn tại chưa
            String checkSql = """
                SELECT 1
                FROM class_memberships
                WHERE class_id = ? AND user_id = ?
                """;

            try (PreparedStatement ps =
                     conn.prepareStatement(checkSql)) {

                ps.setInt(1, classId);
                ps.setInt(2, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "ALREADY_MEMBER";
                    }
                }
            }

            // Thêm thành viên
            String insertSql = """
                INSERT INTO class_memberships
                    (class_id, user_id)
                VALUES (?, ?)
                """;

            try (PreparedStatement ps =
                     conn.prepareStatement(insertSql)) {

                ps.setInt(1, classId);
                ps.setInt(2, userId);

                return ps.executeUpdate() > 0
                        ? "ADDED"
                        : "ERROR";
            }
        }
    }
}
