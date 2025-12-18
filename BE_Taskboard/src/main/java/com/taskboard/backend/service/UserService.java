//kiểm tra trùng username , mã hóa mật khẩu và cập nhật trạng thái khóa
package com.taskboard.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskboard.backend.dto.UserCreateRequest;
import com.taskboard.backend.entity.Project;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.CommentRepository;
import com.taskboard.backend.repository.ProjectRepository;
import com.taskboard.backend.repository.TaskRepository;
import com.taskboard.backend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    // 1. Lấy danh sách user (sắp xếp theo ID)
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }
    
    // 1.1 Tìm kiếm user theo keyword
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    // 2. Tạo user mới
    public User createUser(UserCreateRequest request) {
        // Kiểm tra trùng username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(true); // Mặc định là đang hoạt động
        user.setCreatedAt(LocalDateTime.now()); // Lưu thời gian tạo

        // QUAN TRỌNG: Mã hóa mật khẩu trước khi lưu 
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user); // Lưu vào DB
    }

    // 3. Khóa / Mở khóa user 
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Bảo vệ tài khoản Admin (không phân biệt hoa thường)
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Không thể khóa/mở khóa tài khoản Admin hệ thống!");
        }
        
        // Đảo ngược trạng thái (True -> False, False -> True)
        user.setActive(!user.isActive());
        
        return userRepository.save(user);
    }

    // 4. Cập nhật Role (Thăng chức/Giáng chức)
    public User updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Bảo vệ tài khoản Admin (không phân biệt hoa thường)
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Không thể thay đổi role của tài khoản Admin hệ thống!");
        }

        // Chỉ chấp nhận ADMIN hoặc MEMBER
        if (!"ADMIN".equals(newRole) && !"MEMBER".equals(newRole)) {
             throw new RuntimeException("Role không hợp lệ! Chỉ chấp nhận: ADMIN, MEMBER");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }
    
    // 5. Khôi phục tài khoản admin về role ADMIN (Emergency function)
    public User restoreAdminRole() {
        // Tìm user có username là "admin" (không phân biệt hoa thường)
        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin trong hệ thống!"));
        
        // Set lại role về ADMIN
        admin.setRole("ADMIN");
        admin.setActive(true); // Đảm bảo tài khoản được kích hoạt
        
        return userRepository.save(admin);
    }
    
    // 6. Xóa user
    @Transactional
    public void deleteUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Bảo vệ tài khoản Admin (không phân biệt hoa thường)
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Không thể xóa tài khoản Admin hệ thống!");
        }
        
        // Không cho phép tự xóa chính mình
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Không thể tự xóa chính mình!");
        }
        
        // BƯỚC 1: Xóa tất cả comments của user
        commentRepository.deleteByUserId(userId);
        entityManager.flush();
        entityManager.clear();
        
        // BƯỚC 2: Xóa user khỏi tất cả các tasks (assignees)
        List<com.taskboard.backend.entity.Task> assignedTasks = taskRepository.findTasksByAssigneeId(userId);
        for (com.taskboard.backend.entity.Task task : assignedTasks) {
            task.getAssignees().remove(user);
            taskRepository.save(task);
        }
        entityManager.flush();
        entityManager.clear();
        
        // BƯỚC 3: Xóa user khỏi tất cả các projects mà họ tham gia (members)
        List<Project> allProjects = projectRepository.findAll();
        for (Project project : allProjects) {
            if (project.getMembers().contains(user)) {
                project.removeMember(user);
                projectRepository.save(project);
            }
        }
        entityManager.flush();
        entityManager.clear();
        
        // BƯỚC 4: Xử lý các projects mà user là PM (set pm = null)
        List<Project> pmProjects = projectRepository.findByPmId(userId);
        for (Project project : pmProjects) {
            project.setPm(null); // Hoặc assign cho admin khác
            projectRepository.save(project);
        }
        entityManager.flush();
        entityManager.clear();
        
        // BƯỚC 5: Refresh user entity và xóa
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        userRepository.delete(userToDelete);
        entityManager.flush();
    }
}