<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.ResultDAO.AnswerResultDTO" %>
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
    <title>Káº¿t Quáº£ BÃ i Thi | <%= examTitle %></title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Káº¿t Quáº£ BÃ i Thi | <%= examTitle %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css">
</head>
<body>

<div class="result-card">
    <div style="font-size: 13px; color: #64748b; text-transform: uppercase; font-weight: 700; letter-spacing: 0.5px;">Káº¿t Quáº£ BÃ i LÃ m</div>
    <h1 style="font-size: 22px; margin: 8px 0; color: #0f172a;"><%= examTitle %></h1>
    <div style="color: #475569; font-size: 14px;">Ca thi: <strong><%= sessionName %></strong> &bull; ThÃ­ sinh: <strong><%= studentName %></strong></div>

    <!-- VÃ²ng trÃ²n Ä‘iá»ƒm sá»‘ -->
    <div class="score-circle <%= isPassed ? "score-pass" : "score-fail" %>">
        <div><%= score %></div>
        <div style="font-size: 12px; font-weight: 600; text-transform: uppercase;"><%= isPassed ? "Äáº T" : "CHÆ¯A Äáº T" %></div>
    </div>

    <!-- ThÃ´ng tin chi tiáº¿t -->
    <div class="info-grid">
        <div>Sá»‘ cÃ¢u tráº£ lá»i Ä‘Ãºng: <strong><%= correctCount %> / <%= totalQuestions %> cÃ¢u</strong></div>
        <div>Äiá»ƒm chuáº©n Ä‘áº¡t: <strong><%= passScore %> / 10 Ä‘iá»ƒm</strong></div>
        <div>Thá»i gian báº¯t Ä‘áº§u: <strong><%= startedAt != null ? startedAt : "â€”" %></strong></div>
        <div>Thá»i gian ná»™p bÃ i: <strong><%= submittedAt != null ? submittedAt : "â€”" %></strong></div>
    </div>

    <div style="margin-top: 28px;">
        <a href="${pageContext.request.contextPath}/student/exams" class="btn btn-primary">
            &larr; Trá»Ÿ Vá» Danh SÃ¡ch PhÃ²ng Thi
        </a>
    </div>
</div>

</body>
</html>