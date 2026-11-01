package com.onlineexam.controller;

import com.onlineexam.model.User;
import com.onlineexam.model.Role;
import com.onlineexam.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/auth/login")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    // GET: Hiển thị trang đăng nhập
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)

            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    // POST: Xử lý đăng nhập
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        System.out.println("test1");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            // Bước 1: Xác thực tài khoản
            User user = authService.login(username, password);

            if (user == null) {
                request.setAttribute(
                        "error",
                        "Tên đăng nhập hoặc mật khẩu không đúng");

                request.getRequestDispatcher(
                        "/WEB-INF/views/auth/false.jsp").forward(request, response);

                return;
            }

            // Bước 2: Kiểm tra quyền
            Role role = user.getRole();

            if (role == null || role.getRoleName() == null) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản chưa được phân quyền");
                return;
            }

            String roleName = role.getRoleName();

            // Bước 3: Xác định trang tương ứng
            String destination;

            switch (roleName) {
                case "ADMIN":
                    destination = "/admin";
                    break;

                case "TEACHER":
                    destination = "/teacher/home";
                    break;

                case "STUDENT":
                    destination = "/student/home";
                    break;

                default:
                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "Quyền truy cập không hợp lệ");
                    return;
            }

            // Bước 4: Tạo Session
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            session = request.getSession(true);

            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getUsername());
            session.setAttribute("role", roleName);
            session.setAttribute("fullName", user.getFullName());
            // Hết hạn sau 30 phút không hoạt động
            session.setMaxInactiveInterval(30 * 60);

            // Bước 5: Chuyển trang

            response.sendRedirect(
                    request.getContextPath() + destination);

        } catch (Exception e) {

            System.err.println("===== LOI KHI DANG NHAP =====");

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi xử lý đăng nhập, kiểm tra log Tomcat");
        }
    }

}