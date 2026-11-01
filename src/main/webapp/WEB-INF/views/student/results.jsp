<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.controller.ResultServlet.AnswerResultDTO" %>
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

    if (score == null) score = BigDecimal.ZERO;
    if (passScore == null) passScore = new BigDecimal("5.0");
    boolean isPassed = score.compareTo(passScore) >= 0;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết Quả Bài Thi | <%= examTitle %></title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; background: #f8fafc; margin: 0; padding: 32px 16px; color: #1e293b; }
        .result-card { max-width: 650px; margin: 0 auto; background: #ffffff; border-radius: 16px; padding: 36px 32px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; text-align: center; }
        .score-circle { width: 130px; height: 130px; border-radius: 50%; margin: 20px auto; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 38px; font-weight: 800; }
        .score-pass { background: #dcfce7; color: #15803d; border: 4px solid #86efac; }
        .score-fail { background: #fee2e2; color: #b91c1c; border: 4px solid #fca5a5; }
        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 24px 0; text-align: left; background: #f8fafc; padding: 18px; border-radius: 10px; font-size: 14px; }
        .btn { display: inline-block; padding: 12px 24px; border-radius: 8px; font-weight: 700; text-decoration: none; font-size: 14px; }
        .btn-primary { background: #2563eb; color: #fff; }
        .btn-primary:hover { background: #1d4ed8; }
    </style>
</head>
<body>

<div class="result-card">
    <div style="font-size: 13px; color: #64748b; text-transform: uppercase; font-weight: 700; letter-spacing: 0.5px;">Kết Quả Bài Làm</div>
    <h1 style="font-size: 22px; margin: 8px 0; color: #0f172a;"><%= examTitle %></h1>
    <div style="color: #475569; font-size: 14px;">Ca thi: <strong><%= sessionName %></strong> &bull; Thí sinh: <strong><%= studentName %></strong></div>

    <!-- Vòng tròn điểm số -->
    <div class="score-circle <%= isPassed ? "score-pass" : "score-fail" %>">
        <div><%= score %></div>
        <div style="font-size: 12px; font-weight: 600; text-transform: uppercase;"><%= isPassed ? "ĐẠT" : "CHƯA ĐẠT" %></div>
    </div>

    <!-- Thông tin chi tiết -->
    <div class="info-grid">
        <div>Số câu trả lời đúng: <strong><%= correctCount %> / <%= totalQuestions %> câu</strong></div>
        <div>Điểm chuẩn đạt: <strong><%= passScore %> / 10 điểm</strong></div>
        <div>Thời gian bắt đầu: <strong><%= startedAt != null ? startedAt : "—" %></strong></div>
        <div>Thời gian nộp bài: <strong><%= submittedAt != null ? submittedAt : "—" %></strong></div>
    </div>

    <div style="margin-top: 28px;">
        <a href="${pageContext.request.contextPath}/student/exams" class="btn btn-primary">
            &larr; Trở Về Danh Sách Phòng Thi
        </a>
    </div>
</div>

</body>
</html>