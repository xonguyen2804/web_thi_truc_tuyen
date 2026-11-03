
package com.onlineexam.controller;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.dao.GroupMembershipDAO;
import com.onlineexam.dao.GroupMembershipDAO.GroupMemberDTO;
import com.onlineexam.model.Group;
import com.onlineexam.model.SchoolClass;
import com.onlineexam.model.User;
import com.onlineexam.service.GroupService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/teacher/groups")
public class GroupServlet extends HttpServlet {

    private final GroupService groupService = new GroupService();
    private final ClassDAO classDAO = new ClassDAO();
    private final GroupMembershipDAO groupMembershipDAO =
            new GroupMembershipDAO();

    // Kiểm tra đăng nhập và quyền truy cập
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
                    "Bạn không có quyền truy cập chức năng này."
            );

            return null;
        }

        return user;
    }

    // Kiểm tra lớp tồn tại và người dùng có quyền quản lý lớp
    private SchoolClass checkClass(int classId, User user,
                                   HttpServletResponse response)
            throws SQLException, IOException {

        SchoolClass schoolClass = classDAO.findById(classId);

        if (schoolClass == null) {
            response.sendError(404, "Không tìm thấy lớp học.");
            return null;
        }

        String role = user.getRole() == null
                ? ""
                : user.getRole().getRoleName();

        // ADMIN được quản lý tất cả lớp
        if (!"ADMIN".equalsIgnoreCase(role)
                && schoolClass.getCreatedBy() != user.getUserId()) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý lớp này."
            );

            return null;
        }

        return schoolClass;
    }

    // Kiểm tra nhóm có thuộc lớp đang thao tác không
    private Group checkGroup(int groupId, int classId,
                             HttpServletResponse response)
            throws SQLException, IOException {

        Group group = groupService.getGroupById(groupId);

        if (group == null || group.getClassId() != classId) {
            response.sendError(404, "Không tìm thấy nhóm trong lớp này.");
            return null;
        }

        return group;
    }

    // GET: Hiển thị danh sách nhóm và thành viên
    @Override
    protected void doGet(HttpServletRequest request,
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

            List<Group> groups =
                    groupService.getGroupsByClass(classId);

            // Lưu danh sách thành viên theo từng groupId
            Map<Integer, List<GroupMemberDTO>> membersByGroup =
                    new HashMap<>();

            for (Group group : groups) {
                membersByGroup.put(
                        group.getGroupId(),
                        groupMembershipDAO.findMembers(
                                group.getGroupId()
                        )
                );
            }

            request.setAttribute("schoolClass", schoolClass);
            request.setAttribute("groups", groups);
            request.setAttribute("membersByGroup", membersByGroup);

            request.getRequestDispatcher(
                    "/WEB-INF/views/teacher/groups.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "Mã lớp không hợp lệ.");
        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể tải dữ liệu nhóm học.", e
            );
        }
    }

    // POST: Tạo, sửa, xóa nhóm và quản lý thành viên
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
            String action = request.getParameter("action");

            int classId = Integer.parseInt(
                    request.getParameter("classId")
            );

            SchoolClass schoolClass =
                    checkClass(classId, user, response);

            if (schoolClass == null) {
                return;
            }

            String message;

            if ("create".equals(action)) {

                String groupName =
                        request.getParameter("groupName");

                String description =
                        request.getParameter("description");

                groupService.createGroup(
                        classId, groupName, description
                );

                message = "GROUP_CREATED";

            } else if ("update".equals(action)) {

                int groupId = Integer.parseInt(
                        request.getParameter("groupId")
                );

                Group group = checkGroup(
                        groupId, classId, response
                );

                if (group == null) {
                    return;
                }

                groupService.updateGroup(
                        groupId,
                        request.getParameter("groupName"),
                        request.getParameter("description")
                );

                message = "GROUP_UPDATED";

            } else if ("delete".equals(action)) {

                int groupId = Integer.parseInt(
                        request.getParameter("groupId")
                );

                Group group = checkGroup(
                        groupId, classId, response
                );

                if (group == null) {
                    return;
                }

                groupService.deleteGroup(groupId);

                message = "GROUP_DELETED";

            } else if ("addStudent".equals(action)) {

                int groupId = Integer.parseInt(
                        request.getParameter("groupId")
                );

                Group group = checkGroup(
                        groupId, classId, response
                );

                if (group == null) {
                    return;
                }

                String username = request.getParameter("username");

                if (username == null || username.isBlank()) {
                    message = "EMPTY_USERNAME";
                } else {
                    message = groupMembershipDAO.addStudentToGroup(
                            classId,
                            groupId,
                            username.trim()
                    );
                }

            } else if ("removeStudent".equals(action)) {

                int groupId = Integer.parseInt(
                        request.getParameter("groupId")
                );

                Group group = checkGroup(
                        groupId, classId, response
                );

                if (group == null) {
                    return;
                }

                int userId = Integer.parseInt(
                        request.getParameter("userId")
                );

                boolean removed = groupMembershipDAO.remove(
                        groupId, userId
                );

                message = removed
                        ? "MEMBER_REMOVED"
                        : "MEMBER_NOT_FOUND";

            } else {
                response.sendError(400, "Hành động không hợp lệ.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/teacher/groups?classId=" + classId
                    + "&message=" + message
            );

        } catch (NumberFormatException e) {
            response.sendError(400, "Thông tin ID không hợp lệ.");
        } catch (IllegalArgumentException e) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/teacher/groups?classId="
                    + request.getParameter("classId")
                    + "&message=INVALID_INPUT"
            );
        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể xử lý yêu cầu quản lý nhóm.", e
            );
        }
    }
}
