package com.onlineexam.controller;

import com.onlineexam.dao.ResultDAO;
import com.onlineexam.dao.ResultDAO.AttemptResultDetailDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet({"/student/results", "/results"})
public class ResultServlet extends HttpServlet {

    private final ResultDAO resultDAO = new ResultDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String attemptIdStr = request.getParameter("attemptId");
        if (attemptIdStr == null || attemptIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        try {
            int attemptId = Integer.parseInt(attemptIdStr);
            AttemptResultDetailDTO detail = resultDAO.getAttemptResult(attemptId);

            if (detail == null) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }

            request.setAttribute("attemptId", detail.getAttemptId());
            request.setAttribute("examTitle", detail.getExamTitle());
            request.setAttribute("sessionName", detail.getSessionName());
            request.setAttribute("studentName", detail.getStudentName());
            request.setAttribute("score", detail.getScore());
            request.setAttribute("passScore", detail.getPassScore());
            request.setAttribute("status", detail.getStatus());
            request.setAttribute("startedAt", detail.getStartedAt());
            request.setAttribute("submittedAt", detail.getSubmittedAt());
            request.setAttribute("answers", detail.getAnswers());
            request.setAttribute("correctCount", detail.getCorrectCount());
            request.setAttribute("totalQuestions", detail.getTotalQuestions());

            request.getRequestDispatcher("/WEB-INF/views/student/results.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp kết quả bài thi: " + e.getMessage());
        }
    }
}