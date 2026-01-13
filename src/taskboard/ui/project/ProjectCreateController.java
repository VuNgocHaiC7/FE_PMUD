package taskboard.ui.project;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;

public class ProjectCreateController {
    @FXML private TextField txtName;
    @FXML private TextField txtProjectCode;
    @FXML private TextArea txtDesc;
    @FXML private DatePicker dpStart, dpEnd;

    private boolean isCreated = false;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: ProjectCreateController.initialize() được gọi");
    }

    @FXML
    private void handleCreate() {
        // Validate
        if (txtName.getText().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên dự án!");
            return;
        }
        
        if (txtProjectCode.getText().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã dự án!");
            return;
        }
        
        // Validate ngày bắt đầu và ngày kết thúc
        if (dpStart.getValue() != null && dpEnd.getValue() != null) {
            if (dpStart.getValue().isAfter(dpEnd.getValue())) {
                showAlert("Lỗi", "Ngày bắt đầu không được sau ngày kết thúc!\nVui lòng kiểm tra lại thông tin.");
                return;
            }
        }

        ProjectDTO newProject = new ProjectDTO();
        newProject.setName(txtName.getText());
        newProject.setProjectCode(txtProjectCode.getText());
        newProject.setDescription(txtDesc.getText());
        newProject.setStartDate(dpStart.getValue());
        newProject.setEndDate(dpEnd.getValue());
        newProject.setStatus("ACTIVE"); // Mặc định là ĐANG HOẠT ĐỘNG

        try {
            // GỌI API: POST /api/projects
            ProjectApi.createProject(newProject);
            showAlert("Thành công", "Đã tạo dự án mới!");
            isCreated = true;
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            
            // Kiểm tra loại lỗi để hiển thị thông báo phù hợp
            if (errorMessage != null && errorMessage.contains("Mã dự án")) {
                // Lỗi trùng mã dự án
                showAlert("Lỗi - Trùng mã dự án", errorMessage);
            } else if (errorMessage != null && errorMessage.contains("already exists")) {
                // Lỗi trùng (tiếng Anh)
                showAlert("Lỗi - Trùng mã dự án", "Mã dự án '" + txtProjectCode.getText() + "' đã tồn tại. Vui lòng chọn mã khác!");
            } else {
                // Lỗi khác
                showAlert("Thất bại", "Lỗi khi tạo dự án: " + errorMessage);
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public boolean isCreated() {
        return isCreated;
    }
}
