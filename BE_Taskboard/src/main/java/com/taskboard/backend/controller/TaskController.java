package com.taskboard.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.dto.TaskDTO;
import com.taskboard.backend.service.TaskService;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // API 1: Tạo Task mới -> CHỈ ADMIN (Giao việc)
    @PostMapping("/tasks")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public ResponseEntity<?> createTask(@RequestBody TaskDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.ok(taskService.createTask(request, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 2: Lấy danh sách Task -> ADMIN & MEMBER (Để làm việc)
    @GetMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MEMBER')")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            return ResponseEntity.ok(taskService.getTasksByProject(projectId, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 3: Cập nhật trạng thái (Kéo thả) -> ADMIN & MEMBER (Để báo cáo tiến độ)
    @PutMapping("/tasks/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MEMBER')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        try {
            return ResponseEntity.ok(taskService.updateTaskStatus(id, newStatus));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // API 4: Cập nhật thông tin Task (Tiêu đề, Mô tả, ...) -> CHỈ ADMIN
    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.ok(taskService.updateTaskInfo(id, request, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}