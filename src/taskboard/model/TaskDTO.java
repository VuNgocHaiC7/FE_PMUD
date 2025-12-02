package taskboard.model;

import javafx.beans.property.*;

public class TaskDTO {
    private final LongProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty status;      // TODO, DOING, DONE
    private final StringProperty assignee;    // Tên người được giao
    private final LongProperty projectId;

    // Constructor đầy đủ
    public TaskDTO(Long id, String title, String description, String status, String assignee, Long projectId) {
        this.id = new SimpleLongProperty(id != null ? id : 0L);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty(status);
        this.assignee = new SimpleStringProperty(assignee);
        this.projectId = new SimpleLongProperty(projectId != null ? projectId : 0L);
    }

    // Constructor rỗng
    public TaskDTO() {
        this(null, "", "", "TODO", "", null);
    }

    // --- PROPERTIES (cho TableView) ---
    public LongProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty statusProperty() { return status; }
    public StringProperty assigneeProperty() { return assignee; }
    public LongProperty projectIdProperty() { return projectId; }

    // --- GETTERS ---
    public Long getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getStatus() { return status.get(); }
    public String getAssignee() { return assignee.get(); }
    public Long getProjectId() { return projectId.get(); }

    // --- SETTERS ---
    public void setId(Long value) { this.id.set(value != null ? value : 0L); }
    public void setTitle(String value) { this.title.set(value); }
    public void setDescription(String value) { this.description.set(value); }
    public void setStatus(String value) { this.status.set(value); }
    public void setAssignee(String value) { this.assignee.set(value); }
    public void setProjectId(Long value) { this.projectId.set(value != null ? value : 0L); }

    // --- TOSTRING ---
    @Override
    public String toString() {
        return getTitle() + (getAssignee() != null && !getAssignee().isEmpty() 
            ? " [" + getAssignee() + "]" 
            : "");
    }
}
