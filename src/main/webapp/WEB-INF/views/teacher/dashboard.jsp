<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.controller.TeacherDashboardServlet.RecentSessionDTO" %>
<%
    String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy", new java.util.Locale("vi", "VN")));
    
    Integer totalExams = (Integer) request.getAttribute("totalExams");
    Integer publishedExams = (Integer) request.getAttribute("publishedExams");
    Integer totalSessions = (Integer) request.getAttribute("totalSessions");
    Integer activeSessions = (Integer) request.getAttribute("activeSessions");
    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    String avgScore = (String) request.getAttribute("avgScore");
    List<RecentSessionDTO> recentSessions = (List<RecentSessionDTO>) request.getAttribute("recentSessions");

    if (totalExams == null) totalExams = 0;
    if (publishedExams == null) publishedExams = 0;
    if (totalSessions == null) totalSessions = 0;
    if (activeSessions == null) activeSessions = 0;
    if (totalStudents == null) totalStudents = 0;
    if (avgScore == null) avgScore = "0.00";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng Quan Giáo Viên | Hệ Thống Thi Thử Online</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
    <style>
        :root {
            --primary: #1e40af;
            --primary-dark: #1e3a8a;
            --bg-light: #f8fafc;
            --border-color: #e2e8f0;
            --text-dark: #0f172a;
            --text-muted: #64748b;
        }

        body {
            background-color: var(--bg-light);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            color: var(--text-dark);
            margin: 0;
            padding: 0;
        }

        /* Banner chào mừng & giới thiệu hệ thống */
        .welcome-card {
            background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
            color: #ffffff;
            border-radius: 12px;
            padding: 24px 28px;
            margin-bottom: 24px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
        }

        .welcome-badge {
            display: inline-block;
            background: rgba(255, 255, 255, 0.2);
            color: #ffffff;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            margin-bottom: 10px;
        }

        /* Thẻ thống kê */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 18px;
            margin-bottom: 24px;
        }

        .stat-card {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 18px 20px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
        }

        .stat-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--text-muted);
            margin-bottom: 6px;
        }

        .stat-number {
            font-size: 28px;
            font-weight: 700;
            color: var(--text-dark);
            line-height: 1.1;
        }

        .stat-desc {
            font-size: 12px;
            color: #64748b;
            margin-top: 6px;
        }

        /* Bảng dữ liệu */
        .data-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        .data-table th {
            background: #f8fafc;
            color: #475569;
            font-weight: 600;
            text-align: left;
            padding: 12px 16px;
            border-bottom: 1px solid var(--border-color);
            font-size: 13px;
        }

        .data-table td {
            padding: 12px 16px;
            border-bottom: 1px solid #f1f5f9;
        }

        .data-table tbody tr:hover {
            background-color: #f8fafc;
        }

        /* Khối thao tác nhanh */
        .action-card {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 14px 18px;
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            text-decoration: none;
            color: inherit;
            margin-bottom: 12px;
            transition: all 0.2s ease;
        }

        .action-card:hover {
            border-color: #3b82f6;
            background: #f8fafc;
            transform: translateX(3px);
        }

        .action-icon {
            width: 38px;
            height: 38px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            font-weight: 700;
        }

        .badge-status {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-open { background: #dcfce7; color: #15803d; }
        .badge-sched { background: #e0f2fe; color: #0369a1; }
        .badge-closed { background: #f1f5f9; color: #475569; }
    </style>
</head>
<body>
<div class="teacher-layout">

    <!-- Thanh Điều Hướng Bên Trái -->
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard" style="display: flex; align-items: center; gap: 10px; margin-bottom: 28px;">
            <div style="background: #2563eb; color: white; width: 36px; height: 36px; border-radius: 8px; display: grid; place-items: center; font-weight: 800; font-size: 16px;">
                OE
            </div>
            <div>
                <span style="font-size: 17px; font-weight: 800; color: #1e3a8a; display: block;">OnlineExam</span>
                <span style="font-size: 11px; color: #64748b; font-weight: 600;">Luyện Thi THPT QG</span>
            </div>
        </a>

        <div class="sidebar-label" style="font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; margin-bottom: 10px; padding-left: 8px;">
            Quản Lý Giáo Viên
        </div>

        <nav class="sidebar-nav">
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/dashboard">
                <span class="nav-icon">📊</span>
                Tổng Quan
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">
                <span class="nav-icon">📝</span>
                Quản Lý Đề Thi
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">
                <span class="nav-icon">❓</span>
                Ngân Hàng Câu Hỏi
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/classes">
                <span class="nav-icon">👥</span>
                Lớp Học & Học Sinh
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">
                <span class="nav-icon">⏱</span>
                Đợt Thi / Lịch Thi
            </a>
        </nav>

        <div class="sidebar-bottom" style="margin-top: auto; padding-top: 20px; border-top: 1px solid var(--border-color);">
            <div class="teacher-mini" style="display: flex; align-items: center; gap: 10px;">
                <div style="background: #e2e8f0; color: #1e293b; width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center; font-weight: 700; font-size: 12px;">
                    GV
                </div>
                <div>
                    <div style="font-weight: 700; font-size: 13px;">Giáo Viên</div>
                    <div style="font-size: 11px; color: #64748b;">Khu vực giảng dạy</div>
                </div>
            </div>
        </div>
    </aside>

    <!-- Khu Vực Nội Dung Chính -->
    <main class="main-content" style="padding: 28px; flex: 1;">
        
        <header style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div>
                <h1 style="font-size: 22px; font-weight: 800; color: #0f172a; margin: 0;">
                    Bảng Điều Khiển Giáo Viên
                </h1>
                <p style="color: #64748b; font-size: 13px; margin: 4px 0 0 0;">
                    Hệ thống theo dõi các đợt thi thử và kết quả của học sinh
                </p>
            </div>
            <div style="background: #ffffff; padding: 8px 14px; border-radius: 6px; border: 1px solid var(--border-color); font-size: 13px; color: #475569;">
                <%= currentDate %>
            </div>
        </header>

        <!-- Banner Chào Mừng -->
        <section class="welcome-card">
            <span class="welcome-badge">Hệ Thống Thi Thử THPT Quốc Gia</span>
            <h2 style="font-size: 20px; font-weight: 700; margin: 0 0 6px 0;">
                Xin chào Quý Thầy/Cô!
            </h2>
            <p style="margin: 0; color: #e0e7ff; font-size: 13px; line-height: 1.5; max-width: 650px;">
                Thầy/Cô có thể tạo đề thi theo cấu trúc chuẩn, mở các đợt thi thử cho lớp học,
                và hệ thống sẽ tự động chấm điểm trắc nghiệm cũng như thống kê kết quả bài làm của học sinh.
            </p>
        </section>

        <!-- Thẻ Thống Kê Số Liệu Thật Từ Database -->
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-title">Tổng số đề thi</div>
                <div class="stat-number"><%= totalExams %></div>
                <div class="stat-desc">
                    <span style="color: #16a34a; font-weight: 600;"><%= publishedExams %></span> đề đã công khai
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-title">Đợt thi đã tạo</div>
                <div class="stat-number"><%= totalSessions %></div>
                <div class="stat-desc">
                    <span style="color: #2563eb; font-weight: 600;"><%= activeSessions %></span> đợt đang mở
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-title">Học sinh đăng ký</div>
                <div class="stat-number"><%= totalStudents %></div>
                <div class="stat-desc">Tài khoản học sinh trong hệ thống</div>
            </div>

            <div class="stat-card">
                <div class="stat-title">Điểm trung bình</div>
                <div class="stat-number"><%= avgScore %></div>
                <div class="stat-desc">Tính trên các bài đã nộp (thang 10)</div>
            </div>
        </section>

        <!-- Bố Cục 2 Cột: Đợt Thi Gần Đây & Thao Tác Nhanh -->
        <div style="display: grid; grid-template-columns: 1fr 340px; gap: 20px;">

            <!-- Cột Trái: Bảng Các Đợt Thi Gần Nhất (Từ DB) -->
            <div style="background: #ffffff; border: 1px solid var(--border-color); border-radius: 10px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.03);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div>
                        <h3 style="margin: 0; font-size: 15px; font-weight: 700;">Các Đợt Thi Gần Đây</h3>
                        <p style="margin: 2px 0 0 0; font-size: 12px; color: #64748b;">Dữ liệu từ bảng Exam_Sessions</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/teacher/exam-sessions" style="font-size: 13px; font-weight: 600; color: #2563eb;">
                        Xem tất cả →
                    </a>
                </div>

                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Tên Đợt Thi / Đề Áp Dụng</th>
                            <th>Thời Gian</th>
                            <th>Lớp / Nhóm</th>
                            <th>Trạng Thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (recentSessions != null && !recentSessions.isEmpty()) {
                                for (RecentSessionDTO s : recentSessions) {
                        %>
                        <tr>
                            <td>
                                <strong><%= s.sessionName %></strong>
                                <div style="font-size: 12px; color: #64748b; margin-top: 2px;">
                                    Đề: <%= s.examTitle %> (<%= s.durationMinutes %> phút)
                                </div>
                            </td>
                            <td><%= s.timeRange %></td>
                            <td><%= s.targetName %></td>
                            <td>
                                <% if ("OPEN".equalsIgnoreCase(s.status)) { %>
                                    <span class="badge-status badge-open">Đang mở</span>
                                <% } else if ("SCHEDULED".equalsIgnoreCase(s.status)) { %>
                                    <span class="badge-status badge-sched">Đã lên lịch</span>
                                <% } else { %>
                                    <span class="badge-status badge-closed"><%= s.status %></span>
                                <% } %>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="4" style="text-align: center; color: #94a3b8; padding: 28px;">
                                Chưa có đợt thi nào được tạo trong cơ sở dữ liệu.
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- Cột Phải: Thao Tác Nhanh Thường Dùng -->
            <div style="background: #ffffff; border: 1px solid var(--border-color); border-radius: 10px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.03);">
                <h3 style="margin: 0; font-size: 15px; font-weight: 700;">Thao Tác Nhanh</h3>
                <p style="margin: 2px 0 16px 0; font-size: 12px; color: #64748b;">Các chức năng quản lý thường dùng</p>

                <!-- 1. Tạo Đề Thi -->
                <a class="action-card" href="${pageContext.request.contextPath}/teacher/exams">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="action-icon" style="background: #eff6ff; color: #2563eb;">＋</div>
                        <div>
                            <div style="font-weight: 600; font-size: 14px;">Tạo Đề Thi Mới</div>
                            <div style="font-size: 12px; color: #64748b;">Nhập đề thi và thời gian làm bài</div>
                        </div>
                    </div>
                    <span style="color: #94a3b8; font-size: 16px;">→</span>
                </a>

                <!-- 2. Quản Lý Lớp Học -->
                <a class="action-card" href="${pageContext.request.contextPath}/teacher/classes">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="action-icon" style="background: #ecfdf5; color: #16a34a;">👥</div>
                        <div>
                            <div style="font-weight: 600; font-size: 14px;">Quản Lý Lớp Học</div>
                            <div style="font-size: 12px; color: #64748b;">Danh sách lớp và học sinh</div>
                        </div>
                    </div>
                    <span style="color: #94a3b8; font-size: 16px;">→</span>
                </a>

                <!-- 3. Lên Lịch Thi -->
                <a class="action-card" href="${pageContext.request.contextPath}/teacher/exam-sessions">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="action-icon" style="background: #fffbeb; color: #d97706;">⏱</div>
                        <div>
                            <div style="font-weight: 600; font-size: 14px;">Lên Lịch Thi Thử</div>
                            <div style="font-size: 12px; color: #64748b;">Mở đợt thi cho lớp làm bài</div>
                        </div>
                    </div>
                    <span style="color: #94a3b8; font-size: 16px;">→</span>
                </a>
            </div>

        </div>

        <footer style="margin-top: 32px; padding-top: 16px; border-top: 1px solid var(--border-color); text-align: center; font-size: 12px; color: #94a3b8;">
            Hệ Thống Thi Thử Trực Tuyến THPT Quốc Gia &copy; OnlineExam
        </footer>
    </main>
</div>
</body>
</html>