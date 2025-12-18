package com.taskboard.backend.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    // Thông tin về người gây ra notification
    private String actorUsername;
    private String actorFullName;
    
    // Thông tin về task liên quan
    private Long taskId;
    private String taskTitle;
    
    // Constructors
    public NotificationDTO() {}

    public NotificationDTO(Long id, String type, String message, boolean isRead, 
                          LocalDateTime createdAt, String actorUsername, String actorFullName,
                          Long taskId, String taskTitle) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.actorUsername = actorUsername;
        this.actorFullName = actorFullName;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorFullName() { return actorFullName; }
    public void setActorFullName(String actorFullName) { this.actorFullName = actorFullName; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
}
