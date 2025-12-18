package com.taskboard.backend.controller;

import java.util.List;
import java.util.Map;

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

import com.taskboard.backend.dto.ProjectDTO;
import com.taskboard.backend.entity.Project;
import com.taskboard.backend.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // API 1: Xem danh sách dự án
    // (Admin thấy hết, Member thấy dự án mình tham gia) -> Giữ nguyên logic Service
    @GetMapping
    public ResponseEntity<List<Project>> getProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(projectService.getProjectsForUser(auth.getName()));
    }

    // API 2: Tạo dự án mới -> CHỈ ADMIN
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')") 
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(projectService.createProject(request, auth.getName()));
    }

    // API 3: Thêm thành viên -> CHỈ ADMIN
    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addMember(@PathVariable Long projectId, @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        return ResponseEntity.ok(projectService.addMember(projectId, userId));
    }

    // API 4: Cập nhật dự án -> CHỈ ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO request) {
        try {
            Project updatedProject = projectService.updateProject(id, request);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 5: Xóa thành viên -> CHỈ ADMIN
    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        try {
            projectService.removeMember(projectId, userId);
            return ResponseEntity.ok("Removed user " + userId + " from project " + projectId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 6: Lấy danh sách thành viên của dự án
    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
        try {
            return ResponseEntity.ok(projectService.getProjectMembers(projectId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 7: Xóa dự án -> CHỈ ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok("Deleted project " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}