package com.taskboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taskboard.backend.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Lấy list comment của task, sắp xếp theo thời gian tạo tăng dần (ASC)
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    
    // Xóa tất cả comments của user
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}