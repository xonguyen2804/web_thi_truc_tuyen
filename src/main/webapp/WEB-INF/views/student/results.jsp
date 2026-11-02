<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal" %>
<%
    String examTitle = (String) request.getAttribute("examTitle");
    String sessionName = (String) request.getAttribute("sessionName");
    String studentName = (String) request.getAttribute("studentName");

    BigDecimal score = (BigDecimal) request.getAttribute("score");
    BigDecimal passScore = (BigDecimal) request.getAttribute("passScore");

    int correctCount = (Integer) request.getAttribute("correctCount");
    int totalQuestions = (Integer) request.getAttribute("totalQuestions");

    String startedAt = (String) request.getAttribute("startedAt");
    String submittedAt = (String) request.getAttribute("submittedAt");

    if (score == null) {
        score = BigDecimal.ZERO;
    }

    if (passScore == null) {
        passScore = new BigDecimal("5.0");
    }

    boolean isPassed = score.compareTo(passScore) >= 0;

    int wrongCount = Math.max(0, totalQuestions - correctCount);
    double scorePercent = Math.max(0, Math.min(100, score.doubleValue() * 10));
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Kết Quả Bài Thi | OnlineExam</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/student.css?v=5">
</head>

<body class="student-results-page">

<div class="student-results-container">

    <!-- Header -->
    <div class="results-header">
        <div class="results-header-icon">📊</div>

        <div>
            <h1>Kết quả bài thi</h1>
            <p>
                Xem lại kết quả và đánh giá quá trình làm bài của bạn.
            </p>
        </div>
    </div>

    <!-- Result summary -->
    <section class="results-main-card">

        <div class="results-status <%= isPassed ? "results-status-pass" : "results-status-fail" %>">
            <span class="results-status-icon">
                <%= isPassed ? "✓" : "!" %>
            </span>

            <span>
                <%= isPassed ? "CHÚC MỪNG! BẠN ĐÃ ĐẠT" : "BẠN CHƯA ĐẠT YÊU CẦU" %>
            </span>
        </div>

        <h2 class="results-exam-title">
            <%= examTitle != null ? examTitle : "Bài thi" %>
        </h2>

        <div class="results-participant">
            <span>Ca thi:</span>
            <strong><%= sessionName != null ? sessionName : "—" %></strong>

            <span class="results-dot">•</span>

            <span>Thí sinh:</span>
            <strong><%= studentName != null ? studentName : "—" %></strong>
        </div>

        <!-- Score -->
        <div class="results-score-area">

            <div class="results-score-circle <%= isPassed ? "score-pass" : "score-fail" %>">
                <div class="results-score-value">
                    <%= score.stripTrailingZeros().toPlainString() %>
                </div>

                <div class="results-score-total">/ 10 điểm</div>
            </div>

            <div class="results-score-caption">
                <div class="results-score-label">Điểm số của bạn</div>

                <div class="results-score-description">
                    <%= isPassed
                        ? "Bạn đã hoàn thành bài thi và đạt điểm yêu cầu."
                        : "Bạn chưa đạt điểm chuẩn. Hãy cố gắng hơn ở lần thi tiếp theo." %>
                </div>
            </div>

        </div>

        <!-- Score progress -->
        <div class="results-progress-section">
            <div class="results-progress-heading">
                <span>Mức điểm đạt được</span>
                <strong><%= scorePercent %>%</strong>
            </div>

            <div class="results-progress-track">
                <div class="results-progress-fill <%= isPassed ? "progress-pass" : "progress-fail" %>"
                     style="width: <%= scorePercent %>%;">
                </div>
            </div>

            <div class="results-progress-note">
                Điểm chuẩn đạt:
                <strong><%= passScore.stripTrailingZeros().toPlainString() %> / 10</strong>
            </div>
        </div>

        <!-- Statistics -->
        <div class="results-stats-grid">

            <div class="results-stat-item">
                <div class="results-stat-icon results-stat-correct">✓</div>
                <div class="results-stat-value"><%= correctCount %></div>
                <div class="results-stat-label">Câu trả lời đúng</div>
            </div>

            <div class="results-stat-item">
                <div class="results-stat-icon results-stat-wrong">✕</div>
                <div class="results-stat-value"><%= wrongCount %></div>
                <div class="results-stat-label">Câu chưa đúng</div>
            </div>

            <div class="results-stat-item">
                <div class="results-stat-icon results-stat-total">☰</div>
                <div class="results-stat-value"><%= totalQuestions %></div>
                <div class="results-stat-label">Tổng số câu hỏi</div>
            </div>

        </div>

        <!-- Time information -->
        <div class="results-detail-section">

            <h3 class="results-section-title">
                <span>🕒</span> Thông tin bài làm
            </h3>

            <div class="results-detail-grid">

                <div class="results-detail-item">
                    <span class="results-detail-label">Thời gian bắt đầu</span>
                    <strong><%= startedAt != null ? startedAt : "—" %></strong>
                </div>

                <div class="results-detail-item">
                    <span class="results-detail-label">Thời gian nộp bài</span>
                    <strong><%= submittedAt != null ? submittedAt : "—" %></strong>
                </div>

                <div class="results-detail-item">
                    <span class="results-detail-label">Trạng thái bài thi</span>
                    <strong class="<%= isPassed ? "results-text-pass" : "results-text-fail" %>">
                        <%= isPassed ? "Đạt yêu cầu" : "Chưa đạt" %>
                    </strong>
                </div>

                <div class="results-detail-item">
                    <span class="results-detail-label">Điểm chuẩn</span>
                    <strong><%= passScore.stripTrailingZeros().toPlainString() %> / 10 điểm</strong>
                </div>

            </div>
        </div>

        <!-- Actions -->
        <div class="results-actions">

            <a href="${pageContext.request.contextPath}/student/exams"
               class="results-back-btn">
                <span>←</span> Quay lại phòng thi
            </a>

        </div>

    </section>

    <p class="results-footer">
        OnlineExam · Hệ thống thi trực tuyến
    </p>

</div>

</body>
</html>