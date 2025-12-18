package com.taskboard.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users") // Đặt tên bảng trong DB là "users"
@Data // Lombok tự sinh getter, setter, toString...
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    // Lưu role dạng String đơn giản trước (ADMIN, PM, MEMBER)
    private String role; 

    private boolean isActive = true; // Mặc định là tài khoản đang mở

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLogin;
}