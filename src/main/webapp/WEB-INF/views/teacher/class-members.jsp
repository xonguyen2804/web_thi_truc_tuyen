
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.ClassMemberDAO.MemberDTO" %>
<%@ page import="com.onlineexam.model.SchoolClass" %>

<%
    SchoolClass schoolClass =
        (SchoolClass) request.getAttribute("schoolClass");

    List<MemberDTO> members =
        (List<MemberDTO>) request.getAttribute("members");

    String message = request.getParameter("message");
    String notice = null;

    if ("ADDED".equals(message))
        notice = "Đã thêm học sinh vào lớp thành công.";
    else if ("ALREADY_MEMBER".equals(message))
        notice = "Học sinh này đã có trong lớp.";
    else if ("NOT_FOUND".equals(message))
        notice = "Không tìm thấy tài khoản học sinh.";
    else if ("INACTIVE".equals(message))
        notice = "Tài khoản học sinh hiện không hoạt động.";
    else if ("EMPTY".equals(message))
        notice = "Vui lòng nhập username.";
    else if ("ERROR".equals(message))
        notice = "Không thể thêm thành viên.";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Thành viên lớp | OnlineExam</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css?v=14">

    <style>
        .members-container {
            max-width: 1050px;
            margin: 35px auto;
            padding: 0 20px;
        }

        .members-card {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 25px;
            margin-bottom: 22px;
        }

        .members-back {
            display: inline-block;
            margin-bottom: 20px;
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }

        .members-form {
            display: flex;
            gap: 12px;
            align-items: end;
            flex-wrap: wrap;
        }

        .members-form .form-group {
            flex: 1;
            min-width: 220px;
            margin: 0;
        }

        .members-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }

        .members-table th,
        .members-table td {
            padding: 14px;
            text-align: left;
            border-bottom: 1px solid #e2e8f0;
        }

        .members-table th {
            background: #f8fafc;
            color: #475569;
        }

        .members-notice {
            padding: 12px 15px;
            margin: 15px 0;
            border-radius: 8px;
            background: #eff6ff;
            color: #1d4ed8;
        }

        @media(max-width: 600px) {
            .members-card {
                padding: 16px;
            }

            .members-table {
                font-size: 13px;
            }

            .members-table th,
            .members-table td {
                padding: 9px;
            }
        }
    </style>
</head>

<body>

<div class="members-container">

    <a class="members-back"
       href="${pageContext.request.contextPath}/teacher/classes">
        ← Quay lại quản lý lớp
    </a>

    <!-- Thông tin và thêm thành viên -->
    <section class="members-card">

        <h1>Thành viên lớp: <%= schoolClass.getClassName() %></h1>

        <p>
            Thêm học sinh bằng username tài khoản đã đăng ký.
        </p>

        <% if (notice != null) { %>
            <div class="members-notice">
                <%= notice %>
            </div>
        <% } %>

        <form class="members-form"
              method="POST"
              action="${pageContext.request.contextPath}/teacher/class-members">

            <input type="hidden"
                   name="classId"
                   value="<%= schoolClass.getClassId() %>">

            <div class="form-group">
                <label for="username">Username học sinh</label>

                <input type="text"
                       id="username"
                       name="username"
                       class="form-control"
                       maxlength="50"
                       placeholder="Ví dụ: hocsinh01"
                       required>
            </div>

            <button type="submit" class="btn btn-primary">
                ＋ Thêm học sinh
            </button>

        </form>
    </section>

    <!-- Danh sách thành viên -->
    <section class="members-card">

        <h2>Danh sách thành viên</h2>

        <p>
            Tổng số:
            <strong><%= members == null ? 0 : members.size() %></strong>
            học sinh
        </p>

        <div style="overflow-x:auto">

            <table class="members-table">

                <thead>
                    <tr>
                        <th>#</th>
                        <th>Họ và tên</th>
                        <th>Username</th>
                        <th>Email</th>
                    </tr>
                </thead>

                <tbody>

                <% if (members != null && !members.isEmpty()) {

                    int index = 1;

                    for (MemberDTO member : members) {
                %>

                    <tr>
                        <td><%= index++ %></td>

                        <td>
                            <%= member.getFullName() == null
                                ? "—" : member.getFullName() %>
                        </td>

                        <td><%= member.getUsername() %></td>

                        <td>
                            <%= member.getEmail() == null
                                ? "—" : member.getEmail() %>
                        </td>
                    </tr>

                <%
                    }

                } else {
                %>

                    <tr>
                        <td colspan="4"
                            style="text-align:center;padding:25px;color:#64748b">
                            Lớp chưa có thành viên nào.
                        </td>
                    </tr>

                <% } %>

                </tbody>
            </table>

        </div>
    </section>

</div>

</body>
</html>
