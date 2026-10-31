package com.onlineexam.service;

import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public void createUser(String username, String email,
            String password, String fullName, int roleId) throws Exception {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password không được để trống");
        }

        if (userDAO.existsByUsername(username)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        String hashedPassword = BCrypt.hashpw(
                password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(hashedPassword);
        user.setStatus("ACTIVE");
        userDAO.insertWithRole(user, roleId);
    }

}