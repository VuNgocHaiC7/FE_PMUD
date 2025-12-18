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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.dto.NotificationDTO;
import com.taskboard.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * API: Lấy tất cả notifications chưa đọc
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(auth.getName());
        return ResponseEntity.ok(notifications);
    }

    /**
     * API: Lấy tất cả notifications
     * GET /api/notifications
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<NotificationDTO> notifications = notificationService.getAllNotifications(auth.getName());
        return ResponseEntity.ok(notifications);
    }

    /**
     * API: Đếm số notifications chưa đọc
     * GET /api/notifications/count
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long count = notificationService.getUnreadCount(auth.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * API: Đánh dấu một notification là đã đọc
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Đánh dấu tất cả là đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        notificationService.markAllAsRead(auth.getName());
        return ResponseEntity.ok().build();
    }
}
