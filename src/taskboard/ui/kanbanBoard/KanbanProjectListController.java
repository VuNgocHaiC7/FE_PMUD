package taskboard.ui.kanbanBoard;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;

import java.io.IOException;
import java.util.List;

public class KanbanProjectListController {
    @FXML private TextField txtKeyword;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TableView<ProjectDTO> tblProjects;
    @FXML private TableColumn<ProjectDTO, String> colName;
    @FXML private TableColumn<ProjectDTO, String> colPM;
    @FXML private TableColumn<ProjectDTO, String> colStatus;
    @FXML private TableColumn<ProjectDTO, String> colStart;
    @FXML private TableColumn<ProjectDTO, String> colEnd;

    private ProjectApi projectApi = new ProjectApi();

    public void initialize() {
        try {
            setupColumns();
            
            cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "PLANNING", "IN_PROGRESS", "CLOSED"));
            cbStatus.getSelectionModel().selectFirst();

            // Setup Double-Click to open Kanban board
            tblProjects.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tblProjects.getSelectionModel().getSelectedItem() != null) {
                    openKanbanBoard(tblProjects.getSelectionModel().getSelectedItem());
                }
            });

            loadData();
            
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
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String displayText = item;
                    switch (item) {
                        case "PLANNING":
                            displayText = "LẬP KẾ HOẠCH";
                            badge.setStyle("-fx-background-color: #fef5e7; -fx-text-fill: #d68910; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "IN_PROGRESS":
                            displayText = "ĐANG THỰC HIỆN";
                            badge.setStyle("-fx-background-color: #ebf8ff; -fx-text-fill: #2c5282; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "CLOSED":
                            displayText = "ĐÃ ĐÓNG";
                            badge.setStyle("-fx-background-color: #f0f4f8; -fx-text-fill: #4a5568; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        default:
                            badge.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #718096; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                    }
                    badge.setText(displayText);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        loadData();
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
            tblProjects.setItems(FXCollections.observableArrayList());
        }
    }

    // Mở bảng Kanban cho project đã chọn
    private void openKanbanBoard(ProjectDTO project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/kanbanBoard/BoardView.fxml"));
            Parent root = loader.load();
            
            BoardController controller = loader.getController();
            controller.setProjectId(project.getId());

            Stage stage = new Stage();
            stage.setTitle("Kanban Board - " + project.getName());
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi mở Kanban Board: " + e.getMessage());
            alert.show();
        }
    }
}
