<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.onlineexam.model.ExamSession" %>
<%@ page import="com.onlineexam.model.Exam" %>
<%@ page import="com.onlineexam.model.SchoolClass" %>
<%@ page import="com.onlineexam.model.Group" %>
<%
    List<ExamSession> sessions = (List<ExamSession>) request.getAttribute("sessions");
    List<Exam> exams = (List<Exam>) request.getAttribute("exams");
    List<SchoolClass> classes = (List<SchoolClass>) request.getAttribute("classes");
    List<Group> groups = (List<Group>) request.getAttribute("groups");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản Lý & Mở Ca Thi | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
    <style>
        .session-grid { display: grid; grid-template-columns: 380px 1fr; gap: 24px; margin-top: 20px; }
        .card { background: #fff; padding: 22px; border-radius: 10px; border: 1px solid #e2e8f0; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #334155; }
        .form-control { width: 100%; padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
        .btn { padding: 9px 16px; border-radius: 6px; font-weight: 700; cursor: pointer; border: none; font-size: 13px; }
        .btn-primary { background: #2563eb; color: #fff; width: 100%; }
        .btn-success { background: #16a34a; color: #fff; }
        .btn-danger { background: #ef4444; color: #fff; }
        .badge { padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
        .badge-open { background: #dcfce7; color: #15803d; border: 1px solid #86efac; }
        .badge-sched { background: #e0f2fe; color: #0284c7; border: 1px solid #bae6fd; }
        .badge-closed { background: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1; }
        .table-sessions { width: 100%; border-collapse: collapse; font-size: 14px; }
        .table-sessions th, .table-sessions td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        .table-sessions th { background: #f8fafc; color: #475569; }
    </style>
</head>
<body>
<div class="teacher-layout">
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard" style="display: flex; align-items: center; gap: 10px; margin-bottom: 28px;">
            <div style="background: #2563eb; color: white; width: 36px; height: 36px; border-radius: 8px; display: grid; place-items: center; font-weight: 800;">OE</div>
            <div>
                <span style="font-size: 16px; font-weight: 800; color: #1e3a8a; display: block;">OnlineExam</span>
                <span style="font-size: 11px; color: #64748b;">Giảng Viên</span>
            </div>
        </a>
        <nav class="sidebar-nav">
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/dashboard">📊 Tổng Quan</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">📝 Quản Lý Đề Thi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">❓ Ngân Hàng Câu Hỏi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/classes">👥 Lớp Học</a>
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/exam-sessions">⏱ Lịch & Mở Phòng Thi</a>
        </nav>
    </aside>

    <main class="main-content" style="padding: 28px; flex: 1;">
        <h1 style="font-size: 22px; margin: 0 0 6px 0;">Quản Lý Ca Thi & Mở Phòng Thi</h1>
        <p style="color: #64748b; font-size: 13px; margin: 0 0 20px 0;">Tạo ca thi mới, đặt lịch thi, thiết lập mật khẩu phòng và mở phòng thi cho học sinh.</p>

        <% if ("created_success".equals(request.getParameter("msg"))) { %>
            <div style="padding: 12px; background: #dcfce7; color: #15803d; border-radius: 6px; margin-bottom: 16px;">
                ✓ Đã tạo ca thi mới thành công!
            </div>
        <% } else if ("status_updated".equals(request.getParameter("msg"))) { %>
            <div style="padding: 12px; background: #dcfce7; color: #15803d; border-radius: 6px; margin-bottom: 16px;">
                ✓ Cập nhật trạng thái phòng thi thành công!
            </div>
        <% } %>

        <div class="session-grid">
            <!-- Form Tạo Ca Thi Mới -->
            <div class="card">
                <h3 style="margin-top: 0; font-size: 16px; margin-bottom: 16px;">📅 Thiết Lập Ca Thi Mới</h3>
                <form action="${pageContext.request.contextPath}/teacher/exam-sessions" method="POST">
                    <div class="form-group">
                        <label>Tên Ca Thi <span style="color: red;">*</span></label>
                        <input type="text" name="sessionName" class="form-control" required placeholder="VD: Khảo Sát Vật Lí - Khối 12">
                    </div>

                    <div class="form-group">
                        <label>Chọn Đề Thi <span style="color: red;">*</span></label>
                        <select name="examId" class="form-control" required>
                            <% if (exams != null) {
                                for (Exam e : exams) { %>
                                    <option value="<%= e.getExamId() %>">
                                        [#<%= e.getExamId() %>] <%= e.getExamTitle() %> (<%= e.getDurationMinutes() %> phút)
                                    </option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Đối tượng dự thi</label>
                        <select name="targetType" class="form-control" onchange="switchTarget(this.value)" style="margin-bottom: 8px;">
                            <option value="CLASS">Theo Lớp Học</option>
                            <option value="GROUP">Theo Nhóm Học</option>
                        </select>
                        <select name="targetId" id="selectClass" class="form-control">
                            <% if (classes != null) {
                                for (SchoolClass c : classes) { %>
                                    <option value="<%= c.getClassId() %>">Lớp: <%= c.getClassName() %></option>
                            <%  }
                            } %>
                        </select>
                        <select name="targetId" id="selectGroup" class="form-control" style="display: none;" disabled>
                            <% if (groups != null) {
                                for (Group g : groups) { %>
                                    <option value="<%= g.getGroupId() %>">Nhóm: <%= g.getGroupName() %></option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Mật Khẩu Phòng Thi (session_password)</label>
                        <input type="text" name="sessionPassword" class="form-control" placeholder="Để trống nếu vào tự do (VD: 123456)">
                    </div>

                    <div class="form-group">
                        <label>Thời gian bắt đầu <span style="color: red;">*</span></label>
                        <input type="datetime-local" name="startTime" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label>Thời gian kết thúc <span style="color: red;">*</span></label>
                        <input type="datetime-local" name="endTime" class="form-control" required>
                    </div>

                    <div class="form-group">
                        <label>Trạng thái ban đầu</label>
                        <select name="status" class="form-control">
                            <option value="SCHEDULED">Lên lịch chờ (SCHEDULED)</option>
                            <option value="OPEN">Mở phòng thi ngay (OPEN)</option>
                        </select>
                    </div>

                    <button type="submit" class="btn btn-primary">+ Tạo Ca Thi</button>
                </form>
            </div>

            <!-- Danh Sách Ca Thi & Điều Khiển Mở Phòng -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <div style="padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: #f8fafc;">
                    <h3 style="margin: 0; font-size: 15px;">Danh Sách Ca Thi Hiện Tại</h3>
                </div>
                <table class="table-sessions">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Tên Ca / Đề Thi</th>
                            <th>Đối Tượng & Mật Khẩu</th>
                            <th>Lịch Thi</th>
                            <th>Trạng Thái</th>
                            <th style="text-align: right;">Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (sessions != null && !sessions.isEmpty()) {
                        for (ExamSession s : sessions) { %>
                        <tr>
                            <td>#<%= s.getSessionId() %></td>
                            <td>
                                <strong><%= s.getSessionName() %></strong><br>
                                <span style="font-size: 12px; color: #64748b;">Đề: <%= s.getExamTitle() %> (<%= s.getDurationMinutes() %>p)</span>
                            </td>
                            <td>
                                <span style="font-size: 12px; color: #475569;">Phòng: <strong><%= s.getTargetName() %></strong></span><br>
                                <span style="font-size: 12px; font-family: monospace; font-weight: 700; color: #c2410c;">
                                    Pass: <%= (s.getSessionPassword() != null && !s.getSessionPassword().isBlank()) ? s.getSessionPassword() : "(Không có)" %>
                                </span>
                            </td>
                            <td style="font-size: 12px;">
                                <%= s.getStartTime() != null ? s.getStartTime().format(dtf) : "" %><br>
                                &rarr; <%= s.getEndTime() != null ? s.getEndTime().format(dtf) : "" %>
                            </td>
                            <td>
                                <% if ("OPEN".equalsIgnoreCase(s.getStatus())) { %>
                                    <span class="badge badge-open">🟢 ĐANG MỞ</span>
                                <% } else if ("SCHEDULED".equalsIgnoreCase(s.getStatus())) { %>
                                    <span class="badge badge-sched">🕒 LÊN LỊCH</span>
                                <% } else { %>
                                    <span class="badge badge-closed">🔴 ĐÃ ĐÓNG</span>
                                <% } %>
                            </td>
                            <td style="text-align: right; white-space: nowrap;">
                                <form action="${pageContext.request.contextPath}/teacher/exam-sessions" method="POST" style="display: inline;">
                                    <input type="hidden" name="action" value="toggle_status">
                                    <input type="hidden" name="sessionId" value="<%= s.getSessionId() %>">
                                    <% if ("OPEN".equalsIgnoreCase(s.getStatus())) { %>
                                        <input type="hidden" name="targetStatus" value="CLOSED">
                                        <button type="submit" class="btn btn-danger" style="padding: 6px 10px;">Đóng Phòng</button>
                                    <% } else { %>
                                        <input type="hidden" name="targetStatus" value="OPEN">
                                        <button type="submit" class="btn btn-success" style="padding: 6px 10px;">Mở Phòng</button>
                                    <% } %>
                                </form>
                            </td>
                        </tr>
                    <%  }
                    } else { %>
                        <tr><td colspan="6" style="text-align: center; color: #94a3b8; padding: 24px;">Chưa có ca thi nào được tạo.</td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<script>
function switchTarget(val) {
    const sc = document.getElementById('selectClass');
    const sg = document.getElementById('selectGroup');
    if (val === 'GROUP') {
        sc.style.display = 'none';
        sc.disabled = true;
        sg.style.display = 'block';
        sg.disabled = false;
    } else {
        sc.style.display = 'block';
        sc.disabled = false;
        sg.style.display = 'none';
        sg.disabled = true;
    }
}
</script>
</body>
</html>