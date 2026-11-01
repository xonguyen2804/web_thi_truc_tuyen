
package com.onlineexam.controller;

import com.onlineexam.model.Group;
import com.onlineexam.model.SchoolClass;
import com.onlineexam.service.ClassService;
import com.onlineexam.service.GroupService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/teacher/groups")
public class GroupServlet extends HttpServlet {
    private GroupService groupService;
    private ClassService classService;

    @Override
    public void init() {
        groupService = new GroupService();
        classService = new ClassService();
    }

    private int getTeacherId(HttpServletRequest request)
            throws ServletException {
        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("userId") instanceof Number)) {
            throw new ServletException("Bạn chưa đăng nhập.");
        }

        return ((Number) session.getAttribute("userId")).intValue();
    }

    private boolean ownsClass(int classId, int teacherId)
            throws SQLException {
        SchoolClass schoolClass = classService.getClassById(classId);
        return schoolClass != null
                && schoolClass.getCreatedBy() == teacherId;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int teacherId = getTeacherId(request);
            String classIdParam = request.getParameter("classId");

            if (classIdParam == null || classIdParam.isBlank()) {
                response.sendError(400, "Thiếu classId.");
                return;
            }

            int classId = Integer.parseInt(classIdParam);

            if (!ownsClass(classId, teacherId)) {
                response.sendError(403, "Bạn không có quyền quản lý lớp này.");
                return;
            }

            List<Group> groups = groupService.getGroupsByClass(classId);
            request.setAttribute("groups", groups);
            request.setAttribute("classId", classId);

            request.getRequestDispatcher(
                    "/WEB-INF/views/teacher/groups.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(400, "classId không hợp lệ.");
        } catch (SQLException e) {
            throw new ServletException("Không thể tải danh sách nhóm.", e);
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
            int classId = Integer.parseInt(request.getParameter("classId"));

            if (!ownsClass(classId, teacherId)) {
                response.sendError(403, "Bạn không có quyền quản lý lớp này.");
                return;
            }

            if ("create".equals(action)) {
                String groupName = request.getParameter("groupName");
                String description = request.getParameter("description");

                groupService.createGroup(classId, groupName, description);

            } else if ("update".equals(action)) {
                int groupId = Integer.parseInt(request.getParameter("groupId"));
                Group group = groupService.getGroupById(groupId);

                if (group == null || group.getClassId() != classId) {
                    response.sendError(404, "Không tìm thấy nhóm.");
                    return;
                }

                groupService.updateGroup(
                        groupId,
                        request.getParameter("groupName"),
                        request.getParameter("description")
                );

            } else if ("delete".equals(action)) {
                int groupId = Integer.parseInt(request.getParameter("groupId"));
                Group group = groupService.getGroupById(groupId);

                if (group == null || group.getClassId() != classId) {
                    response.sendError(404, "Không tìm thấy nhóm.");
                    return;
                }

                groupService.deleteGroup(groupId);

            } else {
                response.sendError(400, "Hành động không hợp lệ.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/teacher/groups?classId=" + classId
            );

        } catch (NumberFormatException e) {
            response.sendError(400, "ID không hợp lệ.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể xử lý nhóm.", e);
        }
    }
}