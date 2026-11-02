
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.onlineexam.model.ExamSession" %>
<%@ page import="com.onlineexam.model.Exam" %>
<%@ page import="com.onlineexam.model.SchoolClass" %>
<%@ page import="com.onlineexam.model.Group" %>

<%
    List<ExamSession> sessions =
        (List<ExamSession>) request.getAttribute("sessions");
    List<Exam> exams =
        (List<Exam>) request.getAttribute("exams");
    List<SchoolClass> classes =
        (List<SchoolClass>) request.getAttribute("classes");
    List<Group> groups =
        (List<Group>) request.getAttribute("groups");

    DateTimeFormatter dtf =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    int totalSessions = sessions == null ? 0 : sessions.size();
    int openCount = 0;
    int scheduledCount = 0;
    int closedCount = 0;

    if (sessions != null) {
        for (ExamSession examSession : sessions) {
            if ("OPEN".equalsIgnoreCase(examSession.getStatus())) {
                openCount++;
            } else if ("SCHEDULED".equalsIgnoreCase(examSession.getStatus())) {
                scheduledCount++;
            } else {
                closedCount++;
            }
        }
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý ca thi | OnlineExam</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css?v=11">
</head>

<body>
<div class="teacher-layout">

    <!-- SIDEBAR -->
    <aside class="sidebar">
        <a class="brand"
           href="${pageContext.request.contextPath}/teacher/dashboard">
            <div class="brand-logo">OE</div>
            <div class="brand-info">
                <span class="brand-name">OnlineExam</span>
                <span class="brand-subtitle">Cổng thông tin giáo viên</span>
            </div>
        </a>

        <div class="sidebar-label">QUẢN LÝ GIẢNG DẠY</div>

        <nav class="sidebar-nav">
            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/dashboard">
                <span>📊</span> Tổng quan
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/exams">
                <span>📝</span> Quản lý đề thi
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/questions">
                <span>❓</span> Ngân hàng câu hỏi
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/classes">
                <span>👥</span> Lớp học và nhóm
            </a>

            <a class="nav-item active"
               href="${pageContext.request.contextPath}/teacher/exam-sessions">
                <span>⏱️</span> Lịch thi
            </a>
        </nav>

        <div class="sidebar-bottom">
            <div class="teacher-mini">
                <div class="teacher-avatar">GV</div>
                <div>
                    <div class="teacher-mini-name">Giáo viên</div>
                    <div class="teacher-mini-role">Khu vực giảng dạy</div>
                </div>
            </div>
        </div>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="main-content">

        <div class="page-eyebrow">QUẢN LÝ TỔ CHỨC THI</div>

        <header class="sessions-header">
            <div>
                <h1>Quản lý ca thi</h1>
                <p>
                    Lập lịch thi, phân công đối tượng dự thi và điều khiển
                    trạng thái phòng thi.
                </p>
            </div>
        </header>

        <!-- THÔNG BÁO -->
        <% if ("created_success".equals(request.getParameter("msg"))) { %>
            <div class="session-alert session-alert-success">
                <span class="session-alert-icon">✓</span>
                <div>
                    <strong>Tạo ca thi thành công</strong>
                    <p>Ca thi mới đã được thêm vào hệ thống.</p>
                </div>
            </div>
        <% } else if ("status_updated".equals(request.getParameter("msg"))) { %>
            <div class="session-alert session-alert-success">
                <span class="session-alert-icon">✓</span>
                <div>
                    <strong>Cập nhật trạng thái thành công</strong>
                    <p>Trạng thái phòng thi đã được thay đổi.</p>
                </div>
            </div>
        <% } %>

        <!-- THỐNG KÊ -->
        <section class="sessions-overview">

            <div class="session-stat-card">
                <div class="session-stat-icon session-stat-blue">📅</div>
                <div>
                    <div class="session-stat-label">Tổng số ca thi</div>
                    <div class="session-stat-value"><%= totalSessions %></div>
                    <div class="session-stat-note">Ca thi đã được tạo</div>
                </div>
            </div>

            <div class="session-stat-card">
                <div class="session-stat-icon session-stat-green">🟢</div>
                <div>
                    <div class="session-stat-label">Phòng đang mở</div>
                    <div class="session-stat-value"><%= openCount %></div>
                    <div class="session-stat-note">Có thể tham gia dự thi</div>
                </div>
            </div>

            <div class="session-stat-card">
                <div class="session-stat-icon session-stat-orange">🕒</div>
                <div>
                    <div class="session-stat-label">Ca thi đã lên lịch</div>
                    <div class="session-stat-value"><%= scheduledCount %></div>
                    <div class="session-stat-note">Đang chờ mở phòng</div>
                </div>
            </div>

            <div class="session-stat-card">
                <div class="session-stat-icon session-stat-gray">🔒</div>
                <div>
                    <div class="session-stat-label">Phòng đã đóng</div>
                    <div class="session-stat-value"><%= closedCount %></div>
                    <div class="session-stat-note">Không còn mở tham gia</div>
                </div>
            </div>

        </section>

        <!-- NỘI DUNG CHÍNH -->
        <section class="sessions-content-grid">

            <!-- FORM TẠO CA THI -->
            <div class="session-create-card">

                <div class="session-section-heading">
                    <div class="session-section-icon">📅</div>
                    <div>
                        <h2>Thiết lập ca thi mới</h2>
                        <p>Điền thông tin để tạo một ca thi</p>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/teacher/exam-sessions"
                      method="POST">

                    <div class="session-form-group">
                        <label for="sessionName">
                            Tên ca thi <span class="session-required">*</span>
                        </label>
                        <input
                            type="text"
                            id="sessionName"
                            name="sessionName"
                            class="session-input"
                            required
                            placeholder="Ví dụ: Khảo sát Vật lý - Khối 12">
                    </div>

                    <div class="session-form-group">
                        <label for="examId">
                            Đề thi <span class="session-required">*</span>
                        </label>

                        <select id="examId" name="examId"
                                class="session-input" required>
                            <option value="">-- Chọn đề thi --</option>

                            <% if (exams != null) {
                                for (Exam e : exams) { %>
                                    <option value="<%= e.getExamId() %>">
                                        [#<%= e.getExamId() %>]
                                        <%= e.getExamTitle() %>
                                        (<%= e.getDurationMinutes() %> phút)
                                    </option>
                            <%  }
                            } %>
                        </select>

                        <% if (exams == null || exams.isEmpty()) { %>
                            <p class="session-field-note">
                                Hiện chưa có đề thi. Hãy tạo đề thi trước khi lập ca thi.
                            </p>
                        <% } %>
                    </div>

                    <div class="session-form-group">
                        <label for="targetType">Đối tượng dự thi</label>

                        <select name="targetType" id="targetType"
                                class="session-input"
                                onchange="switchTarget(this.value)">
                            <option value="CLASS">Theo lớp học</option>
                            <option value="GROUP">Theo nhóm học</option>
                        </select>
                    </div>

                    <div class="session-form-group">
                        <label for="selectClass">Lớp học</label>
                        <select name="targetId" id="selectClass"
                                class="session-input">
                            <option value="">-- Chọn lớp học --</option>
                            <% if (classes != null) {
                                for (SchoolClass c : classes) { %>
                                    <option value="<%= c.getClassId() %>">
                                        <%= c.getClassName() %>
                                    </option>
                            <%  }
                            } %>
                        </select>

                        <select name="targetId" id="selectGroup"
                                class="session-input"
                                style="display: none;" disabled>
                            <option value="">-- Chọn nhóm học --</option>
                            <% if (groups != null) {
                                for (Group g : groups) { %>
                                    <option value="<%= g.getGroupId() %>">
                                        <%= g.getGroupName() %>
                                    </option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="session-form-group">
                        <label for="sessionPassword">
                            Mật khẩu phòng thi
                        </label>
                        <input
                            type="password"
                            id="sessionPassword"
                            name="sessionPassword"
                            class="session-input"
                            placeholder="Để trống nếu không yêu cầu mật khẩu">
                        <p class="session-field-note">
                            Học sinh cần nhập mật khẩu này khi tham gia nếu phòng thi có thiết lập mật khẩu.
                        </p>
                    </div>

                    <div class="session-form-row">
                        <div class="session-form-group">
                            <label for="startTime">
                                Thời gian bắt đầu
                                <span class="session-required">*</span>
                            </label>
                            <input
                                type="datetime-local"
                                id="startTime"
                                name="startTime"
                                class="session-input"
                                required>
                        </div>

                        <div class="session-form-group">
                            <label for="endTime">
                                Thời gian kết thúc
                                <span class="session-required">*</span>
                            </label>
                            <input
                                type="datetime-local"
                                id="endTime"
                                name="endTime"
                                class="session-input"
                                required>
                        </div>
                    </div>

                    <div class="session-form-group">
                        <label for="status">Trạng thái ban đầu</label>
                        <select id="status" name="status" class="session-input">
                            <option value="SCHEDULED">Lên lịch chờ</option>
                            <option value="OPEN">Mở phòng thi ngay</option>
                        </select>
                    </div>

                    <button type="submit" class="session-submit-btn"
                            <% if (exams == null || exams.isEmpty()) { %>
                                disabled
                            <% } %>>
                        <span>＋</span> Tạo ca thi
                    </button>

                </form>
            </div>

            <!-- DANH SÁCH CA THI -->
            <div class="session-list-card">

                <div class="session-list-heading">
                    <div>
                        <h2>Danh sách ca thi</h2>
                        <p>Theo dõi lịch và trạng thái các phòng thi</p>
                    </div>

                    <span class="session-total-badge">
                        <%= totalSessions %> ca thi
                    </span>
                </div>

                <% if (sessions != null && !sessions.isEmpty()) { %>

                    <div class="session-table-wrapper">
                        <table class="modern-session-table">
                            <thead>
                                <tr>
                                    <th>Mã</th>
                                    <th>Thông tin ca thi</th>
                                    <th>Đối tượng</th>
                                    <th>Thời gian</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>

                            <tbody>
                            <% for (ExamSession s : sessions) { %>
                                <tr>
                                    <td>
                                        <span class="session-id">
                                            #<%= s.getSessionId() %>
                                        </span>
                                    </td>

                                    <td>
                                        <div class="session-name-cell">
                                            <div class="session-row-icon">📝</div>
                                            <div>
                                                <strong><%= s.getSessionName() %></strong>
                                                <span>
                                                    <%= s.getExamTitle() %>
                                                    · <%= s.getDurationMinutes() %> phút
                                                </span>
                                            </div>
                                        </div>
                                    </td>

                                    <td>
                                        <span class="session-target">
                                            <%= s.getTargetName() %>
                                        </span>
                                        <span class="session-password-note">
                                            <%=
                                                (s.getSessionPassword() != null
                                                && !s.getSessionPassword().isBlank())
                                                ? "Có mật khẩu"
                                                : "Không có mật khẩu"
                                            %>
                                        </span>
                                    </td>

                                    <td>
                                        <div class="session-time">
                                            <span>
                                                <%= s.getStartTime() != null
                                                    ? s.getStartTime().format(dtf) : "Chưa xác định" %>
                                            </span>
                                            <span class="session-time-arrow">↓</span>
                                            <span>
                                                <%= s.getEndTime() != null
                                                    ? s.getEndTime().format(dtf) : "Chưa xác định" %>
                                            </span>
                                        </div>
                                    </td>

                                    <td>
                                        <% if ("OPEN".equalsIgnoreCase(s.getStatus())) { %>
                                            <span class="session-status session-status-open">
                                                <i></i> Đang mở
                                            </span>
                                        <% } else if ("SCHEDULED".equalsIgnoreCase(s.getStatus())) { %>
                                            <span class="session-status session-status-scheduled">
                                                <i></i> Lên lịch
                                            </span>
                                        <% } else { %>
                                            <span class="session-status session-status-closed">
                                                <i></i> Đã đóng
                                            </span>
                                        <% } %>
                                    </td>

                                    <td>
                                        <form
                                            action="${pageContext.request.contextPath}/teacher/exam-sessions"
                                            method="POST"
                                            class="session-action-form">

                                            <input type="hidden" name="action"
                                                   value="toggle_status">
                                            <input type="hidden" name="sessionId"
                                                   value="<%= s.getSessionId() %>">

                                            <% if ("OPEN".equalsIgnoreCase(s.getStatus())) { %>
                                                <input type="hidden" name="targetStatus"
                                                       value="CLOSED">
                                                <button type="submit"
                                                        class="session-action-btn session-close-btn"
                                                        onclick="return confirm('Bạn có chắc muốn đóng phòng thi này?')">
                                                    Đóng phòng
                                                </button>
                                            <% } else { %>
                                                <input type="hidden" name="targetStatus"
                                                       value="OPEN">
                                                <button type="submit"
                                                        class="session-action-btn session-open-btn"
                                                        onclick="return confirm('Bạn có chắc muốn mở phòng thi này?')">
                                                    Mở phòng
                                                </button>
                                            <% } %>
                                        </form>
                                    </td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>

                <% } else { %>

                    <div class="session-empty-state">
                        <div class="session-empty-icon">📅</div>
                        <h3>Chưa có ca thi nào</h3>
                        <p>
                            Tạo ca thi đầu tiên bằng biểu mẫu bên trái
                            để bắt đầu quản lý lịch thi.
                        </p>
                    </div>

                <% } %>

            </div>
        </section>

        <footer class="teacher-page-footer">
            OnlineExam · Hệ thống quản lý thi trực tuyến
        </footer>

    </main>
</div>

<script>
function switchTarget(val) {
    const classSelect = document.getElementById('selectClass');
    const groupSelect = document.getElementById('selectGroup');

    if (val === 'GROUP') {
        classSelect.style.display = 'none';
        classSelect.disabled = true;

        groupSelect.style.display = 'block';
        groupSelect.disabled = false;
    } else {
        classSelect.style.display = 'block';
        classSelect.disabled = false;

        groupSelect.style.display = 'none';
        groupSelect.disabled = true;
    }
}
</script>

</body>
</html>
