package taskboard.ui.project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.auth.AuthContext;
import taskboard.model.ProjectDTO;
import taskboard.model.UserDTO;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.util.List;

public class ProjectListController {
    @FXML private TableView<ProjectDTO> tblProjects;
    @FXML private TableColumn<ProjectDTO, String> colName;
    @FXML private TableColumn<ProjectDTO, String> colPM;
    @FXML private TableColumn<ProjectDTO, String> colStatus;
    @FXML private TableColumn<ProjectDTO, String> colStart;
    @FXML private TableColumn<ProjectDTO, String> colEnd;
    @FXML private TableColumn<ProjectDTO, ProjectDTO> colAction;
    
    // New header fields
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField txtSearchProject;
    @FXML private Button btnCreateProject;

    public void initialize() {
        try {
            setupFilterCombo();
            setupColumns();
            setupDoubleClickHandler();
            setupUIBasedOnRole();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi trong initialize: " + e.getMessage());
        }
    }
    
    /**
     * Ẩn/hiện các UI elements dựa trên role của user
     */
    private void setupUIBasedOnRole() {
        AuthContext authContext = AuthContext.getInstance();
        List<String> roles = authContext.getRoles();
        boolean isAdmin = roles != null && roles.contains("ADMIN");
        
        // Chỉ ADMIN mới thấy button "Tạo Dự Án Mới"
        if (btnCreateProject != null) {
            btnCreateProject.setVisible(isAdmin);
            btnCreateProject.setManaged(isAdmin);
        }
    }
    
    private void setupFilterCombo() {
        if (cbStatusFilter != null) {
            cbStatusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả",
                "ĐANG HOẠT ĐỘNG",
                "HOÀN THÀNH",
                "ĐÃ ĐÓNG"
            ));
            cbStatusFilter.setValue("Tất cả");
            cbStatusFilter.setOnAction(e -> handleSearch());
        }
    }

    private void setupColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPM.setCellValueFactory(new PropertyValueFactory<>("pmName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // Status column with badge style
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
                    switch (item) {
                        case "ACTIVE":
                            displayText = "ĐANG HOẠT ĐỘNG";
                            break;
                        case "COMPLETED":
                            displayText = "HOÀN THÀNH";
                            break;
                        case "CLOSED":
                            displayText = "ĐÃ ĐÓNG";
                            break;
                    }
                    
                    badge.setText(displayText);
                    badge.getStyleClass().clear();
                    badge.getStyleClass().add("status-badge");

                    switch (item) {
                        case "ACTIVE":
                            badge.getStyleClass().add("status-in-progress");
                            break;
                        case "COMPLETED":
                            badge.getStyleClass().add("status-planning");
                            break;
                        case "CLOSED":
                            badge.getStyleClass().add("status-closed");
                            break;
                        default:
                            badge.getStyleClass().add("status-planning");
                            break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Action column setup
        colAction.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        addActionsColumn();
    }

    private void addActionsColumn() {
        colAction.setCellFactory(param -> new TableCell<ProjectDTO, ProjectDTO>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(6, btnEdit, btnDelete);

            {
                pane.setStyle("-fx-alignment: CENTER;");
                btnEdit.getStyleClass().addAll("table-btn", "btn-edit");
                btnDelete.getStyleClass().addAll("table-btn", "btn-delete");

                // Edit button - open detail view for editing and managing members
                btnEdit.setOnAction(e -> {
                    ProjectDTO project = getItem();
                    if (project != null) {
                        openProjectDetail(project);
                    }
                });

                // Delete button - only for Admin
                btnDelete.setOnAction(e -> {
                    ProjectDTO project = getItem();
                    if (project != null) {
                        handleDeleteProject(project);
                    }
                });
            }

            @Override
            protected void updateItem(ProjectDTO project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setGraphic(null);
                } else {
                    // Kiểm tra role của user hiện tại
                    AuthContext authContext = AuthContext.getInstance();
                    List<String> roles = authContext.getRoles();
                    boolean isAdmin = roles != null && roles.contains("ADMIN");
                    
                    // Chỉ hiển thị các button nếu user là ADMIN
                    if (isAdmin) {
                        setGraphic(pane);
                    } else {
                        // User là MEMBER -> ẩn các button
                        setGraphic(null);
                    }
                }
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ ---

    @FXML
    private void handleNewProject() {
        // Mở dialog tạo dự án mới (riêng biệt với dialog sửa)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/project/ProjectCreateView.fxml"));
            Parent root = loader.load();
            ProjectCreateController controller = loader.getController();
            
            Stage dialog = new Stage();
            dialog.setTitle("Tạo Dự Án Mới");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            
            // Nếu tạo thành công, reload danh sách
            if (controller.isCreated()) {
                loadData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở form tạo dự án: " + e.getMessage());
        }
    }

    private void handleDeleteProject(ProjectDTO project) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa dự án này?");
        confirmAlert.setContentText("Dự án: " + project.getName());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // GỌI API: DELETE /api/projects/{id}
                    ProjectApi.deleteProject(project.getId());
                    loadData(); // Reload danh sách
                    showAlert("Thành công", "Xóa dự án thành công!");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể xóa dự án: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Thành công") || title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadData() {
        try {
            System.out.println("=== BẮT ĐẦU LOAD DATA ===");
            // GỌI API: GET /api/projects
            List<ProjectDTO> list = ProjectApi.getProjects();
            
            System.out.println("Số dự án nhận được: " + (list != null ? list.size() : 0));
            if (list != null && !list.isEmpty()) {
                for (ProjectDTO p : list) {
                    System.out.println("  - Project: " + p.getName() + " (ID: " + p.getId() + ")");
                }
            }
            
            // Hiển thị danh sách dạng bảng
            tblProjects.setItems(FXCollections.observableArrayList(list));
            
            System.out.println("=== LOAD DATA THÀNH CÔNG ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("!!! LỖI LOAD DATA: " + e.getMessage());
            
            // Hiển thị alert cho user
            showAlert("Lỗi", "Không thể tải danh sách dự án!\n" + e.getMessage());
            tblProjects.setItems(FXCollections.observableArrayList());
        }
    }
    
    @FXML
    private void handleSearch() {
        try {
            System.out.println("=== BẮT ĐẦU TÌM KIẾM ===");
            
            // Lấy toàn bộ danh sách dự án từ API
            List<ProjectDTO> allProjects = ProjectApi.getProjects();
            
            if (allProjects == null) {
                tblProjects.setItems(FXCollections.observableArrayList());
                return;
            }
            
            // Lấy giá trị tìm kiếm và filter
            String searchText = txtSearchProject.getText() != null ? txtSearchProject.getText().toLowerCase().trim() : "";
            String statusFilter = cbStatusFilter.getValue();
            
            System.out.println("Tìm kiếm với: text='" + searchText + "', status='" + statusFilter + "'");
            
            // Lọc danh sách
            List<ProjectDTO> filteredList = allProjects.stream()
                .filter(project -> {
                    // Lọc theo từ khóa tìm kiếm (tên dự án hoặc PM)
                    boolean matchesSearch = searchText.isEmpty() || 
                        (project.getName() != null && project.getName().toLowerCase().contains(searchText)) ||
                        (project.getPmName() != null && project.getPmName().toLowerCase().contains(searchText));
                    
                    // Lọc theo trạng thái
                    boolean matchesStatus = statusFilter.equals("Tất cả");
                    if (!matchesStatus && project.getStatus() != null) {
                        switch (statusFilter) {
                            case "ĐANG HOẠT ĐỘNG":
                                matchesStatus = project.getStatus().equals("ACTIVE");
                                break;
                            case "HOÀN THÀNH":
                                matchesStatus = project.getStatus().equals("COMPLETED");
                                break;
                            case "ĐÃ ĐÓNG":
                                matchesStatus = project.getStatus().equals("CLOSED");
                                break;
                        }
                    }
                    
                    return matchesSearch && matchesStatus;
                })
                .toList();
            
            System.out.println("Tìm thấy " + filteredList.size() + " dự án phù hợp");
            
            // Hiển thị kết quả
            tblProjects.setItems(FXCollections.observableArrayList(filteredList));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tìm kiếm: " + e.getMessage());
            showAlert("Lỗi", "Không thể thực hiện tìm kiếm: " + e.getMessage());
        }
    }

    // Mở màn hình Sửa/Tạo (ProjectDetailView)
    private void openProjectDetail(ProjectDTO project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/project/ProjectDetailView.fxml"));
            Parent root = loader.load();
            ProjectDetailController controller = loader.getController();
            controller.setProject(project);

            Stage stage = new Stage();
            stage.setTitle(project == null ? "Tạo Dự Án" : "Cập nhật dự án");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            loadData(); // Reload sau khi đóng
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Thiết lập event handler cho double-click
    private void setupDoubleClickHandler() {
        System.out.println("DEBUG: Đang thiết lập double-click handler cho bảng dự án");
        tblProjects.setOnMouseClicked(event -> {
            System.out.println("DEBUG: Mouse clicked - Click count: " + event.getClickCount());
            if (event.getClickCount() == 2) {
                ProjectDTO selectedProject = tblProjects.getSelectionModel().getSelectedItem();
                System.out.println("DEBUG: Selected project: " + (selectedProject != null ? selectedProject.getName() : "null"));
                if (selectedProject != null) {
                    showProjectDetails(selectedProject);
                }
            }
        });
    }
    
    // Hiển thị chi tiết dự án dạng read-only với giao diện đẹp
    private void showProjectDetails(ProjectDTO project) {
        try {
            // Load danh sách thành viên của dự án
            List<UserDTO> members = ProjectApi.getProjectMembers(project.getId());
            
            // Tạo Stage mới
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Chi Tiết Dự Án");
            
            // Container chính
            VBox mainContainer = new VBox(15);
            mainContainer.setStyle("-fx-padding: 25; -fx-background-color: #f8f9fa;");
            
            // Header với tên dự án
            Label headerLabel = new Label("📋 " + project.getName());
            headerLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; " +
                               "-fx-text-fill: #2d3748; -fx-font-family: 'Segoe UI Semibold';");
            
            Separator separator1 = new Separator();
            separator1.setStyle("-fx-background-color: #e2e8f0;");
            
            // GridPane để hiển thị thông tin
            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(15);
            grid.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 8; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
            
            int row = 0;
            
            // Mô tả
            Label lblDescTitle = new Label("📝 Mô tả:");
            lblDescTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
            TextArea txtDesc = new TextArea(
                project.getDescription() != null && !project.getDescription().isEmpty() 
                    ? project.getDescription() : "(Không có mô tả)"
            );
            txtDesc.setEditable(false);
            txtDesc.setWrapText(true);
            txtDesc.setPrefRowCount(3);
            txtDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748; -fx-control-inner-background: #f7fafc;");
            grid.add(lblDescTitle, 0, row, 2, 1);
            grid.add(txtDesc, 0, ++row, 2, 1);
            
            // Trạng thái
            row++;
            Label lblStatusTitle = new Label("🔄 Trạng thái:");
            lblStatusTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
            
            String statusVN = project.getStatus();
            String statusColor = "#48bb78"; // green
            switch (project.getStatus()) {
                case "ACTIVE":
                    statusVN = "ĐANG HOẠT ĐỘNG";
                    statusColor = "#4299e1"; // blue
                    break;
                case "COMPLETED":
                    statusVN = "HOÀN THÀNH";
                    statusColor = "#48bb78"; // green
                    break;
                case "CLOSED":
                    statusVN = "ĐÃ ĐÓNG";
                    statusColor = "#ed8936"; // orange
                    break;
            }
            Label lblStatusValue = new Label(statusVN);
            lblStatusValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: white; " +
                                   "-fx-background-color: " + statusColor + "; -fx-padding: 5 12; " +
                                   "-fx-background-radius: 5;");
            grid.add(lblStatusTitle, 0, row);
            grid.add(lblStatusValue, 1, row);
            
            // Ngày bắt đầu
            row++;
            Label lblStartTitle = new Label("📅 Ngày bắt đầu:");
            lblStartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
            Label lblStartValue = new Label(project.getStartDate() != null ? project.getStartDate().toString() : "(Chưa xác định)");
            lblStartValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
            grid.add(lblStartTitle, 0, row);
            grid.add(lblStartValue, 1, row);
            
            // Ngày kết thúc
            row++;
            Label lblEndTitle = new Label("📅 Ngày kết thúc:");
            lblEndTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
            Label lblEndValue = new Label(project.getEndDate() != null ? project.getEndDate().toString() : "(Chưa xác định)");
            lblEndValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
            grid.add(lblEndTitle, 0, row);
            grid.add(lblEndValue, 1, row);
            
            // Separator
            Separator separator2 = new Separator();
            separator2.setStyle("-fx-background-color: #e2e8f0;");
            
            // Danh sách thành viên
            Label lblMembersTitle = new Label("👥 Thành viên (" + members.size() + " người)");
            lblMembersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            
            // Table cho thành viên
            TableView<UserDTO> membersTable = new TableView<>();
            membersTable.setPrefHeight(200);
            membersTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
            
            TableColumn<UserDTO, String> colMemberName = new TableColumn<>("Họ và Tên");
            colMemberName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            colMemberName.setPrefWidth(200);
            
            TableColumn<UserDTO, String> colMemberUsername = new TableColumn<>("Username");
            colMemberUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
            colMemberUsername.setPrefWidth(150);
            
            TableColumn<UserDTO, String> colMemberRole = new TableColumn<>("Role");
            colMemberRole.setCellValueFactory(new PropertyValueFactory<>("role"));
            colMemberRole.setPrefWidth(100);
            colMemberRole.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label badge = new Label(item);
                        String color = item.equals("ADMIN") ? "#805ad5" : "#3182ce";
                        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                                     "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; " +
                                     "-fx-font-weight: 600;");
                        setGraphic(badge);
                        setText(null);
                    }
                }
            });
            
            membersTable.getColumns().addAll(colMemberName, colMemberUsername, colMemberRole);
            membersTable.setItems(FXCollections.observableArrayList(members));
            
            if (members.isEmpty()) {
                Label emptyLabel = new Label("(Chưa có thành viên nào trong dự án này)");
                emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-font-style: italic;");
                mainContainer.getChildren().addAll(headerLabel, separator1, grid, separator2, lblMembersTitle, emptyLabel);
            } else {
                mainContainer.getChildren().addAll(headerLabel, separator1, grid, separator2, lblMembersTitle, membersTable);
            }
            
            // Button đóng
            Button btnClose = new Button("Đóng");
            btnClose.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-padding: 10 30; -fx-background-radius: 6; -fx-font-weight: 600; " +
                            "-fx-cursor: hand;");
            btnClose.setOnAction(e -> detailStage.close());
            
            HBox buttonBox = new HBox(btnClose);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-padding: 10 0 0 0;");
            
            mainContainer.getChildren().add(buttonBox);
            
            // ScrollPane wrapper
            ScrollPane scrollPane = new ScrollPane(mainContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f8f9fa; -fx-background: #f8f9fa;");
            
            Scene scene = new Scene(scrollPane, 650, 600);
            detailStage.setScene(scene);
            detailStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải thông tin chi tiết dự án: " + e.getMessage());
        }
    }

}
