<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.AttemptDAO.SessionViewDTO" %>
<%
    List<SessionViewDTO> sessions =
        (List<SessionViewDTO>) request.getAttribute("sessions");
    String keyword = (String) request.getAttribute("keyword");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phòng Thi Trực Tuyến | OnlineExam</title>
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/student.css?v=4">
</head>
<body class="student-available-page">
<div class="container student-available-container">
    <div class="available-header">
        <div class="available-header-icon">📝</div>
        <div>
            <h1>Phòng Thi Trực Tuyến</h1>
            <p>
                Danh sách các ca thi được giáo viên phân công.
                Chọn ca thi phù hợp để bắt đầu làm bài.
            </p>
        </div>
    </div>

    <% if (request.getParameter("error") != null) { %>
        <div class="available-error">
            <span>⚠</span>
            <span><%= request.getParameter("error") %></span>
        </div>
    <% } %>

    <!-- Khung tìm kiếm ca thi -->
    <form action="${pageContext.request.contextPath}/student/exams"
        method="GET"
        class="available-search">

        <div class="available-search-input">
            <span>🔍</span>
            <input type="text"
                name="keyword"
                class="form-control"
                placeholder="Nhập tên ca thi, tên đề thi..."
                value="<%= keyword != null ? keyword : "" %>">
        </div>

        <button type="submit" class="btn btn-primary available-search-btn">
            Tìm kiếm
        </button>
    </form>

    <!-- Danh sách ca thi -->
    <div class="available-session-list">
        <% if (sessions != null && !sessions.isEmpty()) {
            for (SessionViewDTO s : sessions) {
                boolean isOpen = "OPEN".equalsIgnoreCase(s.getStatus());
                boolean hasPass = s.isHasPassword();
        %>
            <div class="available-exam-card">

                <div class="available-exam-content">

                    <div class="available-exam-badges">
                        <span class="badge <%= isOpen ? "badge-open" : "badge-sched" %>">
                            <%= isOpen ? "🟢 ĐANG MỞ PHÒNG" : "🕒 LÊN LỊCH CHỜ" %>
                        </span>

                        <% if (hasPass) { %>
                            <span class="available-password-badge">
                                🔒 Có mật khẩu
                            </span>
                        <% } else { %>
                            <span class="available-free-badge">
                                ✓ Không cần mật khẩu
                            </span>
                        <% } %>
                    </div>

                    <h3 class="available-exam-title">
                        <%= s.getSessionName() %>
                    </h3>

                    <div class="available-exam-name">
                        Đề thi:
                        <strong><%= s.getExamTitle() %></strong>
                    </div>

                    <div class="available-exam-info">
                        <span>⏱ <%= s.getDurationMinutes() %> phút</span>
                        <span class="available-info-divider">|</span>
                        <span>
                            📅
                            <%= s.getStartTime() != null ? s.getStartTime() : "" %>
                            &rarr;
                            <%= s.getEndTime() != null ? s.getEndTime() : "" %>
                        </span>
                    </div>

                </div>

                <div class="available-exam-action">
                    <% if (isOpen) { %>
                        <button type="button"
                                class="btn btn-primary available-enter-btn"
                                data-session-id="<%= s.getSessionId() %>"
                                data-session-name="<%= s.getSessionName() %>"
                                data-has-pass="<%= hasPass %>"
                                onclick="openPassModal(this)">
                            Vào thi <span>→</span>
                        </button>
                    <% } else { %>
                        <button type="button"
                                class="available-disabled-btn"
                                disabled>
                            Chưa mở
                        </button>
                    <% } %>
                </div>

            </div>
        <%  }
        } else { %>

            <div class="available-empty">
                <div class="available-empty-icon">📭</div>
                <h3>Không tìm thấy ca thi</h3>
                <p>
                    Không có ca thi nào phù hợp với từ khóa tìm kiếm của bạn.
                </p>
            </div>

        <% } %>
    </div>
</div>

<!-- ================= MODAL NHẬP MẬT KHẨU PHÒNG THI ================= -->
<div id="passModal"
     class="modal-overlay available-modal-overlay"
     onclick="handleOverlayClick(event)">

    <div class="modal-box available-modal-box">
        <div class="available-modal-icon">🔐</div>
            <h3 class="modal-title available-modal-title" id="modalSessionTitle">
                Vào Phòng Thi
            </h3>

            <p class="modal-desc available-modal-desc" id="modalSessionDesc">
                Vui lòng nhập mật khẩu phòng thi do giáo viên cung cấp để bắt đầu làm bài.
            </p>
        
        <form action="${pageContext.request.contextPath}/student/check-pass" method="POST">
            <input type="hidden" name="sessionId" id="modalSessionId">
            
            <div id="passwordFieldGroup" class="available-password-group">
                <label for="sessionPasswordInput" class="available-password-label">
                    Mật khẩu phòng thi
                </label>
                <input type="password"
                    name="sessionPassword"  
                    id="sessionPasswordInput"
                    class="form-control available-password-input"
                    placeholder="Nhập mật khẩu phòng thi...">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <div class="available-modal-actions">
                    <button type="button"
                            class="btn btn-secondary"
                            onclick="closePassModal()">
                        Hủy
                    </button>

                    <button type="submit" class="btn btn-primary available-confirm-btn">
                        Xác nhận & Bắt đầu
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    // Hàm mở modal khi nhấn nút "Vào Thi"
    function openPassModal(btn) {
        const sessionId = btn.getAttribute('data-session-id');
        const sessionName = btn.getAttribute('data-session-name');
        const hasPass = btn.getAttribute('data-has-pass') === 'true';

        document.getElementById('modalSessionId').value = sessionId;
        document.getElementById('modalSessionTitle').innerText = sessionName;

        const passGroup = document.getElementById('passwordFieldGroup');
        const passInput = document.getElementById('sessionPasswordInput');
        const desc = document.getElementById('modalSessionDesc');

        if (hasPass) {
            passGroup.style.display = 'block';
            passInput.value = '';
            passInput.required = true;
            desc.innerText = 'Ca thi này yêu cầu mật khẩu do giáo viên bộ môn cung cấp.';
            setTimeout(() => passInput.focus(), 100);
        } else {
            passGroup.style.display = 'none';
            passInput.value = '';
            passInput.required = false;
            desc.innerText = 'Ca thi mở tự do, không yêu cầu mật khẩu. Bạn có thể bắt đầu làm bài ngay!';
        }

        // Bật hiển thị modal overlay
        document.getElementById('passModal').classList.add('active');
    }

    // Hàm đóng modal
    function closePassModal() {
        document.getElementById('passModal').classList.remove('active');
    }

    // Đóng khi click vào vùng nền mờ phía ngoài
    function handleOverlayClick(event) {
        if (event.target.id === 'passModal') {
            closePassModal();
        }
    }

    // Nhấn phím Escape (ESC) để đóng modal
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closePassModal();
        }
    });
</script>
</body>
</html>