
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.model.User" %>

<%!
    // Escape dữ liệu trước khi hiển thị lên HTML
    private String escapeHtml(String text) {
        if (text == null) return "";

        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
%>

<%
    List<User> users =
        (List<User>) request.getAttribute("users");

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Quản lý tài khoản - OnlineExam</title>

    <style>
        .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
}

.list-header h3 {
    margin: 0;
}

.add-btn {
    background: #2563eb;
    color: white;
    border: none;
    padding: 11px 18px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: bold;
    cursor: pointer;
}

.add-btn:hover {
    background: #1d4ed8;
}

.add-form {
    display: none;
    margin-bottom: 24px;
    padding: 20px;
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
}

.add-form input,
.add-form select {
    width: 100%;
    padding: 10px;
    margin: 8px 0 14px;
    border: 1px solid #cbd5e1;
    border-radius: 6px;
}

.form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 16px;
}

@media (max-width: 600px) {
    .form-grid {
        grid-template-columns: 1fr;
    }
}
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fc;
            color: #243047;
        }

        .header {
            background: #192b47;
            color: white;
            padding: 20px 36px;
        }

        .header h2 {
            margin: 0;
        }

        .container {
            max-width: 1200px;
            margin: 35px auto;
            padding: 0 20px;
        }

        .top-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            margin-bottom: 25px;
        }

        .top-bar h1 {
            margin: 0;
            font-size: 26px;
        }

        .back-btn {
            display: inline-block;
            text-decoration: none;
            color: #2563eb;
            font-weight: bold;
        }

        .card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.05);
        }

        .table-wrapper {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 14px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }

        th {
            background: #f0f4fa;
            font-weight: bold;
        }

        tr:hover {
            background: #f8fafc;
        }

        .status {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: bold;
        }

        .active {
            background: #dcfce7;
            color: #166534;
        }

        .inactive {
            background: #fee2e2;
            color: #991b1b;
        }

        .empty {
            text-align: center;
            padding: 35px;
            color: #64748b;
        }
    </style>
</head>

<body>

<header class="header">
    <h2>OnlineExam - ADMIN</h2>
</header>

<main class="container">

    <div class="top-bar">
        <h1>Quản lý tài khoản</h1>

        <a class="back-btn"
           href="<%= contextPath %>/admin">
            ← Trang chủ ADMIN
        </a>
    </div>

    <div class="card">
<div class="list-header">
    <h3>
        Danh sách người dùng
        (<%= users == null ? 0 : users.size() %>)
    </h3>

    <button type="button"
            class="add-btn"
            onclick="toggleAddForm()">
        + Thêm người dùng
    </button>
</div>
<div id="addUserForm" class="add-form">

    <h3>Thêm tài khoản mới</h3>

    <form action="<%= contextPath %>/admin/users"
          method="post">

        <div class="form-grid">

            <div>
                <label>Tên đăng nhập</label>
                <input type="text"
                       name="username"
                       required>
            </div>

            <div>
                <label>Họ và tên</label>
                <input type="text"
                       name="fullName"
                       required>
            </div>

            <div>
                <label>Email</label>
                <input type="email"
                       name="email"
                       required>
            </div>

            <div>
                <label>Mật khẩu</label>
                <input type="password"
                       name="password"
                       autocomplete="new-password"
                       required>
            </div>

            <div>
                <label>Vai trò</label>
                <select name="role" required>
                    <option value="">-- Chọn vai trò --</option>
                    <option value="1">ADMIN</option>
                    <option value="2">TEACHER</option>
                    <option value="3">STUDENT</option>
                </select>
            </div>

        </div>

        <button type="submit" class="add-btn">
            Lưu người dùng
        </button>

    </form>

</div>
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên đăng nhập</th>
                        <th>Họ và tên</th>
                        <th>Email</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                    </tr>
                </thead>

                <tbody>

                <% if (users != null && !users.isEmpty()) { %>

                    <% for (User user : users) { %>
                        <tr>
                            <td>
                                <%= user.getUserId() %>
                            </td>

                            <td>
                                <%= escapeHtml(user.getUsername()) %>
                            </td>

                            <td>
                                <%= escapeHtml(user.getFullName()) %>
                            </td>

                            <td>
                                <%= escapeHtml(user.getEmail()) %>
                            </td>

                            <td>
                                <%
                                    String status = user.getStatus();
                                    boolean active =
                                        "ACTIVE".equalsIgnoreCase(status);
                                %>

                                <span class="status <%= active ? "active" : "inactive" %>">
                                    <%= escapeHtml(status) %>
                                </span>
                            </td>

                            <td>
                                <%= user.getCreatedAt() == null
                                        ? ""
                                        : user.getCreatedAt() %>
                            </td>
                        </tr>
                    <% } %>

                <% } else { %>

                    <tr>
                        <td colspan="6" class="empty">
                            Chưa có tài khoản nào.
                        </td>
                    </tr>

                <% } %>

                </tbody>
            </table>
        </div>

    </div>

</main>
<script>
function toggleAddForm() {
    const form = document.getElementById("addUserForm");

    form.style.display =
        form.style.display === "block" ? "none" : "block";
}
</script>
</body>
</html>
