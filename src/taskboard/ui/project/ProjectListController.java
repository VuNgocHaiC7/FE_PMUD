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
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;
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

    public void initialize() {
        try {
            setupFilterCombo();
            setupColumns();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi trong initialize: " + e.getMessage());
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
                    setGraphic(pane);
                }
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ ---

    @FXML
    private void handleNewProject() {
        // Nhấn "New Project" -> Nhập tên, mô tả -> Nhấn Save
        // GỌI API: POST /api/projects
        // XỬ LÝ: Thêm dự án mới vào danh sách đang hiển thị
        openProjectDetail(null); // Tạo mới thì vẫn dùng form Detail
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
        // TODO: Implement search and filter functionality
        loadData();
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

}