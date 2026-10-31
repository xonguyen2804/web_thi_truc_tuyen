<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.controller.AttemptServlet.QuestionDTO" %>
<%@ page import="com.onlineexam.controller.AttemptServlet.OptionDTO" %>
<%
    int attemptId = (Integer) request.getAttribute("attemptId");
    String examTitle = (String) request.getAttribute("examTitle");
    int durationMinutes = (Integer) request.getAttribute("durationMinutes");
    List<QuestionDTO> questions = (List<QuestionDTO>) request.getAttribute("questions");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= examTitle %> | Làm Bài Thi</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; background: #f8fafc; margin: 0; padding: 0; color: #1e293b; }
        .header-bar { background: #fff; border-bottom: 1px solid #cbd5e1; padding: 12px 24px; position: sticky; top: 0; z-index: 100; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
        .timer-box { font-size: 20px; font-weight: 800; color: #dc2626; background: #fef2f2; padding: 6px 14px; border-radius: 8px; border: 1px solid #fecaca; }
        .layout { display: grid; grid-template-columns: 1fr 280px; gap: 24px; max-width: 1200px; margin: 24px auto; padding: 0 16px; }
        .question-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 22px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
        
        /* Dạng 1: Trắc nghiệm SINGLE */
        .opt-label { display: flex; align-items: center; gap: 10px; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; margin-top: 8px; cursor: pointer; transition: all 0.15s; }
        .opt-label:hover { background: #f1f5f9; border-color: #cbd5e1; }
        
        /* Dạng 2: Bảng Đúng / Sai (TRUE_FALSE) */
        .tf-table { width: 100%; border-collapse: collapse; margin-top: 12px; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
        .tf-table th, .tf-table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        .tf-table th { background: #f8fafc; color: #475569; font-weight: 600; }
        .tf-radio-group { display: flex; gap: 16px; align-items: center; justify-content: center; }
        .tf-radio-item { display: flex; align-items: center; gap: 4px; cursor: pointer; font-weight: 600; font-size: 13px; }
        .text-correct { color: #16a34a; }
        .text-wrong { color: #dc2626; }

        /* Bảng điều hướng bên phải */
        .sidebar-nav-q { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; position: sticky; top: 80px; }
        .q-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-top: 12px; }
        .q-btn { padding: 10px 0; text-align: center; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 13px; font-weight: 700; text-decoration: none; color: #334155; }
        .q-btn:hover { background: #e2e8f0; }
        .btn-submit { width: 100%; padding: 14px; background: #16a34a; color: white; border: none; border-radius: 8px; font-weight: 700; font-size: 15px; cursor: pointer; margin-top: 18px; }
        .btn-submit:hover { background: #15803d; }
    </style>
</head>
<body>

<div class="header-bar">
    <div>
        <h2 style="margin: 0; font-size: 18px; color: #0f172a;"><%= examTitle %></h2>
        <span style="font-size: 12px; color: #64748b;">Mã phiên thi: #<%= attemptId %></span>
    </div>
    <div class="timer-box">
        ⏱ <span id="countdown">--:--</span>
    </div>
</div>

<form id="examForm" action="${pageContext.request.contextPath}/student/submit-exam" method="POST">
    <input type="hidden" name="attemptId" value="<%= attemptId %>">
    <input type="hidden" id="durationMinutesInput" value="<%= durationMinutes %>">

    <div class="layout">
        <div class="questions-col">
            <% if (questions != null) {
                int qIndex = 1;
                for (QuestionDTO q : questions) { %>
                    <div class="question-card" id="q-<%= q.questionId %>">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                            <div style="font-weight: 700; font-size: 15px; color: #0f172a;">
                                Câu <%= qIndex++ %>: <%= q.questionText %>
                            </div>
                            <span style="font-size: 11px; font-weight: 700; background: #e2e8f0; color: #475569; padding: 2px 6px; border-radius: 4px; white-space: nowrap;">
                                <%= "TRUE_FALSE".equalsIgnoreCase(q.questionType) ? "ĐÚNG / SAI" : ("SHORT_ANSWER".equalsIgnoreCase(q.questionType) ? "TRẢ LỜI NGẮN" : "TRẮC NGHIỆM") %>
                            </span>
                        </div>

                        <!-- 1. DẠNG TRẢ LỜI NGẮN -->
                        <% if ("SHORT_ANSWER".equalsIgnoreCase(q.questionType)) { %>
                            <div>
                                <input type="text" name="short_q_<%= q.questionId %>" maxlength="10" placeholder="Điền kết quả vào đây (ví dụ: 0.5 hoặc 50)..." style="width: 100%; padding: 11px 14px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; box-sizing: border-box;">
                            </div>

                        <!-- 2. DẠNG ĐÚNG / SAI (4 Ý CON) -->
                        <% } else if ("TRUE_FALSE".equalsIgnoreCase(q.questionType)) { %>
                            <table class="tf-table">
                                <thead>
                                    <tr>
                                        <th>Ý nhận định</th>
                                        <th style="width: 150px; text-align: center;">Lựa chọn</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% char subChar = 'a';
                                       for (OptionDTO opt : q.options) { %>
                                        <tr>
                                            <td>
                                                <strong><%= subChar++ %>)</strong> <%= opt.optionText %>
                                            </td>
                                            <td>
                                                <div class="tf-radio-group">
                                                    <label class="tf-radio-item text-correct">
                                                        <input type="radio" name="tf_opt_<%= opt.optionId %>" value="true"> Đúng
                                                    </label>
                                                    <label class="tf-radio-item text-wrong">
                                                        <input type="radio" name="tf_opt_<%= opt.optionId %>" value="false"> Sai
                                                    </label>
                                                </div>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>

                        <!-- 3. DẠNG TRẮC NGHIỆM 4 LỰA CHỌN (SINGLE) -->
                        <% } else { %>
                            <div class="options">
                                <% char optChar = 'A';
                                   for (OptionDTO opt : q.options) { %>
                                    <label class="opt-label">
                                        <input type="radio" name="q_<%= q.questionId %>" value="<%= opt.optionId %>">
                                        <span><strong><%= optChar++ %>.</strong> <%= opt.optionText %></span>
                                    </label>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
            <%  }
            } %>
        </div>

        <div>
            <div class="sidebar-nav-q">
                <h4 style="margin: 0 0 10px 0; font-size: 14px;">Bảng câu hỏi</h4>
                <div class="q-grid">
                    <% if (questions != null) {
                        for (int i = 1; i <= questions.size(); i++) { %>
                            <a href="#q-<%= questions.get(i - 1).questionId %>" class="q-btn"><%= i %></a>
                    <%  }
                    } %>
                </div>
                <button type="button" class="btn-submit" onclick="confirmSubmit()">NỘP BÀI THI</button>
            </div>
        </div>
    </div>
</form>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const inputMinutes = document.getElementById('durationMinutesInput');
        let minutes = inputMinutes && inputMinutes.value ? parseInt(inputMinutes.value, 10) : 45;
        if (isNaN(minutes) || minutes <= 0) minutes = 45;

        let totalSeconds = minutes * 60;
        const timerDisplay = document.getElementById('countdown');

        function renderTimer() {
            let m = Math.floor(totalSeconds / 60);
            let s = totalSeconds % 60;
            timerDisplay.innerText = 
                (m < 10 ? "0" : "") + m + ":" + 
                (s < 10 ? "0" : "") + s;
        }

        renderTimer();

        const timerInterval = setInterval(function () {
            totalSeconds--;
            if (totalSeconds < 0) {
                clearInterval(timerInterval);
                timerDisplay.innerText = "00:00";
                alert("Đã hết thời gian làm bài! Hệ thống tự động nộp bài thi của bạn.");
                document.getElementById('examForm').submit();
                return;
            }
            renderTimer();
        }, 1000);
    });

    function confirmSubmit() {
        if (confirm("Bạn có chắc chắn muốn nộp bài thi ngay bây giờ không?")) {
            document.getElementById('examForm').submit();
        }
    }
</script>
</body>
</html>