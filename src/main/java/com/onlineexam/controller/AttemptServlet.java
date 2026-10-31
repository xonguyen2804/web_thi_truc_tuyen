package com.onlineexam.controller;

import com.onlineexam.dao.ExamSessionDAO;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.User;
import com.onlineexam.service.GradingService;
import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/student/exams", "/student/check-pass", "/student/take-exam", "/student/submit-exam"})
public class AttemptServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAO();
    private final GradingService gradingService = new GradingService();

    public static class QuestionDTO {
        public int questionId;
        public String questionText;
        public String questionType;
        public BigDecimal points;
        public List<OptionDTO> options = new ArrayList<>();
    }

    public static class OptionDTO {
        public int optionId;
        public String optionText;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if ("/student/take-exam".equals(servletPath)) {
            handleTakeExamPage(request, response);
        } else {
            handleAvailableExamsPage(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String servletPath = request.getServletPath();

        if ("/student/check-pass".equals(servletPath)) {
            handleVerifyPassAndStart(request, response);
        } else if ("/student/submit-exam".equals(servletPath)) {
            handleSubmitExam(request, response);
        }
    }

    // 1. Tìm kiếm và hiển thị danh sách ca thi khả dụng
    private void handleAvailableExamsPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        try {
            List<ExamSession> sessions = sessionDAO.findAvailableForStudent(keyword);
            request.setAttribute("sessions", sessions);
            request.setAttribute("keyword", keyword);
            request.getRequestDispatcher("/WEB-INF/views/student/available-exams.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. Xác thực mật khẩu phòng thi (session_password) và tạo bài làm Attempt
    private void handleVerifyPassAndStart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));
        String inputPassword = request.getParameter("sessionPassword");

        User user = (User) request.getSession().getAttribute("user");
        int userId = (user != null) ? user.getUserId() : 2; // ID học sinh đăng nhập (mặc định 2)

        try {
            ExamSession session = sessionDAO.findById(sessionId);
            if (session == null || !"OPEN".equalsIgnoreCase(session.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/student/exams?error=Ca+thi+chua+duoc+giao+vien+mo+phong!");
                return;
            }

            // Kiểm tra mật khẩu nếu ca thi có đặt mật khẩu
            if (session.getSessionPassword() != null && !session.getSessionPassword().isBlank()) {
                if (inputPassword == null || !session.getSessionPassword().trim().equals(inputPassword.trim())) {
                    response.sendRedirect(request.getContextPath() + "/student/exams?error=Mat+khau+phong+thi+khong+dung!&invalidSessionId=" + sessionId);
                    return;
                }
            }

            // Tạo hoặc lấy bài làm IN_PROGRESS hiện tại
            int attemptId = createOrGetActiveAttempt(sessionId, userId);
            response.sendRedirect(request.getContextPath() + "/student/take-exam?attemptId=" + attemptId);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
        }
    }

        // 3. Tải câu hỏi hiển thị lên màn hình làm bài
    private void handleTakeExamPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String attemptIdParam = request.getParameter("attemptId");

        // Kiểm tra an toàn: nếu không có attemptId hoặc attemptId không phải số -> chuyển hướng về danh sách ca thi
        if (attemptIdParam == null || attemptIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        int attemptId;
        try {
            attemptId = Integer.parseInt(attemptIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        String sql = "SELECT a.*, es.session_name, e.exam_id, e.exam_title, e.duration_minutes " +
                     "FROM attempts a " +
                     "JOIN exam_sessions es ON a.session_id = es.session_id " +
                     "JOIN exams e ON es.exam_id = e.exam_id " +
                     "WHERE a.attempt_id = ? AND a.status = 'IN_PROGRESS'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int examId = rs.getInt("exam_id");
                    String examTitle = rs.getString("exam_title");
                    int durationMinutes = rs.getInt("duration_minutes");
                    String sessionName = rs.getString("session_name");

                    List<QuestionDTO> questions = loadExamQuestions(examId, conn);

                    request.setAttribute("attemptId", attemptId);
                    request.setAttribute("examTitle", examTitle);
                    request.setAttribute("sessionName", sessionName);
                    request.setAttribute("durationMinutes", durationMinutes);
                    request.setAttribute("questions", questions);

                    request.getRequestDispatcher("/WEB-INF/views/student/take-exam.jsp").forward(request, response);
                    return;
                }
            }
            // Nếu attempt không tồn tại hoặc đã nộp bài (không còn IN_PROGRESS) thì đưa về danh sách
            response.sendRedirect(request.getContextPath() + "/student/exams");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 4. Học sinh nộp bài: Lưu câu trả lời và tự động chấm điểm
    private void handleSubmitExam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int attemptId = Integer.parseInt(request.getParameter("attemptId"));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Lấy danh sách câu hỏi của đề thi gắn liền với lượt làm (attemptId) này
            String qListSql = "SELECT q.question_id, q.question_type FROM exam_questions eq " +
                              "JOIN questions q ON eq.question_id = q.question_id " +
                              "JOIN exam_sessions es ON eq.exam_id = es.exam_id " +
                              "JOIN attempts a ON es.session_id = a.session_id " +
                              "WHERE a.attempt_id = ?";

            List<Integer> singleQIds = new ArrayList<>();
            List<Integer> tfQIds = new ArrayList<>();
            List<Integer> shortAnsQIds = new ArrayList<>();

            try (PreparedStatement psQ = conn.prepareStatement(qListSql)) {
                psQ.setInt(1, attemptId);
                try (ResultSet rsQ = psQ.executeQuery()) {
                    while (rsQ.next()) {
                        int qId = rsQ.getInt("question_id");
                        String type = rsQ.getString("question_type");
                        if ("SHORT_ANSWER".equalsIgnoreCase(type)) {
                            shortAnsQIds.add(qId);
                        } else if ("TRUE_FALSE".equalsIgnoreCase(type)) {
                            tfQIds.add(qId);
                        } else {
                            singleQIds.add(qId);
                        }
                    }
                }
            }

            String insAnsSql = "INSERT INTO attempt_answers (attempt_id, question_id, points_awarded) VALUES (?, ?, 0)";
            String insOptSql = "INSERT INTO attempt_answer_options (answer_id, option_id) VALUES (?, ?)";

            // 1. Lưu câu hỏi TRẮC NGHIỆM (SINGLE)
            for (int qId : singleQIds) {
                String optIdStr = request.getParameter("q_" + qId);
                if (optIdStr != null && !optIdStr.isBlank()) {
                    int answerId = 0;
                    try (PreparedStatement psA = conn.prepareStatement(insAnsSql, Statement.RETURN_GENERATED_KEYS)) {
                        psA.setInt(1, attemptId);
                        psA.setInt(2, qId);
                        psA.executeUpdate();
                        try (ResultSet rsKey = psA.getGeneratedKeys()) {
                            if (rsKey.next()) answerId = rsKey.getInt(1);
                        }
                    }

                    if (answerId > 0) {
                        try (PreparedStatement psO = conn.prepareStatement(insOptSql)) {
                            psO.setInt(1, answerId);
                            psO.setInt(2, Integer.parseInt(optIdStr));
                            psO.executeUpdate();
                        }
                    }
                }
            }

            // 2. Lưu câu hỏi ĐÚNG / SAI (TRUE_FALSE)
            for (int qId : tfQIds) {
                List<OptionDTO> options = loadOptions(qId, conn);
                int answerId = 0;
                try (PreparedStatement psA = conn.prepareStatement(insAnsSql, Statement.RETURN_GENERATED_KEYS)) {
                    psA.setInt(1, attemptId);
                    psA.setInt(2, qId);
                    psA.executeUpdate();
                    try (ResultSet rsKey = psA.getGeneratedKeys()) {
                        if (rsKey.next()) answerId = rsKey.getInt(1);
                    }
                }

                if (answerId > 0) {
                    try (PreparedStatement psO = conn.prepareStatement(insOptSql)) {
                        for (OptionDTO opt : options) {
                            String choice = request.getParameter("tf_opt_" + opt.optionId);
                            // Nếu học sinh tick chọn "Đúng" thì ghi nhận optionId này
                            if ("true".equalsIgnoreCase(choice)) {
                                psO.setInt(1, answerId);
                                psO.setInt(2, opt.optionId);
                                psO.addBatch();
                            }
                        }
                        psO.executeBatch();
                    }
                }
            }

            // 3. Lưu câu hỏi TRẢ LỜI NGẮN (SHORT_ANSWER)
            String insShortSql = "INSERT INTO attempt_answers (attempt_id, question_id, answer_text, points_awarded) VALUES (?, ?, ?, 0)";
            for (int qId : shortAnsQIds) {
                String shortText = request.getParameter("short_q_" + qId);
                if (shortText != null && !shortText.trim().isEmpty()) {
                    try (PreparedStatement psS = conn.prepareStatement(insShortSql)) {
                        psS.setInt(1, attemptId);
                        psS.setInt(2, qId);
                        psS.setString(3, shortText.trim());
                        psS.executeUpdate();
                    }
                }
            }

            // 4. Chuyển trạng thái lượt thi sang SUBMITTED
            String updateAttSql = "UPDATE attempts SET submitted_at = NOW(), status = 'SUBMITTED' WHERE attempt_id = ?";
            try (PreparedStatement psUp = conn.prepareStatement(updateAttSql)) {
                psUp.setInt(1, attemptId);
                psUp.executeUpdate();
            }

            conn.commit();

            // 5. Kích hoạt tính điểm tự động
            gradingService.gradeAttempt(attemptId);

            response.sendRedirect(request.getContextPath() + "/student/results?attemptId=" + attemptId);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
        }
    }

    private int createOrGetActiveAttempt(int sessionId, int userId) throws SQLException {
        String findSql = "SELECT attempt_id FROM attempts WHERE session_id = ? AND user_id = ? AND status = 'IN_PROGRESS' ORDER BY attempt_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("attempt_id");
            }
        }

        // Tính attempt_number tiếp theo
        int nextAttemptNumber = 1;
        String countSql = "SELECT COALESCE(MAX(attempt_number), 0) + 1 FROM attempts WHERE session_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psC = conn.prepareStatement(countSql)) {
            psC.setInt(1, sessionId);
            psC.setInt(2, userId);
            try (ResultSet rsC = psC.executeQuery()) {
                if (rsC.next()) nextAttemptNumber = rsC.getInt(1);
            }
        }

        String insSql = "INSERT INTO attempts (session_id, user_id, attempt_number, started_at, status) VALUES (?, ?, ?, NOW(), 'IN_PROGRESS')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            ps.setInt(3, nextAttemptNumber);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private List<QuestionDTO> loadExamQuestions(int examId, Connection conn) throws SQLException {
        List<QuestionDTO> list = new ArrayList<>();
        String sql = "SELECT q.question_id, q.question_text, q.question_type, eq.points " +
                     "FROM exam_questions eq " +
                     "JOIN questions q ON eq.question_id = q.question_id " +
                     "WHERE eq.exam_id = ? ORDER BY eq.display_order ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuestionDTO q = new QuestionDTO();
                    q.questionId = rs.getInt("question_id");
                    q.questionText = rs.getString("question_text");
                    q.questionType = rs.getString("question_type");
                    q.points = rs.getBigDecimal("points");
                    q.options = loadOptions(q.questionId, conn);
                    list.add(q);
                }
            }
        }
        return list;
    }

    private List<OptionDTO> loadOptions(int questionId, Connection conn) throws SQLException {
        List<OptionDTO> list = new ArrayList<>();
        String sql = "SELECT option_id, option_text FROM options WHERE question_id = ? ORDER BY display_order ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OptionDTO o = new OptionDTO();
                    o.optionId = rs.getInt("option_id");
                    o.optionText = rs.getString("option_text");
                    list.add(o);
                }
            }
        }
        return list;
    }
}