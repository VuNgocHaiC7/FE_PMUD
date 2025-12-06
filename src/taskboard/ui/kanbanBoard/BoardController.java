package taskboard.ui.kanbanBoard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskboard.api.TaskApi;
import taskboard.model.TaskDTO;
import taskboard.ui.project.TaskDialogController;

import java.io.IOException;
import java.util.List;

public class BoardController {

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private VBox blockedColumn;

    private final TaskApi taskApi = new TaskApi();
    private Long currentProjectId; // ID dự án hiện tại

    // Gọi hàm này khi chuyển màn hình để set Project ID và load dữ liệu
    public void setProjectId(Long projectId) {
        this.currentProjectId = projectId;
        loadBoardData();
    }

    // 1. Load Board [cite: 147]
    private void loadBoardData() {
        // Clear cũ
        todoColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();
        blockedColumn.getChildren().clear();

        // Setup Drag & Drop cho các cột (Target)
        setupColumnDragTarget(todoColumn, "ToDo");
        setupColumnDragTarget(inProgressColumn, "InProgress");
        setupColumnDragTarget(doneColumn, "Done");
        setupColumnDragTarget(blockedColumn, "Blocked");

        // Gọi API lấy task
        List<TaskDTO> tasks = taskApi.getTasksByProject(currentProjectId);

        // Phân loại task vào cột [cite: 151]
        for (TaskDTO task : tasks) {
            Pane taskCard = createTaskCard(task);
            switch (task.getStatus()) {
                case "ToDo" -> todoColumn.getChildren().add(taskCard);
                case "InProgress" -> inProgressColumn.getChildren().add(taskCard);
                case "Done" -> doneColumn.getChildren().add(taskCard);
                case "Blocked" -> blockedColumn.getChildren().add(taskCard);
                default -> todoColumn.getChildren().add(taskCard); // Default
            }
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

        Label lblAssignee = new Label("Assignee: " + (task.getAssigneeName() != null ? task.getAssigneeName() : "Unassigned"));
        lblAssignee.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

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

    // --- XỬ LÝ THẢ (DROP) TẠI CỘT (TARGET) --- [cite: 158-161]
    private void setupColumnDragTarget(VBox column, String targetStatus) {
        // Chấp nhận kéo thả nếu có dữ liệu
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Khi thả task vào cột
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Long taskId = Long.parseLong(db.getString());
                
                // 1. Gọi API update 
                boolean apiSuccess = taskApi.updateTaskStatus(taskId, targetStatus);
                
                if (apiSuccess) {
                    System.out.println("Moved task " + taskId + " to " + targetStatus);
                    // 2. Refresh lại board để UI cập nhật đúng vị trí
                    loadBoardData(); 
                    success = true;
                } else {
                    // [cite: 163] Revert UI hoặc thông báo lỗi
                    System.err.println("Failed to update status");
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
}