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
import javafx.beans.property.SimpleObjectProperty;

public class UserManagementController {

    @FXML private TextField txtSearch;
    @FXML private TableView<UserDTO> tableUsers;
    
    @FXML private TableColumn<UserDTO, Number> colId;
    @FXML private TableColumn<UserDTO, String> colUsername;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colRole;
    @FXML private TableColumn<UserDTO, String> colStatus;
    @FXML private TableColumn<UserDTO, UserDTO> colAction;

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

        // --- STATUS COLUMN (Badge Style) ---
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setMaxWidth(Double.MAX_VALUE);
                setAlignment(javafx.geometry.Pos.CENTER_LEFT); 
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.setText(item.toUpperCase());
                    badge.getStyleClass().clear();
                    badge.getStyleClass().add("status-badge");

                    if ("Active".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-success");
                    } else if ("Locked".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-error");
                    } else {
                        badge.getStyleClass().add("status-neutral");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // [QUAN TRỌNG - THÊM DÒNG NÀY] 
        // Gán giá trị cho cột Action là chính đối tượng UserDTO
        // Điều này giúp hàm addActionsColumn nhận được dữ liệu "UserDTO" thay vì "Void"
        colAction.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

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

    private void addActionsColumn() {
        // Lưu ý: TableCell<UserDTO, UserDTO> (Cả 2 đều là UserDTO)
        colAction.setCellFactory(param -> new TableCell<UserDTO, UserDTO>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnLock = new Button();
            private final HBox pane = new HBox(10, btnEdit, btnLock);

            {
                pane.setStyle("-fx-alignment: CENTER;");
                btnEdit.getStyleClass().addAll("table-btn", "btn-edit");

                // --- LOGIC NÚT EDIT ---
                btnEdit.setOnAction(e -> {
                    // Lấy user trực tiếp từ item của ô (An toàn 100%)
                    UserDTO user = getItem(); 
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                // --- LOGIC NÚT LOCK/UNLOCK ---
                btnLock.setOnAction(e -> {
                    UserDTO user = getItem(); 
                    if (user != null) {
                        String currentStatus = (user.getStatus() == null) ? "" : user.getStatus().trim();
                        boolean isActive = "Active".equalsIgnoreCase(currentStatus);
                        String newStatus = isActive ? "Locked" : "Active";
                        
                        try {
                            // 1. Gọi API cập nhật
                            UserApi.changeStatus(user.getId(), newStatus);
                            
                            // 2. Cập nhật ngay lập tức vào đối tượng hiện tại trên bảng (để không phải load lại API)
                            user.setStatus(newStatus); 
                            
                            // 3. [QUAN TRỌNG] Bắt buộc bảng vẽ lại dòng này
                            // Nếu không có dòng này, cột Status tự đổi (do nó bind property), 
                            // nhưng cột Action sẽ đứng im.
                            tableUsers.refresh(); 
                            
                            // Nếu muốn chắc ăn hơn thì gọi loadData() nhưng sẽ chậm hơn
                            // loadData(); 
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showAlert("Error", "Cannot change status: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }

                String status = (user.getStatus() == null) ? "" : user.getStatus().trim();
                
                // --- THÊM DÒNG NÀY ĐỂ DEBUG ---
                // Nhìn vào Console khi chạy để xem nó in ra gì
                System.out.println("User: " + user.getUsername() + " | Status: [" + status + "]");

                btnLock.getStyleClass().clear();
                btnLock.getStyleClass().add("table-btn");

                // Logic: Nếu đang Active -> Phải hiện nút LOCK (Màu đỏ) để người dùng bấm vào khóa
                if ("Active".equalsIgnoreCase(status)) {
                    btnLock.setText("Lock");
                    btnLock.getStyleClass().add("btn-lock");
                } else {
                    // Ngược lại (Locked) -> Phải hiện nút UNLOCK (Màu xanh) để mở khóa
                    btnLock.setText("Unlock");
                    btnLock.getStyleClass().add("btn-unlock");
                }

                setGraphic(pane);
            }
        });
    }

    private void handleEditUser(UserDTO selectedUser) {
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
            throw new RuntimeException(ex);
        }
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