package taskboard.model;

import java.time.LocalDate;

public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PLANNING, IN_PROGRESS, CLOSED
    private String pmName;
    private Long pmId;

    public ProjectDTO() {}

    public ProjectDTO(Long id, String name, String description, LocalDate startDate, LocalDate endDate, String status, String pmName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.pmName = pmName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPmName() { return pmName; }
    public void setPmName(String pmName) { this.pmName = pmName; }
    public Long getPmId() { return pmId; }
    public void setPmId(Long pmId) { this.pmId = pmId; }
    
    @Override
    public String toString() { return name; }
}