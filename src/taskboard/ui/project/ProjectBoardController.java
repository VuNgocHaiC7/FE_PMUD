package taskboard.ui.project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.TaskApi;
import taskboard.model.ProjectDTO;
import taskboard.model.TaskDTO;

import java.util.List;

public class ProjectBoardController {

    @FXML private Label lblProjectName;
    @FXML private ListView<TaskDTO> lvTodo;
    @FXML private ListView<TaskDTO> lvDoing;
    @FXML private ListView<TaskDTO> lvDone;

    private ProjectDTO project;
    private TaskApi taskApi = new TaskApi();

    public void setProject(ProjectDTO project) {
        this.project = project;
        updateBoardInfo();
        loadTasks();
    }

    private void updateBoardInfo() {
        if (project != null) {
            lblProjectName.setText("Board: " + project.getName() + " (" + project.getStatus() + ")");
        }
    }

    private void loadTasks() {
        if (project == null || project.getId() == null) {
            System.out.println("WARNING: Project chưa được set hoặc không có ID");
            return;
        }

        System.out.println("DEBUG: Loading tasks cho project " + project.getId());
        
        // Load tasks theo từng status
        List<TaskDTO> todoTasks = taskApi.getTasksByStatus(project.getId(), "TODO");
        List<TaskDTO> doingTasks = taskApi.getTasksByStatus(project.getId(), "DOING");
        List<TaskDTO> doneTasks = taskApi.getTasksByStatus(project.getId(), "DONE");

        System.out.println("DEBUG: TODO=" + todoTasks.size() + ", DOING=" + doingTasks.size() + ", DONE=" + doneTasks.size());

        // Hiển thị lên ListView
        lvTodo.setItems(FXCollections.observableArrayList(todoTasks));
        lvDoing.setItems(FXCollections.observableArrayList(doingTasks));
        lvDone.setItems(FXCollections.observableArrayList(doneTasks));

        // Cấu hình cách hiển thị Task trong ListView
        configureCellFactory(lvTodo);
        configureCellFactory(lvDoing);
        configureCellFactory(lvDone);

        // Thêm context menu (click chuột phải) để Sửa/Xóa task
        setupContextMenu(lvTodo);
        setupContextMenu(lvDoing);
        setupContextMenu(lvDone);
    }

    private void configureCellFactory(ListView<TaskDTO> listView) {
        listView.setCellFactory(lv -> new ListCell<TaskDTO>() {
            @Override
            protected void updateItem(TaskDTO task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Tạo task card đẹp
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: #ffffff; -fx-padding: 12 15; -fx-background-radius: 8; " +
                                "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
                    
                    // Title
                    Label lblTitle = new Label(task.getTitle());
                    lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2d3748; -fx-wrap-text: true;");
                    lblTitle.setMaxWidth(280);
                    
                    // Assignee (nếu có)
                    if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                        HBox assigneeBox = new HBox(6);
                        assigneeBox.setStyle("-fx-alignment: center-left;");
                        
                        Label iconLabel = new Label("👤");
                        iconLabel.setStyle("-fx-font-size: 12px;");
                        
                        Label lblAssignee = new Label(task.getAssignee());
                        lblAssignee.setStyle("-fx-font-size: 12px; -fx-text-fill: #667eea; -fx-font-weight: 600;");
                        
                        assigneeBox.getChildren().addAll(iconLabel, lblAssignee);
                        card.getChildren().addAll(lblTitle, assigneeBox);
                    } else {
                        Label lblUnassigned = new Label("⚪ Chưa giao");
                        lblUnassigned.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; -fx-font-style: italic;");
                        card.getChildren().addAll(lblTitle, lblUnassigned);
                    }
                    
                    setText(null);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 6 8;");
                }
            }
        });
    }

    private void setupContextMenu(ListView<TaskDTO> listView) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("Sửa task");
        editItem.setOnAction(e -> {
            TaskDTO selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) handleEditTask(selected);
        });

        MenuItem deleteItem = new MenuItem("Xóa task");
        deleteItem.setOnAction(e -> {
            TaskDTO selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) handleDeleteTask(selected);
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        listView.setContextMenu(contextMenu);
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) lblProjectName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleProjectSettings() {
        System.out.println("DEBUG: Đã bấm nút Setting");

        try {
            System.out.println("DEBUG: Đang tìm file ProjectDetailView.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectDetailView.fxml"));
            
            System.out.println("DEBUG: Đang load FXML...");
            Parent root = loader.load();

            System.out.println("DEBUG: Đang lấy controller...");
            ProjectDetailController controller = loader.getController();
            
            System.out.println("DEBUG: Đang set project: " + (this.project != null ? this.project.getName() : "null"));
            controller.setProject(this.project);

            System.out.println("DEBUG: Đang mở cửa sổ...");
            Stage stage = new Stage();
            stage.setTitle("Cập nhật dự án & Thành viên");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            System.out.println("DEBUG: Đã đóng cửa sổ, cập nhật lại board...");
            updateBoardInfo();

        } catch (Exception e) {
            System.err.println("ERROR: Lỗi khi mở cài đặt dự án!");
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể mở cửa sổ cài đặt dự án");
            alert.setContentText("Chi tiết lỗi: " + e.getMessage() + "\n\nVui lòng kiểm tra console để biết thêm chi tiết.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAddTask() {
        openTaskDialog(null); // null = tạo mới
    }

    private void handleEditTask(TaskDTO task) {
        openTaskDialog(task); // Truyền task vào để sửa
    }

    private void openTaskDialog(TaskDTO task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskDialogView.fxml"));
            Parent root = loader.load();

            TaskDialogController controller = loader.getController();
            controller.setProjectId(project.getId());
            
            if (task != null) {
                // Mode EDIT
                controller.setTask(task);
            }
            // else: Mode CREATE (mặc định)

            Stage stage = new Stage();
            stage.setTitle(task == null ? "Thêm Task Mới" : "Sửa Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Kiểm tra xem user có bấm Lưu không
            if (controller.isSaved()) {
                TaskDTO savedTask = controller.getTask();
                if (taskApi.saveTask(savedTask)) {
                    loadTasks(); // Reload lại board
                    showAlert("Thành công", task == null ? "Đã tạo task mới!" : "Đã cập nhật task!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở dialog: " + e.getMessage());
        }
    }

    private void handleDeleteTask(TaskDTO task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa task: " + task.getTitle());
        confirm.setContentText("Bạn có chắc chắn muốn xóa task này?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskApi.deleteTask(task.getId())) {
                    loadTasks(); // Reload lại danh sách
                    showAlert("Thành công", "Đã xóa task!");
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}