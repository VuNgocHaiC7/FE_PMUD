package taskboard.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDTO {
    private final LongProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty status;      // TODO, DOING, DONE
    private final StringProperty assignee;    // Tên người được giao (deprecated - để tương thích)
    private final LongProperty assigneeId;    // ID người được giao (deprecated - để tương thích)
    private final LongProperty projectId;
    
    // Mới: Hỗ trợ nhiều người được gán
    private List<Long> assigneeIds;           // Danh sách ID người được gán
    private List<String> assigneeNames;       // Danh sách tên người được gán

    // Constructor đầy đủ
    public TaskDTO(Long id, String title, String description, String status, String assignee, Long assigneeId, Long projectId) {
        this.id = new SimpleLongProperty(id != null ? id : 0L);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty(status);
        this.assignee = new SimpleStringProperty(assignee);
        this.assigneeId = new SimpleLongProperty(assigneeId != null ? assigneeId : 0L);
        this.projectId = new SimpleLongProperty(projectId != null ? projectId : 0L);
        this.assigneeIds = new ArrayList<>();
        this.assigneeNames = new ArrayList<>();
    }

    // Constructor cũ (để tương thích ngược)
    public TaskDTO(Long id, String title, String description, String status, String assignee, Long projectId) {
        this(id, title, description, status, assignee, null, projectId);
    }

    // Constructor rỗng
    public TaskDTO() {
        this(null, "", "", "TODO", "", null, null);
    }

    // --- PROPERTIES (cho TableView) ---
    public LongProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty statusProperty() { return status; }
    public StringProperty assigneeProperty() { return assignee; }
    public LongProperty assigneeIdProperty() { return assigneeId; }
    public LongProperty projectIdProperty() { return projectId; }

    // --- GETTERS ---
    public Long getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getStatus() { return status.get(); }
    public String getAssignee() { return assignee.get(); }
    public String getAssigneeName() { return assignee.get(); } // Alias for compatibility
    public Long getAssigneeId() { return assigneeId.get(); }
    public Long getProjectId() { return projectId.get(); }
    
    // Getters cho danh sách nhiều assignees
    public List<Long> getAssigneeIds() { return assigneeIds; }
    public List<String> getAssigneeNames() { return assigneeNames; }

    // --- SETTERS ---
    public void setId(Long value) { this.id.set(value != null ? value : 0L); }
    public void setTitle(String value) { this.title.set(value); }
    public void setDescription(String value) { this.description.set(value); }
    public void setStatus(String value) { this.status.set(value); }
    public void setAssignee(String value) { this.assignee.set(value); }
    public void setAssigneeId(Long value) { this.assigneeId.set(value != null ? value : 0L); }
    public void setProjectId(Long value) { this.projectId.set(value != null ? value : 0L); }
    
    // Setters mới cho danh sách nhiều assignees
    public void setAssigneeIds(List<Long> value) { this.assigneeIds = value != null ? value : new ArrayList<>(); }
    public void setAssigneeNames(List<String> value) { this.assigneeNames = value != null ? value : new ArrayList<>(); }

    // --- TOSTRING ---
    @Override
    public String toString() {
        String assigneeInfo = "";
        if (assigneeNames != null && !assigneeNames.isEmpty()) {
            assigneeInfo = " [" + String.join(", ", assigneeNames) + "]";
        } else if (getAssignee() != null && !getAssignee().isEmpty()) {
            assigneeInfo = " [" + getAssignee() + "]";
        }
        return getTitle() + assigneeInfo;
    }
}
