package com.taskboard.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // COMMENT, TASK_ASSIGNED, etc.

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime createdAt;

    // User nhận notification
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Link đến task liên quan (nếu có)
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    // User gây ra notification (người comment, assign, etc.)
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
}
