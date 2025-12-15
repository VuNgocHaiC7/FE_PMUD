
package taskboard.ui.task;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import taskboard.api.ProjectApi;
import taskboard.api.TaskApi;
import taskboard.model.ProjectDTO;
import taskboard.model.TaskDTO;

import java.util.List;

public class TaskListController {
    @FXML
    private ComboBox<ProjectDTO> cbProject;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private TextField txtKeyword;
    @FXML
    private ListView<TaskDTO> lvTodo;
    @FXML
    private ListView<TaskDTO> lvDoing;
    @FXML
    private ListView<TaskDTO> lvDone;

    private final TaskApi taskApi = new TaskApi();
    private final ProjectApi projectApi = new ProjectApi();

    @FXML
    public void initialize() {
        // Load project list
        List<ProjectDTO> projects = projectApi.getProjects("", "");
        cbProject.setItems(FXCollections.observableArrayList(projects));
        if (!projects.isEmpty())
            cbProject.getSelectionModel().selectFirst();

        cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "ToDo", "InProgress", "Done", "Blocked"));
        cbStatus.getSelectionModel().selectFirst();

        loadTasks();
        cbProject.setOnAction(e -> loadTasks());
        cbStatus.setOnAction(e -> loadTasks());
    }

    @FXML
    private void handleSearch() {
        loadTasks();
    }

    @FXML
    private void handleProjectChange() {
        loadTasks();
    }

    private void loadTasks() {
        ProjectDTO selectedProject = cbProject.getValue();
        if (selectedProject == null) {
            lvTodo.setItems(FXCollections.observableArrayList());
            lvDoing.setItems(FXCollections.observableArrayList());
            lvDone.setItems(FXCollections.observableArrayList());
            return;
        }
        String status = cbStatus.getValue();
        List<TaskDTO> tasks;
        if (status != null && !status.equals("Tất cả")) {
            tasks = taskApi.getTasksByStatus(selectedProject.getId(), status);
        } else {
            tasks = taskApi.getTasksByProject(selectedProject.getId());
        }
        lvTodo
                .setItems(FXCollections
                        .observableArrayList(tasks.stream().filter(t -> "ToDo".equals(t.getStatus())).toList()));
        lvDoing.setItems(
                FXCollections
                        .observableArrayList(tasks.stream().filter(t -> "InProgress".equals(t.getStatus())).toList()));
        lvDone
                .setItems(FXCollections
                        .observableArrayList(tasks.stream().filter(t -> "Done".equals(t.getStatus())).toList()));
    }

    @FXML
    private void handleEditTask() {
        // TODO: Hiện dialog sửa task (có thể mở TaskDialogView.fxml)
        // Gợi ý: lấy task được chọn từ ListView, mở dialog, sau khi sửa xong thì
        // loadTasks();
    }

    @FXML
    private void handleDeleteTask() {
        // TODO: Xóa task được chọn (có thể xác nhận trước khi xóa)
        // Sau khi xóa xong thì loadTasks();
    }
}
