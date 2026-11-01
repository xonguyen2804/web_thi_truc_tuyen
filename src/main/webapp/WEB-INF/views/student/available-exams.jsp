<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.onlineexam.model.ExamSession" %>
<%
    List<ExamSession> sessions = (List<ExamSession>) request.getAttribute("sessions");
    String keyword = (String) request.getAttribute("keyword");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phòng Thi Trực Tuyến | OnlineExam</title>
    <style>
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; 
            background: #f8fafc; 
            margin: 0; 
            padding: 24px; 
            color: #1e293b; 
        }
        .container { max-width: 860px; margin: 0 auto; }
        .header { margin-bottom: 24px; }
        .header h1 { font-size: 24px; color: #0f172a; margin: 0 0 6px 0; }
        .header p { color: #64748b; font-size: 14px; margin: 0; }
        
        /* Search Box */
        .search-box { display: flex; gap: 10px; margin-bottom: 24px; }
        .form-control { 
            flex: 1; 
            padding: 11px 16px; 
            border: 1px solid #cbd5e1; 
            border-radius: 8px; 
            font-size: 14px; 
            box-sizing: border-box;
        }
        .form-control:focus {
            outline: none;
            border-color: #2563eb;
            box-shadow: 0 0 0 2px rgba(37,99,235,0.15);
        }

        .btn { 
            padding: 11px 20px; 
            border-radius: 8px; 
            font-weight: 700; 
            cursor: pointer; 
            border: none; 
            font-size: 14px; 
            transition: all 0.15s;
        }
        .btn-primary { background: #2563eb; color: #fff; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-secondary { background: #f1f5f9; color: #475569; }
        .btn-secondary:hover { background: #e2e8f0; }

        /* Exam Card */
        .exam-card { 
            background: #fff; 
            border: 1px solid #e2e8f0; 
            border-radius: 12px; 
            padding: 20px; 
            margin-bottom: 16px; 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            box-shadow: 0 1px 3px rgba(0,0,0,0.03); 
        }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
        .badge-open { background: #dcfce7; color: #166534; }
        .badge-sched { background: #e0f2fe; color: #0284c7; }

        /* ================= MODAL POPUP ================= */
        .modal-overlay { 
            display: none; 
            position: fixed; 
            top: 0; 
            left: 0; 
            width: 100vw; 
            height: 100vh; 
            background: rgba(15, 23, 42, 0.6); 
            z-index: 9999; 
            align-items: center; 
            justify-content: center; 
            backdrop-filter: blur(2px); 
        }
        /* Class active để mở modal */
        .modal-overlay.active { 
            display: flex !important; 
        }
        .modal-box { 
            background: #ffffff; 
            border-radius: 14px; 
            padding: 24px; 
            width: 90%; 
            max-width: 400px; 
            box-shadow: 0 20px 25px -5px rgba(0,0,0,0.2); 
            animation: modalFadeIn 0.2s ease-out;
        }
        @keyframes modalFadeIn {
            from { transform: scale(0.95); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
        .modal-title { margin: 0 0 6px 0; font-size: 18px; color: #0f172a; }
        .modal-desc { font-size: 13px; color: #64748b; margin: 0 0 18px 0; line-height: 1.4; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>Phòng Thi Trực Tuyến</h1>
        <p>Tìm ca thi được giáo viên phân công, nhập mật khẩu phòng để bắt đầu làm bài.</p>
    </div>

    <% if (request.getParameter("error") != null) { %>
        <div style="padding: 12px 16px; background: #fee2e2; color: #991b1b; border-radius: 8px; margin-bottom: 20px; border: 1px solid #fecaca;">
            ✕ <%= request.getParameter("error") %>
        </div>
    <% } %>

    <!-- Khung tìm kiếm ca thi -->
    <form action="${pageContext.request.contextPath}/student/exams" method="GET" class="search-box">
        <input type="text" name="keyword" class="form-control" placeholder="Nhập tên ca thi, tên đề thi..." value="<%= keyword != null ? keyword : "" %>">
        <button type="submit" class="btn btn-primary">🔍 Tìm Kiếm</button>
    </form>

    <!-- Danh sách ca thi -->
    <div class="session-list">
        <% if (sessions != null && !sessions.isEmpty()) {
            for (ExamSession s : sessions) { 
                boolean isOpen = "OPEN".equalsIgnoreCase(s.getStatus());
                boolean hasPass = s.getSessionPassword() != null && !s.getSessionPassword().trim().isEmpty();
        %>
            <div class="exam-card">
                <div>
                    <div style="margin-bottom: 6px;">
                        <span class="badge <%= isOpen ? "badge-open" : "badge-sched" %>">
                            <%= isOpen ? "🟢 ĐANG MỞ PHÒNG" : "🕒 LÊN LỊCH CHỜ" %>
                        </span>
                        <span style="font-size: 13px; color: #64748b; margin-left: 8px;">Phòng thi: <strong><%= s.getTargetName() %></strong></span>
                        <% if (hasPass) { %>
                            <span style="font-size: 11px; background: #fef3c7; color: #92400e; padding: 2px 6px; border-radius: 4px; margin-left: 4px;">🔒 Có mật khẩu</span>
                        <% } %>
                    </div>
                    <h3 style="margin: 0 0 6px 0; font-size: 18px; color: #0f172a;"><%= s.getSessionName() %></h3>
                    <div style="font-size: 13px; color: #475569;">
                        Đề thi: <strong><%= s.getExamTitle() %></strong> &bull; Thời gian: <strong><%= s.getDurationMinutes() %> phút</strong>
                    </div>
                    <div style="font-size: 12px; color: #64748b; margin-top: 4px;">
                        Lịch: <%= s.getStartTime() != null ? s.getStartTime().format(dtf) : "" %> &rarr; <%= s.getEndTime() != null ? s.getEndTime().format(dtf) : "" %>
                    </div>
                </div>
                <div>
                    <% if (isOpen) { %>
                        <button type="button" 
                                class="btn btn-primary" 
                                data-session-id="<%= s.getSessionId() %>" 
                                data-session-name="<%= s.getSessionName() %>"
                                data-has-pass="<%= hasPass %>"
                                onclick="openPassModal(this)">
                            Vào Thi &rarr;
                        </button>
                    <% } else { %>
                        <button type="button" class="btn" style="background: #e2e8f0; color: #94a3b8; cursor: not-allowed;" disabled>
                            Chưa Mở
                        </button>
                    <% } %>
                </div>
            </div>
        <%  }
        } else { %>
            <div style="text-align: center; padding: 48px; background: #fff; border-radius: 12px; color: #94a3b8; border: 1px dashed #cbd5e1;">
                Không tìm thấy ca thi nào đang mở phù hợp với từ khóa của bạn.
            </div>
        <% } %>
    </div>
</div>

<!-- ================= MODAL NHẬP MẬT KHẨU PHÒNG THI ================= -->
<div id="passModal" class="modal-overlay" onclick="handleOverlayClick(event)">
    <div class="modal-box">
        <h3 class="modal-title" id="modalSessionTitle">Vào Phòng Thi</h3>
        <p class="modal-desc" id="modalSessionDesc">Vui lòng nhập mật khẩu phòng thi do giáo viên cung cấp để bắt đầu làm bài.</p>
        
        <form action="${pageContext.request.contextPath}/student/check-pass" method="POST">
            <input type="hidden" name="sessionId" id="modalSessionId">
            
            <div id="passwordFieldGroup" style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px; color: #334155;">
                    Mật khẩu phòng thi:
                </label>
                <input type="password" 
                       name="sessionPassword" 
                       id="sessionPasswordInput" 
                       class="form-control" 
                       style="width: 100%; font-size: 15px;" 
                       placeholder="Nhập mã mật khẩu...">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" class="btn btn-secondary" onclick="closePassModal()">Hủy</button>
                <button type="submit" class="btn btn-primary">Xác Nhận & Bắt Đầu</button>
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