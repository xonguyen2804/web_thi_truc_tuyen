package com.onlineexam.controller;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.dao.ClassDAO.ClassDTO;
import com.onlineexam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/teacher/classes")
public class ClassServlet extends HttpServlet {

    private final ClassDAO classDAO = new ClassDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 2. Kiểm tra quyền TEACHER hoặc ADMIN
        String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "";
        if (!"TEACHER".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này");
            return;
        }

        try {
            // Lấy dữ liệu qua DAO
            List<ClassDTO> classes = classDAO.findAllWithGroups();
            request.setAttribute("classList", classes);
            request.getRequestDispatcher("/WEB-INF/views/teacher/classes.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp danh sách lớp: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("createClass".equals(action)) {
                String className = request.getParameter("className");
                classDAO.createClass(className, user.getUserId());

            } else if ("createGroup".equals(action)) {
                int classId = Integer.parseInt(request.getParameter("classId"));
                String groupName = request.getParameter("groupName");
                String desc = request.getParameter("description");
                classDAO.createGroup(classId, groupName, desc);
            }

            response.sendRedirect(request.getContextPath() + "/teacher/classes?success=true");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/teacher/classes?error=" + e.getMessage());
        }
    }
}