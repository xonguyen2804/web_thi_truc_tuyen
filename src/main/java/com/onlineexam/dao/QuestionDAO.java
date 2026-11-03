// package com.onlineexam.dao;

// import com.onlineexam.model.Question;
// import com.onlineexam.util.DBConnection;

// import java.sql.*;
// import java.util.ArrayList;
// import java.util.List;

// public class QuestionDAO {
//     public int create(Question question) throws SQLException {
//         String sql = "INSERT INTO Questions (created_by, question_text, question_type, default_points, explanation, status) VALUES (?, ?, ?, ?, ?, ?)";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//             ps.setInt(1, question.getCreatedBy());
//             ps.setString(2, question.getQuestionText());
//             ps.setString(3, question.getQuestionType());
//             ps.setBigDecimal(4, question.getDefaultPoints());
//             ps.setString(5, question.getExplanation());
//             ps.setString(6, question.getStatus());
//             ps.executeUpdate();
//             try (ResultSet rs = ps.getGeneratedKeys()) {
//                 if (rs.next()) return rs.getInt(1);
//             }
//         }
//         throw new SQLException("Creating question failed: no ID returned.");
//     }

//     public Question findById(int questionId) throws SQLException {
//         String sql = "SELECT * FROM Questions WHERE question_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, questionId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 return rs.next() ? mapRow(rs) : null;
//             }
//         }
//     }

//     public List<Question> findByCreator(int createdBy) throws SQLException {
//         String sql = "SELECT * FROM Questions WHERE created_by = ? ORDER BY question_id DESC";
//         List<Question> result = new ArrayList<>();
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, createdBy);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) result.add(mapRow(rs));
//             }
//         }
//         return result;
//     }

//     public List<Question> findAllActive() throws SQLException {
//         String sql = "SELECT * FROM Questions WHERE status = 'ACTIVE' ORDER BY question_id DESC";
//         List<Question> result = new ArrayList<>();
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql);
//              ResultSet rs = ps.executeQuery()) {
//             while (rs.next()) result.add(mapRow(rs));
//         }
//         return result;
//     }

//     public boolean update(Question question) throws SQLException {
//         String sql = "UPDATE Questions SET question_text = ?, question_type = ?, default_points = ?, explanation = ?, status = ? WHERE question_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setString(1, question.getQuestionText());
//             ps.setString(2, question.getQuestionType());
//             ps.setBigDecimal(3, question.getDefaultPoints());
//             ps.setString(4, question.getExplanation());
//             ps.setString(5, question.getStatus());
//             ps.setInt(6, question.getQuestionId());
//             return ps.executeUpdate() > 0;
//         }
//     }

//     public boolean deactivate(int questionId) throws SQLException {
//         String sql = "UPDATE Questions SET status = 'INACTIVE' WHERE question_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, questionId);
//             return ps.executeUpdate() > 0;
//         }
//     }

//     private Question mapRow(ResultSet rs) throws SQLException {
//         Timestamp ts = rs.getTimestamp("created_at");
//         return new Question(rs.getInt("question_id"), rs.getInt("created_by"),
//                 rs.getString("question_text"), rs.getString("question_type"),
//                 rs.getBigDecimal("default_points"), rs.getString("explanation"),
//                 rs.getString("status"), ts == null ? null : ts.toLocalDateTime());
//     }
// }

package com.onlineexam.dao;

import com.onlineexam.model.Question;
import com.onlineexam.model.Option;
import com.onlineexam.model.Exam;
import com.onlineexam.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class QuestionDAO {

    // ==================== CREATE ====================

    public int create(Question question) throws SQLException {
        String sql = """
            INSERT INTO Questions
            (created_by, question_text, question_type, default_points, explanation, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, question.getCreatedBy());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getQuestionType());
            ps.setBigDecimal(4, question.getDefaultPoints());
            ps.setString(5, question.getExplanation());
            ps.setString(6, question.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Không thể tạo câu hỏi.");
    }

    // ==================== FIND BY ID ====================

    public Question findById(int questionId) throws SQLException {
        String sql = "SELECT * FROM Questions WHERE question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Question question = mapRow(rs);
                loadQuestionDetails(conn, question);
                return question;
            }
        }
    }

    // ==================== FIND FILTERED ====================

    public List<Question> findFiltered(
            int teacherId,
            String keyword,
            String type,
            Integer examId) throws SQLException {

        List<Question> questions = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT q.*
            FROM Questions q
            """);

        if (examId != null) {
            sql.append("""
                JOIN Exam_Questions eq
                ON eq.question_id = q.question_id
                """);
        }

        sql.append(" WHERE q.created_by = ? ");

        List<Object> params = new ArrayList<>();
        params.add(teacherId);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND q.question_text LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }

        if (type != null && !type.isBlank()
                && !"ALL".equalsIgnoreCase(type)) {
            sql.append(" AND q.question_type = ? ");
            params.add(type);
        }

        if (examId != null) {
            sql.append(" AND eq.exam_id = ? ");
            params.add(examId);
        }

        sql.append(" ORDER BY q.question_id DESC ");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question question = mapRow(rs);
                    loadQuestionDetails(conn, question);
                    questions.add(question);
                }
            }
        }

        return questions;
    }

    // ==================== FIND ALL BY TEACHER ====================

    public List<Question> findByCreator(int teacherId)
            throws SQLException {
        return findFiltered(teacherId, null, null, null);
    }

    // ==================== LOAD OPTIONS / ANSWERS ====================

    private void loadQuestionDetails(
            Connection conn,
            Question question) throws SQLException {

        question.setOptions(
                loadOptions(conn, question.getQuestionId())
        );

        question.setShortAnswer(
                loadExpectedAnswer(conn, question.getQuestionId())
        );

        question.setLinkedExamTitles(
                loadLinkedExams(conn, question.getQuestionId())
        );
    }

    private List<Option> loadOptions(
            Connection conn,
            int questionId) throws SQLException {

        List<Option> options = new ArrayList<>();

        String sql = """
            SELECT option_id, question_id, option_text,
                   is_correct, display_order
            FROM Options
            WHERE question_id = ?
            ORDER BY display_order ASC, option_id ASC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Option option = new Option(
                            rs.getInt("option_id"),
                            rs.getInt("question_id"),
                            rs.getString("option_text"),
                            rs.getBoolean("is_correct"),
                            rs.getInt("display_order")
                    );

                    options.add(option);
                }
            }
        }

        return options;
    }

    private String loadExpectedAnswer(
            Connection conn,
            int questionId) throws SQLException {

        String sql = """
            SELECT answer_text
            FROM Expected_Answers
            WHERE question_id = ?
            LIMIT 1
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("answer_text") : null;
            }
        }
    }

    private List<String> loadLinkedExams(
            Connection conn,
            int questionId) throws SQLException {

        List<String> titles = new ArrayList<>();

        String sql = """
            SELECT e.exam_title
            FROM Exam_Questions eq
            JOIN Exams e ON e.exam_id = eq.exam_id
            WHERE eq.question_id = ?
            ORDER BY e.created_at DESC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString("exam_title"));
                }
            }
        }

        return titles;
    }

    // ==================== UPDATE QUESTION + ANSWERS ====================

    public boolean update(
            Question question,
            List<Option> options,
            String shortAnswer) throws SQLException {

        String updateQuestionSql = """
            UPDATE Questions
            SET question_text = ?,
                default_points = ?,
                explanation = ?
            WHERE question_id = ?
            """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int affected;

                try (PreparedStatement ps =
                             conn.prepareStatement(updateQuestionSql)) {

                    ps.setString(1, question.getQuestionText());
                    ps.setBigDecimal(2, question.getDefaultPoints());
                    ps.setString(3, question.getExplanation());
                    ps.setInt(4, question.getQuestionId());

                    affected = ps.executeUpdate();
                }

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                int questionId = question.getQuestionId();
                String type = question.getQuestionType();

                if ("SINGLE".equals(type)
                        || "TRUE_FALSE".equals(type)) {

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM Options WHERE question_id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }

                    String insertOptionSql = """
                        INSERT INTO Options
                        (question_id, option_text, is_correct, display_order)
                        VALUES (?, ?, ?, ?)
                        """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(insertOptionSql)) {

                        int order = 1;

                        for (Option option : options) {
                            ps.setInt(1, questionId);
                            ps.setString(2, option.getOptionText());
                            ps.setBoolean(3, option.isCorrect());
                            ps.setInt(4, order++);
                            ps.addBatch();
                        }

                        ps.executeBatch();
                    }

                    // Nếu đổi loại câu hỏi sang trắc nghiệm,
                    // xóa đáp án trả lời ngắn cũ.
                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM Expected_Answers WHERE question_id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }

                } else if ("SHORT_ANSWER".equals(type)) {

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM Options WHERE question_id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM Expected_Answers WHERE question_id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }

                    if (shortAnswer != null && !shortAnswer.isBlank()) {
                        String insertAnswerSql = """
                            INSERT INTO Expected_Answers
                            (question_id, answer_text)
                            VALUES (?, ?)
                            """;

                        try (PreparedStatement ps =
                                     conn.prepareStatement(insertAnswerSql)) {
                            ps.setInt(1, questionId);
                            ps.setString(2, shortAnswer.trim());
                            ps.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==================== STATISTICS ====================

    public Map<String, Integer> getStatistics(int teacherId)
            throws SQLException {

        Map<String, Integer> stats = new HashMap<>();

        stats.put("total", 0);
        stats.put("single", 0);
        stats.put("true_false", 0);
        stats.put("short_answer", 0);

        String sql = """
            SELECT question_type, COUNT(*) AS total
            FROM Questions
            WHERE created_by = ?
            GROUP BY question_type
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("question_type");
                    int count = rs.getInt("total");

                    stats.put("total", stats.get("total") + count);

                    if ("SINGLE".equals(type)) {
                        stats.put("single", count);
                    } else if ("TRUE_FALSE".equals(type)) {
                        stats.put("true_false", count);
                    } else if ("SHORT_ANSWER".equals(type)) {
                        stats.put("short_answer", count);
                    }
                }
            }
        }

        return stats;
    }

    // ==================== EXAMS ====================

    public List<Exam> findExamsByTeacher(int teacherId)
            throws SQLException {

        List<Exam> exams = new ArrayList<>();

        String sql = """
            SELECT *
            FROM Exams
            WHERE created_by = ?
            ORDER BY created_at DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exam exam = new Exam();
                    exam.setExamId(rs.getInt("exam_id"));
                    exam.setCreatedBy(rs.getInt("created_by"));
                    exam.setExamTitle(rs.getString("exam_title"));
                    exam.setDescription(rs.getString("description"));
                    exam.setDurationMinutes(
                            rs.getInt("duration_minutes"));
                    exam.setPassScore(rs.getBigDecimal("pass_score"));
                    exam.setStatus(rs.getString("status"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        exam.setCreatedAt(ts.toLocalDateTime());
                    }

                    exams.add(exam);
                }
            }
        }

        return exams;
    }

    // ==================== DEACTIVATE ====================

    public boolean deactivate(int questionId) throws SQLException {
        String sql = """
            UPDATE Questions
            SET status = 'INACTIVE'
            WHERE question_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    // ==================== MAPPING ====================

    private Question mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");

        return new Question(
                rs.getInt("question_id"),
                rs.getInt("created_by"),
                rs.getString("question_text"),
                rs.getString("question_type"),
                rs.getBigDecimal("default_points"),
                rs.getString("explanation"),
                rs.getString("status"),
                ts == null ? null : ts.toLocalDateTime()
        );
    }
}