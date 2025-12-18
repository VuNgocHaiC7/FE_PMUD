package com.taskboard.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.config.JwtUtils;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Dùng để mã hóa

    @Autowired
    private JwtUtils jwtUtils; // Dùng để tạo token

    // API Đăng ký
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // VALIDATION 1: Kiểm tra username có trùng không
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại! Vui lòng chọn username khác.");
        }
        
        // VALIDATION 2: Chặn đăng ký username "admin" (không phân biệt hoa thường)
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Không thể đăng ký với username 'admin'! Đây là tài khoản hệ thống.");
        }
        
        // Mã hóa mật khẩu trước khi lưu vào DB
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Thiết lập các giá trị mặc định
        user.setRole("MEMBER"); // Mặc định là MEMBER
        user.setActive(true);   // Mặc định đang hoạt động
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    // API Đăng nhập
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // KIỂM TRA: Nếu tài khoản bị khóa thì không cho đăng nhập
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            
            user.setLastLogin(LocalDateTime.now()); // Ghi lại giờ hiện tại
            userRepository.save(user);              // Lưu ngay vào Database
            // ----------------------------------------

            // ... Đoạn code tạo token cũ giữ nguyên ...
            String token = jwtUtils.generateToken(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            // ...
            return response;
        } else {
            throw new RuntimeException("Wrong password");
        }
    }
}