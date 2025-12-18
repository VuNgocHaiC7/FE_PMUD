package com.taskboard.backend.dto;

import java.time.LocalDateTime;

public class ProjectDTO {
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // ACTIVE, COMPLETED, CLOSED
    private Long pmId; // ID của người làm PM (optional, nếu ko gửi thì lấy người tạo)

    // Getter & Setter
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
    public Long getPmId() { return pmId; }
    public void setPmId(Long pmId) { this.pmId = pmId; }
}