package taskboard.ui.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import taskboard.api.UserApi;
import taskboard.model.UserDTO;

import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML private TextField txtSearch;
    @FXML private TableView<UserDTO> tableUsers;
    
    @FXML private TableColumn<UserDTO, Number> colId;
    @FXML private TableColumn<UserDTO, String> colUsername;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colRole;
    @FXML private TableColumn<UserDTO, String> colStatus;
    @FXML private TableColumn<UserDTO, Void> colAction;

    @FXML
    public void initialize() {
        setupColumns();
        loadData(); 
        
        // Tắt focus mặc định vào nút Create User khi mở màn hình
        Platform.runLater(() -> txtSearch.requestFocus());
    }

    private void setupColumns() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colUsername.setCellValueFactory(cell -> cell.getValue().usernameProperty());
        colFullName.setCellValueFactory(cell -> cell.getValue().fullNameProperty());
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
        colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());

        // >>> SỬA CỘT STATUS: Dùng Label bên trong để tạo hình viên thuốc <<<
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            // Tạo một Label để làm badge
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.setText(item.toUpperCase()); // Jira hay viết hoa: ACTIVE
                    
                    // Reset style
                    badge.getStyleClass().clear();
                    badge.getStyleClass().add("status-badge"); // Class tạo hình dáng

                    // Thêm class màu sắc
                    if ("Active".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-success");
                    } else if ("Locked".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-error");
                    } else {
                        badge.getStyleClass().add("status-neutral");
                    }
                    
                    setGraphic(badge); // Nhét cái badge vào ô
                    setText(null);     // Xóa text của ô đi
                }
            }
        });
        
        // >>> SỬA CỘT ACTION: Cho nút nhỏ lại và icon hóa (nếu có) <<<
        // (Giữ logic cũ nhưng style class trong CSS đã chỉnh nhỏ lại rồi)
        addActionsColumn();
    }
    
    // --- CHỨC NĂNG LOAD DANH SÁCH ---
    private void loadData() {
        new Thread(() -> {
            try {
                List<UserDTO> users = UserApi.getAllUsers(txtSearch.getText());
                Platform.runLater(() -> tableUsers.setItems(FXCollections.observableArrayList(users)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Load data failed: " + e.getMessage()));
            }
        }).start();
    }

    // --- CHỨC NĂNG THÊM USER MỚI ---
    @FXML
    private void handleCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/admin/UserDialogView.fxml"));
            DialogPane dialogPane = loader.load();
            UserDialogController dialogController = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New User");
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.isPresent() && clickedButton.get() == ButtonType.OK) {
                UserDTO newUser = dialogController.getNewUser();
                UserApi.createUser(newUser);
                loadData();
                showAlert("Success", "User created successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot open dialog: " + e.getMessage());
        }
    }

    // --- CẬP NHẬT CỘT ACTION DÙNG CSS ---
    private void addActionsColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnLock = new Button();
            // Tăng khoảng cách giữa 2 nút
            private final HBox pane = new HBox(8, btnEdit, btnLock); 

            {
                // >>> ÁP DỤNG CSS CLASS CHO NÚT <<<
                btnEdit.getStyleClass().add("action-button");
                btnLock.getStyleClass().add("action-button");
                pane.setStyle("-fx-alignment: CENTER;"); // Căn giữa HBox

                // Xử lý nút Sửa (Giữ nguyên logic của bạn)
                btnEdit.setOnAction(e -> {
                     UserDTO selectedUser = getTableView().getItems().get(getIndex());
                     try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/admin/UserDialogView.fxml"));
                        DialogPane dialogPane = loader.load();
                        UserDialogController dialogController = loader.getController();
                        dialogController.setEditData(selectedUser);
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setDialogPane(dialogPane);
                        dialog.setTitle("Edit User");
                        Optional<ButtonType> clickedButton = dialog.showAndWait();
                        if (clickedButton.isPresent() && clickedButton.get() == ButtonType.OK) {
                            UserDTO formUser = dialogController.getNewUser();
                            UserDTO userToUpdate = new UserDTO(
                                selectedUser.getId(), selectedUser.getUsername(),
                                formUser.getFullName(), formUser.getEmail(),
                                formUser.getRole(), selectedUser.getStatus()
                            );
                            UserApi.updateUser(userToUpdate);
                            loadData();
                            showAlert("Success", "User updated successfully!");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Error editing user: " + ex.getMessage());
                    }
                });

                // Xử lý nút Khóa/Mở khóa (Cập nhật style động)
                btnLock.setOnAction(e -> {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    String newStatus = "Active".equalsIgnoreCase(user.getStatus()) ? "Locked" : "Active";
                    try {
                        UserApi.changeStatus(user.getId(), newStatus);
                        loadData(); 
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Cannot change status: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    boolean isActive = "Active".equalsIgnoreCase(user.getStatus());
                    
                    btnLock.setText(isActive ? "Lock" : "Unlock");
                    
                    // >>> ĐỔI CLASS CSS CHO NÚT LOCK DỰA TRÊN TRẠNG THÁI <<<
                    btnLock.getStyleClass().removeAll("lock-button-active", "lock-button-locked");
                    if (isActive) {
                        btnLock.getStyleClass().add("lock-button-active"); // Style màu đỏ nhạt
                    } else {
                        btnLock.getStyleClass().add("lock-button-locked"); // Style màu xanh nhạt
                    }
                    
                    setGraphic(pane);
                }
            }
        });
    }

    @FXML private void handleSearch() { loadData(); }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}