package com.onlineexam.util;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "1"; // Đặt thử mật khẩu ngắn gọn, ví dụ: 123
        
        // Sinh muối và băm mật khẩu
        String salt = BCrypt.gensalt(10);
        String hash = BCrypt.hashpw(password, salt);

        System.out.println("----------------------------------------------");
        System.out.println("Mật khẩu: " + password);
        System.out.println("Chuỗi Hash tạo ra:");
        System.out.println(hash);
        System.out.println("Độ dài chuỗi: " + hash.length());
        
        // Kiểm tra lại ngay lập tức
        boolean verify = BCrypt.checkpw(password, hash);
        System.out.println("Tự kiểm tra khớp: " + verify);
        System.out.println("----------------------------------------------");
    }
}