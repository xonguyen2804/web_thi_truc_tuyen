
package com.onlineexam.dao;

import com.onlineexam.model.Role;
import com.onlineexam.model.User;
import com.onlineexam.service.AuthService;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    // 1. Tìm User theo username
    public User findByUsername(String username) throws SQLException {

        String sql = """
                SELECT u.user_id,
                       u.username,
                       u.password_hash,
                       u.full_name,
                       u.email,
                       u.status,
                       u.created_at,
                       r.role_id,
                       r.role_name
                FROM users u
                JOIN user_roles ur ON u.user_id = ur.user_id
                JOIN roles r ON ur.role_id = r.role_id
                WHERE u.username = ?
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    User user = mapUser(rs);

                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    role.setRoleName(rs.getString("role_name"));

                    user.setRole(role);

                    return user;
                }
            }
        }
        
        return null;
    }

    // 2. Tìm User theo ID
    public Optional<User> findById(int userId)
            throws SQLException {

        String sql = """
                SELECT user_id, username, password_hash,
                       full_name, email, status, created_at
                FROM users
                WHERE user_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    // 3. Lấy danh sách tất cả User
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, username, password_hash,
                       full_name, email, status, created_at
                FROM users
                ORDER BY user_id
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }

        return users;
    }

    // 4. Kiểm tra username đã tồn tại
    public boolean existsByUsername(String username)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE username = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 5. Thêm User mới
    // Trả về ID vừa được MySQL tạo

    public int insertWithRole(User user, int roleId)
            throws SQLException {

        String insertUserSql = """
                INSERT INTO users
                    (username, password_hash, full_name, email, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insertRoleSql = """
                INSERT INTO user_roles (user_id, role_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                int userId;

                // Bước 1: Thêm User
                try (PreparedStatement ps = conn.prepareStatement(
                        insertUserSql,
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getFullName());
                    ps.setString(4, user.getEmail());
                    ps.setString(5, user.getStatus());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Insert User failed");
                    }

                    // Bước 2: Lấy ID của User vừa tạo
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Cannot retrieve generated User ID");
                        }

                        userId = keys.getInt(1);
                    }
                }

                // Bước 3: Gán Role cho User
                try (PreparedStatement ps = conn.prepareStatement(insertRoleSql)) {

                    ps.setInt(1, userId);
                    ps.setInt(2, roleId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Assign Role failed");
                    }
                }

                // Bước 4: Lưu cả hai thao tác
                conn.commit();

                return userId;

            } catch (SQLException | RuntimeException e) {

                // Có lỗi thì hủy toàn bộ giao dịch
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    // 6. Cập nhật thông tin User
    public boolean update(User user) throws SQLException {

        String sql = """
                UPDATE users
                SET username = ?,
                    full_name = ?,
                    email = ?,
                    status = ?
                WHERE user_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    // 7. Cập nhật Password Hash
    public boolean updatePasswordHash(
            int userId, String passwordHash)
            throws SQLException {

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE user_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // 8. Xóa User theo ID
    public boolean deleteById(int userId)
            throws SQLException {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // 9. Chuyển ResultSet thành đối tượng User
    private User mapUser(ResultSet rs) throws SQLException {

        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }

}
