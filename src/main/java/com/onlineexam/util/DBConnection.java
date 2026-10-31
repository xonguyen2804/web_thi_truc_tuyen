package com.onlineexam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp kết nối Cơ sở dữ liệu MySQL (phpMyAdmin / XAMPP).
 */
public final class DBConnection {

    // 1. Cấu hình mặc định cho MySQL trong phpMyAdmin (XAMPP)
    // Tên database: OnlineExam, Port: 3306, Mã hóa: utf8mb4
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/OnlineExam?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = ""; // phpMyAdmin mặc định pass là rỗng ""

    static {
        try {
            // Nạp Driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver MySQL: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // Ưu tiên đọc từ biến môi trường, nếu không có thì lấy cấu hình mặc định ở trên
        String envUrl = System.getenv("ONLINEEXAM_DB_URL");
        String envUser = System.getenv("ONLINEEXAM_DB_USER");
        String envPassword = System.getenv("ONLINEEXAM_DB_PASSWORD");

        String url = (envUrl != null && !envUrl.isBlank()) ? envUrl : DEFAULT_URL;
        String user = (envUser != null && !envUser.isBlank()) ? envUser : DEFAULT_USER;
        String password = (envPassword != null) ? envPassword : DEFAULT_PASSWORD;

        // Nếu mật khẩu phpMyAdmin của bạn có đặt mật khẩu riêng, hãy sửa DEFAULT_PASSWORD ở trên
        return DriverManager.getConnection(url, user, password);
    }
}