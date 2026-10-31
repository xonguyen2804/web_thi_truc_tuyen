<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.controller.ClassServlet.ClassDTO" %>
<%@ page import="com.onlineexam.controller.ClassServlet.GroupDTO" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Khối Lớp & Nhóm Học | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
    <style>
        .classes-grid { display: grid; grid-template-columns: 340px 1fr; gap: 24px; margin-top: 20px; }
        .card { background: white; padding: 22px; border-radius: 10px; border: 1px solid #e2e8f0; margin-bottom: 20px; }
        .form-group { margin-bottom: 12px; }
        .form-group label { display: block; margin-bottom: 4px; font-weight: 600; font-size: 13px; }
        .form-control { width: 100%; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; }
        .btn-action { width: 100%; padding: 10px; background: #2563eb; color: white; border: none; border-radius: 6px; font-weight: 700; cursor: pointer; }
        .class-box { border-left: 4px solid #2563eb; padding: 16px; background: #f8fafc; border-radius: 6px; margin-bottom: 16px; border-top: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; }
        .group-tag { display: inline-block; background: #eff6ff; color: #1e40af; border: 1px solid #bfdbfe; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; margin: 4px; }
    </style>
</head>
<body>
<div class="teacher-layout">
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard" style="display: flex; align-items: center; gap: 10px; margin-bottom: 28px;">
            <div style="background: #2563eb; color: white; width: 36px; height: 36px; border-radius: 8px; display: grid; place-items: center; font-weight: 800;">OE</div>
            <div>
                <span style="font-size: 16px; font-weight: 800; color: #1e3a8a; display: block;">OnlineExam</span>
                <span style="font-size: 11px; color: #64748b;">Luyện Thi THPT QG</span>
            </div>
        </a>
        <nav class="sidebar-nav">
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/dashboard">📊 Tổng Quan</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">📝 Quản Lý Đề Thi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">❓ Ngân Hàng Câu Hỏi</a>
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/classes">👥 Lớp Học & Nhóm</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">⏱ Lịch Thi</a>
        </nav>
    </aside>

    <main class="main-content" style="padding: 28px; flex: 1;">
        <h1 style="font-size: 22px; margin: 0 0 4px 0;">Khối Lớp & Nhóm Luyện Thi</h1>
        <p style="color: #64748b; font-size: 13px; margin: 0 0 20px 0;">Thiết lập các Khối 10, 11, 12 và các Nhóm học tập để phân quyền đợt thi</p>

        <div class="classes-grid">
            <!-- Cột Trái: Tạo Khối & Nhóm -->
            <div>
                <!-- Form 1: Tạo Khối Lớp -->
                <div class="card">
                    <h3 style="margin-top: 0; font-size: 15px; margin-bottom: 12px;">+ Tạo Khối Lớp Mới</h3>
                    <form action="${pageContext.request.contextPath}/teacher/classes" method="POST">
                        <input type="hidden" name="action" value="createClass">
                        <div class="form-group">
                            <label>Tên Khối Lớp</label>
                            <input type="text" name="className" class="form-control" placeholder="VD: Khối 12" required>
                        </div>
                        <button type="submit" class="btn-action">+ Thêm Khối</button>
                    </form>
                </div>

                <!-- Form 2: Tạo Nhóm con trong Khối -->
                <div class="card">
                    <h3 style="margin-top: 0; font-size: 15px; margin-bottom: 12px;">+ Tạo Nhóm Học Trong Lớp</h3>
                    <form action="${pageContext.request.contextPath}/teacher/classes" method="POST">
                        <input type="hidden" name="action" value="createGroup">
                        <div class="form-group">
                            <label>Chọn Khối Trực Thuộc</label>
                            <select name="classId" class="form-control" required>
                                <%
                                    List<ClassDTO> classList = (List<ClassDTO>) request.getAttribute("classList");
                                    if (classList != null) {
                                        for (ClassDTO c : classList) {
                                %>
                                    <option value="<%= c.classId %>"><%= c.className %></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Tên Nhóm</label>
                            <input type="text" name="groupName" class="form-control" placeholder="VD: 12VL_1, 12T_1" required>
                        </div>
                        <div class="form-group">
                            <label>Mô tả nhóm</label>
                            <input type="text" name="description" class="form-control" placeholder="Nhóm chuyên Lý, Toán...">
                        </div>
                        <button type="submit" class="btn-action" style="background: #10b981;">+ Thêm Nhóm</button>
                    </form>
                </div>
            </div>

            <!-- Cột Phải: Danh Sách Khối & Nhóm Hiện Có -->
            <div class="card">
                <h3 style="margin-top: 0; font-size: 16px; margin-bottom: 16px;">Cấu Trúc Lớp & Nhóm Đang Có</h3>
                <%
                    if (classList != null && !classList.isEmpty()) {
                        for (ClassDTO c : classList) {
                %>
                <div class="class-box">
                    <h4 style="margin: 0 0 10px 0; font-size: 16px; color: #1e3a8a;"><%= c.className %></h4>
                    <div style="font-size: 13px; color: #475569; margin-bottom: 8px;">Các nhóm đã tạo:</div>
                    <div>
                        <%
                            if (!c.groups.isEmpty()) {
                                for (GroupDTO g : c.groups) {
                        %>
                            <span class="group-tag">🏷 <%= g.groupName %></span>
                        <%
                                }
                            } else {
                        %>
                            <span style="font-size: 12px; color: #94a3b8; font-style: italic;">Chưa có nhóm nào trong khối này.</span>
                        <% } %>
                    </div>
                </div>
                <%
                        }
                    } else {
                %>
                <div style="text-align: center; color: #94a3b8; padding: 32px;">Chưa có khối lớp nào. Hãy tạo Khối 12, 11, 10 ở cột bên trái!</div>
                <% } %>
            </div>
        </div>
    </main>
</div>
</body>
</html>