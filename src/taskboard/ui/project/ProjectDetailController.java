package taskboard.ui.project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;
import taskboard.model.UserDTO;

import java.util.List;
import java.util.Optional;

public class ProjectDetailController {
    @FXML private TextField txtName;
    @FXML private TextField txtProjectCode;
    @FXML private TextArea txtDesc;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblStatus;
    
    @FXML private Tab tabMembers;
    @FXML private ComboBox<UserDTO> cbAllUsers;
    @FXML private ListView<UserDTO> lvMembers;

    private ProjectDTO currentProject;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: ProjectDetailController.initialize() được gọi");
        
        // Khởi tạo danh sách trạng thái hiển thị tiếng Việt
        cbStatus.setItems(FXCollections.observableArrayList("ĐANG HOẠT ĐỘNG", "HOÀN THÀNH", "ĐÃ ĐÓNG"));

        // Tạo Menu chuột phải cho Danh sách thành viên để XÓA
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Xóa thành viên này");
        deleteItem.setOnAction(event -> handleRemoveMember());
        contextMenu.getItems().add(deleteItem);
        
        // Gán menu này vào ListView
        lvMembers.setContextMenu(contextMenu);
        
        // Cấu hình hiển thị cho ComboBox và ListView
        cbAllUsers.setCellFactory(lv -> new javafx.scene.control.ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getFullName() + " (" + user.getUsername() + ")");
            }
        });
        cbAllUsers.setButtonCell(new javafx.scene.control.ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getFullName() + " (" + user.getUsername() + ")");
            }
        });
        
        lvMembers.setCellFactory(lv -> new javafx.scene.control.ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getFullName() + " - " + user.getRole());
            }
        });
    }

    public void setProject(ProjectDTO project) {
        this.currentProject = project;
        
        try {
            System.out.println("DEBUG: setProject được gọi với project: " + (project != null ? project.getName() : "null"));
            
            // Load danh sách user hệ thống để chọn thêm
            System.out.println("DEBUG: Đang tải danh sách users...");
            List<UserDTO> allUsers = ProjectApi.getAllSystemUsers();
            System.out.println("DEBUG: Đã tải " + (allUsers != null ? allUsers.size() : 0) + " users");
            cbAllUsers.setItems(FXCollections.observableArrayList(allUsers));
        } catch (Exception e) {
            System.err.println("ERROR: Lỗi khi tải danh sách users: " + e.getMessage());
            e.printStackTrace();
            cbAllUsers.setItems(FXCollections.observableArrayList());
        }

        if (project == null) {
            // --- MODE TẠO MỚI ---
            tabMembers.setDisable(true); // Tạo xong mới được thêm thành viên
            
            // Ẩn trường Trạng thái khi tạo mới
            if (lblStatus != null) {
                lblStatus.setVisible(false);
                lblStatus.setManaged(false);
            }
            if (cbStatus != null) {
                cbStatus.setVisible(false);
                cbStatus.setManaged(false);
                cbStatus.getSelectionModel().select("ĐANG HOẠT ĐỘNG"); // Default status (ẩn nhưng vẫn set giá trị)
            }
        } else {
            // --- MODE SỬA (EDIT) ---
            System.out.println("DEBUG: Đang load thông tin project vào form...");
            txtName.setText(project.getName());
            txtProjectCode.setText(project.getProjectCode());
            txtDesc.setText(project.getDescription());
            dpStart.setValue(project.getStartDate());
            dpEnd.setValue(project.getEndDate());
            
            // Hiển thị trường Trạng thái khi sửa
            if (lblStatus != null) {
                lblStatus.setVisible(true);
                lblStatus.setManaged(true);
            }
            if (cbStatus != null) {
                cbStatus.setVisible(true);
                cbStatus.setManaged(true);
                cbStatus.setValue(convertStatusToVietnamese(project.getStatus())); // Load trạng thái hiện tại
            }
            
            System.out.println("DEBUG: Đang load danh sách thành viên...");
            loadMembers();
            System.out.println("DEBUG: Hoàn tất load project detail");
        }
    }

    @FXML
    private void handleSave() {
        // Logic này xử lý cả TẠO MỚI (Create) và SỬA (Update)
        
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

        if (currentProject == null) currentProject = new ProjectDTO();
        
        currentProject.setName(txtName.getText());
        currentProject.setProjectCode(txtProjectCode.getText());
        currentProject.setDescription(txtDesc.getText());
        currentProject.setStartDate(dpStart.getValue());
        currentProject.setEndDate(dpEnd.getValue());
        
        // Nếu đang ở chế độ tạo mới (cbStatus ẩn), set mặc định là ACTIVE
        if (cbStatus.isVisible()) {
            currentProject.setStatus(convertStatusToEnglish(cbStatus.getValue()));
        } else {
            currentProject.setStatus("ACTIVE"); // Mặc định ĐANG HOẠT ĐỘNG khi tạo mới
        } 

        try {
            if (currentProject.getId() == null) {
                // GỌI API: POST /api/projects
                ProjectApi.createProject(currentProject);
            } else {
                // GỌI API: PUT /api/projects/{id} (Chỉ Admin)
                ProjectApi.updateProject(currentProject);
            }
            showAlert("Thành công", "Đã lưu dự án!");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            
            // Kiểm tra loại lỗi để hiển thị thông báo phù hợp
            if (errorMessage != null && errorMessage.contains("Mã dự án")) {
                // Lỗi trùng mã dự án
                showAlert("Lỗi - Trùng mã dự án", errorMessage);
            } else if (errorMessage != null && errorMessage.contains("already exists")) {
                // Lỗi trùng
                showAlert("Lỗi - Trùng mã dự án", "Mã dự án '" + txtProjectCode.getText() + "' đã tồn tại. Vui lòng chọn mã khác!");
            } else {
                // Lỗi khác
                showAlert("Thất bại", "Lỗi khi lưu dự án: " + errorMessage);
            }
        }
    }

    @FXML
    private void handleAddMember() {
        UserDTO selectedUser = cbAllUsers.getValue();
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn thành viên cần thêm!");
            return;
        }
        
        try {
            // GỌI API: POST /api/projects/{id}/members
            // Thêm: Chọn user từ dropdown -> Nhấn Add
            ProjectApi.addMember(currentProject.getId(), selectedUser.getId());
            
            // Reload lại list để hiển thị member mới
            loadMembers();
            showAlert("Thành công", "Đã thêm: " + selectedUser.getFullName());
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể thêm thành viên: " + e.getMessage());
        }
    }

    // CHỨC NĂNG XÓA THÀNH VIÊN
    private void handleRemoveMember() {
        UserDTO selectedUser = lvMembers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        // Hỏi xác nhận trước khi xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa " + selectedUser.getFullName() + " khỏi dự án?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // GỌI API: DELETE /api/projects/{id}/members/{userId}
                // Xóa: Chọn member -> Nhấn Remove
                ProjectApi.removeMember(currentProject.getId(), selectedUser.getId());
                
                // Reload lại list
                loadMembers();
            } catch (Exception e) {
                showAlert("Lỗi", "Không thể xóa thành viên: " + e.getMessage());
            }
        }
    }

    private void loadMembers() {
        if (currentProject != null && currentProject.getId() != null) {
            try {
                List<UserDTO> members = ProjectApi.getProjectMembers(currentProject.getId());
                lvMembers.setItems(FXCollections.observableArrayList(members));
            } catch (Exception e) {
                System.err.println("Lỗi load members: " + e.getMessage());
                lvMembers.setItems(FXCollections.observableArrayList());
            }
        }
    }
    
    @FXML
    private void handleCloseWindow() {
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
    
    // Hàm chuyển đổi tiếng Việt
    private String convertStatusToVietnamese(String englishStatus) {
        if (englishStatus == null) return "ĐANG HOẠT ĐỘNG";
        switch (englishStatus) {
            case "ACTIVE":
                return "ĐANG HOẠT ĐỘNG";
            case "COMPLETED":
                return "HOÀN THÀNH";
            case "CLOSED":
                return "ĐÃ ĐÓNG";
            default:
                return englishStatus;
        }
    }
    
    // Hàm chuyển đổi tiếng Anh
    private String convertStatusToEnglish(String vietnameseStatus) {
        if (vietnameseStatus == null) return "ACTIVE";
        switch (vietnameseStatus) {
            case "ĐANG HOẠT ĐỘNG":
                return "ACTIVE";
            case "HOÀN THÀNH":
                return "COMPLETED";
            case "ĐÃ ĐÓNG":
                return "CLOSED";
            default:
                return vietnameseStatus;
        }
    }
}