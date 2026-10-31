<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.controller.StudentDashboardServlet.ActiveSessionDTO" %>
<%@ page import="com.onlineexam.controller.StudentDashboardServlet.AttemptHistoryDTO" %>
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
    <style>
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; 
            background: #f8fafc; 
            margin: 0; 
            padding: 24px; 
            color: #1e293b; 
        }
        .container { max-width: 1000px; margin: 0 auto; }
        
        /* Navbar mini */
        .top-nav {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #fff;
            padding: 14px 20px;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
            margin-bottom: 24px;
        }
        .brand { font-size: 18px; font-weight: 800; color: #2563eb; display: flex; align-items: center; gap: 8px; }
        
        /* Welcome Banner */
        .welcome-banner {
            background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
            color: #fff;
            border-radius: 14px;
            padding: 28px;
            margin-bottom: 24px;
            box-shadow: 0 4px 12px rgba(37,99,235,0.15);
        }
        .welcome-banner h1 { margin: 0 0 6px 0; font-size: 24px; font-weight: 800; }
        .welcome-banner p { margin: 0; font-size: 14px; opacity: 0.9; }

        /* Stats Grid */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-bottom: 28px;
        }
        .stat-card {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .stat-val { font-size: 28px; font-weight: 800; color: #0f172a; line-height: 1.2; }
        .stat-lbl { font-size: 13px; color: #64748b; margin-top: 4px; font-weight: 500; }

        /* Card Section */
        .section-card {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 22px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }
        .section-header h2 { margin: 0; font-size: 17px; color: #0f172a; }

        /* Table */
        .data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
        .data-table th, .data-table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        .data-table th { background: #f8fafc; color: #475569; font-weight: 600; font-size: 13px; }
        
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
        .badge-pass { background: #dcfce7; color: #166534; }
        .badge-fail { background: #fee2e2; color: #991b1b; }
        .badge-open { background: #dcfce7; color: #166534; }

        .btn { padding: 8px 14px; border-radius: 6px; font-weight: 700; cursor: pointer; border: none; font-size: 13px; text-decoration: none; display: inline-block; }
        .btn-primary { background: #2563eb; color: #fff; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-outline { background: #f1f5f9; color: #475569; }
        .btn-outline:hover { background: #e2e8f0; }

        /* Modal Overlay */
        .modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15,23,42,0.6); z-index: 9999; align-items: center; justify-content: center; backdrop-filter: blur(2px); }
        .modal-overlay.active { display: flex !important; }
        .modal-box { background: #fff; border-radius: 14px; padding: 24px; width: 90%; max-width: 380px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.15); }

        .score-passed { color: #16a34a; font-size: 16px; }
        .score-failed { color: #dc2626; font-size: 16px; }
    </style>
</head>
<body>

<div class="container">
    <!-- Top Nav -->
    <div class="top-nav">
        <div class="brand">
            <span style="background: #2563eb; color: white; width: 30px; height: 30px; border-radius: 6px; display: grid; place-items: center; font-size: 14px;">OE</span>
            OnlineExam &bull; Học Sinh
        </div>
        <div style="font-size: 14px; font-weight: 600; color: #334155;">
            👤 <%= studentName %>
        </div>
    </div>

    <!-- Banner Chào Mừng -->
    <div class="welcome-banner">
        <h1>👋 Xin chào, <%= studentName %>!</h1>
        <p>Chào mừng bạn đến với hệ thống luyện thi trực tuyến. Hãy kiểm tra ca thi đang mở để bắt đầu làm bài ôn luyện nhé.</p>
    </div>

    <!-- 3 Thẻ Thống Kê -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-val"><%= totalExamsDone %></div>
            <div class="stat-lbl">📝 Số bài thi đã hoàn thành</div>
        </div>
        <div class="stat-card">
            <div class="stat-val" style="color: #2563eb;"><%= avgScore %></div>
            <div class="stat-lbl">🎯 Điểm số trung bình</div>
        </div>
        <div class="stat-card">
            <div class="stat-val" style="color: #16a34a;"><%= passedCount %></div>
            <div class="stat-lbl">🏆 Số bài thi đạt chuẩn</div>
        </div>
    </div>

    <!-- Khối 1: Ca thi đang mở -->
    <div class="section-card">
        <div class="section-header">
            <h2>🟢 Phòng Thi Sắp Tới / Đang Mở</h2>
            <a href="${pageContext.request.contextPath}/student/exams" style="font-size: 13px; color: #2563eb; text-decoration: none; font-weight: 600;">Xem tất cả &rarr;</a>
        </div>
        <% if (activeSessions != null && !activeSessions.isEmpty()) { %>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                <% for (ActiveSessionDTO s : activeSessions) { 
                    boolean isOpen = "OPEN".equalsIgnoreCase(s.status);
                %>
                    <div style="border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; background: #fafafa; display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                                <span class="badge <%= isOpen ? "badge-open" : "badge-sched" %>"><%= isOpen ? "🟢 ĐANG MỞ" : "🕒 LÊN LỊCH" %></span>
                                <span style="font-size: 12px; color: #64748b;"><%= s.durationMinutes %> phút</span>
                            </div>
                            <strong style="font-size: 15px; color: #0f172a;"><%= s.sessionName %></strong>
                            <div style="font-size: 13px; color: #475569; margin-top: 4px;">Đề: <%= s.examTitle %></div>
                        </div>
                        <div style="margin-top: 14px; text-align: right;">
                            <% if (isOpen) { %>
                                <button type="button" class="btn btn-primary" 
                                        data-session-id="<%= s.sessionId %>" 
                                        data-session-name="<%= s.sessionName %>"
                                        data-has-pass="<%= s.hasPassword %>"
                                        onclick="openPassModal(this)">
                                    Vào Thi &rarr;
                                </button>
                            <% } else { %>
                                <span style="font-size: 12px; color: #94a3b8;">Bắt đầu: <%= s.startTime %></span>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } else { %>
            <div style="text-align: center; padding: 24px; color: #94a3b8;">Hiện tại chưa có ca thi nào đang mở.</div>
        <% } %>
    </div>

    <!-- Khối 2: Lịch sử bài làm gần đây -->
    <div class="section-card">
        <div class="section-header">
            <h2>📜 Lịch Sử Bài Thi Đã Làm Gần Đây</h2>
        </div>
        <table class="data-table">
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