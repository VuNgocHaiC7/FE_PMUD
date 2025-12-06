package taskboard.ui.project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProjectListController {
    @FXML private TextField txtKeyword;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TableView<ProjectDTO> tblProjects;
    @FXML private TableColumn<ProjectDTO, String> colName;
    @FXML private TableColumn<ProjectDTO, String> colPM;
    @FXML private TableColumn<ProjectDTO, String> colStatus;
    @FXML private TableColumn<ProjectDTO, String> colStart;
    @FXML private TableColumn<ProjectDTO, String> colEnd;
    @FXML private TableColumn<ProjectDTO, ProjectDTO> colAction;

    private ProjectApi projectApi = new ProjectApi();

    public void initialize() {
        try {
            setupColumns();
            
            cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "PLANNING", "IN_PROGRESS", "CLOSED"));
            cbStatus.getSelectionModel().selectFirst();

            // Setup Double-Click to open board
            tblProjects.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tblProjects.getSelectionModel().getSelectedItem() != null) {
                    openProjectBoard(tblProjects.getSelectionModel().getSelectedItem());
                }
            });

            loadData();
            
            // Focus on search field instead of create button
            Platform.runLater(() -> txtKeyword.requestFocus());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi trong initialize: " + e.getMessage());
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
                        case "PLANNING":
                            displayText = "LẬP KẾ HOẠCH";
                            break;
                        case "IN_PROGRESS":
                            displayText = "ĐANG THỰC HIỆN";
                            break;
                        case "CLOSED":
                            displayText = "ĐÃ ĐÓNG";
                            break;
                    }
                    
                    badge.setText(displayText);
                    badge.getStyleClass().clear();
                    badge.getStyleClass().add("status-badge");

                    switch (item) {
                        case "PLANNING":
                            badge.getStyleClass().add("status-planning");
                            break;
                        case "IN_PROGRESS":
                            badge.getStyleClass().add("status-in-progress");
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
            private final Button btnView = new Button("Xem");
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(6, btnView, btnEdit, btnDelete);

            {
                pane.setStyle("-fx-alignment: CENTER;");
                btnView.getStyleClass().addAll("table-btn", "btn-view");
                btnEdit.getStyleClass().addAll("table-btn", "btn-edit");
                btnDelete.getStyleClass().addAll("table-btn", "btn-delete");

                // View button - open board
                btnView.setOnAction(e -> {
                    ProjectDTO project = getItem();
                    if (project != null) {
                        openProjectBoard(project);
                    }
                });

                // Edit button - open detail view
                btnEdit.setOnAction(e -> {
                    ProjectDTO project = getItem();
                    if (project != null) {
                        openProjectDetail(project);
                    }
                });

                // Delete button
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
    private void handleSearch() {
        loadData();
    }

    @FXML
    private void handleNewProject() {
        openProjectDetail(null); // Tạo mới thì vẫn dùng form Detail
    }

    private void handleDeleteProject(ProjectDTO project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa dự án này?");
        alert.setContentText("Dự án: " + project.getName());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                projectApi.deleteProject(project.getId());
                loadData();
                showAlert("Thành công", "Xóa dự án thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể xóa dự án: " + e.getMessage());
            }
        }
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
            String keyword = txtKeyword.getText();
            String status = cbStatus.getValue();
            if ("Tất cả".equals(status)) status = "";
            
            List<ProjectDTO> list = projectApi.getProjects(keyword, status);
            tblProjects.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi load data: " + e.getMessage());
            // Hiển thị bảng trống nếu lỗi
            tblProjects.setItems(FXCollections.observableArrayList());
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

    // Mở màn hình Board (ProjectBoardView) -> MỚI THÊM
    private void openProjectBoard(ProjectDTO project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/project/ProjectBoardView.fxml"));
            Parent root = loader.load();
            
            ProjectBoardController controller = loader.getController();
            controller.setProject(project);

            Stage stage = new Stage();
            stage.setTitle("Board: " + project.getName());
            // stage.setMaximized(true); // Nếu muốn mở toàn màn hình
            stage.setScene(new Scene(root));
            stage.show(); // Dùng show() thay vì showAndWait() để có thể mở nhiều board cùng lúc nếu muốn
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi mở Board: " + e.getMessage());
            alert.show();
        }
    }
}