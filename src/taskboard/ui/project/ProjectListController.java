package taskboard.ui.project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.model.ProjectDTO;

import java.io.IOException;
import java.util.List;

public class ProjectListController {
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
            // 1. Setup Cột
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colPM.setCellValueFactory(new PropertyValueFactory<>("pmName"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            
            // Set column resize policy
            tblProjects.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "PLANNING", "IN_PROGRESS", "CLOSED"));
            cbStatus.getSelectionModel().selectFirst();

            // 3. Setup Double-Click
            tblProjects.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tblProjects.getSelectionModel().getSelectedItem() != null) {
                    openProjectBoard(tblProjects.getSelectionModel().getSelectedItem());
                }
            });

            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi trong initialize: " + e.getMessage());
        }
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