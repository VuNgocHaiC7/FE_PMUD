package com.taskboard.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskboard.backend.dto.ProjectDTO;
import com.taskboard.backend.entity.Project;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.ProjectRepository;
import com.taskboard.backend.repository.UserRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Lấy danh sách dự án (Phân quyền: Admin thấy hết, User thấy cái mình tham gia)
    public List<Project> getProjectsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ADMIN".equals(user.getRole())) {
            return projectRepository.findAll(); // Admin thấy hết
        } else {
            return projectRepository.findByUserId(user.getId()); // Member chỉ thấy project liên quan
        }
    }

    // 2. Tạo dự án mới
    public Project createProject(ProjectDTO request, String creatorUsername) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus("ACTIVE");

        // Tìm người tạo để gán làm PM (mặc định)
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setPm(creator);
        project.addMember(creator); // PM cũng là thành viên

        return projectRepository.save(project);
    }

    // 3. Thêm thành viên vào dự án
    public Project addMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to add not found"));

        project.addMember(user);
        return projectRepository.save(project);
    }

    //4. Cập nhật thông tin dự án
    public Project updateProject(Long projectId, ProjectDTO request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Cập nhật các trường thông tin
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        
        // Cập nhật trạng thái nếu có
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        return projectRepository.save(project);
    }

    // 5. Xóa thành viên khỏi dự án
    public void removeMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra xem user có trong dự án không
        if (project.getMembers().contains(user)) {
            project.removeMember(user); // Hàm này đã có trong Entity Project lúc trước
            projectRepository.save(project);
        } else {
            throw new RuntimeException("User is not a member of this project");
        }
    }

    // 6. Lấy danh sách thành viên của dự án
    public List<User> getProjectMembers(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getMembers().stream().toList();
    }

    // 7. Xóa dự án
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        projectRepository.delete(project);
    }
}