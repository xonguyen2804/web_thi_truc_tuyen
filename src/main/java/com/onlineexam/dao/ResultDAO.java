package com.onlineexam.dao;

import com.onlineexam.util.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {

    public static class AnswerResultDTO {
        public int questionId;
        public String questionText;
        public String questionType;
        public String studentAnswer;
        public boolean isCorrect;
        public BigDecimal pointsAwarded;

        public int getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getQuestionType() { return questionType; }
        public String getStudentAnswer() { return studentAnswer; }
        public boolean isCorrect() { return isCorrect; }
        public BigDecimal getPointsAwarded() { return pointsAwarded; }
    }

    public static class AttemptResultDetailDTO {
        private int attemptId;
        private String examTitle;
        private String sessionName;
        private String studentName;
        private BigDecimal score;
        private BigDecimal passScore;
        private String status;
        private String startedAt;
        private String submittedAt;
        private int correctCount;
        private int totalQuestions;
        private List<AnswerResultDTO> answers = new ArrayList<>();

        public int getAttemptId() { return attemptId; }
        public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
        public String getSessionName() { return sessionName; }
        public void setSessionName(String sessionName) { this.sessionName = sessionName; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public BigDecimal getPassScore() { return passScore; }
        public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStartedAt() { return startedAt; }
        public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
        public String getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
        public int getCorrectCount() { return correctCount; }
        public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public List<AnswerResultDTO> getAnswers() { return answers; }
        public void setAnswers(List<AnswerResultDTO> answers) { this.answers = answers; }
    }

    public AttemptResultDetailDTO getAttemptResult(int attemptId) throws SQLException {
        String sqlAttempt = """
            SELECT a.*, e.exam_title AS exam_title, e.pass_score, es.session_name, u.full_name
            FROM attempts a
            JOIN exam_sessions es ON a.session_id = es.session_id
            JOIN exams e ON es.exam_id = e.exam_id
            JOIN users u ON a.user_id = u.user_id
            WHERE a.attempt_id = ?
        """;

        String sqlAnswers = """
            SELECT aa.*, q.question_text, q.question_type
            FROM attempt_answers aa
            JOIN questions q ON aa.question_id = q.question_id
            WHERE aa.attempt_id = ?
        """;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        try (Connection conn = DBConnection.getConnection()) {
            AttemptResultDetailDTO detail = null;

            // 1. Tải thông tin chung bài thi
            try (PreparedStatement ps = conn.prepareStatement(sqlAttempt)) {
                ps.setInt(1, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        detail = new AttemptResultDetailDTO();
                        detail.setAttemptId(attemptId);
                        detail.setExamTitle(rs.getString("exam_title"));
                        detail.setSessionName(rs.getString("session_name"));
                        detail.setStudentName(rs.getString("full_name"));
                        detail.setScore(rs.getBigDecimal("score"));
                        detail.setPassScore(rs.getBigDecimal("pass_score"));
                        detail.setStatus(rs.getString("status"));

                        if (rs.getTimestamp("started_at") != null) {
                            detail.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime().format(dtf));
                        }
                        if (rs.getTimestamp("submitted_at") != null) {
                            detail.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime().format(dtf));
                        }
                    } else {
                        return null; // Không tìm thấy
                    }
                }
            }

            // 2. Tải danh sách chi tiết các câu hỏi và câu trả lời
            int correctCount = 0;
            int totalCount = 0;
            List<AnswerResultDTO> answers = new ArrayList<>();

            try (PreparedStatement psAns = conn.prepareStatement(sqlAnswers)) {
                psAns.setInt(1, attemptId);
                try (ResultSet rsAns = psAns.executeQuery()) {
                    while (rsAns.next()) {
                        totalCount++;
                        AnswerResultDTO dto = new AnswerResultDTO();
                        dto.questionId = rsAns.getInt("question_id");
                        dto.questionText = rsAns.getString("question_text");
                        dto.questionType = rsAns.getString("question_type");
                        dto.isCorrect = rsAns.getBoolean("is_correct");
                        dto.pointsAwarded = rsAns.getBigDecimal("points_awarded");
                        dto.studentAnswer = rsAns.getString("answer_text");

                        if (dto.isCorrect) {
                            correctCount++;
                        }
                        answers.add(dto);
                    }
                }
            }

            detail.setTotalQuestions(totalCount);
            detail.setCorrectCount(correctCount);
            detail.setAnswers(answers);

            return detail;
        }
    }
}