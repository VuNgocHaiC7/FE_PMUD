// bảng lưu thông tin dự án.
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
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private String status; // "ACTIVE", "COMPLETED", "CLOSED"

    // Người quản lý dự án (PM)
    @ManyToOne
    @JoinColumn(name = "pm_id")
    private User pm;

    // Danh sách thành viên (Quan hệ nhiều - nhiều)
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // --- Constructor, Getter, Setter ---
    
    public void addMember(User user) {
        this.members.add(user);
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    // Để tiết kiệm dòng code hiển thị ở đây mình rút gọn, bạn nhớ Generate đủ nhé!
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getPm() { return pm; }
    public void setPm(User pm) { this.pm = pm; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
}