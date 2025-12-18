package com.taskboard.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboard.backend.entity.Project;
import com.taskboard.backend.entity.Task;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.service.ProjectService;
import com.taskboard.backend.service.TaskService;
import com.taskboard.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> stats = new HashMap<>();
            
            // Lấy danh sách projects của user (Admin thấy hết, Member thấy project mình tham gia)
            List<Project> projects = projectService.getProjectsForUser(username);
            stats.put("totalProjects", projects.size());
            
            // Lấy tất cả tasks thuộc các projects này
            List<Task> allTasks = projects.stream()
                    .flatMap(p -> taskService.getTasksByProjectId(p.getId()).stream())
                    .collect(Collectors.toList());
            
            stats.put("totalTasks", allTasks.size());
            
            // Thống kê tasks theo status
            Map<String, Long> tasksByStatus = allTasks.stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
            stats.put("tasksByStatus", tasksByStatus);
            
            // Thống kê tasks theo project
            Map<String, Long> tasksByProject = allTasks.stream()
                    .collect(Collectors.groupingBy(
                        task -> task.getProject().getName(), 
                        Collectors.counting()
                    ));
            stats.put("tasksByProject", tasksByProject);
            
            // Thống kê projects theo status
            Map<String, Long> projectsByStatus = projects.stream()
                    .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));
            stats.put("projectsByStatus", projectsByStatus);
            
            // Thống kê tasks theo priority (nếu có)
            Map<String, Long> tasksByPriority = allTasks.stream()
                    .filter(task -> task.getPriority() != null)
                    .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            stats.put("tasksByPriority", tasksByPriority);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error getting dashboard stats: " + e.getMessage());
        }
    }
}
