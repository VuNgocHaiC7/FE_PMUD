package com.taskboard.backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    
    private String status; // "TODO", "IN_PROGRESS", "DONE"
    private String priority; // "LOW", "MEDIUM", "HIGH"
    
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    
    // Task thuộc về dự án nào
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    // --- SỬA ĐỔI Ở ĐÂY: Thay assignee đơn lẻ bằng danh sách assignees ---
    @ManyToMany
    @JoinTable(
        name = "task_assignees", // Tên bảng phụ sẽ tự tạo trong DB
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "TODO"; 
        if (this.priority == null) this.priority = "MEDIUM";
    }

    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    // Getter & Setter mới cho assignees
    public Set<User> getAssignees() { return assignees; }
    public void setAssignees(Set<User> assignees) { this.assignees = assignees; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}