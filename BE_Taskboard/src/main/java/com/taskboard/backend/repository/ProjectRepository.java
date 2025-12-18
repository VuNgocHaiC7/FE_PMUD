package com.taskboard.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taskboard.backend.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Tìm dự án mà user là PM HOẶC là thành viên
    @Query("SELECT p FROM Project p JOIN p.members m WHERE p.pm.id = :userId OR m.id = :userId")
    List<Project> findByUserId(Long userId);
    
    // Tìm tất cả các dự án mà user là PM
    List<Project> findByPmId(Long pmId);
}