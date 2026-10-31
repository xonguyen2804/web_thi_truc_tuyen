
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    String username = (String) session.getAttribute("username");
    if (username == null) {
        username = "Admin";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - OnlineExam</title>
    <style>
        .sidebar {
    display: flex;
    flex-direction: column;
}

.logout-form {
    margin-top: auto;
}

.logout-btn {
    width: 100%;
    padding: 14px;
    background: #dc2626;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: bold;
    cursor: pointer;
    text-align: left;
}

.logout-btn:hover {
    background: #b91c1c;
}
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fc;
            color: #20304a;
        }

        .layout {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 250px;
            background: #182942;
            color: white;
            padding: 30px 20px;
        }

        .sidebar h2 {
            margin: 0 0 8px;
            color: white;
        }

        .sidebar p {
            font-size: 12px;
            color: #b8cce4;
            margin-bottom: 35px;
        }

        .sidebar a {
            display: block;
            color: #e0e8f3;
            text-decoration: none;
            padding: 14px;
            margin-bottom: 10px;
            border-radius: 8px;
        }

        .sidebar a:hover,
        .sidebar a.active {
            background: #365478;
        }

        .content {
            flex: 1;
            padding: 40px;
        }

        .welcome {
            background: white;
            padding: 30px;
            border-radius: 14px;
            margin-bottom: 25px;
        }

        .welcome h1 {
            margin-top: 0;
        }

        .card {
            background: white;
            padding: 28px;
            border-radius: 14px;
            max-width: 400px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.05);
        }

        .card h3 {
            margin-top: 0;
        }

        .btn {
            display: inline-block;
            background: #2563eb;
            color: white;
            text-decoration: none;
            padding: 12px 20px;
            border-radius: 8px;
            margin-top: 12px;
        }

        .btn:hover {
            background: #1d4ed8;
        }

        @media (max-width: 650px) {
            .layout { flex-direction: column; }
            .sidebar { width: 100%; }
            .content { padding: 20px; }
        }
    </style>
</head>
<body>
    <div class="layout">

        <aside class="sidebar">
            <h2>OnlineExam</h2>
            <p>ADMIN PANEL</p>

            <a href="<%= contextPath %>/admin" class="active">
                Tổng quan
            </a>

            <a href="<%= contextPath %>/admin/users">
                Quản lý tài khoản
            </a>
            <form class="logout-form"
      action="<%= contextPath %>/auth/logout"
      method="post">

    <button type="submit" class="logout-btn">
        ⎋ Đăng xuất
    </button>

</form>
        </aside>

        <main class="content">
            <div class="welcome">
                <h1>Xin chào, <%= username.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;") %>!</h1>
                <p>Chào mừng đến với hệ thống quản trị OnlineExam.</p>
            </div>

            <div class="card">
                <h3>Quản lý tài khoản</h3>

                <p>
                    Xem và quản lý tài khoản ADMIN,
                    TEACHER và STUDENT trong hệ thống.
                </p>

                <a class="btn" href="<%= contextPath %>/admin/users">
                    Quản lý tài khoản →
                </a>
            </div>
        </main>

    </div>
</body>
</html>
