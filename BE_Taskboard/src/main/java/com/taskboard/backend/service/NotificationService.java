package com.taskboard.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskboard.backend.dto.NotificationDTO;
import com.taskboard.backend.entity.Notification;
import com.taskboard.backend.entity.Task;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.NotificationRepository;
import com.taskboard.backend.repository.TaskRepository;
import com.taskboard.backend.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    /**
     * Tạo notification cho một user
     */
    public Notification createNotification(Long userId, String type, String message, 
                                          Long taskId, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        
        if (taskId != null) {
            Task task = taskRepository.findById(taskId).orElse(null);
            notification.setTask(task);
        }
        
        if (actorId != null) {
            User actor = userRepository.findById(actorId).orElse(null);
            notification.setActor(actor);
        }
        
        return notificationRepository.save(notification);
    }

    /**
     * Lấy tất cả notifications chưa đọc của user
     */
    public List<NotificationDTO> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả notifications của user
     */
    public List<NotificationDTO> getAllNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
        
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số notifications chưa đọc
     */
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    /**
     * Đánh dấu một notification là đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        notificationRepository.markAllAsRead(user.getId());
    }

    /**
     * Convert Entity sang DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        String actorUsername = notification.getActor() != null ? 
                notification.getActor().getUsername() : null;
        String actorFullName = notification.getActor() != null ? 
                notification.getActor().getFullName() : null;
        Long taskId = notification.getTask() != null ? 
                notification.getTask().getId() : null;
        String taskTitle = notification.getTask() != null ? 
                notification.getTask().getTitle() : null;
        
        return new NotificationDTO(
            notification.getId(),
            notification.getType(),
            notification.getMessage(),
            notification.isRead(),
            notification.getCreatedAt(),
            actorUsername,
            actorFullName,
            taskId,
            taskTitle
        );
    }
}
