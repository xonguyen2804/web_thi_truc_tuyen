package com.onlineexam.dao;

import com.onlineexam.model.Exam;
import com.onlineexam.util.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho bảng Exams.
 */
public class ExamDAO {

    private Connection connection;

    public ExamDAO() {
    }

    public ExamDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        }
        return DBConnection.getConnection();
    }

    // --- CÁC PHƯƠNG THỨC CRUD CHO EXAMS ---

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

    // --- PHẦN XỬ LÝ NHẬP ĐỀ THI VÀO CSDL ---

    public static class ImportOptionItem {
        public String text;
        public boolean isCorrect;

        public ImportOptionItem(String text, boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }
    }

    /**
     * Đọc Stream file đề thi và lưu câu hỏi vào CSDL theo 1 Transaction hoàn chỉnh.
     */
    public void importQuestionsFromStream(InputStream is, int examId, int createdBy) throws IOException, SQLException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);
            try {
                String line;
                String currentQuestionText = null;
                String currentType = null;
                List<ImportOptionItem> options = new ArrayList<>();
                String shortAnswer = null;
                int displayOrder = 1;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // 1. Nhận diện bắt đầu câu hỏi: "Câu 1: ...", "Câu 2. ..."
                    if (line.matches("^(Câu|Cau)\\s*\\d+[:\\.].*")) {
                        if (currentQuestionText != null) {
                            saveParsedQuestion(conn, examId, createdBy, currentQuestionText, currentType, options,
                                    shortAnswer, displayOrder++);
                            options.clear();
                            shortAnswer = null;
                        }
                        currentQuestionText = line.replaceFirst("^(Câu|Cau)\\s*\\d+[:\\.]\\s*", "").trim();
                        currentType = null;
                        continue;
                    }

                    // 2. Nhận diện lựa chọn SINGLE (A., B., C., D.)
                    if (line.matches("^[A-D][\\.\\)]\\s*.*")) {
                        currentType = "SINGLE";
                        boolean isCorrect = line.endsWith(" *");
                        String cleanText = line.substring(2).replaceAll("\\s*\\*$", "").trim();
                        options.add(new ImportOptionItem(cleanText, isCorrect));
                        continue;
                    }

                    // 3. Nhận diện lựa chọn TRUE_FALSE (Ý 1:, Ý 2:, ...)
                    if (line.matches("^(Ý|Y)\\s*\\d+[:\\.].*")) {
                        currentType = "TRUE_FALSE";
                        boolean isCorrect = line.endsWith(" Đ") || line.endsWith(" đ");
                        String cleanText = line.replaceFirst("^(Ý|Y)\\s*\\d+[:\\.]\\s*", "").replaceAll("\\s+[ĐđSs]$", "").trim();
                        options.add(new ImportOptionItem(cleanText, isCorrect));
                        continue;
                    }

                    // 4. Nhận diện đáp án SHORT_ANSWER
                    if (currentQuestionText != null && options.isEmpty()) {
                        currentType = "SHORT_ANSWER";
                        shortAnswer = line.trim();
                        if (shortAnswer.length() > 4) {
                            shortAnswer = shortAnswer.substring(0, 4);
                        }
                    }
                }

                // Lưu câu hỏi cuối cùng của file
                if (currentQuestionText != null) {
                    saveParsedQuestion(conn, examId, createdBy, currentQuestionText, currentType, options, shortAnswer, displayOrder);
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private void saveParsedQuestion(Connection conn, int examId, int createdBy, String text, String type,
                                    List<ImportOptionItem> options, String shortAnswer, int order) throws SQLException {
        if (type == null) type = "SINGLE";

        // 1. Lưu câu hỏi vào bảng Questions
        String qSql = "INSERT INTO Questions (created_by, question_text, question_type, default_points) VALUES (?, ?, ?, 0.25)";
        int questionId = 0;
        try (PreparedStatement ps = conn.prepareStatement(qSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, createdBy);
            ps.setString(2, text);
            ps.setString(3, type);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) questionId = rs.getInt(1);
            }
        }

        // 2. Lưu các đáp án/lựa chọn
        if ("SINGLE".equals(type) || "TRUE_FALSE".equals(type)) {
            String oSql = "INSERT INTO Options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(oSql)) {
                int optOrder = 1;
                for (ImportOptionItem opt : options) {
                    ps.setInt(1, questionId);
                    ps.setString(2, opt.text);
                    ps.setBoolean(3, opt.isCorrect);
                    ps.setInt(4, optOrder++);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } else if ("SHORT_ANSWER".equals(type) && shortAnswer != null) {
            String aSql = "INSERT INTO Expected_Answers (question_id, answer_text) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(aSql)) {
                ps.setInt(1, questionId);
                ps.setString(2, shortAnswer);
                ps.executeUpdate();
            }
        }

        // 3. Gán vào Exam_Questions
        String eqSql = "INSERT INTO Exam_Questions (exam_id, question_id, points, display_order) VALUES (?, ?, 0.25, ?)";
        try (PreparedStatement ps = conn.prepareStatement(eqSql)) {
            ps.setInt(1, examId);
            ps.setInt(2, questionId);
            ps.setInt(3, order);
            ps.executeUpdate();
        }
    }
}