package com.onlineexam.controller;

import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/student/results", "/results"})
public class ResultServlet extends HttpServlet {

    // DTO chi tiết từng câu trả lời trong kết quả
    public static class AnswerResultDTO {
        public int questionId;
        public String questionText;
        public String questionType;
        public String studentAnswer;
        public boolean isCorrect;
        public BigDecimal pointsAwarded;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String attemptIdStr = request.getParameter("attemptId");
        if (attemptIdStr == null || attemptIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        int attemptId = Integer.parseInt(attemptIdStr);

        String sqlAttempt = "SELECT a.*, e.exam_title, e.pass_score, es.session_name, u.full_name " +
                            "FROM attempts a " +
                            "JOIN exam_sessions es ON a.session_id = es.session_id " +
                            "JOIN exams e ON es.exam_id = e.exam_id " +
                            "JOIN users u ON a.user_id = u.user_id " +
                            "WHERE a.attempt_id = ?";

        String sqlAnswers = "SELECT aa.*, q.question_text, q.question_type " +
                            "FROM attempt_answers aa " +
                            "JOIN questions q ON aa.question_id = q.question_id " +
                            "WHERE aa.attempt_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Tải thông tin chung của bài thi
            try (PreparedStatement ps = conn.prepareStatement(sqlAttempt)) {
                ps.setInt(1, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("attemptId", attemptId);
                        request.setAttribute("examTitle", rs.getString("exam_title"));
                        request.setAttribute("sessionName", rs.getString("session_name"));
                        request.setAttribute("studentName", rs.getString("full_name"));
                        request.setAttribute("score", rs.getBigDecimal("score"));
                        request.setAttribute("passScore", rs.getBigDecimal("pass_score"));
                        request.setAttribute("status", rs.getString("status"));

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
                        if (rs.getTimestamp("started_at") != null) {
                            request.setAttribute("startedAt", rs.getTimestamp("started_at").toLocalDateTime().format(dtf));
                        }
                        if (rs.getTimestamp("submitted_at") != null) {
                            request.setAttribute("submittedAt", rs.getTimestamp("submitted_at").toLocalDateTime().format(dtf));
                        }
                    } else {
                        response.sendRedirect(request.getContextPath() + "/student/exams");
                        return;
                    }
                }
            }

            // 2. Tải danh sách chi tiết các câu đã chấm
            List<AnswerResultDTO> answers = new ArrayList<>();
            int correctCount = 0;
            int totalQuestions = 0;

            try (PreparedStatement psAns = conn.prepareStatement(sqlAnswers)) {
                psAns.setInt(1, attemptId);
                try (ResultSet rsAns = psAns.executeQuery()) {
                    while (rsAns.next()) {
                        totalQuestions++;
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

            request.setAttribute("answers", answers);
            request.setAttribute("correctCount", correctCount);
            request.setAttribute("totalQuestions", totalQuestions);

            request.getRequestDispatcher("/WEB-INF/views/student/results.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp kết quả bài thi: " + e.getMessage());
        }
    }
}