package com.taskboard.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.dto.CommentDTO;
import com.taskboard.backend.service.CommentService;

@RestController
@RequestMapping("/api/tasks")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // API 1: Xem danh sách comment -> ADMIN & MEMBER
    @GetMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MEMBER')")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }

    // API 2: Gửi comment mới -> ADMIN & MEMBER
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MEMBER')")
    public ResponseEntity<?> addComment(@PathVariable Long taskId, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            CommentDTO newComment = commentService.addComment(taskId, content, auth.getName());
            return ResponseEntity.ok(newComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}