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
<div id="fullscreenOverlay"
    style="
    position: fixed;
    inset: 0;
    z-index: 99999;
    background: rgba(15, 23, 42, 0.96);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    text-align: center;
    ">

<div>
    <h2>Chuẩn bị vào làm bài</h2>

    <p>
        Vui lòng chuyển sang chế độ toàn màn hình
        để bắt đầu làm bài thi.
    </p>

    <button type="button"
            id="enterFullscreenBtn"
            style="
                padding: 14px 28px;
                border: none;
                border-radius: 8px;
                background: #2563eb;
                color: white;
                font-size: 16px;
                cursor: pointer;
            ">
        Tiếp tục vào bài thi
    </button>
</div>
</div>

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
    <input type="hidden"
            id="remainingSecondsInput"
            value="<%= request.getAttribute("remainingSeconds") %>">

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

        const remainingInput =
            document.getElementById("remainingSecondsInput");

        const timerDisplay =
            document.getElementById("countdown");

        const examForm =
            document.getElementById("examForm");

        if (!remainingInput || !timerDisplay || !examForm) {
            return;
        }

        let remainingSeconds = parseInt(
            remainingInput.value,
            10
        );

        if (isNaN(remainingSeconds) || remainingSeconds < 0) {
            remainingSeconds = 0;
        }

        // Tính deadline từ số giây server trả về
        const deadline = Date.now() + remainingSeconds * 1000;

        function renderTimer() {

            const minutes = Math.floor(remainingSeconds / 60);
            const seconds = remainingSeconds % 60;

            timerDisplay.textContent =
                String(minutes).padStart(2, "0") + ":" +
                String(seconds).padStart(2, "0");

            if (remainingSeconds <= 300) {
                timerDisplay.classList.add("timer-warning");
            }
        }

        function updateTimer() {

            remainingSeconds = Math.max(
                0,
                Math.ceil((deadline - Date.now()) / 1000)
            );

            renderTimer();

            if (remainingSeconds <= 0) {

                clearInterval(timerInterval);

                alert(
                    "Đã hết thời gian làm bài! " +
                    "Hệ thống sẽ tự động nộp bài."
                );

                examForm.submit();
            }
        }

        renderTimer();

        const timerInterval = setInterval(
            updateTimer,
            250
        );
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