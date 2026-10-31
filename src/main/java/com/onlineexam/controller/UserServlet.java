
package com.onlineexam.controller;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;
import com.onlineexam.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/users")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Kiểm tra quyền ADMIN
        String role = (String) session.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý tài khoản");
            return;
        }

        try {
            // 3. Lấy toàn bộ người dùng
            List<User> users = userDAO.findAll();

            // 4. Gửi dữ liệu sang JSP
            request.setAttribute("users", users);

            // 5. Hiển thị trang quản lý tài khoản
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/users.jsp").forward(request, response);

        } catch (SQLException e) {
            log("Loi khi lay danh sach nguoi dung", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tải danh sách người dùng");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        int roleId = Integer.parseInt(request.getParameter("role"));
        // Kiểm tra dữ liệu và lưu vào database qua Service
        try {
            userService.createUser(username, email, password, fullName, roleId);

        } catch (Exception e) {
            // TODO: handle exception
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/users");
    }
}
