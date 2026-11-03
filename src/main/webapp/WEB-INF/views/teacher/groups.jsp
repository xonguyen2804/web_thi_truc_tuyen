
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.onlineexam.model.Group" %>
<%@ page import="com.onlineexam.model.SchoolClass" %>
<%@ page import="com.onlineexam.dao.GroupMembershipDAO.GroupMemberDTO" %>

<%!
    // Mã hóa HTML để tránh hiển thị trực tiếp dữ liệu người dùng nhập
    public String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
%>

<%
    SchoolClass schoolClass =
            (SchoolClass) request.getAttribute("schoolClass");

    List<Group> groups =
            (List<Group>) request.getAttribute("groups");

    Map<Integer, List<GroupMemberDTO>> membersByGroup =
            (Map<Integer, List<GroupMemberDTO>>)
                    request.getAttribute("membersByGroup");

    String message = request.getParameter("message");
    String notice = null;

    if ("GROUP_CREATED".equals(message))
        notice = "Tạo nhóm thành công.";
    else if ("GROUP_UPDATED".equals(message))
        notice = "Cập nhật nhóm thành công.";
    else if ("GROUP_DELETED".equals(message))
        notice = "Xóa nhóm thành công.";
    else if ("ADDED".equals(message))
        notice = "Thêm học sinh vào nhóm thành công.";
    else if ("MEMBER_REMOVED".equals(message))
        notice = "Đã xóa học sinh khỏi nhóm.";
    else if ("NOT_FOUND".equals(message))
        notice = "Không tìm thấy tài khoản học sinh.";
    else if ("NOT_STUDENT".equals(message))
        notice = "Tài khoản này không có vai trò học sinh.";
    else if ("INACTIVE".equals(message))
        notice = "Tài khoản học sinh hiện không hoạt động.";
    else if ("GROUP_NOT_FOUND".equals(message))
        notice = "Nhóm không tồn tại hoặc không thuộc lớp này.";
    else if ("NOT_IN_CLASS".equals(message))
        notice = "Học sinh chưa được thêm vào lớp. Hãy thêm học sinh vào lớp trước.";
    else if ("ALREADY_IN_GROUP".equals(message))
        notice = "Học sinh đã thuộc một nhóm khác trong lớp này.";
    else if ("EMPTY_USERNAME".equals(message))
        notice = "Vui lòng nhập username học sinh.";
    else if ("MEMBER_NOT_FOUND".equals(message))
        notice = "Không tìm thấy thành viên cần xóa.";
    else if ("INVALID_INPUT".equals(message))
        notice = "Dữ liệu nhập không hợp lệ.";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Quản lý nhóm | OnlineExam</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css?v=14">

    <style>
        .groups-container {
            max-width: 1150px;
            margin: 30px auto;
            padding: 0 22px;
        }

        .groups-back {
            display: inline-block;
            margin-bottom: 20px;
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }

        .groups-card {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 24px;
            margin-bottom: 22px;
        }

        .groups-notice {
            padding: 13px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            background: #eff6ff;
            color: #1d4ed8;
        }

        .groups-create-form {
            display: grid;
            grid-template-columns: 1fr 1.5fr auto;
            gap: 14px;
            align-items: end;
        }

        .groups-create-form .form-group {
            margin: 0;
        }

        .groups-create-form textarea {
            min-height: 42px;
        }

        .group-item {
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            margin-top: 18px;
            overflow: hidden;
        }

        .group-header {
            padding: 18px;
            background: #f8fafc;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            flex-wrap: wrap;
        }

        .group-header h3 {
            margin: 0 0 5px;
        }

        .group-description {
            color: #64748b;
            margin: 0;
            font-size: 14px;
        }

        .group-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .group-body {
            padding: 18px;
        }

        .group-edit-form {
            display: grid;
            grid-template-columns: 1fr 1.5fr auto;
            gap: 10px;
            align-items: end;
            margin-bottom: 20px;
        }

        .group-edit-form .form-group {
            margin: 0;
        }

        .group-add-form {
            display: flex;
            gap: 10px;
            align-items: end;
            margin-bottom: 18px;
            flex-wrap: wrap;
        }

        .group-add-form .form-group {
            flex: 1;
            min-width: 220px;
            margin: 0;
        }

        .group-table {
            width: 100%;
            border-collapse: collapse;
        }

        .group-table th,
        .group-table td {
            padding: 12px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
        }

        .group-table th {
            background: #f8fafc;
            color: #475569;
        }

        .btn-danger {
            background: #dc2626;
            color: white;
            border: none;
            border-radius: 7px;
            padding: 8px 12px;
            cursor: pointer;
        }

        .btn-danger:hover {
            background: #b91c1c;
        }

        .group-empty {
            padding: 22px;
            text-align: center;
            color: #64748b;
        }

        .group-section-title {
            margin: 0 0 15px;
        }

        @media(max-width: 750px) {
            .groups-create-form,
            .group-edit-form {
                grid-template-columns: 1fr;
            }

            .groups-card {
                padding: 16px;
            }

            .group-table {
                font-size: 13px;
            }

            .group-table th,
            .group-table td {
                padding: 8px;
            }
        }
    </style>
</head>

<body>

<div class="groups-container">

    <a class="groups-back"
       href="${pageContext.request.contextPath}/teacher/classes">
        ← Quay lại quản lý lớp
    </a>

    <section class="groups-card">

        <h1>
            Quản lý nhóm:
            <%= escapeHtml(schoolClass.getClassName()) %>
        </h1>

        <p>
            Thêm, chỉnh sửa nhóm và phân công học sinh vào từng nhóm.
        </p>

        <% if (notice != null) { %>
            <div class="groups-notice">
                <%= escapeHtml(notice) %>
            </div>
        <% } %>

    </section>

    <!-- Tạo nhóm mới -->
    <section class="groups-card">

        <h2 class="group-section-title">＋ Tạo nhóm mới</h2>

        <form class="groups-create-form"
              method="POST"
              action="${pageContext.request.contextPath}/teacher/groups">

            <input type="hidden" name="action" value="create">

            <input type="hidden"
                   name="classId"
                   value="<%= schoolClass.getClassId() %>">

            <div class="form-group">
                <label for="groupName">Tên nhóm</label>

                <input type="text"
                       id="groupName"
                       name="groupName"
                       class="form-control"
                       maxlength="100"
                       placeholder="Ví dụ: Nhóm 1"
                       required>
            </div>

            <div class="form-group">
                <label for="description">Mô tả</label>

                <input type="text"
                       id="description"
                       name="description"
                       class="form-control"
                       placeholder="Mô tả nhóm học">
            </div>

            <button type="submit" class="btn btn-primary">
                Tạo nhóm
            </button>

        </form>

    </section>

    <!-- Danh sách nhóm -->
    <section class="groups-card">

        <h2 class="group-section-title">
            Danh sách nhóm
            (<%= groups == null ? 0 : groups.size() %>)
        </h2>

        <% if (groups != null && !groups.isEmpty()) { %>

            <% for (Group group : groups) {
                List<GroupMemberDTO> members =
                        membersByGroup == null
                        ? null
                        : membersByGroup.get(group.getGroupId());
            %>

                <div class="group-item">

                    <div class="group-header">

                        <div>
                            <h3>
                                👥 <%= escapeHtml(group.getGroupName()) %>
                            </h3>

                            <p class="group-description">
                                <%= escapeHtml(group.getDescription()) %>
                            </p>

                            <small>
                                <%= members == null ? 0 : members.size() %>
                                học sinh
                            </small>
                        </div>

                        <div class="group-actions">

                            <form method="POST"
                                  action="${pageContext.request.contextPath}/teacher/groups"
                                  onsubmit="return confirm('Bạn có chắc muốn xóa nhóm này?');">

                                <input type="hidden"
                                       name="action"
                                       value="delete">

                                <input type="hidden"
                                       name="classId"
                                       value="<%= schoolClass.getClassId() %>">

                                <input type="hidden"
                                       name="groupId"
                                       value="<%= group.getGroupId() %>">

                                <button type="submit" class="btn-danger">
                                    Xóa nhóm
                                </button>

                            </form>

                        </div>

                    </div>

                    <div class="group-body">

                        <!-- Sửa thông tin nhóm -->
                        <form class="group-edit-form"
                              method="POST"
                              action="${pageContext.request.contextPath}/teacher/groups">

                            <input type="hidden"
                                   name="action"
                                   value="update">

                            <input type="hidden"
                                   name="classId"
                                   value="<%= schoolClass.getClassId() %>">

                            <input type="hidden"
                                   name="groupId"
                                   value="<%= group.getGroupId() %>">

                            <div class="form-group">
                                <label>Tên nhóm</label>

                                <input type="text"
                                       name="groupName"
                                       class="form-control"
                                       maxlength="100"
                                       value="<%= escapeHtml(group.getGroupName()) %>"
                                       required>
                            </div>

                            <div class="form-group">
                                <label>Mô tả</label>

                                <input type="text"
                                       name="description"
                                       class="form-control"
                                       value="<%= escapeHtml(group.getDescription()) %>">
                            </div>

                            <button type="submit" class="btn btn-primary">
                                Lưu thay đổi
                            </button>

                        </form>

                        <hr>

                        <!-- Thêm học sinh vào nhóm -->
                        <h4>Thêm học sinh vào nhóm</h4>

                        <form class="group-add-form"
                              method="POST"
                              action="${pageContext.request.contextPath}/teacher/groups">

                            <input type="hidden"
                                   name="action"
                                   value="addStudent">

                            <input type="hidden"
                                   name="classId"
                                   value="<%= schoolClass.getClassId() %>">

                            <input type="hidden"
                                   name="groupId"
                                   value="<%= group.getGroupId() %>">

                            <div class="form-group">
                                <label>Username học sinh</label>

                                <input type="text"
                                       name="username"
                                       class="form-control"
                                       maxlength="50"
                                       placeholder="Nhập username"
                                       required>
                            </div>

                            <button type="submit" class="btn btn-success">
                                ＋ Thêm học sinh
                            </button>

                        </form>

                        <!-- Danh sách thành viên -->
                        <h4>Thành viên nhóm</h4>

                        <div style="overflow-x:auto">

                            <table class="group-table">

                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Họ và tên</th>
                                        <th>Username</th>
                                        <th>Email</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>

                                <% if (members != null && !members.isEmpty()) {
                                    int index = 1;

                                    for (GroupMemberDTO member : members) {
                                %>

                                    <tr>
                                        <td><%= index++ %></td>

                                        <td>
                                            <%= escapeHtml(member.getFullName()) %>
                                        </td>

                                        <td>
                                            <%= escapeHtml(member.getUsername()) %>
                                        </td>

                                        <td>
                                            <%= escapeHtml(member.getEmail()) %>
                                        </td>

                                        <td>
                                            <form method="POST"
                                                  action="${pageContext.request.contextPath}/teacher/groups"
                                                  onsubmit="return confirm('Xóa học sinh này khỏi nhóm?');">

                                                <input type="hidden"
                                                       name="action"
                                                       value="removeStudent">

                                                <input type="hidden"
                                                       name="classId"
                                                       value="<%= schoolClass.getClassId() %>">

                                                <input type="hidden"
                                                       name="groupId"
                                                       value="<%= group.getGroupId() %>">

                                                <input type="hidden"
                                                       name="userId"
                                                       value="<%= member.getUserId() %>">

                                                <button type="submit"
                                                        class="btn-danger">
                                                    Xóa
                                                </button>

                                            </form>
                                        </td>
                                    </tr>

                                <%
                                    }
                                } else {
                                %>

                                    <tr>
                                        <td colspan="5" class="group-empty">
                                            Nhóm chưa có học sinh nào.
                                        </td>
                                    </tr>

                                <% } %>

                                </tbody>
                            </table>

                        </div>

                    </div>
                </div>

            <% } %>

        <% } else { %>

            <div class="group-empty">
                Lớp học chưa có nhóm nào. Hãy tạo nhóm mới ở phía trên.
            </div>

        <% } %>

    </section>

</div>

</body>
</html>
