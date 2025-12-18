package taskboard.ui.kanbanBoard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.TaskApi;
import taskboard.auth.AuthContext;
import taskboard.model.TaskDTO;
import taskboard.ui.project.TaskDialogController;

import java.io.IOException;
import java.util.List;

public class BoardController {

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private Button btnAddTask;

    private Long currentProjectId; // ID dự án hiện tại

    // Gọi hàm này khi chuyển màn hình để set Project ID và load dữ liệu
    public void setProjectId(Long projectId) {
        this.currentProjectId = projectId;
        
        // Kiểm tra quyền: Chỉ Admin mới thấy nút Add Task
        boolean isAdmin = AuthContext.getInstance().getRoles() != null 
                && AuthContext.getInstance().getRoles().contains("ADMIN");
        btnAddTask.setVisible(isAdmin);
        btnAddTask.setManaged(isAdmin);
        
        loadBoardData();
    }

    // 1. Load Board [cite: 147]
    private void loadBoardData() {
        // Clear cũ
        todoColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        // Setup Drag & Drop cho các cột (Target)
        setupColumnDragTarget(todoColumn, "TODO");
        setupColumnDragTarget(inProgressColumn, "IN_PROGRESS");
        setupColumnDragTarget(doneColumn, "DONE");

        // Gọi API lấy task
        try {
            List<TaskDTO> tasks = TaskApi.getTasksByProject(currentProjectId);
            System.out.println("📥 Loaded " + tasks.size() + " tasks from API");

            // Phân loại task vào cột
            for (TaskDTO task : tasks) {
                Pane taskCard = createTaskCard(task);
                String status = task.getStatus();
                System.out.println("  Task: " + task.getTitle() + " | Status: '" + status + "'");
                
                switch (status) {
                    case "TODO" -> todoColumn.getChildren().add(taskCard);
                    case "IN_PROGRESS" -> inProgressColumn.getChildren().add(taskCard);
                    case "DONE" -> doneColumn.getChildren().add(taskCard);
                    default -> {
                        System.out.println("⚠️ Unknown status: " + status + ", adding to TODO");
                        todoColumn.getChildren().add(taskCard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi load tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tạo UI cho 1 thẻ Task (Card)
    private Pane createTaskCard(TaskDTO task) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setSpacing(5);
        card.setPrefWidth(150);

        Label lblTitle = new Label(task.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold;");
        lblTitle.setWrapText(true);

        // Hiển thị tất cả người được gán
        String assigneeText = "Unassigned";
        if (task.getAssigneeNames() != null && !task.getAssigneeNames().isEmpty()) {
            assigneeText = String.join(", ", task.getAssigneeNames());
        } else if (task.getAssigneeName() != null && !task.getAssigneeName().isEmpty()) {
            assigneeText = task.getAssigneeName();
        }
        Label lblAssignee = new Label("👥 " + assigneeText);
        lblAssignee.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        lblAssignee.setWrapText(true);

        card.getChildren().addAll(lblTitle, lblAssignee);

        // --- XỬ LÝ KÉO THẢ (DRAG) TẠI CARD (SOURCE) --- [cite: 157]
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId())); // Lưu ID task vào bộ nhớ đệm
            db.setContent(content);
            event.consume();
        });
        
        // Double click để xem chi tiết 
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openTaskDetail(task);
            }
        });

        return card;
    }

    // --- XỬ LÝ THẢ (DROP) TẠI CỘT (TARGET) ===
    private void setupColumnDragTarget(VBox column, String targetStatus) {
        // Chấp nhận kéo thả nếu có dữ liệu
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Hiệu ứng hover khi kéo task vào cột
        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle(column.getStyle() + "; -fx-background-color: #e6f3ff; -fx-border-color: #3182ce; -fx-border-width: 2;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle(""); // Reset style
            event.consume();
        });

        // Khi thả task vào cột
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Long taskId = Long.parseLong(db.getString());
                try {
                    // Gọi API: PUT /api/tasks/{id}/status (Admin & Member đều có thể kéo thả)
                    TaskApi.updateTaskStatus(taskId, targetStatus);
                    System.out.println("✅ Moved task " + taskId + " to " + targetStatus);
                    
                    // Refresh lại board để UI cập nhật đúng vị trí
                    loadBoardData(); 
                    success = true;
                } catch (Exception e) {
                    // Hiển thị thông báo lỗi
                    System.err.println("❌ Failed to update status: " + e.getMessage());
                    showAlert("Lỗi", "Không thể cập nhật trạng thái task: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // Mở dialog để xem/sửa task
    private void openTaskDetail(TaskDTO task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/project/TaskDialogView.fxml"));
            Parent root = loader.load();
            
            TaskDialogController controller = loader.getController();
            controller.setTask(task);
            controller.setProjectId(currentProjectId);
            controller.setOnTaskSaved(() -> loadBoardData()); // Callback để reload board sau khi lưu
            
            Stage stage = new Stage();
            stage.setTitle("Chi tiết Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể mở TaskDetailView: " + e.getMessage());
        }
    }

    // === XỬ LÝ TẠO TASK MỚI (CHỈ ADMIN) ===
    @FXML
    private void handleAddTask() {
        // Kiểm tra quyền Admin
        boolean isAdmin = AuthContext.getInstance().getRoles() != null 
                && AuthContext.getInstance().getRoles().contains("ADMIN");
        
        if (!isAdmin) {
            showAlert("Không có quyền", "Chỉ Admin mới có thể tạo task mới!", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Mở dialog tạo task mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/project/TaskDialogView.fxml"));
            Parent root = loader.load();
            
            TaskDialogController controller = loader.getController();
            controller.setProjectId(currentProjectId);
            // Không set task -> mode tạo mới
            controller.setOnTaskSaved(() -> {
                System.out.println("Task mới đã được tạo, đang reload board...");
                loadBoardData(); // Reload board để hiển thị task mới
            });
            
            Stage stage = new Stage();
            stage.setTitle("Tạo Task Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở form tạo task: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Hiển thị thông báo
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}