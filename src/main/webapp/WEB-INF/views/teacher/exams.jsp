<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.model.Exam" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Đề Thi | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
    <style>
        .exam-container { display: grid; grid-template-columns: 380px 1fr; gap: 24px; margin-top: 20px; }
        .form-card, .table-card { background: #fff; padding: 24px; border-radius: 10px; border: 1px solid #e2e8f0; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #334155; }
        .form-control { width: 100%; padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
        .btn-submit { width: 100%; padding: 11px; background: #2563eb; color: white; border: none; border-radius: 6px; font-weight: 700; cursor: pointer; }
        .btn-submit:hover { background: #1d4ed8; }
        .exam-table { width: 100%; border-collapse: collapse; font-size: 14px; }
        .exam-table th, .exam-table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        .exam-table th { background: #f8fafc; color: #475569; font-weight: 600; }
        .badge { padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
        .badge-draft { background: #fef3c7; color: #92400e; }
        .badge-published { background: #dcfce7; color: #166534; }
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
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/exams">📝 Quản Lý Đề Thi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">❓ Ngân Hàng Câu Hỏi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/classes">👥 Lớp Học</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">⏱ Lịch Thi</a>
        </nav>
    </aside>

    <main class="main-content" style="padding: 28px; flex: 1;">
        <h1 style="font-size: 22px; margin: 0 0 4px 0;">Quản Lý Đề Thi</h1>
        <p style="color: #64748b; font-size: 13px; margin: 0 0 20px 0;">Tạo đề và nạp tự động câu hỏi từ file trắc nghiệm chuẩn</p>

        <% if (request.getParameter("success") != null) { %>
            <div style="padding: 12px; background: #dcfce7; color: #15803d; border-radius: 6px; margin-bottom: 16px;">
                ✓ Tạo đề thi và import câu hỏi thành công!
            </div>
        <% } %>

        <div class="exam-container">
            <!-- Form Tạo & Upload File -->
            <div class="form-card">
                <h3 style="margin-top: 0; font-size: 16px; margin-bottom: 16px;">Tạo Đề Thi & Import File</h3>
                <form action="${pageContext.request.contextPath}/teacher/exams" method="POST" enctype="multipart/form-data">
                    <div class="form-group">
                        <label>Tên Đề Thi <span style="color: red;">*</span></label>
                        <input type="text" name="examTitle" class="form-control" required placeholder="VD: Đề Khảo Sát Toán THPTQG - Lần 1">
                    </div>

                    <div class="form-group">
                        <label>Thời gian làm bài (phút) <span style="color: red;">*</span></label>
                        <input type="number" name="durationMinutes" class="form-control" value="50" min="1" required>
                    </div>

                    <div class="form-group">
                        <label>Điểm chuẩn đạt (thang 10)</label>
                        <input type="number" step="0.25" name="passScore" class="form-control" value="5.0" required>
                    </div>

                    <div class="form-group">
                        <label>File đề thi (.txt) - Tối đa 5MB</label>
                        <input type="file" name="examFile" accept=".txt" class="form-control">
                        <small style="font-size: 11px; color: #64748b;">Định dạng: Câu 1:... A. ... B. ... (đáp án đúng thêm dấu * ở cuối)</small>
                    </div>

                    <div class="form-group">
                        <label>Trạng thái</label>
                        <select name="status" class="form-control">
                            <option value="PUBLISHED">Công khai (PUBLISHED)</option>
                            <option value="DRAFT">Lưu nháp (DRAFT)</option>
                        </select>
                    </div>

                    <button type="submit" class="btn-submit">+ Tạo & Import Đề</button>
                </form>
            </div>

            <!-- Danh Sách Đề Đã Có -->
            <div class="table-card">
                <h3 style="margin-top: 0; font-size: 16px; margin-bottom: 16px;">Danh Sách Đề Thi</h3>
                <table class="exam-table">
                    <thead>
                        <tr>
                            <th>Mã Đề</th>
                            <th>Tên Đề</th>
                            <th>Thời Lượng</th>
                            <th>Trạng Thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Exam> exams = (List<Exam>) request.getAttribute("exams");
                            if (exams != null && !exams.isEmpty()) {
                                for (Exam e : exams) {
                        %>
                        <tr>
                            <td>#<%= e.getExamId() %></td>
                            <td><strong><%= e.getExamTitle() %></strong></td>
                            <td><%= e.getDurationMinutes() %> phút</td>
                            <td>
                                <% if ("PUBLISHED".equalsIgnoreCase(e.getStatus())) { %>
                                    <span class="badge badge-published">Đã công khai</span>
                                <% } else { %>
                                    <span class="badge badge-draft">Bản nháp</span>
                                <% } %>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr><td colspan="4" style="text-align: center; color: #94a3b8; padding: 24px;">Chưa có đề thi nào.</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>
</body>
</html>