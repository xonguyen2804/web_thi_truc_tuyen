<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.onlineexam.model.Question" %>
<%@ page import="com.onlineexam.model.Option" %>
<%@ page import="com.onlineexam.model.Exam" %>
<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    List<Question> questions = (List<Question>) request.getAttribute("questions");
    Map<String, Integer> stats = (Map<String, Integer>) request.getAttribute("stats");
    List<Exam> exams = (List<Exam>) request.getAttribute("exams");

    String keyword = (String) request.getAttribute("keyword");
    String selectedType = (String) request.getAttribute("selectedType");
    Integer selectedExamId = (Integer) request.getAttribute("selectedExamId");

    int totalQuestions = (stats != null && stats.get("total") != null) ? stats.get("total") : 0;
    int singleCount = (stats != null && stats.get("single") != null) ? stats.get("single") : 0;
    int trueFalseCount = (stats != null && stats.get("true_false") != null) ? stats.get("true_false") : 0;
    int shortAnswerCount = (stats != null && stats.get("short_answer") != null) ? stats.get("short_answer") : 0;

    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ngân Hàng Câu Hỏi | OnlineExam</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher.css">
    <style>
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }
        .stat-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 18px 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .stat-card .val {
            font-size: 26px;
            font-weight: 800;
            color: #0f172a;
            line-height: 1.2;
        }
        .stat-card .lbl {
            font-size: 13px;
            color: #64748b;
            margin-top: 4px;
        }
        .stat-card .icon-box {
            width: 44px;
            height: 44px;
            border-radius: 10px;
            display: grid;
            place-items: center;
            font-size: 20px;
        }

        .filter-card, .table-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.03);
        }

        .filter-form {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            align-items: flex-end;
        }
        .filter-group {
            flex: 1;
            min-width: 180px;
        }
        .filter-group label {
            display: block;
            font-size: 12px;
            font-weight: 600;
            color: #475569;
            margin-bottom: 6px;
        }
        .form-control {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 14px;
            box-sizing: border-box;
            background: #fff;
        }
        .form-control:focus {
            outline: none;
            border-color: #2563eb;
            box-shadow: 0 0 0 2px rgba(37,99,235,0.15);
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            padding: 9px 16px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            border: none;
            transition: all 0.15s ease-in-out;
            text-decoration: none;
        }
        .btn-primary { background: #2563eb; color: #ffffff; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-success { background: #16a34a; color: #ffffff; }
        .btn-success:hover { background: #15803d; }
        .btn-secondary { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; }
        .btn-secondary:hover { background: #e2e8f0; }
        .btn-danger { background: #ef4444; color: #ffffff; }
        .btn-danger:hover { background: #dc2626; }
        .btn-warning { background: #f59e0b; color: #ffffff; }
        .btn-warning:hover { background: #d97706; }
        .btn-sm { padding: 5px 10px; font-size: 12px; border-radius: 4px; }

        /* Bulk Action Bar */
        .bulk-bar {
            display: none;
            position: sticky;
            top: 15px;
            z-index: 100;
            background: #1e293b;
            color: #ffffff;
            padding: 12px 20px;
            border-radius: 8px;
            margin-bottom: 18px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            align-items: center;
            justify-content: space-between;
            animation: slideDown 0.2s ease-out;
        }
        @keyframes slideDown {
            from { transform: translateY(-10px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }
        .bulk-bar.active { display: flex; }
        .bulk-actions { display: flex; gap: 10px; align-items: center; }

        /* Table */
        .q-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        .q-table th, .q-table td {
            padding: 12px 14px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
            vertical-align: middle;
        }
        .q-table th {
            background: #f8fafc;
            color: #475569;
            font-weight: 600;
            font-size: 13px;
        }
        .q-table tr:hover td {
            background: #f8fafc;
        }

        /* Badges */
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.3px;
        }
        .badge-single { background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; }
        .badge-tf { background: #f5f3ff; color: #6d28d9; border: 1px solid #ddd6fe; }
        .badge-short { background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; }
        .badge-exam { background: #f1f5f9; color: #334155; font-size: 11px; padding: 2px 6px; border-radius: 3px; }

        /* Question preview details */
        .q-detail-container {
            display: none;
            background: #f8fafc;
            padding: 16px 20px;
            border-bottom: 2px solid #cbd5e1;
            font-size: 13px;
        }
        .q-options-list {
            margin: 10px 0 0 0;
            padding-left: 0;
            list-style: none;
            display: grid;
            gap: 6px;
        }
        .q-option-item {
            padding: 8px 12px;
            border-radius: 6px;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .q-option-item.correct {
            background: #f0fdf4;
            border-color: #86efac;
            font-weight: 600;
            color: #166534;
        }
        .q-explanation {
            margin-top: 10px;
            padding: 8px 12px;
            background: #fefce8;
            border-left: 3px solid #eab308;
            color: #713f12;
            border-radius: 0 4px 4px 0;
        }

        /* Modal */
        .modal {
            display: none;
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(2px);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            overflow-y: auto;
            padding: 20px;
            box-sizing: border-box;
        }
        .modal.active { display: flex; }
        .modal-content {
            background: #ffffff;
            border-radius: 12px;
            width: 100%;
            max-width: 680px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
            animation: modalFadeIn 0.2s ease-out;
            max-height: 90vh;
            display: flex;
            flex-direction: column;
        }
        @keyframes modalFadeIn {
            from { transform: scale(0.95); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
        .modal-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .modal-header h3 { margin: 0; font-size: 17px; color: #0f172a; }
        .modal-close {
            background: none; border: none; font-size: 20px; cursor: pointer; color: #94a3b8;
        }
        .modal-close:hover { color: #0f172a; }
        .modal-body {
            padding: 20px;
            overflow-y: auto;
        }
        .modal-footer {
            padding: 14px 20px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            background: #f8fafc;
            border-radius: 0 0 12px 12px;
        }
    </style>
</head>
<body>
<div class="teacher-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/teacher/dashboard" style="display: flex; align-items: center; gap: 10px; margin-bottom: 28px;">
            <div style="background: #2563eb; color: white; width: 36px; height: 36px; border-radius: 8px; display: grid; place-items: center; font-weight: 800;">OE</div>
            <div>
                <span style="font-size: 16px; font-weight: 800; color: #1e3a8a; display: block;">OnlineExam</span>
                <span style="font-size: 11px; color: #64748b;">Luyện Thi THPT QG</span>
            </div>
        </a>
        <div class="sidebar-label">CHỨC NĂNG GIẢNG DẠY</div>
        <nav class="sidebar-nav">
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/dashboard">📊 Tổng Quan</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exams">📝 Quản Lý Đề Thi</a>
            <a class="nav-item active" href="${pageContext.request.contextPath}/teacher/questions">❓ Ngân Hàng Câu Hỏi</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/classes">👥 Lớp Học</a>
            <a class="nav-item" href="${pageContext.request.contextPath}/teacher/exam-sessions">⏱ Lịch Thi</a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content" style="padding: 28px; flex: 1;">
        <!-- Header bar -->
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;">
            <div>
                <h1 style="font-size: 24px; margin: 0 0 4px 0; color: #0f172a;">Ngân Hàng Câu Hỏi</h1>
                <p style="color: #64748b; font-size: 13px; margin: 0;">
                    Kho lưu trữ câu hỏi dùng chung &bull; Rà soát và sửa lỗi sau import &bull; Tái sử dụng để trộn / ghép đề mới
                </p>
            </div>
            <div style="display: flex; gap: 10px;">
                <button type="button" class="btn btn-primary" onclick="openAddModal()">
                    + Thêm Câu Hỏi Mới
                </button>
            </div>
        </div>

        <!-- Flash Messages -->
        <% if ("created_success".equals(msg)) { %>
            <div style="padding: 12px 16px; background: #dcfce7; color: #15803d; border-radius: 8px; margin-bottom: 18px; border: 1px solid #bbf7d0;">
                ✓ Đã thêm câu hỏi mới vào ngân hàng thành công!
            </div>
        <% } else if ("updated_success".equals(msg)) { %>
            <div style="padding: 12px 16px; background: #dcfce7; color: #15803d; border-radius: 8px; margin-bottom: 18px; border: 1px solid #bbf7d0;">
                ✓ Cập nhật nội dung và đáp án câu hỏi thành công!
            </div>
        <% } else if ("deleted_success".equals(msg)) { %>
            <div style="padding: 12px 16px; background: #fef2f2; color: #b91c1c; border-radius: 8px; margin-bottom: 18px; border: 1px solid #fecaca;">
                ✓ Đã xóa câu hỏi khỏi ngân hàng an toàn.
            </div>
        <% } else if ("exam_created_success".equals(msg)) { %>
            <div style="padding: 12px 16px; background: #dbeafe; color: #1e40af; border-radius: 8px; margin-bottom: 18px; border: 1px solid #bfdbfe; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    ✓ <strong>Tạo đề thi mới thành công!</strong> Đã ghép các câu hỏi đã chọn vào đề thi mới.
                </div>
                <a href="${pageContext.request.contextPath}/teacher/exams" class="btn btn-sm btn-primary" style="margin-left: 12px;">Xem danh sách đề &rarr;</a>
            </div>
        <% } else if ("added_to_exam_success".equals(msg)) { %>
            <div style="padding: 12px 16px; background: #dcfce7; color: #15803d; border-radius: 8px; margin-bottom: 18px; border: 1px solid #bbf7d0;">
                ✓ Đã thêm thành công các câu hỏi vào đề thi được chỉ định!
            </div>
        <% } else if (request.getParameter("error") != null) { %>
            <div style="padding: 12px 16px; background: #fef2f2; color: #b91c1c; border-radius: 8px; margin-bottom: 18px; border: 1px solid #fecaca;">
                ✕ <%= request.getSession().getAttribute("errorMessage") != null ? request.getSession().getAttribute("errorMessage") : "Có lỗi xảy ra trong quá trình thao tác." %>
            </div>
            <% request.getSession().removeAttribute("errorMessage"); %>
        <% } %>

        <!-- Feature 2: Thống kê ngân hàng câu hỏi (Repository) -->
        <div class="stats-grid">
            <div class="stat-card">
                <div>
                    <div class="val"><%= totalQuestions %></div>
                    <div class="lbl">Tổng số câu hỏi</div>
                </div>
                <div class="icon-box" style="background: #e0f2fe; color: #0284c7;">📚</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val"><%= singleCount %></div>
                    <div class="lbl">Trắc nghiệm 4 lựa chọn</div>
                </div>
                <div class="icon-box" style="background: #eff6ff; color: #2563eb;">🔘</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val"><%= trueFalseCount %></div>
                    <div class="lbl">Đúng / Sai (4 ý con)</div>
                </div>
                <div class="icon-box" style="background: #f5f3ff; color: #7c3aed;">⚖️</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val"><%= shortAnswerCount %></div>
                    <div class="lbl">Trả lời ngắn (&le; 4 ký tự)</div>
                </div>
                <div class="icon-box" style="background: #fff7ed; color: #ea580c;">✏️</div>
            </div>
        </div>

        <!-- Bộ lọc và tìm kiếm -->
        <div class="filter-card">
            <form action="${pageContext.request.contextPath}/teacher/questions" method="GET" class="filter-form">
                <div class="filter-group" style="flex: 2; min-width: 250px;">
                    <label>Tìm kiếm nội dung câu hỏi</label>
                    <input type="text" name="keyword" class="form-control" placeholder="Nhập từ khóa cần tìm (VD: dao động, sóng cơ, phương trình...)" value="<%= (keyword != null) ? keyword : "" %>">
                </div>
                <div class="filter-group">
                    <label>Loại câu hỏi</label>
                    <select name="type" class="form-control">
                        <option value="ALL">-- Tất cả loại câu hỏi --</option>
                        <option value="SINGLE" <%= "SINGLE".equalsIgnoreCase(selectedType) ? "selected" : "" %>>Trắc nghiệm 4 lựa chọn (SINGLE)</option>
                        <option value="TRUE_FALSE" <%= "TRUE_FALSE".equalsIgnoreCase(selectedType) ? "selected" : "" %>>Đúng / Sai (TRUE_FALSE)</option>
                        <option value="SHORT_ANSWER" <%= "SHORT_ANSWER".equalsIgnoreCase(selectedType) ? "selected" : "" %>>Trả lời ngắn (SHORT_ANSWER)</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label>Thuộc đề thi</label>
                    <select name="examId" class="form-control">
                        <option value="">-- Tất cả đề thi --</option>
                        <% if (exams != null) {
                            for (Exam ex : exams) {
                        %>
                            <option value="<%= ex.getExamId() %>" <%= (selectedExamId != null && selectedExamId == ex.getExamId()) ? "selected" : "" %>>
                                [Mã <%= ex.getExamId() %>] <%= ex.getExamTitle() %>
                            </option>
                        <%  }
                        } %>
                    </select>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary" style="margin-right: 6px;">🔍 Lọc</button>
                    <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-secondary">Đặt lại</a>
                </div>
            </form>
        </div>

        <!-- Feature 3: Thanh tác vụ khi chọn câu hỏi (Tái sử dụng / Trộn ghép đề mới) -->
        <div id="bulkBar" class="bulk-bar">
            <div>
                <span style="font-weight: 700; font-size: 15px;">Đã chọn <span id="selectedCount" style="color: #60a5fa;">0</span> câu hỏi</span>
                <span style="font-size: 12px; color: #94a3b8; margin-left: 8px;">(Sử dụng để tạo đề thi mới hoặc bổ sung vào đề có sẵn)</span>
            </div>
            <div class="bulk-actions">
                <button type="button" class="btn btn-sm btn-success" onclick="openCreateExamModal()">
                    🚀 Tạo Đề Thi Mới Từ Các Câu Đã Chọn
                </button>
                <button type="button" class="btn btn-sm btn-warning" onclick="openAddToExamModal()">
                    ➕ Thêm Vào Đề Có Sẵn
                </button>
                <button type="button" class="btn btn-sm btn-secondary" onclick="clearSelection()">
                    Bỏ chọn
                </button>
            </div>
        </div>

        <!-- Bảng danh sách câu hỏi -->
        <div class="table-card" style="padding: 0; overflow: hidden;">
            <div style="padding: 16px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center;">
                <h3 style="margin: 0; font-size: 16px; color: #0f172a;">
                    Danh Sách Câu Hỏi Trong Ngân Hàng 
                    <span style="font-size: 13px; font-weight: normal; color: #64748b;">(<%= (questions != null) ? questions.size() : 0 %> câu phù hợp)</span>
                </h3>
                <small style="color: #64748b;">Bấm vào nút <strong>"Xem đáp án"</strong> để rà soát chi tiết từng câu</small>
            </div>

            <table class="q-table">
                <thead>
                    <tr>
                        <th style="width: 40px; text-align: center;">
                            <input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAll(this)" title="Chọn tất cả câu hỏi">
                        </th>
                        <th style="width: 60px;">Mã</th>
                        <th style="width: 140px;">Loại câu</th>
                        <th>Nội dung câu hỏi</th>
                        <th style="width: 90px; text-align: center;">Điểm chuẩn</th>
                        <th style="width: 170px;">Đề thi chứa câu</th>
                        <th style="width: 170px; text-align: right;">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                <%
                    if (questions != null && !questions.isEmpty()) {
                        for (Question q : questions) {
                            String typeBadge = "badge-single";
                            if ("TRUE_FALSE".equalsIgnoreCase(q.getQuestionType())) typeBadge = "badge-tf";
                            else if ("SHORT_ANSWER".equalsIgnoreCase(q.getQuestionType())) typeBadge = "badge-short";
                %>
                    <tr id="row-<%= q.getQuestionId() %>">
                        <td style="text-align: center;">
                            <input type="checkbox" class="q-checkbox" value="<%= q.getQuestionId() %>" onchange="updateSelectedCount()">
                        </td>
                        <td><strong>#<%= q.getQuestionId() %></strong></td>
                        <td>
                            <span class="badge <%= typeBadge %>"><%= q.getQuestionType() %></span>
                        </td>
                        <td>
                            <div style="font-weight: 600; color: #1e293b; margin-bottom: 2px;">
                                <%= (q.getQuestionText() != null && q.getQuestionText().length() > 110) 
                                        ? q.getQuestionText().substring(0, 110) + "..." 
                                        : q.getQuestionText() %>
                            </div>
                            <div style="font-size: 12px; color: #64748b;">
                                <% if ("SINGLE".equalsIgnoreCase(q.getQuestionType())) { %>
                                    <%= q.getOptions().size() %> phương án A, B, C, D
                                <% } else if ("TRUE_FALSE".equalsIgnoreCase(q.getQuestionType())) { %>
                                    <%= q.getOptions().size() %> ý Đúng / Sai
                                <% } else if ("SHORT_ANSWER".equalsIgnoreCase(q.getQuestionType())) { %>
                                    Đáp án ngắn: <span style="font-family: monospace; font-weight: 700; color: #c2410c;"><%= q.getShortAnswer() != null ? q.getShortAnswer() : "Chưa đặt" %></span>
                                <% } %>
                            </div>
                        </td>
                        <td style="text-align: center;">
                            <span style="font-weight: 600; color: #0284c7;"><%= q.getDefaultPoints() %></span>
                        </td>
                        <td>
                            <% if (q.getLinkedExamTitles() != null && !q.getLinkedExamTitles().isEmpty()) { 
                                for (String exTitle : q.getLinkedExamTitles()) { %>
                                    <div class="badge-exam" title="<%= exTitle %>" style="margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 160px;">
                                        📄 <%= exTitle %>
                                    </div>
                            <%  }
                            } else { %>
                                <span style="color: #94a3b8; font-size: 12px;">(Chưa gán đề)</span>
                            <% } %>
                        </td>
                        <td style="text-align: right; white-space: nowrap;">
                            <button type="button" class="btn btn-sm btn-secondary" onclick="toggleDetails(<%= q.getQuestionId() %>)" title="Xem chi tiết câu hỏi">
                                👁 Đáp án
                            </button>
                            <button type="button" class="btn btn-sm btn-primary" onclick="openEditModal(<%= q.getQuestionId() %>)" title="Sửa nội dung câu hỏi">
                                ✏ Sửa
                            </button>
                            <button type="button" class="btn btn-sm btn-danger" onclick="confirmDelete(<%= q.getQuestionId() %>)" title="Xóa câu hỏi">
                                🗑
                            </button>
                        </td>
                    </tr>

                    <!-- Hàng chi tiết rà soát câu hỏi (Review & Edit preview) -->
                    <tr id="details-row-<%= q.getQuestionId() %>" style="display: none;">
                        <td colspan="7" style="padding: 0;">
                            <div class="q-detail-container">
                                <div style="font-weight: 700; font-size: 14px; margin-bottom: 8px; color: #0f172a;">
                                    Nội dung câu hỏi: <%= q.getQuestionText() %>
                                </div>

                                <!-- Phương án cho câu hỏi SINGLE -->
                                <% if ("SINGLE".equalsIgnoreCase(q.getQuestionType())) { %>
                                    <div style="font-size: 12px; font-weight: 600; color: #475569;">CÁC PHƯƠNG ÁN LỰA CHỌN:</div>
                                    <ul class="q-options-list">
                                        <%
                                            char optLabel = 'A';
                                            for (Option opt : q.getOptions()) {
                                        %>
                                            <li class="q-option-item <%= opt.isCorrect() ? "correct" : "" %>">
                                                <span>
                                                    <strong><%= optLabel++ %>.</strong> <%= opt.getOptionText() %>
                                                </span>
                                                <% if (opt.isCorrect()) { %>
                                                    <span style="font-size: 11px; background: #16a34a; color: white; padding: 2px 6px; border-radius: 4px;">✓ ĐÁP ÁN ĐÚNG</span>
                                                <% } %>
                                            </li>
                                        <% } %>
                                    </ul>

                                <!-- Phương án cho câu hỏi TRUE_FALSE -->
                                <% } else if ("TRUE_FALSE".equalsIgnoreCase(q.getQuestionType())) { %>
                                    <div style="font-size: 12px; font-weight: 600; color: #475569;">CÁC Ý XÉT ĐÚNG / SAI:</div>
                                    <ul class="q-options-list">
                                        <%
                                            int ýIndex = 1;
                                            for (Option opt : q.getOptions()) {
                                        %>
                                            <li class="q-option-item <%= opt.isCorrect() ? "correct" : "" %>">
                                                <span>
                                                    <strong>Ý <%= ýIndex++ %>:</strong> <%= opt.getOptionText() %>
                                                </span>
                                                <% if (opt.isCorrect()) { %>
                                                    <span style="font-size: 11px; background: #16a34a; color: white; padding: 2px 6px; border-radius: 4px;">ĐÚNG (Đ)</span>
                                                <% } else { %>
                                                    <span style="font-size: 11px; background: #ef4444; color: white; padding: 2px 6px; border-radius: 4px;">SAI (S)</span>
                                                <% } %>
                                            </li>
                                        <% } %>
                                    </ul>

                                <!-- Đáp án cho câu hỏi SHORT_ANSWER -->
                                <% } else if ("SHORT_ANSWER".equalsIgnoreCase(q.getQuestionType())) { %>
                                    <div style="margin-top: 6px;">
                                        <span style="font-weight: 600; color: #475569;">Đáp án ngắn chuẩn:</span>
                                        <span style="padding: 4px 10px; background: #fff7ed; border: 1px solid #fed7aa; border-radius: 4px; font-family: monospace; font-size: 14px; font-weight: 700; color: #c2410c;">
                                            <%= q.getShortAnswer() != null ? q.getShortAnswer() : "Chưa có đáp án" %>
                                        </span>
                                    </div>
                                <% } %>

                                <% if (q.getExplanation() != null && !q.getExplanation().isBlank()) { %>
                                    <div class="q-explanation">
                                        <strong>💡 Lời giải thích:</strong> <%= q.getExplanation() %>
                                    </div>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                <%
                        }
                    } else {
                %>
                    <tr>
                        <td colspan="7" style="text-align: center; color: #94a3b8; padding: 36px;">
                            <div style="font-size: 28px; margin-bottom: 8px;">📭</div>
                            Chưa có câu hỏi nào trong ngân hàng hoặc không có câu hỏi nào thỏa mãn bộ lọc tìm kiếm.
                            <br>
                            <button type="button" class="btn btn-sm btn-primary" onclick="openAddModal()" style="margin-top: 12px;">+ Thêm câu hỏi đầu tiên</button>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>

<!-- ==================== MODAL 1: THÊM CÂU HỎI MỚI THỦ CÔNG ==================== -->
<div id="addModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>+ Thêm Câu Hỏi Mới Vào Ngân Hàng</h3>
            <button type="button" class="modal-close" onclick="closeModal('addModal')">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/teacher/questions" method="POST">
            <input type="hidden" name="action" value="create">
            <div class="modal-body">
                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Loại câu hỏi <span style="color: red;">*</span></label>
                    <select name="questionType" id="addQuestionType" class="form-control" onchange="switchAddTypeFields(this.value)">
                        <option value="SINGLE">Trắc nghiệm 4 lựa chọn (SINGLE - A, B, C, D)</option>
                        <option value="TRUE_FALSE">Đúng / Sai 4 ý (TRUE_FALSE)</option>
                        <option value="SHORT_ANSWER">Trả lời ngắn (SHORT_ANSWER &le; 4 ký tự)</option>
                    </select>
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Nội dung câu hỏi <span style="color: red;">*</span></label>
                    <textarea name="questionText" class="form-control" rows="3" required placeholder="Nhập đề bài câu hỏi tại đây..."></textarea>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
                    <div>
                        <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Điểm số mặc định</label>
                        <input type="number" step="0.05" name="defaultPoints" class="form-control" value="0.25" required>
                    </div>
                </div>

                <!-- Fields cho SINGLE -->
                <div id="addSingleFields">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">4 Phương án lựa chọn (Chọn 1 đáp án đúng):</label>
                    <div style="display: grid; gap: 8px;">
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="correctOption" value="1" checked title="Đáp án đúng">
                            <span style="font-weight: 700; width: 20px;">A.</span>
                            <input type="text" name="optionText_1" class="form-control" placeholder="Nội dung đáp án A">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="correctOption" value="2" title="Đáp án đúng">
                            <span style="font-weight: 700; width: 20px;">B.</span>
                            <input type="text" name="optionText_2" class="form-control" placeholder="Nội dung đáp án B">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="correctOption" value="3" title="Đáp án đúng">
                            <span style="font-weight: 700; width: 20px;">C.</span>
                            <input type="text" name="optionText_3" class="form-control" placeholder="Nội dung đáp án C">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="correctOption" value="4" title="Đáp án đúng">
                            <span style="font-weight: 700; width: 20px;">D.</span>
                            <input type="text" name="optionText_4" class="form-control" placeholder="Nội dung đáp án D">
                        </div>
                    </div>
                </div>

                <!-- Fields cho TRUE_FALSE -->
                <div id="addTfFields" style="display: none;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">4 Ý con xét tính Đúng / Sai:</label>
                    <div style="display: grid; gap: 8px;">
                        <% for (int i = 1; i <= 4; i++) { %>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <span style="font-weight: 700; width: 35px;">Ý <%= i %>:</span>
                            <input type="text" name="tfText_<%= i %>" class="form-control" placeholder="Nội dung ý con <%= i %>">
                            <select name="tfCorrect_<%= i %>" class="form-control" style="width: 100px;">
                                <option value="true">Đúng (Đ)</option>
                                <option value="false">Sai (S)</option>
                            </select>
                        </div>
                        <% } %>
                    </div>
                </div>

                <!-- Fields cho SHORT_ANSWER -->
                <div id="addShortFields" style="display: none;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Đáp án ngắn chuẩn (Tối đa 4 ký tự):</label>
                    <input type="text" name="shortAnswer" maxlength="4" class="form-control" placeholder="VD: 50 hoặc 3.14 hoặc 1/2">
                </div>

                <div style="margin-top: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Lời giải thích chi tiết (Tùy chọn)</label>
                    <textarea name="explanation" class="form-control" rows="2" placeholder="Ghi chú giải thích cách giải..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('addModal')">Hủy</button>
                <button type="submit" class="btn btn-primary">+ Lưu Vào Ngân Hàng</button>
            </div>
        </form>
    </div>
</div>

<!-- ==================== MODAL 2: SỬA CÂU HỎI (REVIEW & EDIT) ==================== -->
<div id="editModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>✏ Chỉnh Sửa Câu Hỏi #<span id="editModalQId"></span></h3>
            <button type="button" class="modal-close" onclick="closeModal('editModal')">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/teacher/questions" method="POST">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="questionId" id="editQuestionId">
            <input type="hidden" name="questionType" id="editQuestionType">

            <div class="modal-body">
                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Loại câu hỏi:</label>
                    <input type="text" id="editQuestionTypeDisplay" class="form-control" readonly style="background: #f1f5f9; color: #475569;">
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Nội dung câu hỏi <span style="color: red;">*</span></label>
                    <textarea name="questionText" id="editQuestionText" class="form-control" rows="3" required></textarea>
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Điểm số</label>
                    <input type="number" step="0.05" name="defaultPoints" id="editDefaultPoints" class="form-control" required>
                </div>

                <!-- Form edit cho SINGLE -->
                <div id="editSingleContainer" style="display: none;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Các phương án & Chọn đáp án đúng:</label>
                    <div style="display: grid; gap: 8px;">
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="editCorrectOption" value="1" id="editRadio_1">
                            <span style="font-weight: 700; width: 20px;">A.</span>
                            <input type="text" name="editOptionText_1" id="editOpt_1" class="form-control">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="editCorrectOption" value="2" id="editRadio_2">
                            <span style="font-weight: 700; width: 20px;">B.</span>
                            <input type="text" name="editOptionText_2" id="editOpt_2" class="form-control">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="editCorrectOption" value="3" id="editRadio_3">
                            <span style="font-weight: 700; width: 20px;">C.</span>
                            <input type="text" name="editOptionText_3" id="editOpt_3" class="form-control">
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="radio" name="editCorrectOption" value="4" id="editRadio_4">
                            <span style="font-weight: 700; width: 20px;">D.</span>
                            <input type="text" name="editOptionText_4" id="editOpt_4" class="form-control">
                        </div>
                    </div>
                </div>

                <!-- Form edit cho TRUE_FALSE -->
                <div id="editTfContainer" style="display: none;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">4 Ý con xét Đúng / Sai:</label>
                    <div style="display: grid; gap: 8px;">
                        <% for (int i = 1; i <= 4; i++) { %>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <span style="font-weight: 700; width: 35px;">Ý <%= i %>:</span>
                            <input type="text" name="editTfText_<%= i %>" id="editTfText_<%= i %>" class="form-control">
                            <select name="editTfCorrect_<%= i %>" id="editTfCorrect_<%= i %>" class="form-control" style="width: 100px;">
                                <option value="true">Đúng (Đ)</option>
                                <option value="false">Sai (S)</option>
                            </select>
                        </div>
                        <% } %>
                    </div>
                </div>

                <!-- Form edit cho SHORT_ANSWER -->
                <div id="editShortContainer" style="display: none;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Đáp án ngắn chuẩn (&le; 4 ký tự):</label>
                    <input type="text" name="editShortAnswer" id="editShortAnswer" maxlength="4" class="form-control">
                </div>

                <div style="margin-top: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Lời giải thích chi tiết</label>
                    <textarea name="explanation" id="editExplanation" class="form-control" rows="2"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('editModal')">Hủy</button>
                <button type="submit" class="btn btn-primary">Lưu Thay Đổi</button>
            </div>
        </form>
    </div>
</div>

<!-- ==================== MODAL 3: TẠO ĐỀ THI MỚI TỪ CÂU ĐÃ CHỌN (TRỘN / GHÉP ĐỀ) ==================== -->
<div id="createExamModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>🚀 Tạo Đề Thi Mới Từ Ngân Hàng Câu Hỏi</h3>
            <button type="button" class="modal-close" onclick="closeModal('createExamModal')">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/teacher/questions" method="POST" id="createExamForm">
            <input type="hidden" name="action" value="create_exam_from_bank">
            <div id="createExamHiddenInputs"></div>

            <div class="modal-body">
                <div style="background: #eff6ff; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-size: 13px; color: #1e40af; border: 1px solid #bfdbfe;">
                    Hệ thống sẽ lấy <strong id="createExamSelectedCount">0</strong> câu hỏi bạn đã chọn để tạo một bộ đề thi mới hoàn chỉnh.
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Tên đề thi mới <span style="color: red;">*</span></label>
                    <input type="text" name="newExamTitle" class="form-control" required placeholder="VD: Đề Khảo Sát Ôn Tập Tổng Hợp Số 1">
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
                    <div>
                        <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Thời gian làm bài (phút) <span style="color: red;">*</span></label>
                        <input type="number" name="newExamDuration" class="form-control" value="50" min="1" required>
                    </div>
                    <div>
                        <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Điểm chuẩn đạt (thang 10)</label>
                        <input type="number" step="0.25" name="newExamPassScore" class="form-control" value="5.0" required>
                    </div>
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Trạng thái đề thi</label>
                    <select name="newExamStatus" class="form-control">
                        <option value="PUBLISHED">Công khai ngay (PUBLISHED)</option>
                        <option value="DRAFT">Lưu bản nháp (DRAFT)</option>
                    </select>
                </div>

                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Mô tả đề thi (Tùy chọn)</label>
                    <textarea name="newExamDescription" class="form-control" rows="2" placeholder="Ghi chú về kỳ thi, khối lớp áp dụng..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('createExamModal')">Hủy</button>
                <button type="submit" class="btn btn-success">Xác Nhận Tạo Đề</button>
            </div>
        </form>
    </div>
</div>

<!-- ==================== MODAL 4: THÊM CÂU ĐÃ CHỌN VÀO ĐỀ CÓ SẴN ==================== -->
<div id="addToExamModal" class="modal">
    <div class="modal-content" style="max-width: 500px;">
        <div class="modal-header">
            <h3>➕ Thêm Vào Đề Thi Có Sẵn</h3>
            <button type="button" class="modal-close" onclick="closeModal('addToExamModal')">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/teacher/questions" method="POST" id="addToExamForm">
            <input type="hidden" name="action" value="add_to_exam">
            <div id="addToExamHiddenInputs"></div>

            <div class="modal-body">
                <div style="margin-bottom: 14px;">
                    <label style="display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px;">Chọn đề thi mục tiêu:</label>
                    <select name="targetExamId" class="form-control" required>
                        <option value="">-- Chọn đề thi --</option>
                        <% if (exams != null) {
                            for (Exam ex : exams) { %>
                                <option value="<%= ex.getExamId() %>">
                                    [#<%= ex.getExamId() %>] <%= ex.getExamTitle() %> (<%= ex.getDurationMinutes() %> phút)
                                </option>
                        <%  }
                        } %>
                    </select>
                </div>
                <p style="font-size: 12px; color: #64748b; margin: 0;">
                    Hệ thống sẽ thêm các câu hỏi đã chọn vào cuối danh sách câu hỏi của đề thi này (bỏ qua nếu câu hỏi đã tồn tại trong đề).
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('addToExamModal')">Hủy</button>
                <button type="submit" class="btn btn-warning">Thêm Vào Đề</button>
            </div>
        </form>
    </div>
</div>

<!-- Form ẩn dùng để xóa câu hỏi an toàn -->
<form id="deleteForm" action="${pageContext.request.contextPath}/teacher/questions" method="POST" style="display: none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="questionId" id="deleteQuestionId">
</form>

<script>
    // 1. Quản lý Modal
    function openModal(id) {
        document.getElementById(id).classList.add('active');
    }
    function closeModal(id) {
        document.getElementById(id).classList.remove('active');
    }

    function openAddModal() {
        openModal('addModal');
    }

    function switchAddTypeFields(type) {
        document.getElementById('addSingleFields').style.display = (type === 'SINGLE') ? 'block' : 'none';
        document.getElementById('addTfFields').style.display = (type === 'TRUE_FALSE') ? 'block' : 'none';
        document.getElementById('addShortFields').style.display = (type === 'SHORT_ANSWER') ? 'block' : 'none';
    }

    // 2. Mở / Đóng chi tiết câu hỏi trên bảng
    function toggleDetails(qId) {
        const row = document.getElementById('details-row-' + qId);
        if (row.style.display === 'none' || row.style.display === '') {
            row.style.display = 'table-row';
        } else {
            row.style.display = 'none';
        }
    }

    // 3. Xử lý mở Modal Sửa (Edit)
    function openEditModal(qId) {
        // Gọi AJAX lấy chi tiết câu hỏi từ servlet
        fetch('${pageContext.request.contextPath}/teacher/questions?action=get_detail&id=' + qId)
            .then(res => res.json())
            .then(data => {
                if (data.error) {
                    alert('Lỗi: ' + data.error);
                    return;
                }

                document.getElementById('editModalQId').innerText = data.questionId;
                document.getElementById('editQuestionId').value = data.questionId;
                document.getElementById('editQuestionType').value = data.questionType;
                document.getElementById('editQuestionText').value = data.questionText;
                document.getElementById('editDefaultPoints').value = data.defaultPoints;
                document.getElementById('editExplanation').value = data.explanation || '';

                // Hiển thị loại câu hỏi
                let typeDisplay = 'Trắc nghiệm 4 lựa chọn (SINGLE)';
                if (data.questionType === 'TRUE_FALSE') typeDisplay = 'Đúng / Sai 4 ý (TRUE_FALSE)';
                else if (data.questionType === 'SHORT_ANSWER') typeDisplay = 'Trả lời ngắn (SHORT_ANSWER)';
                document.getElementById('editQuestionTypeDisplay').value = typeDisplay;

                // Ẩn tất cả các khối đáp án
                document.getElementById('editSingleContainer').style.display = 'none';
                document.getElementById('editTfContainer').style.display = 'none';
                document.getElementById('editShortContainer').style.display = 'none';

                if (data.questionType === 'SINGLE') {
                    document.getElementById('editSingleContainer').style.display = 'block';
                    for (let i = 1; i <= 4; i++) {
                        const opt = data.options[i - 1];
                        document.getElementById('editOpt_' + i).value = opt ? opt.optionText : '';
                        if (opt && opt.isCorrect) {
                            document.getElementById('editRadio_' + i).checked = true;
                        }
                    }
                } else if (data.questionType === 'TRUE_FALSE') {
                    document.getElementById('editTfContainer').style.display = 'block';
                    for (let i = 1; i <= 4; i++) {
                        const opt = data.options[i - 1];
                        document.getElementById('editTfText_' + i).value = opt ? opt.optionText : '';
                        document.getElementById('editTfCorrect_' + i).value = (opt && opt.isCorrect) ? 'true' : 'false';
                    }
                } else if (data.questionType === 'SHORT_ANSWER') {
                    document.getElementById('editShortContainer').style.display = 'block';
                    document.getElementById('editShortAnswer').value = data.shortAnswer || '';
                }

                openModal('editModal');
            })
            .catch(err => {
                console.error(err);
                alert('Không thể tải thông tin câu hỏi. Vui lòng thử lại!');
            });
    }

    // 4. Xóa câu hỏi
    function confirmDelete(qId) {
        if (confirm('Bạn có chắc chắn muốn xóa câu hỏi #' + qId + ' khỏi ngân hàng?\nThao tác này sẽ đồng thời xóa câu hỏi khỏi các đề thi liên quan!')) {
            document.getElementById('deleteQuestionId').value = qId;
            document.getElementById('deleteForm').submit();
        }
    }

    // 5. Quản lý Bulk Actions (Chọn nhiều câu hỏi để trộn ghép đề)
    function toggleSelectAll(master) {
        const checkboxes = document.querySelectorAll('.q-checkbox');
        checkboxes.forEach(cb => cb.checked = master.checked);
        updateSelectedCount();
    }

    function updateSelectedCount() {
        const selected = document.querySelectorAll('.q-checkbox:checked');
        const count = selected.length;
        document.getElementById('selectedCount').innerText = count;

        const bulkBar = document.getElementById('bulkBar');
        if (count > 0) {
            bulkBar.classList.add('active');
        } else {
            bulkBar.classList.remove('active');
            document.getElementById('selectAllCheckbox').checked = false;
        }
    }

    function clearSelection() {
        document.querySelectorAll('.q-checkbox').forEach(cb => cb.checked = false);
        document.getElementById('selectAllCheckbox').checked = false;
        updateSelectedCount();
    }

    function getSelectedIds() {
        const selected = document.querySelectorAll('.q-checkbox:checked');
        const ids = [];
        selected.forEach(cb => ids.push(cb.value));
        return ids;
    }

    function openCreateExamModal() {
        const ids = getSelectedIds();
        if (ids.length === 0) {
            alert('Vui lòng chọn ít nhất 1 câu hỏi.');
            return;
        }

        document.getElementById('createExamSelectedCount').innerText = ids.length;

        // Điền hidden inputs
        const container = document.getElementById('createExamHiddenInputs');
        container.innerHTML = '';
        ids.forEach(id => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'selectedQuestions';
            input.value = id;
            container.appendChild(input);
        });

        openModal('createExamModal');
    }

    function openAddToExamModal() {
        const ids = getSelectedIds();
        if (ids.length === 0) {
            alert('Vui lòng chọn ít nhất 1 câu hỏi.');
            return;
        }

        const container = document.getElementById('addToExamHiddenInputs');
        container.innerHTML = '';
        ids.forEach(id => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'selectedQuestions';
            input.value = id;
            container.appendChild(input);
        });

        openModal('addToExamModal');
    }
</script>
</body>
</html>
