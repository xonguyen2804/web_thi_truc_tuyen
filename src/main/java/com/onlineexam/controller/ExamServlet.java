package com.onlineexam.controller;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/teacher/exams", "/exams"})
@MultipartConfig(
    maxFileSize = 5 * 1024 * 1024,      // Tối đa 5MB cho 1 file
    maxRequestSize = 6 * 1024 * 1024    // Tối đa 6MB cho toàn bộ request
)
public class ExamServlet extends HttpServlet {

    private ExamDAO examDAO;

    public ExamServlet() {
        super();
        this.examDAO = new ExamDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Exam> exams = examDAO.findAll();
            request.setAttribute("exams", exams);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/WEB-INF/views/teacher/exams.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            String title = request.getParameter("examTitle");
            String description = request.getParameter("description");
            int durationMinutes = Integer.parseInt(request.getParameter("durationMinutes"));
            BigDecimal passScore = new BigDecimal(request.getParameter("passScore"));
            String status = request.getParameter("status");

            Exam exam = new Exam();
            exam.setCreatedBy(1); // ID giáo viên tạo đề
            exam.setExamTitle(title);
            exam.setDescription(description);
            exam.setDurationMinutes(durationMinutes);
            exam.setPassScore(passScore);
            exam.setStatus(status != null ? status : "PUBLISHED");

            // 1. Lưu thông tin đề thi
            examDAO.insert(exam);

            // 2. Xử lý file đề thi upload (nếu có)
            Part filePart = request.getPart("examFile");
            if (filePart != null && filePart.getSize() > 0) {
                parseAndImportQuestions(filePart, exam.getExamId(), exam.getCreatedBy());
            }

            response.sendRedirect(request.getContextPath() + "/teacher/exams?success=true");
        } catch (Exception e) {
            e.printStackTrace();
            // Lưu thông báo lỗi vào Session hoặc mã hóa URL an toàn
            request.getSession().setAttribute("errorMessage", "Lỗi tạo đề thi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/teacher/exams?error=true");
        }
    }

    //30-10
    /**
     * Phân tích cú pháp file đề thi THPT QG gồm 3 phần: SINGLE, TRUE_FALSE, SHORT_ANSWER.
     */
    private void parseAndImportQuestions(Part filePart, int examId, int createdBy) throws IOException, SQLException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8));
             Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false); // Dùng Transaction để đảm bảo tính toàn vẹn

            String line;
            String currentQuestionText = null;
            String currentType = null; // SINGLE, TRUE_FALSE, SHORT_ANSWER
            List<OptionItem> options = new ArrayList<>();
            String shortAnswer = null;
            int displayOrder = 1;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 1. Nhận diện dòng bắt đầu câu hỏi: "Câu 1: ...", "Câu 2. ..."
                if (line.matches("^(Câu|Cau)\\s*\\d+[:\\.].*")) {
                    // Nếu đã có câu hỏi trước đó -> lưu vào DB
                    if (currentQuestionText != null) {
                        saveParsedQuestion(conn, examId, createdBy, currentQuestionText, currentType, options, shortAnswer, displayOrder++);
                        options.clear();
                        shortAnswer = null;
                    }
                    currentQuestionText = line.replaceFirst("^(Câu|Cau)\\s*\\d+[:\\.]\\s*", "").trim();
                    currentType = null; // Chờ dòng tiếp theo xác định loại câu hỏi
                    continue;
                }

                // 2. Nhận diện câu hỏi SINGLE (A., B., C., D.)
                if (line.matches("^[A-D][\\.\\)]\\s*.*")) {
                    currentType = "SINGLE";
                    boolean isCorrect = line.endsWith(" *");
                    String cleanText = line.substring(2).replaceAll("\\s*\\*$", "").trim();
                    options.add(new OptionItem(cleanText, isCorrect));
                    continue;
                }

                // 3. Nhận diện câu hỏi TRUE_FALSE (Ý 1:, Ý 2:, ...)
                if (line.matches("^(Ý|Y)\\s*\\d+[:\\.].*")) {
                    currentType = "TRUE_FALSE";
                    boolean isCorrect = line.endsWith(" Đ") || line.endsWith(" đ");
                    String cleanText = line.replaceFirst("^(Ý|Y)\\s*\\d+[:\\.]\\s*", "").replaceAll("\\s+[ĐđSs]$", "").trim();
                    options.add(new OptionItem(cleanText, isCorrect));
                    continue;
                }

                // 4. Nhận diện câu hỏi SHORT_ANSWER (dòng đáp án <= 4 ký tự sau câu hỏi)
                if (currentQuestionText != null && options.isEmpty()) {
                    currentType = "SHORT_ANSWER";
                    shortAnswer = line.trim();
                    if (shortAnswer.length() > 4) {
                        shortAnswer = shortAnswer.substring(0, 4); // Cắt tối đa 4 ký tự theo quy định
                    }
                }
            }

            // Lưu câu hỏi cuối cùng
            if (currentQuestionText != null) {
                saveParsedQuestion(conn, examId, createdBy, currentQuestionText, currentType, options, shortAnswer, displayOrder);
            }

            conn.commit();
        }
    }

    // private void saveQuestionToDb(Connection conn, int examId, int createdBy, String text, List<OptionItem> options, int order) throws SQLException {
    //     // 1. Thêm vào Questions
    //     String qSql = "INSERT INTO Questions (created_by, question_text, question_type, default_points) VALUES (?, ?, 'SINGLE', 0.2)";
    //     int questionId = 0;
    //     try (PreparedStatement ps = conn.prepareStatement(qSql, Statement.RETURN_GENERATED_KEYS)) {
    //         ps.setInt(1, createdBy);
    //         ps.setString(2, text);
    //         ps.executeUpdate();
    //         try (ResultSet rs = ps.getGeneratedKeys()) {
    //             if (rs.next()) questionId = rs.getInt(1);
    //         }
    //     }

    //     // 2. Thêm vào Options
    //     String oSql = "INSERT INTO Options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
    //     try (PreparedStatement ps = conn.prepareStatement(oSql)) {
    //         int optOrder = 1;
    //         for (OptionItem opt : options) {
    //             ps.setInt(1, questionId);
    //             ps.setString(2, opt.text);
    //             ps.setBoolean(3, opt.isCorrect);
    //             ps.setInt(4, optOrder++);
    //             ps.addBatch();
    //         }
    //         ps.executeBatch();
    //     }

    //     // 3. Gán câu hỏi vào Exam_Questions
    //     String eqSql = "INSERT INTO Exam_Questions (exam_id, question_id, points, display_order) VALUES (?, ?, 0.2, ?)";
    //     try (PreparedStatement ps = conn.prepareStatement(eqSql)) {
    //         ps.setInt(1, examId);
    //         ps.setInt(2, questionId);
    //         ps.setInt(3, order);
    //         ps.executeUpdate();
    //     }
    // }

    private void saveParsedQuestion(Connection conn, int examId, int createdBy, String text, String type, 
                                    List<OptionItem> options, String shortAnswer, int order) throws SQLException {
        if (type == null) type = "SINGLE";

        // 1. Lưu vào bảng Questions
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

        // 2. Lưu đáp án tùy theo loại
        if ("SINGLE".equals(type) || "TRUE_FALSE".equals(type)) {
            String oSql = "INSERT INTO Options (question_id, option_text, is_correct, display_order) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(oSql)) {
                int optOrder = 1;
                for (OptionItem opt : options) {
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

        // 3. Gán vào bảng liên kết Exam_Questions
        String eqSql = "INSERT INTO Exam_Questions (exam_id, question_id, points, display_order) VALUES (?, ?, 0.25, ?)";
        try (PreparedStatement ps = conn.prepareStatement(eqSql)) {
            ps.setInt(1, examId);
            ps.setInt(2, questionId);
            ps.setInt(3, order);
            ps.executeUpdate();
        }
    }

    private static class OptionItem {
    String text;
    boolean isCorrect;
    OptionItem(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }
}

}