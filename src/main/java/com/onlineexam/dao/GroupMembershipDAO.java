// package com.onlineexam.dao;

// import com.onlineexam.model.GroupMembership;
// import com.onlineexam.util.DBConnection;

// import java.sql.*;
// import java.util.ArrayList;
// import java.util.List;

// public class GroupMembershipDAO {
//     public boolean add(int groupId, int userId) throws SQLException {
//         String sql = "INSERT INTO Group_Memberships (group_id, user_id) VALUES (?, ?)";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, groupId);
//             ps.setInt(2, userId);
//             return ps.executeUpdate() > 0;
//         }
//     }

//     public List<GroupMembership> findByGroup(int groupId) throws SQLException {
//         String sql = "SELECT group_id, user_id, joined_at FROM Group_Memberships WHERE group_id = ? ORDER BY joined_at";
//         List<GroupMembership> result = new ArrayList<>();
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, groupId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     Timestamp ts = rs.getTimestamp("joined_at");
//                     result.add(new GroupMembership(rs.getInt("group_id"), rs.getInt("user_id"),
//                             ts == null ? null : ts.toLocalDateTime()));
//                 }
//             }
//         }
//         return result;
//     }

//     public boolean remove(int groupId, int userId) throws SQLException {
//         String sql = "DELETE FROM Group_Memberships WHERE group_id = ? AND user_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, groupId);
//             ps.setInt(2, userId);
//             return ps.executeUpdate() > 0;
//         }
//     }
// }


package com.onlineexam.dao;

import com.onlineexam.model.GroupMembership;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMembershipDAO {

    // DTO phục vụ hiển thị thành viên của nhóm
    public static class GroupMemberDTO {
        private int userId;
        private String username;
        private String fullName;
        private String email;

        public GroupMemberDTO(int userId, String username,
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

    /*
     * Thêm học sinh vào nhóm bằng username.
     *
     * Kết quả:
     * ADDED
     * NOT_FOUND
     * NOT_STUDENT
     * INACTIVE
     * GROUP_NOT_FOUND
     * NOT_IN_CLASS
     * ALREADY_IN_GROUP
     */
    public String addStudentToGroup(int classId, int groupId,
                                    String username)
            throws SQLException {

        String findUserSql = """
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

        String checkGroupSql = """
            SELECT 1
            FROM `Groups`
            WHERE group_id = ? AND class_id = ?
            """;

        String checkClassMemberSql = """
            SELECT 1
            FROM Class_Memberships
            WHERE class_id = ? AND user_id = ?
            """;

        String checkOtherGroupSql = """
            SELECT 1
            FROM Group_Memberships gm
            JOIN `Groups` g ON g.group_id = gm.group_id
            WHERE g.class_id = ?
              AND gm.user_id = ?
            """;

        String insertSql = """
            INSERT INTO Group_Memberships (group_id, user_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                int userId;
                String status;
                boolean isStudent;

                // 1. Tìm tài khoản học sinh
                try (PreparedStatement ps =
                             conn.prepareStatement(findUserSql)) {

                    ps.setString(1, username);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "NOT_FOUND";
                        }

                        userId = rs.getInt("user_id");
                        status = rs.getString("status");
                        isStudent = rs.getInt("is_student") == 1;
                    }
                }

                if (!isStudent) {
                    conn.rollback();
                    return "NOT_STUDENT";
                }

                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    conn.rollback();
                    return "INACTIVE";
                }

                // 2. Kiểm tra nhóm có thuộc lớp đang chọn không
                try (PreparedStatement ps =
                             conn.prepareStatement(checkGroupSql)) {

                    ps.setInt(1, groupId);
                    ps.setInt(2, classId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "GROUP_NOT_FOUND";
                        }
                    }
                }

                // 3. Học sinh phải là thành viên của lớp
                try (PreparedStatement ps =
                             conn.prepareStatement(checkClassMemberSql)) {

                    ps.setInt(1, classId);
                    ps.setInt(2, userId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "NOT_IN_CLASS";
                        }
                    }
                }

                // 4. Không cho học sinh thuộc hai nhóm trong cùng lớp
                try (PreparedStatement ps =
                             conn.prepareStatement(checkOtherGroupSql)) {

                    ps.setInt(1, classId);
                    ps.setInt(2, userId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            conn.rollback();
                            return "ALREADY_IN_GROUP";
                        }
                    }
                }

                // 5. Thêm thành viên vào nhóm
                try (PreparedStatement ps =
                             conn.prepareStatement(insertSql)) {

                    ps.setInt(1, groupId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                return "ADDED";

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Lấy danh sách thành viên của một nhóm
    public List<GroupMemberDTO> findMembers(int groupId)
            throws SQLException {

        List<GroupMemberDTO> members = new ArrayList<>();

        String sql = """
            SELECT u.user_id,
                   u.username,
                   u.full_name,
                   u.email
            FROM Group_Memberships gm
            JOIN users u ON u.user_id = gm.user_id
            WHERE gm.group_id = ?
            ORDER BY u.full_name ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new GroupMemberDTO(
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

    // Giữ lại phương thức thêm thành viên theo userId
    public boolean add(int groupId, int userId)
            throws SQLException {

        String sql = """
            INSERT INTO Group_Memberships (group_id, user_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // Giữ lại phương thức lấy danh sách membership
    public List<GroupMembership> findByGroup(int groupId)
            throws SQLException {

        String sql = """
            SELECT group_id, user_id, joined_at
            FROM Group_Memberships
            WHERE group_id = ?
            ORDER BY joined_at
            """;

        List<GroupMembership> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("joined_at");

                    result.add(new GroupMembership(
                            rs.getInt("group_id"),
                            rs.getInt("user_id"),
                            ts == null ? null : ts.toLocalDateTime()
                    ));
                }
            }
        }

        return result;
    }

    // Xóa học sinh khỏi nhóm
    public boolean remove(int groupId, int userId)
            throws SQLException {

        String sql = """
            DELETE FROM Group_Memberships
            WHERE group_id = ? AND user_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }
}
