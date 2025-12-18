package com.taskboard.backend.controller;

import java.util.List;
import java.util.Map; // Nhớ import Map

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.dto.UserCreateRequest;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // API 1: Lấy danh sách user (hỗ trợ tìm kiếm) - TẤT CẢ user đều có thể xem
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword) {
        System.out.println(">>> GET /api/users được gọi - keyword: " + keyword);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(userService.searchUsers(keyword));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // API 2: Thêm user mới
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        try {
            User newUser = userService.createUser(request);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 3: Khóa/Mở khóa user
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            User updatedUser = userService.toggleUserStatus(id);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 4: Thay đổi Role (MỚI THÊM)
    // PUT /api/users/{id}/role
    // Body: { "role": "ADMIN" }
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newRole = payload.get("role");
        try {
            User updatedUser = userService.updateUserRole(id, newRole);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // API 5: Xóa user - CHỈ ADMIN có username là "admin" mới được phép
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // Lấy thông tin user hiện tại
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            
            System.out.println(">>> DELETE USER REQUEST:");
            System.out.println(">>> Current username from token: " + currentUsername);
            System.out.println(">>> Trying to delete user ID: " + id);
            
            // Kiểm tra xem có phải admin không (không phân biệt hoa thường)
            if (!"admin".equalsIgnoreCase(currentUsername)) {
                System.out.println(">>> DENIED: User '" + currentUsername + "' is not admin");
                return ResponseEntity.status(403).body("Chỉ tài khoản admin mới có quyền xóa người dùng!");
            }
            
            System.out.println(">>> ALLOWED: Proceeding to delete user");
            userService.deleteUser(id, currentUsername);
            return ResponseEntity.ok(Map.of("message", "Đã xóa người dùng thành công!"));
        } catch (RuntimeException e) {
            System.err.println(">>> ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}