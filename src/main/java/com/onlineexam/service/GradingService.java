package com.onlineexam.service;

import com.onlineexam.dao.AttemptDAO;
import com.onlineexam.model.Attempt;
import com.onlineexam.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class GradingService {

    private final AttemptDAO attemptDAO;

    public GradingService() {
        this.attemptDAO = new AttemptDAO();
    }

    public BigDecimal gradeAttempt(int attemptId) {
        BigDecimal totalScore = BigDecimal.ZERO;

        // 1. Lấy danh sách các câu trả lời của thí sinh trong attempt này
        String sqlAnswers = "SELECT aa.answer_id, aa.question_id, aa.answer_text, q.question_type, eq.points " +
                            "FROM attempt_answers aa " +
                            "JOIN attempts a ON aa.attempt_id = a.attempt_id " +
                            "JOIN exam_sessions es ON a.session_id = es.session_id " +
                            "JOIN exam_questions eq ON es.exam_id = eq.exam_id AND aa.question_id = eq.question_id " +
                            "JOIN questions q ON aa.question_id = q.question_id " +
                            "WHERE aa.attempt_id = ?";

        String sqlUpdateAnswer = "UPDATE attempt_answers SET is_correct = ?, points_awarded = ? WHERE answer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psAns = conn.prepareStatement(sqlAnswers);
             PreparedStatement psUp = conn.prepareStatement(sqlUpdateAnswer)) {

            psAns.setInt(1, attemptId);
            try (ResultSet rs = psAns.executeQuery()) {
                while (rs.next()) {
                    int answerId = rs.getInt("answer_id");
                    int questionId = rs.getInt("question_id");
                    String questionType = rs.getString("question_type");
                    String studentShortText = rs.getString("answer_text");
                    BigDecimal maxPoints = rs.getBigDecimal("points");

                    boolean isCorrect = false;
                    BigDecimal awardedPoints = BigDecimal.ZERO;

                    // ==========================================
                    // 1. CHẤM CÂU HỎI TRẮC NGHIỆM (SINGLE)
                    // ==========================================
                    if ("SINGLE".equalsIgnoreCase(questionType) || "MULTIPLE".equalsIgnoreCase(questionType)) {
                        String sqlSingle = "SELECT o.is_correct " +
                                           "FROM attempt_answer_options aao " +
                                           "JOIN options o ON aao.option_id = o.option_id " +
                                           "WHERE aao.answer_id = ? LIMIT 1";
                        try (PreparedStatement psS = conn.prepareStatement(sqlSingle)) {
                            psS.setInt(1, answerId);
                            try (ResultSet rsS = psS.executeQuery()) {
                                if (rsS.next()) {
                                    isCorrect = rsS.getBoolean("is_correct");
                                    if (isCorrect) {
                                        awardedPoints = (maxPoints != null && maxPoints.compareTo(BigDecimal.ZERO) > 0) 
                                                        ? maxPoints : new BigDecimal("0.25");
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 2. CHẤM CÂU HỎI ĐÚNG / SAI (TRUE_FALSE)
                    // Biểu điểm chuẩn Bộ GD&ĐT:
                    // Đúng 1 ý: 0.1 điểm
                    // Đúng 2 ý: 0.25 điểm
                    // Đúng 3 ý: 0.50 điểm
                    // Đúng 4 ý: 1.00 điểm
                    // ==========================================
                    else if ("TRUE_FALSE".equalsIgnoreCase(questionType)) {
                        // Lấy tất cả option của câu hỏi này trong DB
                        Map<Integer, Boolean> correctMap = new HashMap<>();
                        String sqlOpts = "SELECT option_id, is_correct FROM options WHERE question_id = ?";
                        try (PreparedStatement psO = conn.prepareStatement(sqlOpts)) {
                            psO.setInt(1, questionId);
                            try (ResultSet rsO = psO.executeQuery()) {
                                while (rsO.next()) {
                                    correctMap.put(rsO.getInt("option_id"), rsO.getBoolean("is_correct"));
                                }
                            }
                        }

                        // Lấy danh sách option_id mà học sinh đã chọn là "ĐÚNG"
                        Set<Integer> studentSelectedTrue = new HashSet<>();
                        String sqlSelected = "SELECT option_id FROM attempt_answer_options WHERE answer_id = ?";
                        try (PreparedStatement psSel = conn.prepareStatement(sqlSelected)) {
                            psSel.setInt(1, answerId);
                            try (ResultSet rsSel = psSel.executeQuery()) {
                                while (rsSel.next()) {
                                    studentSelectedTrue.add(rsSel.getInt("option_id"));
                                }
                            }
                        }

                        // Đếm số lượng ý con học sinh trả lời đúng
                        int correctSubCount = 0;
                        for (Map.Entry<Integer, Boolean> entry : correctMap.entrySet()) {
                            int optId = entry.getKey();
                            boolean shouldBeTrue = entry.getValue();
                            boolean studentMarkedTrue = studentSelectedTrue.contains(optId);

                            // Học sinh được tính là đúng ý này nếu:
                            // Đề bảo ĐÚNG và học sinh chọn ĐÚNG, HOẶC Đề bảo SAI và học sinh không chọn ĐÚNG (tức chọn SAI)
                            if (shouldBeTrue == studentMarkedTrue) {
                                correctSubCount++;
                            }
                        }

                        // Áp dụng biểu điểm mới
                        if (correctSubCount == 1) {
                            awardedPoints = new BigDecimal("0.10");
                        } else if (correctSubCount == 2) {
                            awardedPoints = new BigDecimal("0.25");
                        } else if (correctSubCount == 3) {
                            awardedPoints = new BigDecimal("0.50");
                        } else if (correctSubCount == 4) {
                            awardedPoints = new BigDecimal("1.00");
                            isCorrect = true;
                        }
                    }

                    // ==========================================
                    // 3. CHẤM CÂU HỎI TRẢ LỜI NGẮN (SHORT_ANSWER)
                    // Đúng 1 câu = 0.25 điểm
                    // ==========================================
                    else if ("SHORT_ANSWER".equalsIgnoreCase(questionType)) {
                        if (studentShortText != null && !studentShortText.trim().isEmpty()) {
                            String sqlExp = "SELECT answer_text FROM expected_answers WHERE question_id = ?";
                            try (PreparedStatement psExp = conn.prepareStatement(sqlExp)) {
                                psExp.setInt(1, questionId);
                                try (ResultSet rsExp = psExp.executeQuery()) {
                                    while (rsExp.next()) {
                                        String expected = rsExp.getString("answer_text");
                                        // So sánh không phân biệt hoa thường và loại bỏ khoảng trắng thừa
                                        if (expected != null && expected.trim().equalsIgnoreCase(studentShortText.trim())) {
                                            isCorrect = true;
                                            awardedPoints = (maxPoints != null && maxPoints.compareTo(BigDecimal.ZERO) > 0) 
                                                            ? maxPoints : new BigDecimal("0.25");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cộng dồn tổng điểm
                    totalScore = totalScore.add(awardedPoints);

                    // Cập nhật điểm cho từng câu trả lời vào bảng attempt_answers
                    psUp.setBoolean(1, isCorrect);
                    psUp.setBigDecimal(2, awardedPoints);
                    psUp.setInt(3, answerId);
                    psUp.addBatch();
                }
            }
            psUp.executeBatch();

            // Cập nhật tổng điểm và trạng thái GRADED cho Attempt
            String sqlUpdateAttempt = "UPDATE attempts SET score = ?, status = 'GRADED', graded_at = NOW() WHERE attempt_id = ?";
            try (PreparedStatement psAtt = conn.prepareStatement(sqlUpdateAttempt)) {
                psAtt.setBigDecimal(1, totalScore);
                psAtt.setInt(2, attemptId);
                psAtt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalScore;
    }
}