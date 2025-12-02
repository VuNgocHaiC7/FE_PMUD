package taskboard.ui.project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.TaskDTO;
import taskboard.model.UserDTO;

import java.util.List;

public class TaskDialogController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<UserDTO> cbAssignee;

    private TaskDTO currentTask;
    private Long projectId;
    private ProjectApi projectApi = new ProjectApi();
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Khởi tạo danh sách trạng thái
        cbStatus.setItems(FXCollections.observableArrayList("TODO", "DOING", "DONE"));
        cbStatus.getSelectionModel().select("TODO"); // Mặc định là TODO

        // Cấu hình hiển thị cho ComboBox thành viên
        cbAssignee.setCellFactory(lv -> new ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getFullName() + " (" + user.getRole() + ")");
            }
        });
        cbAssignee.setButtonCell(new ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "-- Chưa giao --" : user.getFullName());
            }
        });
    }

    /**
     * Set dự án và load danh sách thành viên
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
        loadProjectMembers();
    }

    /**
     * Set task để sửa (nếu là mode EDIT)
     */
    public void setTask(TaskDTO task) {
        this.currentTask = task;
        if (task != null) {
            txtTitle.setText(task.getTitle());
            txtDescription.setText(task.getDescription());
            cbStatus.setValue(task.getStatus());
            
            // Tìm và chọn assignee trong ComboBox
            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                for (UserDTO user : cbAssignee.getItems()) {
                    if (user.getFullName().equals(task.getAssignee())) {
                        cbAssignee.setValue(user);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Load danh sách thành viên của dự án
     */
    private void loadProjectMembers() {
        if (projectId != null) {
            List<UserDTO> members = projectApi.getProjectMembers(projectId);
            cbAssignee.setItems(FXCollections.observableArrayList(members));
            System.out.println("DEBUG: Đã load " + members.size() + " thành viên");
        }
    }

    @FXML
    private void handleSave() {
        // Validate
        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tiêu đề task!");
            return;
        }

        // Tạo hoặc cập nhật task
        if (currentTask == null) {
            currentTask = new TaskDTO();
            currentTask.setProjectId(projectId);
        }

        currentTask.setTitle(txtTitle.getText().trim());
        currentTask.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : "");
        currentTask.setStatus(cbStatus.getValue());
        
        // Lấy tên người được giao (nếu có)
        UserDTO selectedUser = cbAssignee.getValue();
        currentTask.setAssignee(selectedUser != null ? selectedUser.getFullName() : "");

        saved = true;
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getter để ProjectBoardController biết task đã được lưu chưa
    public boolean isSaved() {
        return saved;
    }

    public TaskDTO getTask() {
        return currentTask;
    }
}
