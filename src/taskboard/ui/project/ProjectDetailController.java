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
    @FXML private TextArea txtDesc;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private ComboBox<String> cbStatus;
    
    @FXML private Tab tabMembers;
    @FXML private ComboBox<UserDTO> cbAllUsers;
    @FXML private ListView<UserDTO> lvMembers;

    private ProjectDTO currentProject;
    private ProjectApi projectApi = new ProjectApi();

    @FXML
    public void initialize() {
        System.out.println("DEBUG: ProjectDetailController.initialize() được gọi");
        
        // 1. Khởi tạo danh sách trạng thái
        cbStatus.setItems(FXCollections.observableArrayList("PLANNING", "IN_PROGRESS", "CLOSED"));

        // 2. Tạo Menu chuột phải cho Danh sách thành viên để XÓA
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Xóa thành viên này");
        deleteItem.setOnAction(event -> handleRemoveMember());
        contextMenu.getItems().add(deleteItem);
        
        // Gán menu này vào ListView
        lvMembers.setContextMenu(contextMenu);
        
        // 3. Cấu hình hiển thị cho ComboBox và ListView
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
            List<UserDTO> allUsers = projectApi.getAllSystemUsers();
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
            cbStatus.getSelectionModel().select("PLANNING");
        } else {
            // --- MODE SỬA (EDIT) / ĐÓNG ---
            System.out.println("DEBUG: Đang load thông tin project vào form...");
            txtName.setText(project.getName());
            txtDesc.setText(project.getDescription());
            dpStart.setValue(project.getStartDate());
            dpEnd.setValue(project.getEndDate());
            cbStatus.setValue(project.getStatus()); // Load trạng thái hiện tại (có thể là CLOSED)
            
            System.out.println("DEBUG: Đang load danh sách thành viên...");
            loadMembers();
            System.out.println("DEBUG: Hoàn tất load project detail");
        }
    }

    @FXML
    private void handleSave() {
        // Logic này xử lý cả TẠO MỚI (Create) và SỬA/ĐÓNG (Update)
        
        if (txtName.getText().isEmpty() || cbStatus.getValue() == null) {
            showAlert("Lỗi", "Vui lòng nhập tên và trạng thái!");
            return;
        }

        if (currentProject == null) currentProject = new ProjectDTO();
        
        currentProject.setName(txtName.getText());
        currentProject.setDescription(txtDesc.getText());
        currentProject.setStartDate(dpStart.getValue());
        currentProject.setEndDate(dpEnd.getValue());
        
        // >>> CHỨC NĂNG ĐÓNG PROJECT: Người dùng chỉ cần chọn status là "CLOSED" ở giao diện và bấm Lưu
        currentProject.setStatus(cbStatus.getValue()); 

        if (projectApi.saveProject(currentProject)) {
            showAlert("Thành công", "Đã lưu dự án!");
            closeWindow();
        } else {
            showAlert("Thất bại", "Lỗi khi lưu dự án.");
        }
    }

    @FXML
    private void handleAddMember() {
        UserDTO selectedUser = cbAllUsers.getValue();
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn thành viên cần thêm!");
            return;
        }
        
        // Gọi API thêm
        boolean success = projectApi.addMember(currentProject.getId(), selectedUser);
        if (success) {
            loadMembers(); // Reload lại list
            showAlert("Thành công", "Đã thêm: " + selectedUser.getFullName());
        }
    }

    // >>> CHỨC NĂNG MỚI: XÓA THÀNH VIÊN
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
            // Gọi API xóa
            boolean success = projectApi.removeMember(currentProject.getId(), selectedUser.getId());
            if (success) {
                loadMembers(); // Reload lại list
            }
        }
    }

    private void loadMembers() {
        if (currentProject != null && currentProject.getId() != null) {
            List<UserDTO> members = projectApi.getProjectMembers(currentProject.getId());
            lvMembers.setItems(FXCollections.observableArrayList(members));
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
}