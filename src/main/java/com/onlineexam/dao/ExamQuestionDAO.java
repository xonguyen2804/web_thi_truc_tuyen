package com.onlineexam.dao;

import com.onlineexam.model.ExamQuestion;
import com.onlineexam.model.Option;
import com.onlineexam.model.Question;
import com.onlineexam.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho bảng exam_questions (liên kết đề thi và câu hỏi).
 */
public class ExamQuestionDAO {

    private Connection connection;

    public ExamQuestionDAO() {
    }

    public ExamQuestionDAO(Connection connection) {
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

    // 1. Thêm một câu hỏi vào đề thi
    public boolean insert(ExamQuestion eq) throws SQLException {
        String sql = "INSERT INTO exam_questions (exam_id, question_id, points, display_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eq.getExamId());
            ps.setInt(2, eq.getQuestionId());
            ps.setBigDecimal(3, eq.getPoints() != null ? eq.getPoints() : new BigDecimal("0.25"));
            ps.setInt(4, eq.getDisplayOrder());

            return ps.executeUpdate() > 0;
        }
    }

    // 2. Thêm danh sách câu hỏi vào đề thi theo đợt (Batch Insert)
    public boolean insertBatch(int examId, List<Integer> questionIds, BigDecimal pointsPerQuestion) throws SQLException {
        if (questionIds == null || questionIds.isEmpty()) return false;

        String maxOrderSql = "SELECT COALESCE(MAX(display_order), 0) FROM exam_questions WHERE exam_id = ?";
        String insertSql = "INSERT INTO exam_questions (exam_id, question_id, points, display_order) VALUES (?, ?, ?, ?)";

        try (Connection conn = getActiveConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int startOrder = 0;
                try (PreparedStatement psOrder = conn.prepareStatement(maxOrderSql)) {
                    psOrder.setInt(1, examId);
                    try (ResultSet rs = psOrder.executeQuery()) {
                        if (rs.next()) startOrder = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int qId : questionIds) {
                        startOrder++;
                        ps.setInt(1, examId);
                        ps.setInt(2, qId);
                        ps.setBigDecimal(3, pointsPerQuestion != null ? pointsPerQuestion : new BigDecimal("0.25"));
                        ps.setInt(4, startOrder);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    // 3. Lấy danh sách liên kết ExamQuestion theo exam_id
    public List<ExamQuestion> findByExamId(int examId) throws SQLException {
        List<ExamQuestion> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_questions WHERE exam_id = ? ORDER BY display_order ASC";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamQuestion eq = new ExamQuestion();
                    eq.setExamId(rs.getInt("exam_id"));
                    eq.setQuestionId(rs.getInt("question_id"));
                    eq.setPoints(rs.getBigDecimal("points"));
                    eq.setDisplayOrder(rs.getInt("display_order"));
                    list.add(eq);
                }
            }
        }
        return list;
    }

    // 4. Lấy danh sách chi tiết các Question trong đề thi kèm theo các Options của từng câu
    public List<Question> findQuestionsByExamId(int examId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, eq.points, eq.display_order " +
                     "FROM exam_questions eq " +
                     "JOIN questions q ON eq.question_id = q.question_id " +
                     "WHERE eq.exam_id = ? " +
                     "ORDER BY eq.display_order ASC";

        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("question_id"));
                    q.setCreatedBy(rs.getInt("created_by"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setQuestionType(rs.getString("question_type"));
                    q.setDefaultPoints(rs.getBigDecimal("points")); // lấy điểm được cấu hình riêng trong đề thi
                    q.setExplanation(rs.getString("explanation"));
                    q.setStatus(rs.getString("status"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        q.setCreatedAt(ts.toLocalDateTime());
                    }

                    // Tải các lựa chọn (options) hoặc đáp án ngắn của câu hỏi
                    q.setOptions(loadOptionsForQuestion(q.getQuestionId(), conn));
                    q.setShortAnswer(loadExpectedAnswerForQuestion(q.getQuestionId(), conn));

                    questions.add(q);
                }
            }
        }
        return questions;
    }

    // 5. Cập nhật số điểm của câu hỏi trong đề thi
    public boolean updatePoints(int examId, int questionId, BigDecimal points) throws SQLException {
        String sql = "UPDATE exam_questions SET points = ? WHERE exam_id = ? AND question_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, points);
            ps.setInt(2, examId);
            ps.setInt(3, questionId);

            return ps.executeUpdate() > 0;
        }
    }

    // 6. Xóa một câu hỏi ra khỏi đề thi
    public boolean delete(int examId, int questionId) throws SQLException {
        String sql = "DELETE FROM exam_questions WHERE exam_id = ? AND question_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            ps.setInt(2, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    // 7. Xóa tất cả câu hỏi của đề thi
    public boolean deleteByExamId(int examId) throws SQLException {
        String sql = "DELETE FROM exam_questions WHERE exam_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        }
    }

    // 8. Đếm tổng số câu hỏi trong đề
    public int countByExamId(int examId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exam_questions WHERE exam_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // --- Helper methods tải options và expected_answers nội bộ ---
    private List<Option> loadOptionsForQuestion(int questionId, Connection conn) throws SQLException {
        List<Option> list = new ArrayList<>();
        String sql = "SELECT * FROM options WHERE question_id = ? ORDER BY display_order ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Option opt = new Option();
                    opt.setOptionId(rs.getInt("option_id"));
                    opt.setQuestionId(rs.getInt("question_id"));
                    opt.setOptionText(rs.getString("option_text"));
                    opt.setCorrect(rs.getBoolean("is_correct"));
                    opt.setDisplayOrder(rs.getInt("display_order"));
                    list.add(opt);
                }
            }
        }
        return list;
    }

    private String loadExpectedAnswerForQuestion(int questionId, Connection conn) throws SQLException {
        String sql = "SELECT answer_text FROM expected_answers WHERE question_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("answer_text");
                }
            }
        }
        return null;
    }
}