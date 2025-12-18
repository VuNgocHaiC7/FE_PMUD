package com.taskboard.backend.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskboard.backend.dto.TaskDTO;
import com.taskboard.backend.entity.Project;
import com.taskboard.backend.entity.Task;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.ProjectRepository;
import com.taskboard.backend.repository.TaskRepository;
import com.taskboard.backend.repository.UserRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Tạo Task (Đã sửa để nhận nhiều người)
    public Task createTask(TaskDTO request, String currentUsername) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setProject(project);

        // Xử lý danh sách người được giao
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> users = userRepository.findAllById(request.getAssigneeIds());
            task.setAssignees(new HashSet<>(users));
        }

        return taskRepository.save(task);
    }

    // 2. Lấy danh sách Task (Code logic này giữ nguyên, nhưng Task trả về sẽ tự có list assignees)
    public List<Task> getTasksByProject(Long projectId, String currentUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isMember = project.getMembers().contains(currentUser);
        
        // Cho phép xem nếu là Admin hoặc Member dự án
        if (!isAdmin && !isMember) {
             throw new RuntimeException("Bạn không có quyền xem Task của dự án này!");
        }

        return taskRepository.findByProjectId(projectId);
    }

    // 3. Cập nhật trạng thái Task (Giữ nguyên)
    public Task updateTaskStatus(Long taskId, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setStatus(newStatus); 
        return taskRepository.save(task);
    }

    // 4. Sửa thông tin Task (Đã sửa để update danh sách người)
    public Task updateTaskInfo(Long taskId, TaskDTO request, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        
        // Cập nhật người được giao
        if (request.getAssigneeIds() != null) {
            List<User> users = userRepository.findAllById(request.getAssigneeIds());
            task.setAssignees(new HashSet<>(users));
        }

        return taskRepository.save(task);
    }

    // 5. Xóa Task (Giữ nguyên)
    public void deleteTask(Long taskId, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }
    
    // 6. Xem chi tiết (Giữ nguyên)
    public Task getTaskDetail(Long taskId, String currentUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        getTasksByProject(task.getProject().getId(), currentUsername);
        return task;
    }
    
    // 7. Lấy tất cả tasks của một project (không cần authentication - dùng cho dashboard)
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}