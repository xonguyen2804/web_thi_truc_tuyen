// package com.onlineexam.dao;

// import com.onlineexam.model.Exam;
// import com.onlineexam.model.ExpectedAnswer;
// import com.onlineexam.model.Option;
// import com.onlineexam.model.Question;
// import com.onlineexam.util.DBConnection;

// import java.math.BigDecimal;
// import java.sql.*;
// import java.util.*;

// /**
//  * Tầng truy xuất dữ liệu cho Ngân hàng câu hỏi (Questions, Options, Expected_Answers, Exam_Questions).
//  */
// public class QuestionDAO {

//     public List<Question> findFiltered(String keyword, String type, Integer examId) throws SQLException {
//         List<Question> questions = new ArrayList<>();

//         StringBuilder sql = new StringBuilder();
//         sql.append("SELECT DISTINCT q.question_id, q.created_by, q.question_text, q.question_type, ")
//            .append("q.default_points, q.explanation, q.status, q.created_at ")
//            .append("FROM questions q ");

//         if (examId != null && examId > 0) {
//             sql.append("JOIN exam_questions eq ON q.question_id = eq.question_id ");
//         }

//         sql.append("WHERE 1=1 ");

//         List<Object> params = new ArrayList<>();

//         if (examId != null && examId > 0) {
//             sql.append("AND eq.exam_id = ? ");
//             params.add(examId);
//         }

//         if (keyword != null && !keyword.trim().isEmpty()) {
//             sql.append("AND q.question_text LIKE ? ");
//             params.add("%" + keyword.trim() + "%");
//         }

//         if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim())) {
//             sql.append("AND q.question_type = ? ");
//             params.add(type.trim());
//         }

//         sql.append("ORDER BY q.question_id DESC");

//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql.toString())) {

//             for (int i = 0; i < params.size(); i++) {
//                 ps.setObject(i + 1, params.get(i));
//             }

//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     Question q = mapResultSetToQuestion(rs);
//                     q.setOptions(loadOptionsForQuestion(q.getQuestionId(), conn));
//                     q.setShortAnswer(loadExpectedAnswerForQuestion(q.getQuestionId(), conn));
//                     q.setLinkedExamTitles(loadLinkedExams(q.getQuestionId(), conn));
//                     questions.add(q);
//                 }
//             }
//         }

//         return questions;
//     }

//     public Question findById(int questionId) throws SQLException {
//         String sql = "SELECT * FROM questions WHERE question_id = ?";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, questionId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 if (rs.next()) {
//                     Question q = mapResultSetToQuestion(rs);
//                     q.setOptions(loadOptionsForQuestion(questionId, conn));
//                     q.setShortAnswer(loadExpectedAnswerForQuestion(questionId, conn));
//                     q.setLinkedExamTitles(loadLinkedExams(questionId, conn));
//                     return q;
//                 }
//             }
//         }
//         return null;
//     }

//     public int insert(Question q, List<Option> options, String shortAnswer) throws SQLException {
//         String insertQSql = "INSERT INTO questions (created_by, question_text, question_type, default_points, explanation, status) " +
//                             "VALUES (?, ?, ?, ?, ?, ?)";
//         try (Connection conn = DBConnection.getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 int questionId = 0;
//                 try (PreparedStatement psQ = conn.prepareStatement(insertQSql, Statement.RETURN_GENERATED_KEYS)) {
//                     psQ.setInt(1, q.getCreatedBy());
//                     psQ.setString(2, q.getQuestionText());
//                     psQ.setString(3, q.getQuestionType());
//                     psQ.setBigDecimal(4, q.getDefaultPoints() != null ? q.getDefaultPoints() : new BigDecimal("0.25"));
//                     psQ.setString(5, q.getExplanation());
//                     psQ.setString(6, q.getStatus() != null ? q.getStatus() : "ACTIVE");
//                     psQ.executeUpdate();

//                     try (ResultSet rs = psQ.getGeneratedKeys()) {
//                         if (rs.next()) {
//                             questionId = rs.getInt(1);
//                             q.setQuestionId(questionId);
//                         }
//                     }
//                 }

//                 if (questionId > 0) {
//                     if (("SINGLE".equals(q.getQuestionType()) || "TRUE_FALSE".equals(q.getQuestionType()) || "MULTIPLE".equals(q.getQuestionType())) 
//                             && options != null && !options.isEmpty()) {
//                         String optSql = "INSERT INTO options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
//                         try (PreparedStatement psOpt = conn.prepareStatement(optSql)) {
//                             int order = 1;
//                             for (Option opt : options) {
//                                 psOpt.setInt(1, questionId);
//                                 psOpt.setString(2, opt.getOptionText());
//                                 psOpt.setBoolean(3, opt.isCorrect());
//                                 psOpt.setInt(4, opt.getDisplayOrder() > 0 ? opt.getDisplayOrder() : order++);
//                                 psOpt.addBatch();
//                             }
//                             psOpt.executeBatch();
//                         }
//                     } else if ("SHORT_ANSWER".equals(q.getQuestionType()) && shortAnswer != null && !shortAnswer.isBlank()) {
//                         String expSql = "INSERT INTO expected_answers (question_id, answer_text) VALUES (?, ?)";
//                         try (PreparedStatement psExp = conn.prepareStatement(expSql)) {
//                             psExp.setInt(1, questionId);
//                             psExp.setString(2, shortAnswer.trim());
//                             psExp.executeUpdate();
//                         }
//                     }
//                 }

//                 conn.commit();
//                 return questionId;
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         }
//     }

//     public boolean update(Question q, List<Option> options, String shortAnswer) throws SQLException {
//         String updateQSql = "UPDATE questions SET question_text = ?, default_points = ?, explanation = ? WHERE question_id = ?";
//         try (Connection conn = DBConnection.getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 try (PreparedStatement ps = conn.prepareStatement(updateQSql)) {
//                     ps.setString(1, q.getQuestionText());
//                     ps.setBigDecimal(2, q.getDefaultPoints());
//                     ps.setString(3, q.getExplanation());
//                     ps.setInt(4, q.getQuestionId());
//                     ps.executeUpdate();
//                 }

//                 // Cập nhật các lựa chọn đáp án
//                 if ("SINGLE".equals(q.getQuestionType()) || "TRUE_FALSE".equals(q.getQuestionType()) || "MULTIPLE".equals(q.getQuestionType())) {
//                     if (options != null && !options.isEmpty()) {
//                         // Xóa các options cũ
//                         String delOptSql = "DELETE FROM options WHERE question_id = ?";
//                         try (PreparedStatement psDel = conn.prepareStatement(delOptSql)) {
//                             psDel.setInt(1, q.getQuestionId());
//                             psDel.executeUpdate();
//                         }

//                         // Thêm lại các options mới
//                         String insertOptSql = "INSERT INTO options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
//                         try (PreparedStatement psOpt = conn.prepareStatement(insertOptSql)) {
//                             int order = 1;
//                             for (Option opt : options) {
//                                 psOpt.setInt(1, q.getQuestionId());
//                                 psOpt.setString(2, opt.getOptionText());
//                                 psOpt.setBoolean(3, opt.isCorrect());
//                                 psOpt.setInt(4, opt.getDisplayOrder() > 0 ? opt.getDisplayOrder() : order++);
//                                 psOpt.addBatch();
//                             }
//                             psOpt.executeBatch();
//                         }
//                     }
//                 } else if ("SHORT_ANSWER".equals(q.getQuestionType()) && shortAnswer != null) {
//                     String delExpSql = "DELETE FROM expected_answers WHERE question_id = ?";
//                     try (PreparedStatement psDel = conn.prepareStatement(delExpSql)) {
//                         psDel.setInt(1, q.getQuestionId());
//                         psDel.executeUpdate();
//                     }

//                     if (!shortAnswer.trim().isEmpty()) {
//                         String insExpSql = "INSERT INTO expected_answers (question_id, answer_text) VALUES (?, ?)";
//                         try (PreparedStatement psIns = conn.prepareStatement(insExpSql)) {
//                             psIns.setInt(1, q.getQuestionId());
//                             psIns.setString(2, shortAnswer.trim());
//                             psIns.executeUpdate();
//                         }
//                     }
//                 }

//                 conn.commit();
//                 return true;
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         }
//     }

//     public boolean delete(int questionId) throws SQLException {
//         try (Connection conn = DBConnection.getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 // 1. Xóa các liên kết trong attempt_answer_options
//                 String sqlDelAao = "DELETE aao FROM attempt_answer_options aao " +
//                                    "JOIN attempt_answers aa ON aao.answer_id = aa.answer_id " +
//                                    "WHERE aa.question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelAao)) {
//                     ps.setInt(1, questionId);
//                     ps.executeUpdate();
//                 }

//                 // 2. Xóa trong attempt_answers
//                 String sqlDelAa = "DELETE FROM attempt_answers WHERE question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelAa)) {
//                     ps.setInt(1, questionId);
//                     ps.executeUpdate();
//                 }

//                 // 3. Xóa trong exam_questions
//                 String sqlDelEq = "DELETE FROM exam_questions WHERE question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelEq)) {
//                     ps.setInt(1, questionId);
//                     ps.executeUpdate();
//                 }

//                 // 4. Xóa trong options
//                 String sqlDelOpt = "DELETE FROM options WHERE question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelOpt)) {
//                     ps.setInt(1, questionId);
//                     ps.executeUpdate();
//                 }

//                 // 5. Xóa trong expected_answers
//                 String sqlDelExp = "DELETE FROM expected_answers WHERE question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelExp)) {
//                     ps.setInt(1, questionId);
//                     ps.executeUpdate();
//                 }

//                 // 6. Xóa trong questions
//                 String sqlDelQ = "DELETE FROM questions WHERE question_id = ?";
//                 try (PreparedStatement ps = conn.prepareStatement(sqlDelQ)) {
//                     ps.setInt(1, questionId);
//                     int affected = ps.executeUpdate();
//                     conn.commit();
//                     return affected > 0;
//                 }
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         }
//     }

//     public Map<String, Integer> getStatistics() throws SQLException {
//         Map<String, Integer> stats = new HashMap<>();
//         stats.put("total", 0);
//         stats.put("single", 0);
//         stats.put("true_false", 0);
//         stats.put("short_answer", 0);

//         String sql = "SELECT question_type, COUNT(*) as cnt FROM questions GROUP BY question_type";
//         try (Connection conn = DBConnection.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql);
//              ResultSet rs = ps.executeQuery()) {
//             int total = 0;
//             while (rs.next()) {
//                 String type = rs.getString("question_type");
//                 int cnt = rs.getInt("cnt");
//                 total += cnt;
//                 if ("SINGLE".equalsIgnoreCase(type)) {
//                     stats.put("single", cnt);
//                 } else if ("TRUE_FALSE".equalsIgnoreCase(type)) {
//                     stats.put("true_false", cnt);
//                 } else if ("SHORT_ANSWER".equalsIgnoreCase(type)) {
//                     stats.put("short_answer", cnt);
//                 }
//             }
//             stats.put("total", total);
//         }
//         return stats;
//     }

//     /**
//      * Tạo một đề thi mới từ danh sách các câu hỏi đã chọn trong ngân hàng.
//      */
//     public int createExamFromQuestions(Exam exam, List<Integer> questionIds) throws SQLException {
//         String examSql = "INSERT INTO exams (created_by, exam_title, description, duration_minutes, pass_score, status) " +
//                          "VALUES (?, ?, ?, ?, ?, ?)";
//         String eqSql = "INSERT INTO exam_questions (exam_id, question_id, points, display_order) VALUES (?, ?, ?, ?)";

//         try (Connection conn = DBConnection.getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 int examId = 0;
//                 try (PreparedStatement ps = conn.prepareStatement(examSql, Statement.RETURN_GENERATED_KEYS)) {
//                     ps.setInt(1, exam.getCreatedBy());
//                     ps.setString(2, exam.getExamTitle());
//                     ps.setString(3, exam.getDescription());
//                     ps.setInt(4, exam.getDurationMinutes());
//                     ps.setBigDecimal(5, exam.getPassScore() != null ? exam.getPassScore() : new BigDecimal("5.0"));
//                     ps.setString(6, exam.getStatus() != null ? exam.getStatus() : "PUBLISHED");
//                     ps.executeUpdate();

//                     try (ResultSet rs = ps.getGeneratedKeys()) {
//                         if (rs.next()) {
//                             examId = rs.getInt(1);
//                         }
//                     }
//                 }

//                 if (examId > 0 && questionIds != null && !questionIds.isEmpty()) {
//                     // Tính điểm tự động: ví dụ thang điểm 10 chia đều số câu hỏi
//                     BigDecimal pointsPerQ = BigDecimal.valueOf(10.0 / questionIds.size()).setScale(2, java.math.RoundingMode.HALF_UP);

//                     try (PreparedStatement psEq = conn.prepareStatement(eqSql)) {
//                         int order = 1;
//                         for (int qId : questionIds) {
//                             psEq.setInt(1, examId);
//                             psEq.setInt(2, qId);
//                             psEq.setBigDecimal(3, pointsPerQ);
//                             psEq.setInt(4, order++);
//                             psEq.addBatch();
//                         }
//                         psEq.executeBatch();
//                     }
//                 }

//                 conn.commit();
//                 return examId;
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         }
//     }

//     /**
//      * Thêm các câu hỏi đã chọn vào một đề thi đã có sẵn.
//      */
//     public int addQuestionsToExam(int examId, List<Integer> questionIds) throws SQLException {
//         if (questionIds == null || questionIds.isEmpty()) return 0;

//         String maxOrderSql = "SELECT COALESCE(MAX(display_order), 0) FROM exam_questions WHERE exam_id = ?";
//         String checkSql = "SELECT 1 FROM exam_questions WHERE exam_id = ? AND question_id = ?";
//         String insertSql = "INSERT INTO exam_questions (exam_id, question_id, points, display_order) VALUES (?, ?, 0.25, ?)";

//         int addedCount = 0;

//         try (Connection conn = DBConnection.getConnection()) {
//             conn.setAutoCommit(false);
//             try {
//                 int currentOrder = 0;
//                 try (PreparedStatement psOrder = conn.prepareStatement(maxOrderSql)) {
//                     psOrder.setInt(1, examId);
//                     try (ResultSet rs = psOrder.executeQuery()) {
//                         if (rs.next()) currentOrder = rs.getInt(1);
//                     }
//                 }

//                 try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
//                      PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

//                     for (int qId : questionIds) {
//                         psCheck.setInt(1, examId);
//                         psCheck.setInt(2, qId);
//                         boolean exists = false;
//                         try (ResultSet rs = psCheck.executeQuery()) {
//                             exists = rs.next();
//                         }

//                         if (!exists) {
//                             currentOrder++;
//                             psInsert.setInt(1, examId);
//                             psInsert.setInt(2, qId);
//                             psInsert.setInt(3, currentOrder);
//                             psInsert.addBatch();
//                             addedCount++;
//                         }
//                     }
//                     psInsert.executeBatch();
//                 }

//                 conn.commit();
//                 return addedCount;
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         }
//     }

//     // Helper methods
//     private List<Option> loadOptionsForQuestion(int questionId, Connection conn) throws SQLException {
//         List<Option> list = new ArrayList<>();
//         String sql = "SELECT * FROM options WHERE question_id = ? ORDER BY display_order ASC";
//         try (PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, questionId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     Option opt = new Option();
//                     opt.setOptionId(rs.getInt("option_id"));
//                     opt.setQuestionId(rs.getInt("question_id"));
//                     opt.setOptionText(rs.getString("option_text"));
//                     opt.setCorrect(rs.getBoolean("is_correct"));
//                     opt.setDisplayOrder(rs.getInt("display_order"));
//                     list.add(opt);
//                 }
//             }
//         }
//         return list;
//     }

//     private String loadExpectedAnswerForQuestion(int questionId, Connection conn) throws SQLException {
//         String sql = "SELECT answer_text FROM expected_answers WHERE question_id = ? LIMIT 1";
//         try (PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, questionId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 if (rs.next()) {
//                     return rs.getString("answer_text");
//                 }
//             }
//         }
//         return null;
//     }

//     private List<String> loadLinkedExams(int questionId, Connection conn) throws SQLException {
//         List<String> exams = new ArrayList<>();
//         String sql = "SELECT e.exam_title FROM exam_questions eq " +
//                      "JOIN exams e ON eq.exam_id = e.exam_id " +
//                      "WHERE eq.question_id = ? ORDER BY e.created_at DESC";
//         try (PreparedStatement ps = conn.prepareStatement(sql)) {
//             ps.setInt(1, questionId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 while (rs.next()) {
//                     exams.add(rs.getString("exam_title"));
//                 }
//             }
//         }
//         return exams;
//     }

//     private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
//         Question q = new Question();
//         q.setQuestionId(rs.getInt("question_id"));
//         q.setCreatedBy(rs.getInt("created_by"));
//         q.setQuestionText(rs.getString("question_text"));
//         q.setQuestionType(rs.getString("question_type"));
//         q.setDefaultPoints(rs.getBigDecimal("default_points"));
//         q.setExplanation(rs.getString("explanation"));
//         q.setStatus(rs.getString("status"));
//         Timestamp ts = rs.getTimestamp("created_at");
//         if (ts != null) {
//             q.setCreatedAt(ts.toLocalDateTime());
//         }
//         return q;
//     }
// }

package com.onlineexam.dao;

import com.onlineexam.model.Question;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    public int create(Question question) throws SQLException {
        String sql = "INSERT INTO Questions (created_by, question_text, question_type, default_points, explanation, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getCreatedBy());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getQuestionType());
            ps.setBigDecimal(4, question.getDefaultPoints());
            ps.setString(5, question.getExplanation());
            ps.setString(6, question.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Creating question failed: no ID returned.");
    }

    public Question findById(int questionId) throws SQLException {
        String sql = "SELECT * FROM Questions WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Question> findByCreator(int createdBy) throws SQLException {
        String sql = "SELECT * FROM Questions WHERE created_by = ? ORDER BY question_id DESC";
        List<Question> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Question> findAllActive() throws SQLException {
        String sql = "SELECT * FROM Questions WHERE status = 'ACTIVE' ORDER BY question_id DESC";
        List<Question> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public boolean update(Question question) throws SQLException {
        String sql = "UPDATE Questions SET question_text = ?, question_type = ?, default_points = ?, explanation = ?, status = ? WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getQuestionType());
            ps.setBigDecimal(3, question.getDefaultPoints());
            ps.setString(4, question.getExplanation());
            ps.setString(5, question.getStatus());
            ps.setInt(6, question.getQuestionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int questionId) throws SQLException {
        String sql = "UPDATE Questions SET status = 'INACTIVE' WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Question(rs.getInt("question_id"), rs.getInt("created_by"),
                rs.getString("question_text"), rs.getString("question_type"),
                rs.getBigDecimal("default_points"), rs.getString("explanation"),
                rs.getString("status"), ts == null ? null : ts.toLocalDateTime());
    }
}