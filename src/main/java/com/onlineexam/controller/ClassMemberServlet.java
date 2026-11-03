
package com.onlineexam.controller;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.dao.ClassMemberDAO;
import com.onlineexam.dao.ClassMemberDAO.MemberDTO;
import com.onlineexam.model.SchoolClass;
import com.onlineexam.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/teacher/class-members")
public class ClassMemberServlet extends HttpServlet {

    private final ClassDAO classDAO = new ClassDAO();
    private final ClassMemberDAO memberDAO = new ClassMemberDAO();

    // Kiểm tra tài khoản và quyền truy cập
    private User checkLogin(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/auth/login"
            );
            return null;
        }

        String role = user.getRole() == null
                ? ""
                : user.getRole().getRoleName();

        if (!"TEACHER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {

            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền truy cập chức năng này"
            );

            return null;
        }

        return user;
    }

    // Kiểm tra lớp có tồn tại và thuộc giáo viên không
    private SchoolClass checkClass(int classId, User user,
                                   HttpServletResponse response)
            throws SQLException, IOException {

        SchoolClass schoolClass = classDAO.findById(classId);

        if (schoolClass == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy lớp học"
            );
            return null;
        }

        String role = user.getRole() == null
                ? ""
                : user.getRole().getRoleName();

        if (!"ADMIN".equalsIgnoreCase(role)
                && schoolClass.getCreatedBy() != user.getUserId()) {

            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền quản lý lớp này"
            );

            return null;
        }

        return schoolClass;
    }

    // GET: Xem danh sách thành viên
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = checkLogin(request, response);

        if (user == null) {
            return;
        }

        try {
            int classId = Integer.parseInt(
                request.getParameter("classId")
            );

            SchoolClass schoolClass =
                checkClass(classId, user, response);

            if (schoolClass == null) {
                return;
            }

            List<MemberDTO> members =
                memberDAO.findMembers(classId);

            request.setAttribute("schoolClass", schoolClass);
            request.setAttribute("members", members);

            request.getRequestDispatcher(
                "/WEB-INF/views/teacher/class-members.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã lớp không hợp lệ"
            );

        } catch (SQLException e) {

            throw new ServletException(
                "Không thể tải danh sách thành viên", e
            );
        }
    }

    // POST: Thêm học sinh vào lớp
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = checkLogin(request, response);

        if (user == null) {
            return;
        }

        try {
            int classId = Integer.parseInt(
                request.getParameter("classId")
            );

            SchoolClass schoolClass =
                checkClass(classId, user, response);

            if (schoolClass == null) {
                return;
            }

            String username = request.getParameter("username");

            if (username == null || username.trim().isEmpty()) {
                response.sendRedirect(
                    request.getContextPath()
                    + "/teacher/class-members?classId="
                    + classId + "&message=EMPTY"
                );
                return;
            }

            String result = memberDAO.addStudent(
                classId,
                username.trim()
            );

            response.sendRedirect(
                request.getContextPath()
                + "/teacher/class-members?classId="
                + classId + "&message=" + result
            );

        } catch (NumberFormatException e) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã lớp không hợp lệ"
            );

        } catch (SQLException e) {

            throw new ServletException(
                "Không thể thêm học sinh vào lớp", e
            );
        }
    }
}
