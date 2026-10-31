package com.onlineexam.dao;

import com.onlineexam.model.ExamSession;
import com.onlineexam.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamSessionDAO {

    // 1. Thêm mới ca thi vào bảng exam_sessions
    public boolean insert(ExamSession session) throws SQLException {
        String sql = "INSERT INTO exam_sessions (exam_id, created_by, class_id, group_id, session_name, session_password, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, session.getExamId());
            ps.setInt(2, session.getCreatedBy());

            if (session.getClassId() != null && session.getClassId() > 0) {
                ps.setInt(3, session.getClassId());
                ps.setNull(4, Types.INTEGER);
            } else if (session.getGroupId() != null && session.getGroupId() > 0) {
                ps.setNull(3, Types.INTEGER);
                ps.setInt(4, session.getGroupId());
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, session.getSessionName());
            ps.setString(6, session.getSessionPassword());
            ps.setTimestamp(7, Timestamp.valueOf(session.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(session.getEndTime()));
            ps.setString(9, session.getStatus() != null ? session.getStatus() : "SCHEDULED");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        session.setSessionId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Mở hoặc đóng phòng thi (Đổi status sang OPEN hoặc CLOSED)
    public boolean updateStatus(int sessionId, String status) throws SQLException {
        String sql = "UPDATE exam_sessions SET status = ? WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    // 3. Lấy tất cả ca thi dành cho màn hình giáo viên
    public List<ExamSession> findAllForTeacher() throws SQLException {
        List<ExamSession> list = new ArrayList<>();
        String sql = "SELECT es.*, e.exam_title, e.duration_minutes, " +
                     "COALESCE(c.class_name, g.group_name, 'Tự do') AS target_name " +
                     "FROM exam_sessions es " +
                     "JOIN exams e ON es.exam_id = e.exam_id " +
                     "LEFT JOIN classes c ON es.class_id = c.class_id " +
                     "LEFT JOIN `groups` g ON es.group_id = g.group_id " +
                     "ORDER BY es.session_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSession(rs));
            }
        }
        return list;
    }

    // 4. Tìm kiếm ca thi dành cho học sinh (OPEN hoặc SCHEDULED)
    public List<ExamSession> findAvailableForStudent(String keyword) throws SQLException {
        List<ExamSession> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT es.*, e.exam_title, e.duration_minutes, " +
            "COALESCE(c.class_name, g.group_name, 'Tất cả') AS target_name " +
            "FROM exam_sessions es " +
            "JOIN exams e ON es.exam_id = e.exam_id " +
            "LEFT JOIN classes c ON es.class_id = c.class_id " +
            "LEFT JOIN `groups` g ON es.group_id = g.group_id " +
            "WHERE es.status IN ('OPEN', 'SCHEDULED') "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (es.session_name LIKE ? OR e.exam_title LIKE ?) ");
        }
        sql.append("ORDER BY (es.status = 'OPEN') DESC, es.start_time ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSession(rs));
                }
            }
        }
        return list;
    }

    // 5. Chi tiết 1 ca thi theo sessionId
    public ExamSession findById(int sessionId) throws SQLException {
        String sql = "SELECT es.*, e.exam_title, e.duration_minutes, " +
                     "COALESCE(c.class_name, g.group_name, 'Tất cả') AS target_name " +
                     "FROM exam_sessions es " +
                     "JOIN exams e ON es.exam_id = e.exam_id " +
                     "LEFT JOIN classes c ON es.class_id = c.class_id " +
                     "LEFT JOIN `groups` g ON es.group_id = g.group_id " +
                     "WHERE es.session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSession(rs);
                }
            }
        }
        return null;
    }

    private ExamSession mapResultSetToSession(ResultSet rs) throws SQLException {
        ExamSession s = new ExamSession();
        s.setSessionId(rs.getInt("session_id"));
        s.setExamId(rs.getInt("exam_id"));
        s.setCreatedBy(rs.getInt("created_by"));
        s.setClassId((Integer) rs.getObject("class_id"));
        s.setGroupId((Integer) rs.getObject("group_id"));
        s.setSessionName(rs.getString("session_name"));
        s.setSessionPassword(rs.getString("session_password"));

        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) s.setStartTime(st.toLocalDateTime());

        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) s.setEndTime(et.toLocalDateTime());

        s.setStatus(rs.getString("status"));
        s.setExamTitle(rs.getString("exam_title"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setTargetName(rs.getString("target_name"));
        return s;
    }
}