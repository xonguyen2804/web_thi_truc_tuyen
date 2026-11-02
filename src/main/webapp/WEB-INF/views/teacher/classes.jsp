<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.ClassDAO.ClassDTO" %>
<%@ page import="com.onlineexam.dao.ClassDAO.GroupDTO" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quáº£n LÃ½ Khá»‘i Lá»›p & NhÃ³m Há»c | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
</head>
<body>
<div class="teacher-layout">
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard" style="display: flex; align-items: center; gap: 10px; margin-bottom: 28px;">
            <div style="background: #2563eb; color: white; width: 36px; height: 36px; border-radius: 8px; display: grid; place-items: center; font-weight: 800;">OE</div>
            <div>
                <span style="font-size: 16px; font-weight: 800; color: #1e3a8a; display: block;">OnlineExam</span>
                <span style="font-size: 11px; color: #64748b;">Luyá»‡n Thi THPT QG</span>
            </div>
        </a>
        <nav class="sidebar-nav">
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/dashboard">ðŸ“Š Tá»•ng Quan</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">ðŸ“ Quáº£n LÃ½ Äá» Thi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">â“ NgÃ¢n HÃ ng CÃ¢u Há»i</a>
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/classes">ðŸ‘¥ Lá»›p Há»c & NhÃ³m</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">â± Lá»‹ch Thi</a>
        </nav>
    </aside>

    <main class="main-content" style="padding: 28px; flex: 1;">
        <h1 style="font-size: 22px; margin: 0 0 4px 0;">Khá»‘i Lá»›p & NhÃ³m Luyá»‡n Thi</h1>
        <p style="color: #64748b; font-size: 13px; margin: 0 0 20px 0;">Thiáº¿t láº­p cÃ¡c Khá»‘i 10, 11, 12 vÃ  cÃ¡c NhÃ³m há»c táº­p Ä‘á»ƒ phÃ¢n quyá»n Ä‘á»£t thi</p>

        <div class="classes-grid">
            <!-- Cá»™t TrÃ¡i: Táº¡o Khá»‘i & NhÃ³m -->
            <div>
                <!-- Form 1: Táº¡o Khá»‘i Lá»›p -->
                <div class="card">
                    <h3 style="margin-top: 0; font-size: 15px; margin-bottom: 12px;">+ Táº¡o Khá»‘i Lá»›p Má»›i</h3>
                    <form action="${pageContext.request.contextPath}/teacher/classes" method="POST">
                        <input type="hidden" name="action" value="createClass">
                        <div class="form-group">
                            <label>TÃªn Khá»‘i Lá»›p</label>
                            <input type="text" name="className" class="form-control" placeholder="VD: Khá»‘i 12" required>
                        </div>
                        <button type="submit" class="btn-action">+ ThÃªm Khá»‘i</button>
                    </form>
                </div>

                <!-- Form 2: Táº¡o NhÃ³m con trong Khá»‘i -->
                <div class="card">
                    <h3 style="margin-top: 0; font-size: 15px; margin-bottom: 12px;">+ Táº¡o NhÃ³m Há»c Trong Lá»›p</h3>
                    <form action="${pageContext.request.contextPath}/teacher/classes" method="POST">
                        <input type="hidden" name="action" value="createGroup">
                        <div class="form-group">
                            <label>Chá»n Khá»‘i Trá»±c Thuá»™c</label>
                            <select name="classId" class="form-control" required>
                                <%
                                    List<ClassDTO> classList = (List<ClassDTO>) request.getAttribute("classList");
                                    if (classList != null) {
                                        for (ClassDTO c : classList) {
                                %>
                                    <option value="<%= c.classId %>"><%= c.className %></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>TÃªn NhÃ³m</label>
                            <input type="text" name="groupName" class="form-control" placeholder="VD: 12VL_1, 12T_1" required>
                        </div>
                        <div class="form-group">
                            <label>MÃ´ táº£ nhÃ³m</label>
                            <input type="text" name="description" class="form-control" placeholder="NhÃ³m chuyÃªn LÃ½, ToÃ¡n...">
                        </div>
                        <button type="submit" class="btn-action" style="background: #10b981;">+ ThÃªm NhÃ³m</button>
                    </form>
                </div>
            </div>

            <!-- Cá»™t Pháº£i: Danh SÃ¡ch Khá»‘i & NhÃ³m Hiá»‡n CÃ³ -->
            <div class="card">
                <h3 style="margin-top: 0; font-size: 16px; margin-bottom: 16px;">Cáº¥u TrÃºc Lá»›p & NhÃ³m Äang CÃ³</h3>
                <%
                    if (classList != null && !classList.isEmpty()) {
                        for (ClassDTO c : classList) {
                %>
                <div class="class-box">
                    <h4 style="margin: 0 0 10px 0; font-size: 16px; color: #1e3a8a;"><%= c.className %></h4>
                    <div style="font-size: 13px; color: #475569; margin-bottom: 8px;">CÃ¡c nhÃ³m Ä‘Ã£ táº¡o:</div>
                    <div>
                        <%
                            if (!c.groups.isEmpty()) {
                                for (GroupDTO g : c.groups) {
                        %>
                            <span class="group-tag">ðŸ· <%= g.groupName %></span>
                        <%
                                }
                            } else {
                        %>
                            <span style="font-size: 12px; color: #94a3b8; font-style: italic;">ChÆ°a cÃ³ nhÃ³m nÃ o trong khá»‘i nÃ y.</span>
                        <% } %>
                    </div>
                </div>
                <%
                        }
                    } else {
                %>
                <div style="text-align: center; color: #94a3b8; padding: 32px;">ChÆ°a cÃ³ khá»‘i lá»›p nÃ o. HÃ£y táº¡o Khá»‘i 12, 11, 10 á»Ÿ cá»™t bÃªn trÃ¡i!</div>
                <% } %>
            </div>
        </div>
    </main>
</div>
</body>
</html>