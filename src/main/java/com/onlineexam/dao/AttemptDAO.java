package com.onlineexam.dao;

import com.onlineexam.model.Attempt;
import com.onlineexam.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tầng truy xuất dữ liệu cho bảng Attempts (bài thi của thí sinh).
 */
public class AttemptDAO {

    // --- CÁC DTO PHỤC VỤ CONTROLLER VÀ VIEW ---

    public static class OptionDTO {
        public int optionId;
        public String optionText;

        public int getOptionId() { return optionId; }
        public String getOptionText() { return optionText; }
    }

    public static class QuestionDTO {
        public int questionId;
        public String questionText;
        public String questionType;
        public BigDecimal points;
        public List<OptionDTO> options = new ArrayList<>();

        public int getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getQuestionType() { return questionType; }
        public BigDecimal getPoints() { return points; }
        public List<OptionDTO> getOptions() { return options; }
    }

    public static class SessionViewDTO {
        public int sessionId;
        public String sessionName;
        public String examTitle;
        public int durationMinutes;
        public String status;
        public String startTime;
        public String endTime;
        public boolean hasPassword;

        public int getSessionId() { return sessionId; }
        public String getSessionName() { return sessionName; }
        public String getExamTitle() { return examTitle; }
        public int getDurationMinutes() { return durationMinutes; }
        public String getStatus() { return status; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public boolean isHasPassword() { return hasPassword; }
    }

    public static class TakeExamDetailDTO {
        private int attemptId;
        private int examId;
        private String examTitle;
        private String sessionName;
        private int durationMinutes;
        private List<QuestionDTO> questions = new ArrayList<>();

        public int getAttemptId() { return attemptId; }
        public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
        public int getExamId() { return examId; }
        public void setExamId(int examId) { this.examId = examId; }
        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
        public String getSessionName() { return sessionName; }
        public void setSessionName(String sessionName) { this.sessionName = sessionName; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public List<QuestionDTO> getQuestions() { return questions; }
        public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
    }

    private Connection connection;

    public AttemptDAO() {}

    public AttemptDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        }
        return DBConnection.getConnection();
    }

    // --- CÁC PHƯƠNG THỨC TRUY XUẤT CƠ BẢN ---

    public boolean insert(Attempt attempt) throws SQLException {
        String sql = "INSERT INTO Attempts (session_id, user_id, attempt_number, started_at, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attempt.getSessionId());
            ps.setInt(2, attempt.getUserId());
            ps.setInt(3, attempt.getAttemptNumber());
            ps.setTimestamp(4, Timestamp.valueOf(attempt.getStartedAt()));
            ps.setString(5, attempt.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) attempt.setAttemptId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public Attempt findById(int attemptId) throws SQLException {
        String sql = "SELECT * FROM Attempts WHERE attempt_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Attempt a = new Attempt();
                    a.setAttemptId(rs.getInt("attempt_id"));
                    a.setSessionId(rs.getInt("session_id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setAttemptNumber(rs.getInt("attempt_number"));
                    Timestamp st = rs.getTimestamp("started_at");
                    if (st != null) a.setStartedAt(st.toLocalDateTime());
                    Timestamp sub = rs.getTimestamp("submitted_at");
                    if (sub != null) a.setSubmittedAt(sub.toLocalDateTime());
                    a.setScore(rs.getBigDecimal("score"));
                    a.setStatus(rs.getString("status"));
                    return a;
                }
            }
        }
        return null;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ CA THI & BÀI THI CỦA HỌC SINH ---

    // 1. Danh sách ca thi mở cho học sinh
    public List<SessionViewDTO> findAvailableSessions() throws SQLException {
        List<SessionViewDTO> list = new ArrayList<>();
        String sql = """
            SELECT es.*, e.exam_title AS exam_title, e.duration_minutes
            FROM exam_sessions es
            JOIN exams e ON es.exam_id = e.exam_id
            WHERE es.status IN ('OPEN', 'SCHEDULED')
            ORDER BY (es.status = 'OPEN') DESC, es.start_time ASC
        """;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SessionViewDTO s = new SessionViewDTO();
                s.sessionId = rs.getInt("session_id");
                s.sessionName = rs.getString("session_name");
                s.examTitle = rs.getString("exam_title");
                s.durationMinutes = rs.getInt("duration_minutes");
                s.status = rs.getString("status");
                String pass = rs.getString("session_password");
                s.hasPassword = (pass != null && !pass.trim().isEmpty());

                if (rs.getTimestamp("start_time") != null) {
                    s.startTime = rs.getTimestamp("start_time").toLocalDateTime().format(dtf);
                }
                if (rs.getTimestamp("end_time") != null) {
                    s.endTime = rs.getTimestamp("end_time").toLocalDateTime().format(dtf);
                }
                list.add(s);
            }
        }
        return list;
    }

    // 2. Lấy hoặc tạo mới lượt làm bài (IN_PROGRESS)
    public int getOrCreateActiveAttempt(int sessionId, int userId) throws SQLException {
        String findSql = "SELECT attempt_id FROM attempts WHERE session_id = ? AND user_id = ? AND status = 'IN_PROGRESS' ORDER BY attempt_id DESC LIMIT 1";
        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("attempt_id");
            }
        }

        int nextAttemptNumber = 1;
        String countSql = "SELECT COALESCE(MAX(attempt_number), 0) + 1 FROM attempts WHERE session_id = ? AND user_id = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement psC = conn.prepareStatement(countSql)) {
            psC.setInt(1, sessionId);
            psC.setInt(2, userId);
            try (ResultSet rsC = psC.executeQuery()) {
                if (rsC.next()) nextAttemptNumber = rsC.getInt(1);
            }
        }

        String insSql = "INSERT INTO attempts (session_id, user_id, attempt_number, started_at, status) VALUES (?, ?, ?, NOW(), 'IN_PROGRESS')";
        try (Connection conn = getActiveConnection();
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

    // 3. Tải câu hỏi và các lựa chọn
    public List<QuestionDTO> loadExamQuestions(int examId) throws SQLException {
        try (Connection conn = getActiveConnection()) {
            return loadExamQuestions(examId, conn);
        }
    }

    public List<QuestionDTO> loadExamQuestions(int examId, Connection conn) throws SQLException {
        List<QuestionDTO> list = new ArrayList<>();
        String sql = """
            SELECT q.question_id, q.question_text, q.question_type, eq.points
            FROM exam_questions eq
            JOIN questions q ON eq.question_id = q.question_id
            WHERE eq.exam_id = ? 
            ORDER BY eq.display_order ASC
        """;
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

    public List<OptionDTO> loadOptions(int questionId, Connection conn) throws SQLException {
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

    // 4. Lấy chi tiết bài thi để làm (Take Exam)
    public TakeExamDetailDTO getTakeExamDetail(int attemptId) throws SQLException {
        String sql = """
            SELECT a.attempt_id, es.session_name, e.exam_id, e.exam_title AS exam_title, e.duration_minutes
            FROM attempts a
            JOIN exam_sessions es ON a.session_id = es.session_id
            JOIN exams e ON es.exam_id = e.exam_id
            WHERE a.attempt_id = ? AND a.status = 'IN_PROGRESS'
        """;

        try (Connection conn = getActiveConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TakeExamDetailDTO dto = new TakeExamDetailDTO();
                    dto.setAttemptId(rs.getInt("attempt_id"));
                    dto.setExamId(rs.getInt("exam_id"));
                    dto.setExamTitle(rs.getString("exam_title"));
                    dto.setSessionName(rs.getString("session_name"));
                    dto.setDurationMinutes(rs.getInt("duration_minutes"));
                    dto.setQuestions(loadExamQuestions(dto.getExamId(), conn));
                    return dto;
                }
            }
        }
        return null;
    }

    // 5. Lưu toàn bộ bài nộp của học sinh (Sử dụng Transaction an toàn)
    public void submitAttemptAnswers(int attemptId, Map<String, String[]> parameterMap) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lấy danh sách câu hỏi theo attempt
                String qListSql = """
                    SELECT q.question_id, q.question_type 
                    FROM exam_questions eq
                    JOIN questions q ON eq.question_id = q.question_id
                    JOIN exam_sessions es ON eq.exam_id = es.exam_id
                    JOIN attempts a ON es.session_id = a.session_id
                    WHERE a.attempt_id = ?
                """;

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

                // 5.1 Lưu trắc nghiệm 1 đáp án (SINGLE)
                for (int qId : singleQIds) {
                    String[] vals = parameterMap.get("q_" + qId);
                    String optIdStr = (vals != null && vals.length > 0) ? vals[0] : null;
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

                // 5.2 Lưu trắc nghiệm đúng/sai (TRUE_FALSE)
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
                                String[] vals = parameterMap.get("tf_opt_" + opt.optionId);
                                String choice = (vals != null && vals.length > 0) ? vals[0] : null;
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

                // 5.3 Lưu câu trả lời ngắn (SHORT_ANSWER)
                String insShortSql = "INSERT INTO attempt_answers (attempt_id, question_id, answer_text, points_awarded) VALUES (?, ?, ?, 0)";
                for (int qId : shortAnsQIds) {
                    String[] vals = parameterMap.get("short_q_" + qId);
                    String shortText = (vals != null && vals.length > 0) ? vals[0] : null;
                    if (shortText != null && !shortText.trim().isEmpty()) {
                        try (PreparedStatement psS = conn.prepareStatement(insShortSql)) {
                            psS.setInt(1, attemptId);
                            psS.setInt(2, qId);
                            psS.setString(3, shortText.trim());
                            psS.executeUpdate();
                        }
                    }
                }

                // 5.4 Cập nhật trạng thái bài thi sang SUBMITTED
                String updateAttSql = "UPDATE attempts SET submitted_at = NOW(), status = 'SUBMITTED' WHERE attempt_id = ?";
                try (PreparedStatement psUp = conn.prepareStatement(updateAttSql)) {
                    psUp.setInt(1, attemptId);
                    psUp.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
}