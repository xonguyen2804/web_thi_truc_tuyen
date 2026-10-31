
package com.onlineexam.controller;

import com.onlineexam.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/teacher/classes")
public class ClassServlet extends HttpServlet {

    public static class ClassDTO {
        public int classId;
        public String className;
        public List<GroupDTO> groups = new ArrayList<>();
    }

    public static class GroupDTO {
        public int groupId;
        public String groupName;
        public String description;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ClassDTO> classes = new ArrayList<>();

        String sqlClasses = "SELECT * FROM Classes ORDER BY class_name ASC";
        String sqlGroups = "SELECT * FROM `Groups` WHERE class_id = ? ORDER BY group_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psC = conn.prepareStatement(sqlClasses);
             ResultSet rsC = psC.executeQuery()) {

            while (rsC.next()) {
                ClassDTO c = new ClassDTO();
                c.classId = rsC.getInt("class_id");
                c.className = rsC.getString("class_name");

                try (PreparedStatement psG = conn.prepareStatement(sqlGroups)) {
                    psG.setInt(1, c.classId);
                    try (ResultSet rsG = psG.executeQuery()) {
                        while (rsG.next()) {
                            GroupDTO g = new GroupDTO();
                            g.groupId = rsG.getInt("group_id");
                            g.groupName = rsG.getString("group_name");
                            g.description = rsG.getString("description");
                            c.groups.add(g);
                        }
                    }
                }
                classes.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("classList", classes);
        request.getRequestDispatcher("/WEB-INF/views/teacher/classes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try (Connection conn = DBConnection.getConnection()) {
            if ("createClass".equals(action)) {
                String className = request.getParameter("className"); // VD: Khối 12, Khối 11
                String sql = "INSERT INTO Classes (class_name, created_by) VALUES (?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, className);
                    ps.executeUpdate();
                }
            } else if ("createGroup".equals(action)) {
                int classId = Integer.parseInt(request.getParameter("classId"));
                String groupName = request.getParameter("groupName"); // VD: 12VL_1, 12T_1
                String desc = request.getParameter("description");
                String sql = "INSERT INTO `Groups` (class_id, group_name, description) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, classId);
                    ps.setString(2, groupName);
                    ps.setString(3, desc);
                    ps.executeUpdate();
                }
            }
            response.sendRedirect(request.getContextPath() + "/teacher/classes?success=true");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/teacher/classes?error=" + e.getMessage());
        }
    }
}


// package com.onlineexam.controller;

// import com.onlineexam.model.SchoolClass;
// import com.onlineexam.service.ClassService;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;

// import java.io.IOException;
// import java.sql.SQLException;
// import java.util.List;

// @WebServlet("/teacher/classes")
// public class ClassServlet extends HttpServlet {
//     private ClassService classService;

//     @Override
//     public void init() {
//         classService = new ClassService();
//     }

//     private int getTeacherId(HttpServletRequest request)
//             throws ServletException {
//         HttpSession session = request.getSession(false);
//         if (session == null || session.getAttribute("userId") == null) {
//             throw new ServletException("Bạn chưa đăng nhập.");
//         }

//         Object value = session.getAttribute("userId");
//         if (!(value instanceof Number)) {
//             throw new ServletException("Thông tin đăng nhập không hợp lệ.");
//         }

//         return ((Number) value).intValue();
//     }

//     @Override
//     protected void doGet(HttpServletRequest request,
//                          HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");

//         try {
//             int teacherId = getTeacherId(request);
//             List<SchoolClass> classes =
//                     classService.getClassesByTeacher(teacherId);

//             request.setAttribute("classes", classes);
//             request.getRequestDispatcher(
//                     "/WEB-INF/views/teacher/classes.jsp"
//             ).forward(request, response);
//         } catch (SQLException e) {
//             throw new ServletException("Không thể tải danh sách lớp.", e);
//         }
//     }

//     @Override
//     protected void doPost(HttpServletRequest request,
//                           HttpServletResponse response)
//             throws ServletException, IOException {
//         request.setCharacterEncoding("UTF-8");

//         try {
//             int teacherId = getTeacherId(request);
//             String action = request.getParameter("action");

//             if ("create".equals(action)) {
//                 String className = request.getParameter("className");
//                 String description = request.getParameter("description");

//                 classService.createClass(className, description, teacherId);

//             } else if ("update".equals(action)) {
//                 int classId = Integer.parseInt(
//                         request.getParameter("classId")
//                 );
//                 String className = request.getParameter("className");
//                 String description = request.getParameter("description");

//                 SchoolClass schoolClass =
//                         classService.getClassById(classId);

//                 if (schoolClass == null
//                         || schoolClass.getCreatedBy() != teacherId) {
//                     response.sendError(HttpServletResponse.SC_FORBIDDEN);
//                     return;
//                 }

//                 classService.updateClass(classId, className, description);

//             } else if ("delete".equals(action)) {
//                 int classId = Integer.parseInt(
//                         request.getParameter("classId")
//                 );

//                 SchoolClass schoolClass =
//                         classService.getClassById(classId);

//                 if (schoolClass == null
//                         || schoolClass.getCreatedBy() != teacherId) {
//                     response.sendError(HttpServletResponse.SC_FORBIDDEN);
//                     return;
//                 }

//                 classService.deleteClass(classId);

//             } else {
//                 response.sendError(
//                         HttpServletResponse.SC_BAD_REQUEST,
//                         "Hành động không hợp lệ."
//                 );
//                 return;
//             }

//             response.sendRedirect(
//                     request.getContextPath() + "/teacher/classes"
//             );

//         } catch (NumberFormatException e) {
//             response.sendError(
//                     HttpServletResponse.SC_BAD_REQUEST,
//                     "ID không hợp lệ."
//             );
//         } catch (IllegalArgumentException e) {
//             request.setAttribute("error", e.getMessage());
//             doGet(request, response);
//         } catch (SQLException e) {
//             throw new ServletException("Không thể xử lý lớp học.", e);
//         }
//     }
// }