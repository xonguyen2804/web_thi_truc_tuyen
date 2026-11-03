// package com.onlineexam.controller;

// import com.onlineexam.model.Question;
// import com.onlineexam.service.QuestionService;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.sql.SQLException;
// import java.util.List;

// @WebServlet("/teacher/questions")
// public class QuestionServlet extends HttpServlet {
//     private QuestionService questionService;

//     @Override
//     public void init() {
//         questionService = new QuestionService();
//     }

//     private int getTeacherId(HttpServletRequest request)
//             throws ServletException {
//         HttpSession session = request.getSession(false);

//         if (session == null || !(session.getAttribute("userId") instanceof Number)) {
//             throw new ServletException("Bạn chưa đăng nhập.");
//         }

//         return ((Number) session.getAttribute("userId")).intValue();
//     }

//     private boolean ownsQuestion(int questionId, int teacherId)
//             throws SQLException {
//         Question question = questionService.getQuestionById(questionId);

//         return question != null
//                 && question.getCreatedBy() == teacherId;
//     }

//     @Override
//     protected void doGet(HttpServletRequest request,
//                          HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");

//         try {
//             int teacherId = getTeacherId(request);
//             List<Question> questions =
//                     questionService.getQuestionsByTeacher(teacherId);

//             request.setAttribute("questions", questions);
//             request.getRequestDispatcher(
//                     "/WEB-INF/views/teacher/questions.jsp"
//             ).forward(request, response);

//         } catch (SQLException e) {
//             throw new ServletException("Không thể tải ngân hàng câu hỏi.", e);
//         }
//     }

//     @Override
//     protected void doPost(HttpServletRequest request,
//                           HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");

//         try {
//             int teacherId = getTeacherId(request);
//             String action = request.getParameter("action");

//             if ("create".equals(action)) {
//                 String questionText = request.getParameter("questionText");
//                 String questionType = request.getParameter("questionType");
//                 String pointsText = request.getParameter("points");
//                 String explanation = request.getParameter("explanation");

//                 BigDecimal points = new BigDecimal(pointsText);

//                 questionService.createQuestion(
//                         teacherId,
//                         questionText,
//                         questionType,
//                         points,
//                         explanation
//                 );

//             } else if ("update".equals(action)) {
//                 int questionId = Integer.parseInt(
//                         request.getParameter("questionId")
//                 );

//                 if (!ownsQuestion(questionId, teacherId)) {
//                     response.sendError(404, "Không tìm thấy câu hỏi.");
//                     return;
//                 }

//                 String questionText = request.getParameter("questionText");
//                 String questionType = request.getParameter("questionType");
//                 BigDecimal points = new BigDecimal(
//                         request.getParameter("points")
//                 );
//                 String explanation = request.getParameter("explanation");
//                 String status = request.getParameter("status");

//                 questionService.updateQuestion(
//                         questionId,
//                         questionText,
//                         questionType,
//                         points,
//                         explanation,
//                         status
//                 );

//             } else if ("deactivate".equals(action)) {
//                 int questionId = Integer.parseInt(
//                         request.getParameter("questionId")
//                 );

//                 if (!ownsQuestion(questionId, teacherId)) {
//                     response.sendError(404, "Không tìm thấy câu hỏi.");
//                     return;
//                 }

//                 questionService.deactivateQuestion(questionId);

//             } else {
//                 response.sendError(400, "Hành động không hợp lệ.");
//                 return;
//             }

//             response.sendRedirect(
//                     request.getContextPath() + "/teacher/questions"
//             );

//         } catch (NumberFormatException e) {
//             response.sendError(400, "ID hoặc số điểm không hợp lệ.");
//         } catch (IllegalArgumentException e) {
//             request.setAttribute("error", e.getMessage());
//             doGet(request, response);
//         } catch (SQLException e) {
//             throw new ServletException("Không thể xử lý câu hỏi.", e);
//         }
//     }
// }

package com.onlineexam.controller;

import com.onlineexam.model.Question;
import com.onlineexam.model.Option;
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
import java.util.ArrayList;
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

        if (session == null
                || !(session.getAttribute("userId") instanceof Number)) {
            throw new ServletException("Bạn chưa đăng nhập.");
        }

        return ((Number) session.getAttribute("userId")).intValue();
    }

    private boolean ownsQuestion(int questionId, int teacherId)
            throws SQLException {

        Question question =
                questionService.getQuestionById(questionId);

        return question != null
                && question.getCreatedBy() == teacherId;
    }

    // ==================== GET ====================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);

            String action = request.getParameter("action");

            // AJAX: lấy chi tiết câu hỏi để sửa
            if ("get_detail".equals(action)) {
                handleGetDetail(request, response, teacherId);
                return;
            }

            String keyword = request.getParameter("keyword");
            String type = request.getParameter("type");

            if (type == null || type.isBlank()) {
                type = "ALL";
            }

            Integer examId = null;
            String examIdText = request.getParameter("examId");

            if (examIdText != null && !examIdText.isBlank()) {
                examId = Integer.parseInt(examIdText);
            }

            List<Question> questions =
                    questionService.getFilteredQuestions(
                            teacherId, keyword, type, examId
                    );

            request.setAttribute("questions", questions);
            request.setAttribute(
                    "stats", questionService.getStatistics(teacherId)
            );
            request.setAttribute(
                    "exams", questionService.getExamsByTeacher(teacherId)
            );

            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedType", type);
            request.setAttribute("selectedExamId", examId);

            request.getRequestDispatcher(
                    "/WEB-INF/views/teacher/questions.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "ID đề thi không hợp lệ.");
        } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể tải ngân hàng câu hỏi.", e
            );
        }
    }

    // ==================== GET DETAIL JSON ====================

    private void handleGetDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            int teacherId)
            throws IOException, SQLException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            int questionId = Integer.parseInt(
                    request.getParameter("id")
            );

            Question question =
                    questionService.getQuestionById(questionId);

            if (question == null
                    || question.getCreatedBy() != teacherId) {
                response.setStatus(404);
                response.getWriter().write(
                        "{\"error\":\"Không tìm thấy câu hỏi.\"}"
                );
                return;
            }

            StringBuilder json = new StringBuilder();

            json.append("{");
            json.append("\"questionId\":").append(question.getQuestionId()).append(",");
            json.append("\"questionType\":\"").append(escapeJson(question.getQuestionType())).append("\",");
            json.append("\"questionText\":\"").append(escapeJson(question.getQuestionText())).append("\",");
            json.append("\"defaultPoints\":").append(question.getDefaultPoints()).append(",");
            json.append("\"explanation\":\"").append(escapeJson(question.getExplanation())).append("\",");
            json.append("\"shortAnswer\":\"").append(escapeJson(question.getShortAnswer())).append("\",");

            json.append("\"options\":[");

            List<Option> options = question.getOptions();

            for (int i = 0; i < options.size(); i++) {
                Option option = options.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"optionId\":").append(option.getOptionId()).append(",")
                        .append("\"optionText\":\"").append(escapeJson(option.getOptionText())).append("\",")
                        .append("\"isCorrect\":").append(option.isCorrect())
                        .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().write(
                    "{\"error\":\"ID câu hỏi không hợp lệ.\"}"
            );
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==================== POST ====================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);
            String action = request.getParameter("action");

            if ("update".equals(action)) {
                handleUpdate(request, response, teacherId);

            } else if ("deactivate".equals(action)
                    || "delete".equals(action)) {

                int questionId = Integer.parseInt(
                        request.getParameter("questionId")
                );

                if (!ownsQuestion(questionId, teacherId)) {
                    response.sendError(404, "Không tìm thấy câu hỏi.");
                    return;
                }

                questionService.deactivateQuestion(questionId);

                response.sendRedirect(
                        request.getContextPath()
                                + "/teacher/questions?msg=deleted_success"
                );

            } else {
                response.sendError(
                        400,
                        "Action chưa được hỗ trợ: " + action
                );
            }

        } catch (NumberFormatException e) {
            response.sendError(400, "ID hoặc điểm số không hợp lệ.");

        } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage());

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể cập nhật câu hỏi.", e
            );
        }
    }

    // ==================== UPDATE ====================

    private void handleUpdate(
            HttpServletRequest request,
            HttpServletResponse response,
            int teacherId)
            throws SQLException, IOException {

        int questionId = Integer.parseInt(
                request.getParameter("questionId")
        );

        if (!ownsQuestion(questionId, teacherId)) {
            response.sendError(404, "Không tìm thấy câu hỏi.");
            return;
        }

        String questionText =
                request.getParameter("questionText");

        String questionType =
                request.getParameter("questionType");

        String pointsText = request.getParameter("defaultPoints");

        if (pointsText == null || pointsText.isBlank()) {
            pointsText = request.getParameter("points");
        }

        BigDecimal points = new BigDecimal(pointsText);

        String explanation =
                request.getParameter("explanation");

        List<Option> options = new ArrayList<>();
        String shortAnswer = null;

        if ("SINGLE".equals(questionType)) {

            int correctOption = Integer.parseInt(
                    request.getParameter("editCorrectOption")
            );

            for (int i = 1; i <= 4; i++) {
                String text = request.getParameter(
                        "editOptionText_" + i
                );

                options.add(new Option(
                        text == null ? "" : text.trim(),
                        i == correctOption,
                        i
                ));
            }

        } else if ("TRUE_FALSE".equals(questionType)) {

            for (int i = 1; i <= 4; i++) {
                String text = request.getParameter(
                        "editTfText_" + i
                );

                boolean correct = Boolean.parseBoolean(
                        request.getParameter("editTfCorrect_" + i)
                );

                options.add(new Option(
                        text == null ? "" : text.trim(),
                        correct,
                        i
                ));
            }

        } else if ("SHORT_ANSWER".equals(questionType)) {

            shortAnswer = request.getParameter("editShortAnswer");
        }

        boolean updated = questionService.updateQuestion(
                questionId,
                teacherId,
                questionText,
                questionType,
                points,
                explanation,
                options,
                shortAnswer
        );

        if (!updated) {
            response.sendError(404, "Không thể cập nhật câu hỏi.");
            return;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/teacher/questions?msg=updated_success"
        );
    }
}