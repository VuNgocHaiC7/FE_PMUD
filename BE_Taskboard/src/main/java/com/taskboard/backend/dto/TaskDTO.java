package com.taskboard.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TaskDTO {
    private String title;
    private String description;
    private String priority;
    private LocalDateTime deadline;
    private Long projectId; 
    
    private List<Long> assigneeIds; 

    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public List<Long> getAssigneeIds() { return assigneeIds; }
    public void setAssigneeIds(List<Long> assigneeIds) { this.assigneeIds = assigneeIds; }
}