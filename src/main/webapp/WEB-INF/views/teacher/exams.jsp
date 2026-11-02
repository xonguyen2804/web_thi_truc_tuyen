
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.model.Exam" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đề thi | OnlineExam</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css?v=3">
</head>
<body>

<div class="teacher-layout">

    <!-- SIDEBAR -->
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard">
            <div class="brand-logo">OE</div>
            <div class="brand-info">
                <span class="brand-name">OnlineExam</span>
                <span class="brand-subtitle">Cổng thông tin giáo viên</span>
            </div>
        </a>

        <div class="sidebar-label">QUẢN LÝ GIẢNG DẠY</div>

        <nav class="sidebar-nav">
            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/dashboard">
                <span>📊</span> Tổng quan
            </a>

            <a class="nav-item active"
               href="${pageContext.request.contextPath}/teacher/exams">
                <span>📝</span> Quản lý đề thi
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/questions">
                <span>❓</span> Ngân hàng câu hỏi
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/classes">
                <span>👥</span> Lớp học và nhóm
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/exam-sessions">
                <span>⏱️</span> Lịch thi
            </a>
        </nav>

        <div class="sidebar-bottom">
            <div class="teacher-mini">
                <div class="teacher-avatar">GV</div>
                <div>
                    <div class="teacher-mini-name">Giáo viên</div>
                    <div class="teacher-mini-role">Khu vực giảng dạy</div>
                </div>
            </div>
        </div>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="main-content">

        <div class="page-eyebrow">QUẢN LÝ BÀI KIỂM TRA</div>

        <div class="exams-header">
            <div>
                <h1>Quản lý đề thi</h1>
                <p>
                    Tạo đề thi, nhập câu hỏi từ tệp văn bản và quản lý
                    các đề kiểm tra trong hệ thống.
                </p>
            </div>
        </div>

        <%
            List<Exam> exams = (List<Exam>) request.getAttribute("exams");
            int totalExams = exams == null ? 0 : exams.size();
            int publishedCount = 0;
            int draftCount = 0;

            if (exams != null) {
                for (Exam exam : exams) {
                    if ("PUBLISHED".equalsIgnoreCase(exam.getStatus())) {
                        publishedCount++;
                    } else {
                        draftCount++;
                    }
                }
            }
        %>

        <!-- THÔNG BÁO -->
        <% if (request.getParameter("success") != null) { %>
            <div class="exam-alert exam-alert-success">
                <span class="exam-alert-icon">✓</span>
                <div>
                    <strong>Thao tác thành công</strong>
                    <p>Đề thi đã được tạo và xử lý câu hỏi thành công.</p>
                </div>
            </div>
        <% } %>

        <!-- THỐNG KÊ -->
        <section class="exams-overview">

            <div class="exams-stat-card">
                <div class="exams-stat-icon stat-blue">📝</div>
                <div>
                    <div class="exams-stat-label">Tổng số đề thi</div>
                    <div class="exams-stat-value"><%= totalExams %></div>
                    <div class="exams-stat-note">Đề thi trong hệ thống</div>
                </div>
            </div>

            <div class="exams-stat-card">
                <div class="exams-stat-icon stat-green">🌐</div>
                <div>
                    <div class="exams-stat-label">Đề đã công khai</div>
                    <div class="exams-stat-value"><%= publishedCount %></div>
                    <div class="exams-stat-note">Trạng thái PUBLISHED</div>
                </div>
            </div>

            <div class="exams-stat-card">
                <div class="exams-stat-icon stat-orange">📄</div>
                <div>
                    <div class="exams-stat-label">Đề đang lưu nháp</div>
                    <div class="exams-stat-value"><%= draftCount %></div>
                    <div class="exams-stat-note">Trạng thái DRAFT</div>
                </div>
            </div>

        </section>

        <!-- NỘI DUNG CHÍNH -->
        <section class="exams-content-grid">

            <!-- FORM TẠO ĐỀ -->
            <div class="exam-create-card">

                <div class="exam-section-heading">
                    <div class="exam-section-icon">✏️</div>
                    <div>
                        <h2>Tạo đề thi mới</h2>
                        <p>Nhập thông tin và tải lên tệp câu hỏi</p>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/teacher/exams"
                      method="POST"
                      enctype="multipart/form-data">

                    <div class="exam-form-group">
                        <label for="examTitle">
                            Tên đề thi <span class="exam-required">*</span>
                        </label>
                        <input
                            type="text"
                            id="examTitle"
                            name="examTitle"
                            class="exam-input"
                            required
                            maxlength="255"
                            placeholder="Ví dụ: Đề khảo sát Toán THPT - Lần 1">
                    </div>

                    <div class="exam-form-row">
                        <div class="exam-form-group">
                            <label for="durationMinutes">
                                Thời gian làm bài
                                <span class="exam-required">*</span>
                            </label>
                            <div class="exam-input-with-unit">
                                <input
                                    type="number"
                                    id="durationMinutes"
                                    name="durationMinutes"
                                    class="exam-input"
                                    value="50"
                                    min="1"
                                    required>
                                <span>phút</span>
                            </div>
                        </div>

                        <div class="exam-form-group">
                            <label for="passScore">Điểm đạt</label>
                            <div class="exam-input-with-unit">
                                <input
                                    type="number"
                                    id="passScore"
                                    name="passScore"
                                    class="exam-input"
                                    value="5.0"
                                    min="0"
                                    max="10"
                                    step="0.25"
                                    required>
                                <span>/ 10</span>
                            </div>
                        </div>
                    </div>

                    <div class="exam-form-group">
                        <label for="examFile">Tệp câu hỏi</label>

                        <div class="exam-upload-area">
                            <div class="exam-upload-icon">📁</div>
                            <div class="exam-upload-title">
                                Chọn tệp đề thi định dạng .txt
                            </div>
                            <div class="exam-upload-desc">
                                Dung lượng tối đa 5MB
                            </div>

                            <input
                                type="file"
                                id="examFile"
                                name="examFile"
                                accept=".txt"
                                class="exam-file-input">
                        </div>

                        <div class="exam-format-guide">
                            <strong>Định dạng tệp:</strong>
                            <p>
                                Câu 1: Nội dung câu hỏi<br>
                                A. Phương án A<br>
                                B. Phương án B*<br>
                                C. Phương án C<br>
                                D. Phương án D
                            </p>
                            <span>Dấu * được dùng để đánh dấu đáp án đúng theo định dạng import hiện tại.</span>
                        </div>
                    </div>

                    <div class="exam-form-group">
                        <label for="status">Trạng thái đề thi</label>
                        <select id="status" name="status" class="exam-input">
                            <option value="PUBLISHED">Công khai</option>
                            <option value="DRAFT">Lưu bản nháp</option>
                        </select>
                    </div>

                    <div class="exam-form-actions">
                        <button type="submit" class="exam-submit-btn">
                            <span>＋</span> Tạo và import đề thi
                        </button>
                    </div>

                </form>
            </div>

            <!-- DANH SÁCH ĐỀ THI -->
            <div class="exam-list-card">

                <div class="exam-list-heading">
                    <div>
                        <h2>Danh sách đề thi</h2>
                        <p>Các đề thi hiện có trong hệ thống</p>
                    </div>

                    <span class="exam-total-badge">
                        <%= totalExams %> đề thi
                    </span>
                </div>

                <% if (exams != null && !exams.isEmpty()) { %>

                    <div class="exam-table-wrapper">
                        <table class="modern-exam-table">
                            <thead>
                                <tr>
                                    <th>Mã đề</th>
                                    <th>Tên đề thi</th>
                                    <th>Thời lượng</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>

                            <tbody>
                                <% for (Exam e : exams) { %>
                                    <tr>
                                        <td>
                                            <span class="exam-id">
                                                #<%= e.getExamId() %>
                                            </span>
                                        </td>

                                        <td>
                                            <div class="exam-name-cell">
                                                <div class="exam-row-icon">📝</div>
                                                <strong><%= e.getExamTitle() %></strong>
                                            </div>
                                        </td>

                                        <td>
                                            <span class="exam-duration">
                                                <%= e.getDurationMinutes() %> phút
                                            </span>
                                        </td>

                                        <td>
                                            <% if ("PUBLISHED".equalsIgnoreCase(e.getStatus())) { %>
                                                <span class="exam-status status-published">
                                                    <span></span> Đã công khai
                                                </span>
                                            <% } else { %>
                                                <span class="exam-status status-draft">
                                                    <span></span> Bản nháp
                                                </span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>

                <% } else { %>

                    <div class="exam-empty-state">
                        <div class="exam-empty-icon">📋</div>
                        <h3>Chưa có đề thi nào</h3>
                        <p>
                            Hãy tạo đề thi đầu tiên bằng biểu mẫu bên trái.
                        </p>
                    </div>

                <% } %>

            </div>
        </section>

        <footer class="teacher-page-footer">
            OnlineExam · Hệ thống quản lý thi trực tuyến
        </footer>

    </main>
</div>

</body>
</html>
