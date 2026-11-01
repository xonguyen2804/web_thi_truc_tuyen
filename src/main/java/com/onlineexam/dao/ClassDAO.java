// package com.onlineexam.dao;

// import com.onlineexam.model.SchoolClass;
// import com.onlineexam.util.DBConnection;

// import java.sql.*;
// import java.util.ArrayList;
// import java.util.List;

// public class ClassDAO {

//     // 1. Lấy tất cả các lớp học
//     public List<SchoolClass> findAll() throws SQLException {
//         List<SchoolClass> list = new ArrayList<>();
//         String sql = "SELECT * FROM classes ORDER BY class_name ASC";

//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql);
//              ResultSet rs = ps.executeQuery()) {

//             while (rs.next()) {
//                 list.add(mapResultSetToClass(rs));
//             }
//         }
//         return list;
//     }

//     // 2. Tìm lớp học theo class_id
//     public SchoolClass findById(int classId) throws SQLException {
//         String sql = "SELECT * FROM classes WHERE class_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, classId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 if (rs.next()) {
//                     return mapResultSetToClass(rs);
//                 }
//             }
//         }
//         return null;
//     }

//     // 3. Lấy danh sách lớp do 1 giáo viên phụ trách (created_by)
//     public List<SchoolClass> findByCreatedBy(int teacherId) throws SQLException {
//         List<SchoolClass> list = new ArrayList<>();
//         String sql = "SELECT * FROM classes WHERE created_by = ? ORDER BY created_at DESC";

//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, teacherId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     list.add(mapResultSetToClass(rs));
//                 }
//             }
//         }
//         return list;
//     }

//     // 4. Thêm một lớp học mới
//     public boolean insert(SchoolClass schoolClass) throws SQLException {
//         String sql = "INSERT INTO classes (class_name, description, created_by) VALUES (?, ?, ?)";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

//             ps.setString(1, schoolClass.getClassName());
//             ps.setString(2, schoolClass.getDescription());
//             if (schoolClass.getCreatedBy() != null && schoolClass.getCreatedBy() > 0) {
//                 ps.setInt(3, schoolClass.getCreatedBy());
//             } else {
//                 ps.setNull(3, Types.INTEGER);
//             }

//             int affected = ps.executeUpdate();
//             if (affected > 0) {
//                 try (ResultSet rs = ps.getGeneratedKeys()) {
//                     if (rs.next()) {
//                         schoolClass.setClassId(rs.getInt(1));
//                     }
//                 }
//                 return true;
//             }
//         }
//         return false;
//     }

//     // 5. Cập nhật thông tin lớp học
//     public boolean update(SchoolClass schoolClass) throws SQLException {
//         String sql = "UPDATE classes SET class_name = ?, description = ? WHERE class_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setString(1, schoolClass.getClassName());
//             ps.setString(2, schoolClass.getDescription());
//             ps.setInt(3, schoolClass.getClassId());

//             return ps.executeUpdate() > 0;
//         }
//     }

//     // 6. Xóa lớp học theo ID
//     public boolean delete(int classId) throws SQLException {
//         String sql = "DELETE FROM classes WHERE class_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, classId);
//             return ps.executeUpdate() > 0;
//         }
//     }

//     // 7. Đếm số lượng học sinh trong một lớp
//     public int getStudentCount(int classId) throws SQLException {
//         String sql = "SELECT COUNT(*) FROM class_memberships WHERE class_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, classId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 if (rs.next()) {
//                     return rs.getInt(1);
//                 }
//             }
//         }
//         return 0;
//     }

//     // Hàm tiện ích map ResultSet thành Object SchoolClass
//     private SchoolClass mapResultSetToClass(ResultSet rs) throws SQLException {
//         SchoolClass c = new SchoolClass();
//         c.setClassId(rs.getInt("class_id"));
//         c.setClassName(rs.getString("class_name"));
//         c.setDescription(rs.getString("description"));
//         c.setCreatedBy((Integer) rs.getObject("created_by"));

//         Timestamp ts = rs.getTimestamp("created_at");
//         if (ts != null) {
//             c.setCreatedAt(ts.toLocalDateTime());
//         }
//         return c;
//     }
// }

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