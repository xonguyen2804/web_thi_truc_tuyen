<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.AttemptDAO.QuestionDTO" %>
<%@ page import="com.onlineexam.dao.AttemptDAO.OptionDTO" %>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css?v=3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/exam-security.css?v=1">
</head>
<body>

<header class="exam-topbar">
     <a href="${pageContext.request.contextPath}/student/dashboard"
        class="exam-home-btn"
        onclick="return confirmLeaveExam()">
            ← Trang chủ
    </a>
    <div class="exam-topbar-info">
        <span class="exam-topbar-label">PHÒNG THI TRỰC TUYẾN</span>
        <h2><%= examTitle %></h2>
        <span class="exam-attempt-id">Mã phiên thi: #<%= attemptId %></span>
    </div>

    <button type="button" id="startExamBtn" class="exam-start-btn">
        BẮT ĐẦU THI
    </button>

    <div class="exam-timer">
        <span class="exam-timer-icon">⏱</span>
        <div>
            <span class="exam-timer-label">Thời gian còn lại</span>
            <strong id="countdown">--:--</strong>
        </div>
    </div>
</header>

<form id="examForm" action="${pageContext.request.contextPath}/student/submit-exam" method="POST">
    <input type="hidden" name="action" value="submit">
    <input type="hidden" name="attemptId" value="<%= attemptId %>">
    <input type="hidden" id="durationMinutesInput" value="<%= durationMinutes %>">

    <div class="layout-exam student-take-exam">
        <div class="questions-col">
            <% if (questions != null) {
                int qIndex = 1;
                for (QuestionDTO q : questions) { %>
                    <div class="question-card student-question-card" id="q-<%= q.questionId %>">
                        <div class="student-question-heading">
                            <div class="student-question-title">
                                Câu <%= qIndex++ %>: <%= q.questionText %>
                            </div>

                            <span class="student-question-type">
                                <%= "TRUE_FALSE".equalsIgnoreCase(q.questionType) ? "ĐÚNG / SAI" : ("SHORT_ANSWER".equalsIgnoreCase(q.questionType) ? "TRẢ LỜI NGẮN" : "TRẮC NGHIỆM") %>
                            </span>
                        </div>

                        <!-- 1. Dạng trả lời ngắn-->
                        <% if ("SHORT_ANSWER".equalsIgnoreCase(q.questionType)) { %>
                            <div>
                                <input
                                    type="text"
                                    class="student-short-answer short-answer-input"
                                    name="short_q_<%= q.questionId %>"
                                    maxlength="4"
                                    autocomplete="off"
                                    placeholder="Nhập đáp án (tối đa 4 ký tự)..."
                                    style="width: 100%; padding: 11px 14px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; box-sizing: border-box;"
                                >
                            </div>

                        <!-- 2. Dạng ĐÚNG/SAI (4 Ã CON) -->
                        <% } else if ("TRUE_FALSE".equalsIgnoreCase(q.questionType)) { %>
                            <table class="tf-table student-tf-table">
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
                                                <div class="tf-radio-group student-tf-radio-group">
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

                        <!-- 3. Dạng trắc nghiệm 4 đáp án (SINGLE) -->
                        <% } else { %>
                            <div class="options">
                                <% char optChar = 'A';
                                   for (OptionDTO opt : q.options) { %>
                                    <label class="opt-label student-option">
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
            <aside class="sidebar-nav-q student-question-sidebar">
                <h4 class="student-sidebar-title">Bảng câu hỏi</h4>
                <p class="student-sidebar-desc">Chọn số câu để di chuyển nhanh</p>
                <div class="q-grid">
                    <% if (questions != null) {
                        for (int i = 1; i <= questions.size(); i++) { %>
                            <a href="#q-<%= questions.get(i - 1).questionId %>" class="q-btn"><%= i %></a>
                    <%  }
                    } %>
                </div>
                <button type="button" class="btn-submit student-submit-btn" onclick="confirmSubmit()">
                    NỘP BÀI THI
                </button>
            </aside>
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

    function confirmLeaveExam() {
        return confirm(
            "Bạn có chắc muốn rời khỏi trang làm bài không? " +
            "Bài làm hiện tại chưa được nộp."
        );
    }
</script>
<script src="${pageContext.request.contextPath}/assets/js/exam-security.js?v=1"></script>
</body>
</html>