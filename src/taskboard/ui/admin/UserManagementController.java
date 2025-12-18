package taskboard.ui.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import taskboard.api.UserApi;
import taskboard.auth.AuthContext;
import taskboard.model.UserDTO;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;

public class UserManagementController {

    @FXML private TextField txtSearch;
    @FXML private Button btnCreateUser;
    @FXML private TableView<UserDTO> tableUsers;
    
    @FXML private TableColumn<UserDTO, Number> colId;
    @FXML private TableColumn<UserDTO, String> colUsername;
    @FXML private TableColumn<UserDTO, String> colFullName;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colRole;
    @FXML private TableColumn<UserDTO, String> colStatus;
    @FXML private TableColumn<UserDTO, UserDTO> colAction;
    
    // New header fields
    @FXML private ComboBox<String> cbRoleFilter;
    
    private boolean isAdmin = false;

    @FXML
    public void initialize() {
        // Kiểm tra role của user hiện tại
        checkUserRole();
        
        setupFilterCombo();
        setupColumns();
        loadData(); 
        
        // Tắt focus mặc định vào nút Create User khi mở màn hình
        Platform.runLater(() -> txtSearch.requestFocus());
    }
    
    private void setupFilterCombo() {
        if (cbRoleFilter != null) {
            cbRoleFilter.setItems(FXCollections.observableArrayList(
                "Tất cả",
                "ADMIN",
                "MEMBER"
            ));
            cbRoleFilter.setValue("Tất cả");
            cbRoleFilter.setOnAction(e -> handleSearch());
        }
    }
    
    private void checkUserRole() {
        List<String> roles = AuthContext.getInstance().getRoles();
        isAdmin = roles != null && roles.contains("ADMIN");
        
        // Ẩn nút Create User nếu không phải admin
        if (btnCreateUser != null) {
            btnCreateUser.setVisible(isAdmin);
            btnCreateUser.setManaged(isAdmin);
        }
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
                    // Hiển thị tiếng Việt cho status
                    String displayText = item;
                    if ("Active".equalsIgnoreCase(item)) {
                        displayText = "HOẠT ĐỘNG";
                    } else if ("Locked".equalsIgnoreCase(item)) {
                        displayText = "BỊ KHÓA";
                    }
                    
                    badge.setText(displayText);
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
                // GỌI API: GET /api/users
                List<UserDTO> users = UserApi.getAllUsers(txtSearch.getText());
                // Hiển thị lên bảng (TableView)
                Platform.runLater(() -> {
                    tableUsers.setItems(FXCollections.observableArrayList(users));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Tải dữ liệu thất bại: " + e.getMessage());
                });
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
            dialog.setTitle("Thêm Người Dùng Mới");
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.isPresent() && clickedButton.get() == ButtonType.OK) {
                UserDTO newUser = dialogController.getNewUser();
                
                // GỌI API: POST /api/users
                UserApi.createUser(newUser);
                
                // XỬ LÝ: Tắt popup, reload danh sách để thấy user mới
                loadData();
                showAlert("Thành công", "Tạo người dùng thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở hộp thoại: " + e.getMessage());
        }
    }

    private void addActionsColumn() {
        // TableCell<UserDTO, UserDTO> (Cả 2 đều là UserDTO)
        colAction.setCellFactory(param -> new TableCell<UserDTO, UserDTO>() {
            private final Button btnChangeRole = new Button("Gán Role");
            private final Button btnLock = new Button();
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(8, btnChangeRole, btnLock, btnDelete);

            {
                pane.setStyle("-fx-alignment: CENTER;");
                
                // Set minimum width để text hiển thị đầy đủ
                btnChangeRole.setMinWidth(90);
                btnChangeRole.setPrefWidth(90);
                btnLock.setMinWidth(80);
                btnLock.setPrefWidth(80);
                btnDelete.setMinWidth(70);
                btnDelete.setPrefWidth(70);
                
                // Style classes
                btnChangeRole.getStyleClass().addAll("table-btn", "btn-info");
                btnDelete.getStyleClass().addAll("table-btn", "btn-delete");

                // --- LOGIC NÚT CHANGE ROLE ---
                btnChangeRole.setOnAction(e -> {
                    UserDTO user = getItem(); 
                    if (user != null) {
                        handleChangeRole(user);
                    }
                });
                
                // --- LOGIC NÚT DELETE ---
                btnDelete.setOnAction(e -> {
                    UserDTO user = getItem();
                    if (user != null) {
                        handleDeleteUser(user);
                    }
                });

                // --- LOGIC NÚT LOCK/UNLOCK ---
                btnLock.setOnAction(e -> {
                    UserDTO user = getItem(); 
                    if (user != null) {
                        String currentStatus = (user.getStatus() == null) ? "" : user.getStatus().trim();
                        boolean isActive = "Active".equalsIgnoreCase(currentStatus);
                        String newStatus = isActive ? "Locked" : "Active";
                        
                        // Chạy API call trong background thread để không block UI
                        new Thread(() -> {
                            try {
                                // 1. GỌI API: PUT /api/users/{id}/status
                                UserApi.changeStatus(user.getId(), newStatus);
                                
                                // 2. Cập nhật UI trên JavaFX Application Thread
                                Platform.runLater(() -> {
                                    // Cập nhật ngay lập tức vào đối tượng hiện tại trên bảng
                                    user.setStatus(newStatus); 
                                    
                                    // Bắt buộc bảng vẽ lại dòng này
                                    tableUsers.refresh(); 
                                    
                                    // Hiển thị thông báo thành công
                                    String message = isActive ? 
                                        "Đã khóa tài khoản " + user.getFullName() : 
                                        "Đã mở khóa tài khoản " + user.getFullName();
                                    showAlert("Thành công", message);
                                });
                                
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Platform.runLater(() -> {
                                    showAlert("Lỗi", "Không thể thay đổi trạng thái: " + ex.getMessage());
                                });
                            }
                        }).start();
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
                
                // Kiểm tra xem có phải tài khoản Admin hệ thống không (không phân biệt hoa thường)
                boolean isAdminAccount = "admin".equalsIgnoreCase(user.getUsername());
                
                // Kiểm tra user hiện tại có phải là "admin" không
                String currentUsername = AuthContext.getInstance().getUsername();
                boolean isCurrentUserAdmin = "admin".equalsIgnoreCase(currentUsername);
                
                // Chỉ user có username là "admin" mới thấy nút xóa
                // Và không được xóa chính tài khoản admin
                boolean canEdit = isAdmin && !isAdminAccount;
                boolean canDelete = isCurrentUserAdmin && !isAdminAccount;
                
                btnChangeRole.setVisible(canEdit);
                btnChangeRole.setManaged(canEdit);
                btnLock.setVisible(canEdit);
                btnLock.setManaged(canEdit);
                btnDelete.setVisible(canDelete);
                btnDelete.setManaged(canDelete);

                String status = (user.getStatus() == null) ? "" : user.getStatus().trim();
                
                // --- THÊM DÒNG NÀY ĐỂ DEBUG ---
                // Nhìn vào Console khi chạy để xem nó in ra gì
                System.out.println("User: " + user.getUsername() + " | Status: [" + status + "]");

                btnLock.getStyleClass().clear();
                btnLock.getStyleClass().add("table-btn");

                // Logic: Nếu đang Active -> Phải hiện nút LOCK (Màu đỏ) để người dùng bấm vào khóa
                if ("Active".equalsIgnoreCase(status)) {
                    btnLock.setText("Khóa");
                    btnLock.getStyleClass().add("btn-lock");
                } else {
                    // Ngược lại (Locked) -> Phải hiện nút UNLOCK (Màu xanh) để mở khóa
                    btnLock.setText("Mở khóa");
                    btnLock.getStyleClass().add("btn-unlock");
                }

                setGraphic(pane);
            }
        });
    }

    @FXML private void handleSearch() { loadData(); }

    // --- CHỨC NĂNG GÁN ROLE ---
    private void handleChangeRole(UserDTO selectedUser) {
        // Tạo danh sách các role có thể chọn (chỉ ADMIN và MEMBER)
        List<String> roles = List.of("ADMIN", "MEMBER");
        
        // Tạo ChoiceDialog để chọn role
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedUser.getRole(), roles);
        dialog.setTitle("Gán Role");
        dialog.setHeaderText("Thay đổi quyền hạn cho: " + selectedUser.getFullName());
        dialog.setContentText("Chọn Role mới:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            try {
                // GỌI API: PUT /api/users/{id}/role
                // Body: { "role": "PM" }
                UserApi.updateUserRole(selectedUser.getId(), newRole);
                
                // Cập nhật ngay trên bảng (không cần reload)
                selectedUser.setRole(newRole);
                tableUsers.refresh();
                
                showAlert("Thành công", "Đã cập nhật role của " + selectedUser.getFullName() + " thành " + newRole);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không thể cập nhật role: " + ex.getMessage());
            }
        });
    }
    
    // --- CHỨC NĂNG XÓA USER ---
    private void handleDeleteUser(UserDTO selectedUser) {
        // Hiển thị hộp thoại xác nhận
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa người dùng này?");
        confirmAlert.setContentText("Người dùng: " + selectedUser.getFullName() + " (" + selectedUser.getUsername() + ")\n\n" +
                                   "Hành động này KHÔNG thể hoàn tác!");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Chạy trong background thread
            new Thread(() -> {
                try {
                    // GỌI API: DELETE /api/users/{id}
                    UserApi.deleteUser(selectedUser.getId());
                    
                    // Cập nhật UI
                    Platform.runLater(() -> {
                        // Reload lại danh sách
                        loadData();
                        showAlert("Thành công", "Đã xóa người dùng " + selectedUser.getFullName() + " thành công!");
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert("Lỗi", "Không thể xóa người dùng: " + ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}