
package com.onlineexam.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy Session hiện tại, không tạo Session mới
        HttpSession session = request.getSession(false);

        // Chưa có Session -> hiển thị Login
        if (session == null) {
            showLogin(request, response);
            return;
        }

        String role = (String) session.getAttribute("role");
        Object userId = session.getAttribute("userId");

        // Session không có thông tin đăng nhập
        if (userId == null || role == null) {
            showLogin(request, response);
            return;
        }

        // Điều hướng theo vai trò
        String contextPath = request.getContextPath();

        switch (role.toUpperCase(java.util.Locale.ROOT)) {

            case "ADMIN":
                response.sendRedirect(contextPath + "/admin");
                break;

            case "TEACHER":
                response.sendRedirect(contextPath + "/teacher");
                break;

            case "STUDENT":
                response.sendRedirect(contextPath + "/student");
                break;

            default:
                showLogin(request, response);
                break;
        }
    }

    private void showLogin(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/login.jsp").forward(request, response);
    }
}
