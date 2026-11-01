// package com.onlineexam.controller;

// import com.onlineexam.model.Exam;
// import com.onlineexam.model.Option;
// import com.onlineexam.model.Question;
// import com.onlineexam.model.User;
// import com.onlineexam.service.QuestionService;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// /**
//  * Controller xử lý các yêu cầu liên quan đến Ngân hàng câu hỏi.
//  */
// @WebServlet({"/teacher/questions", "/questions"})
// public class QuestionServlet extends HttpServlet {

//     private final QuestionService questionService;

//     public QuestionServlet() {
//         this.questionService = new QuestionService();
//     }

//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");
//         response.setCharacterEncoding("UTF-8");

//         String action = request.getParameter("action");
//         if ("get_detail".equalsIgnoreCase(action)) {
//             handleGetQuestionDetail(request, response);
//             return;
//         }

//         try {
//             String keyword = request.getParameter("keyword");
//             String type = request.getParameter("type");
//             String examIdParam = request.getParameter("examId");
//             Integer examId = null;
//             if (examIdParam != null && !examIdParam.isBlank()) {
//                 try {
//                     examId = Integer.parseInt(examIdParam.trim());
//                 } catch (NumberFormatException ignored) {}
//             }

//             // 1. Tải danh sách câu hỏi theo bộ lọc
//             List<Question> questions = questionService.getFilteredQuestions(keyword, type, examId);

//             // 2. Thống kê theo loại câu hỏi
//             Map<String, Integer> stats = questionService.getStatistics();

//             // 3. Tải danh sách các đề thi để chọn bộ lọc và ghép đề
//             List<Exam> exams = questionService.getAllExams();

//             request.setAttribute("questions", questions);
//             request.setAttribute("stats", stats);
//             request.setAttribute("exams", exams);
//             request.setAttribute("keyword", keyword);
//             request.setAttribute("selectedType", type);
//             request.setAttribute("selectedExamId", examId);

//         } catch (SQLException e) {
//             e.printStackTrace();
//             request.setAttribute("errorMessage", "Lỗi tải cơ sở dữ liệu câu hỏi: " + e.getMessage());
//         }

//         request.getRequestDispatcher("/WEB-INF/views/teacher/questions.jsp").forward(request, response);
//     }

//     @Override
//     protected void doPost(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");
//         response.setCharacterEncoding("UTF-8");

//         String action = request.getParameter("action");
//         if (action == null || action.isBlank()) {
//             action = "create";
//         }

//         // Xác định ID người tạo (giáo viên đăng nhập hoặc mặc định 1)
//         User currentUser = (User) request.getSession().getAttribute("user");
//         int userId = (currentUser != null) ? currentUser.getUserId() : 1;

//         try {
//             switch (action) {
//                 case "create":
//                     handleCreateQuestion(request, userId);
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions?msg=created_success");
//                     break;

//                 case "update":
//                     handleUpdateQuestion(request);
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions?msg=updated_success");
//                     break;

//                 case "delete":
//                     handleDeleteQuestion(request);
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions?msg=deleted_success");
//                     break;

//                 case "create_exam_from_bank":
//                     int newExamId = handleCreateExamFromBank(request, userId);
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions?msg=exam_created_success&newExamId=" + newExamId);
//                     break;

//                 case "add_to_exam":
//                     int addedCount = handleAddToExam(request);
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions?msg=added_to_exam_success&addedCount=" + addedCount);
//                     break;

//                 default:
//                     response.sendRedirect(request.getContextPath() + "/teacher/questions");
//                     break;
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             request.getSession().setAttribute("errorMessage", "Thao tác thất bại: " + e.getMessage());
//             response.sendRedirect(request.getContextPath() + "/teacher/questions?error=true");
//         }
//     }

//     private void handleCreateQuestion(HttpServletRequest request, int userId) throws SQLException {
//         String questionText = request.getParameter("questionText");
//         String questionType = request.getParameter("questionType");
//         String defaultPointsStr = request.getParameter("defaultPoints");
//         String explanation = request.getParameter("explanation");

//         BigDecimal points = new BigDecimal("0.25");
//         if (defaultPointsStr != null && !defaultPointsStr.isBlank()) {
//             try {
//                 points = new BigDecimal(defaultPointsStr.trim());
//             } catch (NumberFormatException ignored) {}
//         }

//         Question q = new Question();
//         q.setCreatedBy(userId);
//         q.setQuestionText(questionText != null ? questionText.trim() : "");
//         q.setQuestionType(questionType != null ? questionType.trim() : "SINGLE");
//         q.setDefaultPoints(points);
//         q.setExplanation(explanation != null ? explanation.trim() : "");
//         q.setStatus("ACTIVE");

//         List<Option> options = new ArrayList<>();
//         String shortAnswer = null;

//         if ("SINGLE".equalsIgnoreCase(questionType)) {
//             String correctIndexStr = request.getParameter("correctOption");
//             int correctIndex = (correctIndexStr != null && !correctIndexStr.isBlank()) ? Integer.parseInt(correctIndexStr) : 1;
//             for (int i = 1; i <= 4; i++) {
//                 String optText = request.getParameter("optionText_" + i);
//                 if (optText != null && !optText.isBlank()) {
//                     options.add(new Option(optText.trim(), (i == correctIndex), i));
//                 }
//             }
//         } else if ("TRUE_FALSE".equalsIgnoreCase(questionType)) {
//             for (int i = 1; i <= 4; i++) {
//                 String optText = request.getParameter("tfText_" + i);
//                 String isCorrectVal = request.getParameter("tfCorrect_" + i);
//                 boolean isCorrect = "true".equalsIgnoreCase(isCorrectVal);
//                 if (optText != null && !optText.isBlank()) {
//                     options.add(new Option(optText.trim(), isCorrect, i));
//                 }
//             }
//         } else if ("SHORT_ANSWER".equalsIgnoreCase(questionType)) {
//             shortAnswer = request.getParameter("shortAnswer");
//             if (shortAnswer != null && shortAnswer.length() > 4) {
//                 shortAnswer = shortAnswer.substring(0, 4);
//             }
//         }

//         questionService.createQuestion(q, options, shortAnswer);
//     }

//     private void handleUpdateQuestion(HttpServletRequest request) throws SQLException {
//         int questionId = Integer.parseInt(request.getParameter("questionId"));
//         String questionText = request.getParameter("questionText");
//         String defaultPointsStr = request.getParameter("defaultPoints");
//         String explanation = request.getParameter("explanation");
//         String questionType = request.getParameter("questionType");

//         Question existing = questionService.getQuestionById(questionId);
//         if (existing == null) {
//             throw new IllegalArgumentException("Không tìm thấy câu hỏi #" + questionId);
//         }

//         BigDecimal points = existing.getDefaultPoints();
//         if (defaultPointsStr != null && !defaultPointsStr.isBlank()) {
//             try {
//                 points = new BigDecimal(defaultPointsStr.trim());
//             } catch (NumberFormatException ignored) {}
//         }

//         existing.setQuestionText(questionText != null ? questionText.trim() : "");
//         existing.setDefaultPoints(points);
//         existing.setExplanation(explanation != null ? explanation.trim() : "");

//         List<Option> options = new ArrayList<>();
//         String shortAnswer = null;

//         if ("SINGLE".equalsIgnoreCase(existing.getQuestionType())) {
//             String correctIndexStr = request.getParameter("editCorrectOption");
//             int correctIndex = (correctIndexStr != null && !correctIndexStr.isBlank()) ? Integer.parseInt(correctIndexStr) : 1;
//             for (int i = 1; i <= 4; i++) {
//                 String optText = request.getParameter("editOptionText_" + i);
//                 if (optText != null && !optText.isBlank()) {
//                     options.add(new Option(optText.trim(), (i == correctIndex), i));
//                 }
//             }
//         } else if ("TRUE_FALSE".equalsIgnoreCase(existing.getQuestionType())) {
//             for (int i = 1; i <= 4; i++) {
//                 String optText = request.getParameter("editTfText_" + i);
//                 String isCorrectVal = request.getParameter("editTfCorrect_" + i);
//                 boolean isCorrect = "true".equalsIgnoreCase(isCorrectVal);
//                 if (optText != null && !optText.isBlank()) {
//                     options.add(new Option(optText.trim(), isCorrect, i));
//                 }
//             }
//         } else if ("SHORT_ANSWER".equalsIgnoreCase(existing.getQuestionType())) {
//             shortAnswer = request.getParameter("editShortAnswer");
//             if (shortAnswer != null && shortAnswer.length() > 4) {
//                 shortAnswer = shortAnswer.substring(0, 4);
//             }
//         }

//         questionService.updateQuestion(existing, options, shortAnswer);
//     }

//     private void handleDeleteQuestion(HttpServletRequest request) throws SQLException {
//         int questionId = Integer.parseInt(request.getParameter("questionId"));
//         questionService.deleteQuestion(questionId);
//     }

//     private int handleCreateExamFromBank(HttpServletRequest request, int userId) throws SQLException {
//         String examTitle = request.getParameter("newExamTitle");
//         String description = request.getParameter("newExamDescription");
//         int durationMinutes = Integer.parseInt(request.getParameter("newExamDuration"));
//         BigDecimal passScore = new BigDecimal(request.getParameter("newExamPassScore"));
//         String status = request.getParameter("newExamStatus");

//         String[] selected = request.getParameterValues("selectedQuestions");
//         List<Integer> questionIds = new ArrayList<>();
//         if (selected != null) {
//             for (String s : selected) {
//                 try {
//                     questionIds.add(Integer.parseInt(s));
//                 } catch (NumberFormatException ignored) {}
//             }
//         }

//         if (questionIds.isEmpty()) {
//             throw new IllegalArgumentException("Vui lòng chọn ít nhất một câu hỏi để tạo đề thi mới.");
//         }

//         Exam exam = new Exam();
//         exam.setCreatedBy(userId);
//         exam.setExamTitle(examTitle != null ? examTitle.trim() : "Đề thi mới từ ngân hàng");
//         exam.setDescription(description);
//         exam.setDurationMinutes(durationMinutes);
//         exam.setPassScore(passScore);
//         exam.setStatus(status != null ? status : "PUBLISHED");

//         return questionService.createExamFromQuestions(exam, questionIds);
//     }

//     private int handleAddToExam(HttpServletRequest request) throws SQLException {
//         int targetExamId = Integer.parseInt(request.getParameter("targetExamId"));
//         String[] selected = request.getParameterValues("selectedQuestions");
//         List<Integer> questionIds = new ArrayList<>();
//         if (selected != null) {
//             for (String s : selected) {
//                 try {
//                     questionIds.add(Integer.parseInt(s));
//                 } catch (NumberFormatException ignored) {}
//             }
//         }

//         if (questionIds.isEmpty()) {
//             throw new IllegalArgumentException("Vui lòng chọn ít nhất một câu hỏi.");
//         }

//         return questionService.addQuestionsToExam(targetExamId, questionIds);
//     }

//     private void handleGetQuestionDetail(HttpServletRequest request, HttpServletResponse response) throws IOException {
//         response.setContentType("application/json;charset=UTF-8");
//         try {
//             int qId = Integer.parseInt(request.getParameter("id"));
//             Question q = questionService.getQuestionById(qId);
//             if (q == null) {
//                 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                 response.getWriter().write("{\"error\": \"Not found\"}");
//                 return;
//             }

//             StringBuilder json = new StringBuilder();
//             json.append("{");
//             json.append("\"questionId\":").append(q.getQuestionId()).append(",");
//             json.append("\"questionText\":").append(escapeJson(q.getQuestionText())).append(",");
//             json.append("\"questionType\":").append(escapeJson(q.getQuestionType())).append(",");
//             json.append("\"defaultPoints\":").append(q.getDefaultPoints()).append(",");
//             json.append("\"explanation\":").append(escapeJson(q.getExplanation() != null ? q.getExplanation() : "")).append(",");
//             json.append("\"shortAnswer\":").append(escapeJson(q.getShortAnswer() != null ? q.getShortAnswer() : "")).append(",");
//             json.append("\"options\":[");
//             List<Option> opts = q.getOptions();
//             for (int i = 0; i < opts.size(); i++) {
//                 Option opt = opts.get(i);
//                 json.append("{");
//                 json.append("\"optionId\":").append(opt.getOptionId()).append(",");
//                 json.append("\"optionText\":").append(escapeJson(opt.getOptionText())).append(",");
//                 json.append("\"isCorrect\":").append(opt.isCorrect()).append(",");
//                 json.append("\"displayOrder\":").append(opt.getDisplayOrder());
//                 json.append("}");
//                 if (i < opts.size() - 1) json.append(",");
//             }
//             json.append("]");
//             json.append("}");

//             response.getWriter().write(json.toString());
//         } catch (Exception e) {
//             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//             response.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
//         }
//     }

//     private String escapeJson(String s) {
//         if (s == null) return "\"\"";
//         return "\"" + s.replace("\\", "\\\\")
//                        .replace("\"", "\\\"")
//                        .replace("\n", "\\n")
//                        .replace("\r", "\\r")
//                        .replace("\t", "\\t") + "\"";
//     }
// }


package com.onlineexam.controller;

import com.onlineexam.model.Question;
import com.onlineexam.service.QuestionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/teacher/questions")
public class QuestionServlet extends HttpServlet {
    private QuestionService questionService;

    @Override
    public void init() {
        questionService = new QuestionService();
    }

    private int getTeacherId(HttpServletRequest request)
            throws ServletException {
        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Number)) {
            throw new ServletException("Bạn chưa đăng nhập.");
        }

        return ((Number) session.getAttribute("userId")).intValue();
    }

    private boolean ownsQuestion(int questionId, int teacherId)
            throws SQLException {
        Question question = questionService.getQuestionById(questionId);

        return question != null
                && question.getCreatedBy() == teacherId;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);
            List<Question> questions =
                    questionService.getQuestionsByTeacher(teacherId);

            request.setAttribute("questions", questions);
            request.getRequestDispatcher(
                    "/WEB-INF/views/teacher/questions.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Không thể tải ngân hàng câu hỏi.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);
            String action = request.getParameter("action");

            if ("create".equals(action)) {
                String questionText = request.getParameter("questionText");
                String questionType = request.getParameter("questionType");
                String pointsText = request.getParameter("points");
                String explanation = request.getParameter("explanation");

                BigDecimal points = new BigDecimal(pointsText);

                questionService.createQuestion(
                        teacherId,
                        questionText,
                        questionType,
                        points,
                        explanation
                );

            } else if ("update".equals(action)) {
                int questionId = Integer.parseInt(
                        request.getParameter("questionId")
                );

                if (!ownsQuestion(questionId, teacherId)) {
                    response.sendError(404, "Không tìm thấy câu hỏi.");
                    return;
                }

                String questionText = request.getParameter("questionText");
                String questionType = request.getParameter("questionType");
                BigDecimal points = new BigDecimal(
                        request.getParameter("points")
                );
                String explanation = request.getParameter("explanation");
                String status = request.getParameter("status");

                questionService.updateQuestion(
                        questionId,
                        questionText,
                        questionType,
                        points,
                        explanation,
                        status
                );

            } else if ("deactivate".equals(action)) {
                int questionId = Integer.parseInt(
                        request.getParameter("questionId")
                );

                if (!ownsQuestion(questionId, teacherId)) {
                    response.sendError(404, "Không tìm thấy câu hỏi.");
                    return;
                }

                questionService.deactivateQuestion(questionId);

            } else {
                response.sendError(400, "Hành động không hợp lệ.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/teacher/questions"
            );

        } catch (NumberFormatException e) {
            response.sendError(400, "ID hoặc số điểm không hợp lệ.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể xử lý câu hỏi.", e);
        }
    }
}