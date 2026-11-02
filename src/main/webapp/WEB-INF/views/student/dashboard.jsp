<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.DashboardDAO.ActiveSessionDTO" %>
<%@ page import="com.onlineexam.dao.DashboardDAO.AttemptHistoryDTO" %>
<%
    String studentName = (String) request.getAttribute("studentName");
    Integer totalExamsDone = (Integer) request.getAttribute("totalExamsDone");
    Double avgScore = (Double) request.getAttribute("avgScore");
    Integer passedCount = (Integer) request.getAttribute("passedCount");
    List<ActiveSessionDTO> activeSessions = (List<ActiveSessionDTO>) request.getAttribute("activeSessions");
    List<AttemptHistoryDTO> historyList = (List<AttemptHistoryDTO>) request.getAttribute("historyList");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang Học Sinh | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css?v=2">
</head>
<body>

<div class="container student-dashboard">
    <!-- Top Navigation -->
    <header class="student-top-nav">
        <a href="${pageContext.request.contextPath}/student/dashboard"
        class="student-brand">
            <span class="student-brand-logo">OE</span>
            <span>
                <strong>OnlineExam</strong>
                <small>CỔNG THÔNG TIN HỌC SINH</small>
            </span>
        </a>

        <div class="student-nav-right">
            <div class="student-user">
                <span class="student-avatar">👤</span>
                <span><%= studentName %></span>
            </div>

            <form class="logout-form"
                action="<%= request.getContextPath() %>/auth/logout"
                method="post">
                <button type="submit" class="logout-btn">
                    ⎋ Đăng xuất
                </button>
            </form>
        </div>
    </header>

    <!-- Welcome Banner -->
    <section class="student-welcome-banner">
        <div class="student-welcome-content">
            <div class="student-welcome-label">
                HỆ THỐNG LUYỆN THI TRỰC TUYẾN
            </div>

            <h1>Xin chào, <%= studentName %>!</h1>

            <p>
                Chào mừng bạn quay trở lại. Hãy kiểm tra các ca thi đang mở
                và tiếp tục hành trình ôn luyện của mình nhé.
            </p>
        </div>

        <div class="student-welcome-icon">🎓</div>
    </section>

    <!-- Student Statistics -->
    <section class="student-stats-grid">

        <div class="student-stat-card">
            <div class="student-stat-icon stat-blue">📝</div>
            <div class="student-stat-info">
                <div class="student-stat-label">BÀI THI ĐÃ HOÀN THÀNH</div>
                <div class="student-stat-value">
                    <%= totalExamsDone %>
                </div>
                <div class="student-stat-desc">Tổng số lượt thi đã hoàn thành</div>
            </div>
        </div>

        <div class="student-stat-card">
            <div class="student-stat-icon stat-purple">🎯</div>
            <div class="student-stat-info">
                <div class="student-stat-label">ĐIỂM TRUNG BÌNH</div>
                <div class="student-stat-value">
                    <%= avgScore %>
                </div>
                <div class="student-stat-desc">Điểm trung bình các bài thi</div>
            </div>
        </div>

        <div class="student-stat-card">
            <div class="student-stat-icon stat-green">🏆</div>
            <div class="student-stat-info">
                <div class="student-stat-label">BÀI THI ĐẠT CHUẨN</div>
                <div class="student-stat-value">
                    <%= passedCount %>
                </div>
                <div class="student-stat-desc">Số bài thi đạt yêu cầu</div>
            </div>
        </div>

    </section>

    <!-- Khối 1: Ca thi đang mở -->
    <section class="student-section-card">
        <div class="student-section-header">
            <div>
                <div class="student-section-eyebrow">EXAM SESSIONS</div>
                <h2>Ca thi sắp tới / đang mở</h2>
                <p>Các phòng thi bạn có thể tham gia</p>
            </div>

            <a href="${pageContext.request.contextPath}/student/exams"
            class="student-view-all">
                Xem tất cả →
            </a>
        </div>
        <% if (activeSessions != null && !activeSessions.isEmpty()) { %>
            <div class="student-session-grid">
                <% for (ActiveSessionDTO s : activeSessions) { 
                    boolean isOpen = "OPEN".equalsIgnoreCase(s.status);
                %>
                    <div class="student-session-item">

                        <!-- Thông tin trạng thái và thời lượng -->
                        <div class="student-session-meta">
                            <span class="badge <%= isOpen ? "badge-open" : "badge-sched" %>">
                                <%= isOpen ? "🟢 ĐANG MỞ" : "🕒 LÊN LỊCH" %>
                            </span>

                            <span class="student-session-duration">
                                <%= s.durationMinutes %> phút
                            </span>
                        </div>

                        <!-- Tên ca thi -->
                        <strong class="student-session-name">
                            <%= s.sessionName %>
                        </strong>

                        <!-- Tên đề thi -->
                        <div class="student-session-exam">
                            Đề thi: <%= s.examTitle %>
                        </div>

                        <!-- Khu vực nút vào thi hoặc thời gian bắt đầu -->
                        <div class="student-session-action">
                            <% if (isOpen) { %>
                                <button type="button" class="btn btn-primary"
                                        data-session-id="<%= s.sessionId %>"
                                        data-session-name="<%= s.sessionName %>"
                                        data-has-pass="<%= s.hasPassword %>"
                                        onclick="openPassModal(this)">
                                    Vào Thi &rarr;
                                </button>
                            <% } else { %>
                                <span style="font-size: 12px; color: #94a3b8;">
                                    Bắt đầu: <%= s.startTime %>
                                </span>
                            <% } %>
                        </div>

                    </div>
                <% } %>
            </div>
        <% } else { %>
            <div class="student-empty-state">
                <div>📭</div>
                <strong>Chưa có ca thi nào</strong>
                <p>Hiện tại chưa có phòng thi nào đang mở hoặc sắp diễn ra.</p>
            </div>
        <% } %>
        </section>

    <!-- Khối 2: Lịch sử bài làm gần đây -->
    <section class="student-section-card">
        <div class="student-section-header">
            <div>
                <div class="student-section-eyebrow">EXAM HISTORY</div>
                <h2>Lịch sử bài thi gần đây</h2>
                <p>Theo dõi kết quả các bài thi đã hoàn thành</p>
            </div>
        </div>
        <div class="student-table-wrapper">
            <table class="data-table student-history-table">
                <thead>
                    <tr>
                        <th>Mã Lượt</th>
                        <th>Đề Thi / Ca Thi</th>
                        <th>Điểm Số</th>
                        <th>Kết Quả</th>
                        <th>Thời Gian Nộp</th>
                        <th style="text-align: right;">Chi Tiết</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (historyList != null && !historyList.isEmpty()) {
                        for (AttemptHistoryDTO h : historyList) { %>
                            <tr>
                                <td>#<%= h.attemptId %></td>
                                <td>
                                    <strong><%= h.examTitle %></strong><br>
                                    <span style="font-size: 12px; color: #64748b;"><%= h.sessionName %></span>
                                </td>
                                <td>
                                    <strong class="<%= h.isPassed ? "score-passed" : "score-failed" %>"><%= h.score %></strong>
                                    <span style="font-size: 12px; color: #64748b;">/ 10</span>
                                </td>
                                <td>
                                    <span class="badge <%= h.isPassed ? "badge-pass" : "badge-fail" %>">
                                        <%= h.isPassed ? "✓ ĐẠT" : "✕ CHƯA ĐẠT" %>
                                    </span>
                                </td>
                                <td style="font-size: 13px; color: #475569;"><%= h.submittedAt %></td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/student/results?attemptId=<%= h.attemptId %>" class="btn btn-outline">
                                        👁 Xem Lại
                                    </a>
                                </td>
                            </tr>
                    <%  }
                    } else { %>
                        <tr>
                            <td colspan="6" style="text-align: center; color: #94a3b8; padding: 24px;">Bạn chưa hoàn thành bài thi nào.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>
</div>

<!-- Modal Nhập Mật Khẩu -->
<div id="passModal" class="modal-overlay">
    <div class="modal-box">
        <h3 style="margin: 0 0 6px 0; font-size: 17px;" id="modalSessionTitle">Vào Phòng Thi</h3>
        <p style="font-size: 13px; color: #64748b; margin-bottom: 16px;" id="modalDesc">Nhập mật khẩu do giáo viên cung cấp để bắt đầu làm bài.</p>
        <form action="${pageContext.request.contextPath}/student/check-pass" method="POST">
            <input type="hidden" name="sessionId" id="modalSessionId">
            <div id="passGroup" style="margin-bottom: 16px;">
                <input type="password" name="sessionPassword" id="modalPassInput" class="form-control" placeholder="Nhập mã mật khẩu..." style="width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box;">
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" class="btn btn-outline" onclick="closePassModal()">Hủy</button>
                <button type="submit" class="btn btn-primary">Xác Nhận & Bắt Đầu</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openPassModal(btn) {
        const id = btn.getAttribute('data-session-id');
        const name = btn.getAttribute('data-session-name');
        const hasPass = btn.getAttribute('data-has-pass') === 'true';

        document.getElementById('modalSessionId').value = id;
        document.getElementById('modalSessionTitle').innerText = name;

        const passGroup = document.getElementById('passGroup');
        const passInput = document.getElementById('modalPassInput');
        const desc = document.getElementById('modalDesc');

        if (hasPass) {
            passGroup.style.display = 'block';
            passInput.value = '';
            passInput.required = true;
            desc.innerText = 'Ca thi này yêu cầu mật khẩu từ giáo viên bộ môn.';
        } else {
            passGroup.style.display = 'none';
            passInput.value = '';
            passInput.required = false;
            desc.innerText = 'Ca thi mở tự do. Bấm nút bên dưới để bắt đầu làm bài!';
        }

        document.getElementById('passModal').classList.add('active');
    }

    function closePassModal() {
        document.getElementById('passModal').classList.remove('active');
    }
</script>
</body>
</html>