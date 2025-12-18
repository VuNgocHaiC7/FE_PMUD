package com.taskboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taskboard.backend.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy tất cả notifications của user, sắp xếp mới nhất trước
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Lấy notifications chưa đọc của user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Đếm số notifications chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);
    
    // Đánh dấu tất cả là đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(Long userId);
    
    // Xóa notifications của user
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}
