package com.taskboard.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskboard.backend.dto.CommentDTO;
import com.taskboard.backend.entity.Comment;
import com.taskboard.backend.entity.Task;
import com.taskboard.backend.entity.User;
import com.taskboard.backend.repository.CommentRepository;
import com.taskboard.backend.repository.TaskRepository;
import com.taskboard.backend.repository.UserRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    // 1. Xem danh sách comment
    public List<CommentDTO> getCommentsByTask(Long taskId) {
        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. Thêm comment mới
    public CommentDTO addComment(Long taskId, String content, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // CHECK QUYỀN: Phải là Admin hoặc thành viên dự án mới được comment
        boolean isAdmin = "ADMIN".equals(user.getRole());
        boolean isMember = task.getProject().getMembers().contains(user);
        boolean isPM = task.getProject().getPm().equals(user);

        if (!isAdmin && !isMember && !isPM) {
            throw new RuntimeException("Bạn không phải thành viên dự án này, không được bình luận!");
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        
        // TẠO NOTIFICATION cho các thành viên khác trong dự án
        createNotificationsForProjectMembers(task, user, content);
        
        return convertToDTO(savedComment);
    }
    
    /**
     * Tạo notifications cho các members trong project (trừ người comment)
     */
    private void createNotificationsForProjectMembers(Task task, User commenter, String content) {
        try {
            Set<Long> notifiedUserIds = new HashSet<>(); // Tránh gửi trùng
            
            // Tạo message
            String message = commenter.getFullName() + " đã bình luận: \"" + 
                           (content.length() > 50 ? content.substring(0, 50) + "..." : content) + "\"";
            
            // 1. Gửi cho tất cả members trong project (trừ người comment)
            List<User> members = new java.util.ArrayList<>(task.getProject().getMembers());
            for (User member : members) {
                if (!member.getId().equals(commenter.getId())) {
                    notificationService.createNotification(
                        member.getId(),
                        "COMMENT",
                        message,
                        task.getId(),
                        commenter.getId()
                    );
                    notifiedUserIds.add(member.getId());
                }
            }
            
            // 2. Gửi cho PM (nếu chưa được gửi)
            User pm = task.getProject().getPm();
            if (pm != null && !pm.getId().equals(commenter.getId()) && !notifiedUserIds.contains(pm.getId())) {
                notificationService.createNotification(
                    pm.getId(),
                    "COMMENT",
                    message,
                    task.getId(),
                    commenter.getId()
                );
                notifiedUserIds.add(pm.getId());
            }
            
            // 3. Gửi cho tất cả người đã comment trong task này (trừ người vừa comment)
            List<Comment> previousComments = commentRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
            for (Comment prevComment : previousComments) {
                User prevCommenter = prevComment.getUser();
                if (!prevCommenter.getId().equals(commenter.getId()) && !notifiedUserIds.contains(prevCommenter.getId())) {
                    notificationService.createNotification(
                        prevCommenter.getId(),
                        "COMMENT",
                        message,
                        task.getId(),
                        commenter.getId()
                    );
                    notifiedUserIds.add(prevCommenter.getId());
                }
            }
            
            System.out.println("✓ Đã gửi " + notifiedUserIds.size() + " thông báo cho các users liên quan");
            
        } catch (Exception e) {
            // Log error nhưng không throw exception (để comment vẫn được lưu)
            System.err.println("Lỗi tạo notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper: Convert Comment Entity sang CommentDTO
    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(
            comment.getId(),
            comment.getTask().getId(),
            comment.getUser().getUsername(),
            comment.getContent(),
            comment.getCreatedAt()
        );
    }
}