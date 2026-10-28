
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
