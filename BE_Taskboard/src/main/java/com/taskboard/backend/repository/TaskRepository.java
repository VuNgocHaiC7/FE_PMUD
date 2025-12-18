package com.taskboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskboard.backend.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Tìm task theo ID dự án
    List<Task> findByProjectId(Long projectId);
    
    // Xóa user khỏi tất cả các tasks (xóa trong bảng task_assignees)
    @Modifying
    @Query(value = "DELETE FROM task_assignees WHERE user_id = :userId", nativeQuery = true)
    void removeUserFromAllTasks(@Param("userId") Long userId);
    
    // Tìm tất cả tasks mà user được assign
    @Query("SELECT t FROM Task t JOIN t.assignees a WHERE a.id = :userId")
    List<Task> findTasksByAssigneeId(@Param("userId") Long userId);
}