package com.onlineexam.service;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;

/**
 * Business logic for Auth.
 * TODO: Implement validation and coordinate DAO calls here.
 */
public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password)
            throws SQLException {

        // 1. Kiểm tra dữ liệu đầu vào
        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        // 2. Tìm tài khoản trong database
        User user = userDAO.findByUsername(username.trim());

        if (user == null) {
            return null;
        }

        // 3. Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }

        // 4. Kiểm tra mật khẩu
        String hash = user.getPasswordHash();

        if (hash == null || hash.isBlank()) {
            return null;
        }

        try {
            if (!BCrypt.checkpw(password, hash)) {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        // 5. Đăng nhập thành công
        return user;
    }

}
