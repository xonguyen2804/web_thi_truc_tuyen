package com.onlineexam.dao;

import com.onlineexam.model.Exam;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho bảng Exams.
 */
public class ExamDAO {

    // 1. Thuộc tính (Tùy chọn connection nếu muốn truyền từ ngoài, ví dụ transaction)
    private Connection connection;

    // 2. Constructors
    public ExamDAO() {
    }

    public ExamDAO(Connection connection) {
        this.connection = connection;
    }

    // 3. Getter và Setter
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Phương thức helper lấy connection
    private Connection getActiveConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        }
        return DBConnection.getConnection();
    }

    // 4. Các phương thức CRUD

    public boolean insert(Exam exam) throws SQLException {
        String sql = "INSERT INTO Exams (created_by, exam_title, description, duration_minutes, pass_score, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, exam.getCreatedBy());
            ps.setString(2, exam.getExamTitle());
            ps.setString(3, exam.getDescription());
            ps.setInt(4, exam.getDurationMinutes());
            ps.setBigDecimal(5, exam.getPassScore());
            ps.setString(6, exam.getStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        exam.setExamId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Exam findById(int examId) throws SQLException {
        String sql = "SELECT * FROM Exams WHERE exam_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs);
                }
            }
        }
        return null;
    }

    public List<Exam> findAll() throws SQLException {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT * FROM Exams ORDER BY created_at DESC";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToExam(rs));
            }
        }
        return list;
    }

    public boolean update(Exam exam) throws SQLException {
        String sql = "UPDATE Exams SET exam_title = ?, description = ?, duration_minutes = ?, " +
                     "pass_score = ?, status = ? WHERE exam_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, exam.getExamTitle());
            ps.setString(2, exam.getDescription());
            ps.setInt(3, exam.getDurationMinutes());
            ps.setBigDecimal(4, exam.getPassScore());
            ps.setString(5, exam.getStatus());
            ps.setInt(6, exam.getExamId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int examId) throws SQLException {
        String sql = "DELETE FROM Exams WHERE exam_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        }
    }

    private Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setCreatedBy(rs.getInt("created_by"));
        exam.setExamTitle(rs.getString("exam_title"));
        exam.setDescription(rs.getString("description"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setPassScore(rs.getBigDecimal("pass_score"));
        exam.setStatus(rs.getString("status"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            exam.setCreatedAt(ts.toLocalDateTime());
        }
        return exam;
    }
}