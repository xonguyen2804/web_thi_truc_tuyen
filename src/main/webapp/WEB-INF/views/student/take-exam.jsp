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
    <title><%= examTitle %> | LÃ m BÃ i Thi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css">
</head>
<body>

<div class="header-bar">
    <div>
        <h2 style="margin: 0; font-size: 18px; color: #0f172a;"><%= examTitle %></h2>
        <span style="font-size: 12px; color: #64748b;">MÃ£ phiÃªn thi: #<%= attemptId %></span>
    </div>
    <div class="timer-box">
        â± <span id="countdown">--:--</span>
    </div>
</div>

<form id="examForm" action="${pageContext.request.contextPath}/student/submit-exam" method="POST">
    <input type="hidden" name="attemptId" value="<%= attemptId %>">
    <input type="hidden" id="durationMinutesInput" value="<%= durationMinutes %>">

    <div class="layout-exam">
        <div class="questions-col">
            <% if (questions != null) {
                int qIndex = 1;
                for (QuestionDTO q : questions) { %>
                    <div class="question-card" id="q-<%= q.questionId %>">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                            <div style="font-weight: 700; font-size: 15px; color: #0f172a;">
                                CÃ¢u <%= qIndex++ %>: <%= q.questionText %>
                            </div>
                            <span style="font-size: 11px; font-weight: 700; background: #e2e8f0; color: #475569; padding: 2px 6px; border-radius: 4px; white-space: nowrap;">
                                <%= "TRUE_FALSE".equalsIgnoreCase(q.questionType) ? "ÄÃšNG / SAI" : ("SHORT_ANSWER".equalsIgnoreCase(q.questionType) ? "TRáº¢ Lá»œI NGáº®N" : "TRáº®C NGHIá»†M") %>
                            </span>
                        </div>

                        <!-- 1. Dáº NG TRáº¢ Lá»œI NGáº®N -->
                        <% if ("SHORT_ANSWER".equalsIgnoreCase(q.questionType)) { %>
                            <div>
                                <input type="text" name="short_q_<%= q.questionId %>" maxlength="10" placeholder="Äiá»n káº¿t quáº£ vÃ o Ä‘Ã¢y (vÃ­ dá»¥: 0.5 hoáº·c 50)..." style="width: 100%; padding: 11px 14px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; box-sizing: border-box;">
                            </div>

                        <!-- 2. Dáº NG ÄÃšNG / SAI (4 Ã CON) -->
                        <% } else if ("TRUE_FALSE".equalsIgnoreCase(q.questionType)) { %>
                            <table class="tf-table">
                                <thead>
                                    <tr>
                                        <th>Ã nháº­n Ä‘á»‹nh</th>
                                        <th style="width: 150px; text-align: center;">Lá»±a chá»n</th>
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
                                                        <input type="radio" name="tf_opt_<%= opt.optionId %>" value="true"> ÄÃºng
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

                        <!-- 3. Dáº NG TRáº®C NGHIá»†M 4 Lá»°A CHá»ŒN (SINGLE) -->
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
                <h4 style="margin: 0 0 10px 0; font-size: 14px;">Báº£ng cÃ¢u há»i</h4>
                <div class="q-grid">
                    <% if (questions != null) {
                        for (int i = 1; i <= questions.size(); i++) { %>
                            <a href="#q-<%= questions.get(i - 1).questionId %>" class="q-btn"><%= i %></a>
                    <%  }
                    } %>
                </div>
                <button type="button" class="btn-submit" onclick="confirmSubmit()">Ná»˜P BÃ€I THI</button>
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
                alert("ÄÃ£ háº¿t thá»i gian lÃ m bÃ i! Há»‡ thá»‘ng tá»± Ä‘á»™ng ná»™p bÃ i thi cá»§a báº¡n.");
                document.getElementById('examForm').submit();
                return;
            }
            renderTimer();
        }, 1000);
    });

    function confirmSubmit() {
        if (confirm("Báº¡n cÃ³ cháº¯c cháº¯n muá»‘n ná»™p bÃ i thi ngay bÃ¢y giá» khÃ´ng?")) {
            document.getElementById('examForm').submit();
        }
    }
</script>
</body>
</html>