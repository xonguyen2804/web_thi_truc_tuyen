
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan giáo viên | OnlineExam</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css">
</head>
<body>
<div class="teacher-layout">

    <!-- Thanh điều hướng -->
    <aside class="sidebar">
        <a class="brand" href="#">
            <span class="brand-icon">OE</span>
            <span>OnlineExam</span>
        </a>

        <div class="sidebar-label">GIẢNG DẠY</div>

        <nav class="sidebar-nav">
            <a class="nav-item active" href="#">
                <span class="nav-icon">▦</span>
                Tổng quan
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">
                <span class="nav-icon">▤</span>
                Quản lý đề thi
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/questions">
                <span class="nav-icon">☷</span>
                Ngân hàng câu hỏi
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/classes">
                <span class="nav-icon">♙</span>
                Lớp học
            </a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">
                <span class="nav-icon">▦</span>
                Lịch thi
            </a>
        </nav>

        <div class="sidebar-bottom">
            <div class="teacher-mini">
                <div class="avatar">GV</div>
                <div>
                    <strong>Giáo viên</strong>
                    <span>Khu vực giảng dạy</span>
                </div>
            </div>
        </div>
    </aside>

    <!-- Nội dung chính -->
    <main class="main-content">
        <header class="topbar">
            <div>
                <div class="breadcrumb">OnlineExam / Giáo viên</div>
                <h1>Tổng quan</h1>
            </div>
            <div class="topbar-right">
                <span class="status-dot"></span>
                <span>Không gian giảng dạy</span>
            </div>
        </header>

        <section class="welcome-panel">
            <div>
                <p class="eyebrow">TRUNG TÂM ĐIỀU KHIỂN</p>
                <h2>Xin chào, Giáo viên!</h2>
                <p>
                    Quản lý đề thi, câu hỏi, lớp học và các kỳ thi
                    của bạn tại một nơi.
                </p>
            </div>
            <div class="welcome-icon">🎓</div>
        </section>

        <!-- Thẻ thống kê minh họa -->
        <section class="stats-grid">
            <article class="stat-card">
                <div class="stat-top">
                    <span>Tổng số đề thi</span>
                    <span class="stat-icon blue">▤</span>
                </div>
                <div class="stat-value">12</div>
                <div class="stat-note">Đề thi mẫu</div>
            </article>

            <article class="stat-card">
                <div class="stat-top">
                    <span>Câu hỏi</span>
                    <span class="stat-icon purple">☷</span>
                </div>
                <div class="stat-value">86</div>
                <div class="stat-note">Trong ngân hàng câu hỏi</div>
            </article>

            <article class="stat-card">
                <div class="stat-top">
                    <span>Lớp học</span>
                    <span class="stat-icon green">♙</span>
                </div>
                <div class="stat-value">4</div>
                <div class="stat-note">Lớp học mẫu</div>
            </article>

            <article class="stat-card">
                <div class="stat-top">
                    <span>Kỳ thi</span>
                    <span class="stat-icon orange">◷</span>
                </div>
                <div class="stat-value">3</div>
                <div class="stat-note">Lịch thi minh họa</div>
            </article>
        </section>

        <section class="content-grid">
            <!-- Danh sách đề thi -->
            <article class="panel exam-panel">
                <div class="panel-heading">
                    <div>
                        <h3>Đề thi gần đây</h3>
                        <p>Theo dõi các đề thi đã tạo</p>
                    </div>
                    <a class="text-link"
                       href="${pageContext.request.contextPath}/teacher/exams">
                        Xem tất cả →
                    </a>
                </div>

                <div class="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>Tên đề thi</th>
                            <th>Số câu</th>
                            <th>Thời gian</th>
                            <th>Trạng thái</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>
                                <strong>Kiểm tra Java cơ bản</strong>
                                <span class="cell-subtitle">Mã đề: DEMO01</span>
                            </td>
                            <td>20 câu</td>
                            <td>30 phút</td>
                            <td><span class="badge badge-green">Đã xuất bản</span></td>
                        </tr>
                        <tr>
                            <td>
                                <strong>Cơ sở dữ liệu</strong>
                                <span class="cell-subtitle">Mã đề: DEMO02</span>
                            </td>
                            <td>30 câu</td>
                            <td>45 phút</td>
                            <td><span class="badge badge-blue">Bản nháp</span></td>
                        </tr>
                        <tr>
                            <td>
                                <strong>Mạng máy tính</strong>
                                <span class="cell-subtitle">Mã đề: DEMO03</span>
                            </td>
                            <td>25 câu</td>
                            <td>40 phút</td>
                            <td><span class="badge badge-green">Đã xuất bản</span></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </article>

            <!-- Thao tác nhanh -->
            <article class="panel quick-panel">
                <div class="panel-heading">
                    <div>
                        <h3>Thao tác nhanh</h3>
                        <p>Truy cập các chức năng thường dùng</p>
                    </div>
                </div>

                <a class="quick-action"
                   href="${pageContext.request.contextPath}/teacher/exams">
                    <span class="quick-icon blue">＋</span>
                    <span>
                        <strong>Tạo đề thi</strong>
                        <small>Tạo và tổ chức nội dung đề</small>
                    </span>
                    <span class="quick-arrow">→</span>
                </a>

                <a class="quick-action"
                   href="${pageContext.request.contextPath}/teacher/questions">
                    <span class="quick-icon purple">☷</span>
                    <span>
                        <strong>Thêm câu hỏi</strong>
                        <small>Bổ sung vào ngân hàng câu hỏi</small>
                    </span>
                    <span class="quick-arrow">→</span>
                </a>

                <a class="quick-action"
                   href="${pageContext.request.contextPath}/teacher/classes">
                    <span class="quick-icon green">♙</span>
                    <span>
                        <strong>Quản lý lớp</strong>
                        <small>Xem lớp và danh sách thành viên</small>
                    </span>
                    <span class="quick-arrow">→</span>
                </a>

                <a class="quick-action"
                   href="${pageContext.request.contextPath}/teacher/exam-sessions">
                    <span class="quick-icon orange">◷</span>
                    <span>
                        <strong>Lên lịch thi</strong>
                        <small>Thiết lập đợt thi cho lớp</small>
                    </span>
                    <span class="quick-arrow">→</span>
                </a>
            </article>
        </section>

        <footer class="page-footer">
            OnlineExam · Khu vực quản lý dành cho giáo viên
        </footer>
    </main>
</div>
</body>
</html>
