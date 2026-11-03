<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.onlineexam.dao.ClassDAO.ClassDTO" %>
<%@ page import="com.onlineexam.dao.ClassDAO.GroupDTO" %>

<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    List<ClassDTO> classList =
        (List<ClassDTO>) request.getAttribute("classList");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý lớp học và nhóm | OnlineExam</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/teacher.css?v=14">
</head>

<body>
<div class="teacher-layout">

    <!-- Thanh điều hướng -->
    <aside class="sidebar">

        <a class="brand"
           href="${pageContext.request.contextPath}/teacher/dashboard">

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
                <span class="nav-icon">📊</span>
                Tổng quan
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/exams">
                <span class="nav-icon">📝</span>
                Quản lý đề thi
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/questions">
                <span class="nav-icon">❓</span>
                Ngân hàng câu hỏi
            </a>

            <a class="nav-item active"
               href="${pageContext.request.contextPath}/teacher/classes">
                <span class="nav-icon">👥</span>
                Lớp học và nhóm
            </a>

            <a class="nav-item"
               href="${pageContext.request.contextPath}/teacher/exam-sessions">
                <span class="nav-icon">⏱</span>
                Lịch thi
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

    <!-- Nội dung chính -->
    <main class="main-content classes-page">

        <!-- Tiêu đề -->
        <header class="page-header classes-header">
            <div>
                <div class="page-eyebrow">QUẢN LÝ ĐỐI TƯỢNG HỌC TẬP</div>

                <h1 class="page-title">Quản lý lớp học và nhóm</h1>

                <p class="page-desc">
                    Tổ chức các khối lớp và nhóm học tập để thuận tiện
                    trong việc phân công và quản lý các đợt thi.
                </p>
            </div>
            <div class="classes-header-icon">🏫</div>
        </header>

        <!-- Thống kê -->
        <section class="stats-grid classes-overview">

            <div class="stat-card">
                <div class="stat-title">TỔNG SỐ KHỐI LỚP</div>
                <div class="stat-number">
                    <%= classList != null ? classList.size() : 0 %>
                </div>
                <div class="stat-desc">Các khối lớp đang được quản lý</div>
            </div>

            <div class="stat-card">
                <div class="stat-title">TỔNG SỐ NHÓM HỌC</div>
                <div class="stat-number">
                    <%
                        int totalGroups = 0;

                        if (classList != null) {
                            for (ClassDTO c : classList) {
                                if (c.groups != null) {
                                    totalGroups += c.groups.size();
                                }
                            }
                        }
                    %>
                    <%= totalGroups %>
                </div>
                <div class="stat-desc">Tổng số nhóm thuộc các khối lớp</div>
            </div>

            <div class="stat-card">
                <div class="stat-title">MÔ HÌNH TỔ CHỨC</div>
                <div class="stat-number" style="font-size: 23px;">
                    Khối lớp → Nhóm học
                </div>
                <div class="stat-desc">Cấu trúc phân cấp học tập</div>
            </div>

        </section>

        <!-- Nội dung quản lý -->
        <section class="classes-grid classes-content-grid">

            <!-- Cột tạo mới -->
            <div class="classes-forms-column">

                <!-- Tạo khối lớp -->
                <div class="card classes-form-card">

                    <div class="classes-card-heading">
                        <div class="classes-card-icon icon-blue">🏫</div>
                        <div>
                            <h3>Tạo khối lớp mới</h3>
                            <p>Thêm một khối lớp vào hệ thống</p>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/teacher/classes"
                          method="POST">

                        <input type="hidden"
                               name="action"
                               value="createClass">

                        <div class="form-group">
                            <label for="className">
                                Tên khối lớp
                                <span class="required-mark">*</span>
                            </label>

                            <input type="text"
                                   id="className"
                                   name="className"
                                   class="form-control"
                                   placeholder="Ví dụ: Khối 10, Khối 11, Khối 12"
                                   maxlength="100"
                                   required>
                        </div>

                        <button type="submit"
                                class="btn btn-primary btn-block">
                            <span>＋</span> Thêm khối lớp
                        </button>

                    </form>
                </div>

                <!-- Tạo nhóm học -->
                <div class="card classes-form-card">

                    <div class="classes-card-heading">
                        <div class="classes-card-icon icon-green">👥</div>
                        <div>
                            <h3>Tạo nhóm học mới</h3>
                            <p>Tạo nhóm trực thuộc một khối lớp</p>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/teacher/classes"
                          method="POST">

                        <input type="hidden"
                               name="action"
                               value="createGroup">

                        <div class="form-group">
                            <label for="classId">
                                Khối lớp trực thuộc
                                <span class="required-mark">*</span>
                            </label>

                            <select name="classId"
                                    id="classId"
                                    class="form-control"
                                    required>

                                <option value="">-- Chọn khối lớp --</option>

                                <%
                                    if (classList != null) {
                                        for (ClassDTO c : classList) {
                                %>
                                    <option value="<%= c.classId %>">
                                        <%= c.className %>
                                    </option>
                                <%
                                        }
                                    }
                                %>

                            </select>
                        </div>

                        <div class="form-group">
                            <label for="groupName">
                                Tên nhóm
                                <span class="required-mark">*</span>
                            </label>

                            <input type="text"
                                   id="groupName"
                                   name="groupName"
                                   class="form-control"
                                   placeholder="Ví dụ: 12A1, 12A2, 12VL1"
                                   maxlength="100"
                                   required>
                        </div>

                        <div class="form-group">
                            <label for="description">Mô tả nhóm</label>

                            <textarea id="description"
                                      name="description"
                                      class="form-control classes-textarea"
                                      rows="3"
                                      placeholder="Mô tả môn học hoặc mục đích của nhóm..."></textarea>
                        </div>

                        <button type="submit"
                                class="btn btn-success btn-block">
                            <span>＋</span> Thêm nhóm học
                        </button>

                    </form>
                </div>

            </div>

            <!-- Danh sách khối lớp và nhóm -->
            <div class="card classes-list-card">

                <div class="classes-list-header">
                    <div>
                        <h3>Cấu trúc lớp học</h3>
                        <p>Danh sách các khối lớp và nhóm học hiện có</p>
                    </div>

                    <span class="classes-count">
                        <%= classList != null ? classList.size() : 0 %> khối lớp
                    </span>
                </div>

                <div class="classes-list-body">

                    <%
                        if (classList != null && !classList.isEmpty()) {
                            for (ClassDTO c : classList) {
                    %>

                    <div class="class-box classes-item">

                        <div class="classes-item-header">

                            <div class="classes-item-title">
                                <div class="class-item-icon">🏫</div>

                                <div>
                                    <h4><%= c.className %></h4>
                                    <span>Khối lớp</span>
                                </div>
                            </div>

                            <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap">

                                <span class="group-count">
                                    <%= c.groups != null ? c.groups.size() : 0 %> nhóm
                                </span>

                                <a href="${pageContext.request.contextPath}/teacher/class-members?classId=<%= c.classId %>"
                                class="btn btn-primary"
                                style="text-decoration:none; font-size:13px; padding:8px 12px">
                                    👥 Quản lý thành viên
                                </a>
                                <br>
                                <a href="${pageContext.request.contextPath}/teacher/groups?classId=<%= c.classId %>"
                                    class="btn btn-success"
                                    style="text-decoration:none; font-size:13px; padding:8px 12px">
                                        👥 Quản lý nhóm
                                </a>

                            </div>

                        </div>

                        <div class="classes-groups-heading">
                            Các nhóm trực thuộc
                        </div>

                        <div class="classes-group-list">

                            <%
                                if (c.groups != null && !c.groups.isEmpty()) {
                                    for (GroupDTO g : c.groups) {
                            %>

                                <a href="${pageContext.request.contextPath}/teacher/groups?classId=<%= c.classId %>"
                                    class="group-tag"
                                    style="text-decoration:none;">

                                        <span>👥</span>
                                        <%= g.groupName %>

                                </a>

                            <%
                                    }
                                } else {
                            %>

                                <div class="classes-empty-groups">
                                    Khối lớp này chưa có nhóm học nào.
                                </div>

                            <%
                                }
                            %>

                        </div>

                    </div>

                    <%
                            }
                        } else {
                    %>

                    <div class="classes-empty-state">
                        <div class="empty-state-icon">🏫</div>
                        <h4>Chưa có khối lớp nào</h4>
                        <p>
                            Hãy tạo khối lớp đầu tiên bằng biểu mẫu
                            ở bên trái để bắt đầu quản lý.
                        </p>
                    </div>

                    <%
                        }
                    %>

                </div>
            </div>

        </section>

        <footer class="teacher-page-footer">
            OnlineExam · Hệ thống quản lý thi trực tuyến
        </footer>

    </main>
</div>
</body>
</html>