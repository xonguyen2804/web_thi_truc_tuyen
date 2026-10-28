
package com.onlineexam.controller;

import com.onlineexam.model.SchoolClass;
import com.onlineexam.service.ClassService;

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
    private ClassService classService;

    @Override
    public void init() {
        classService = new ClassService();
    }

    private int getTeacherId(HttpServletRequest request)
            throws ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new ServletException("Bạn chưa đăng nhập.");
        }

        Object value = session.getAttribute("userId");
        if (!(value instanceof Number)) {
            throw new ServletException("Thông tin đăng nhập không hợp lệ.");
        }

        return ((Number) value).intValue();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);
            List<SchoolClass> classes =
                    classService.getClassesByTeacher(teacherId);

            request.setAttribute("classes", classes);
            request.getRequestDispatcher(
                    "/WEB-INF/views/teacher/classes.jsp"
            ).forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải danh sách lớp.", e);
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
                String className = request.getParameter("className");
                String description = request.getParameter("description");

                classService.createClass(className, description, teacherId);

            } else if ("update".equals(action)) {
                int classId = Integer.parseInt(
                        request.getParameter("classId")
                );
                String className = request.getParameter("className");
                String description = request.getParameter("description");

                SchoolClass schoolClass =
                        classService.getClassById(classId);

                if (schoolClass == null
                        || schoolClass.getCreatedBy() != teacherId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                classService.updateClass(classId, className, description);

            } else if ("delete".equals(action)) {
                int classId = Integer.parseInt(
                        request.getParameter("classId")
                );

                SchoolClass schoolClass =
                        classService.getClassById(classId);

                if (schoolClass == null
                        || schoolClass.getCreatedBy() != teacherId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                classService.deleteClass(classId);

            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Hành động không hợp lệ."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/teacher/classes"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID không hợp lệ."
            );
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể xử lý lớp học.", e);
        }
    }
}
