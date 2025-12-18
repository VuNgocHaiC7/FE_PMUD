package taskboard.model;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private Long taskId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public CommentDTO(Long id, Long taskId, String username, String content, LocalDateTime createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return username + ": " + content;
    }
}
