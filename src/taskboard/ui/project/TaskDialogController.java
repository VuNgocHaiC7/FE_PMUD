package taskboard.ui.project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import taskboard.api.ProjectApi;
import taskboard.api.TaskApi;
import taskboard.auth.AuthContext;
import taskboard.model.CommentDTO;
import taskboard.model.TaskDTO;
import taskboard.model.UserDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class TaskDialogController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ListView<UserDTO> lvAssignees; // Thay ListView
    
    // VBox containers for hiding
    @FXML private VBox vboxStatus;
    @FXML private VBox vboxAssignees;
    
    // COMMENT FIELDS
    @FXML private VBox vboxComments;
    @FXML private TextArea txtComment;
    @FXML private Button btnSendComment;
    @FXML private Label lblCommentCount;
    
    // BUTTONS
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private TaskDTO currentTask;
    private Long projectId;
    private boolean saved = false;
    private Runnable onTaskSavedCallback;
    
    // Auto-refresh for comments
    private Timer commentRefreshTimer;
    private Set<Long> existingCommentIds = new HashSet<>();

    /**
     * Chuyển đổi status từ tiếng Anh tiếng Việt
     */
    private String statusToVietnamese(String englishStatus) {
        if (englishStatus == null) return "CẦN LÀM";
        switch (englishStatus) {
            case "TODO":
            case "ToDo":
                return "CẦN LÀM";
            case "IN_PROGRESS":
            case "InProgress":
                return "ĐANG LÀM";
            case "DONE":
            case "Done":
                return "HOÀN THÀNH";
            default:
                return "CẦN LÀM";
        }
    }
    
    /**
     * Chuyển đổi status từ tiếng Việt sang tiếng Anh 
     */
    private String statusToEnglish(String vietnameseStatus) {
        if (vietnameseStatus == null) return "TODO";
        switch (vietnameseStatus) {
            case "CẦN LÀM":
                return "TODO";
            case "ĐANG LÀM":
                return "IN_PROGRESS";
            case "HOÀN THÀNH":
                return "DONE";
            default:
                return "TODO";
        }
    }

    @FXML
    public void initialize() {
        // Khởi tạo danh sách trạng thái
        cbStatus.setItems(FXCollections.observableArrayList("CẦN LÀM", "ĐANG LÀM", "HOÀN THÀNH"));
        cbStatus.getSelectionModel().select("CẦN LÀM"); // Mặc định là CẦN LÀM
        
        // 1. Cấu hình cho phép chọn nhiều
        lvAssignees.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 2. Xử lý Logic Click (Dùng EventFilter)
        lvAssignees.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            javafx.scene.Node node = (javafx.scene.Node) event.getTarget();
            
            // Tìm component cha là ListCell
            while (node != null && node != lvAssignees && !(node instanceof ListCell)) {
                node = node.getParent();
            }

            // Nếu click trúng cell có dữ liệu
            if (node instanceof ListCell) {
                event.consume(); // Chặn sự kiện mặc định
                
                ListCell<UserDTO> cell = (ListCell<UserDTO>) node;
                if (cell.getItem() != null) {
                    int index = cell.getIndex();
                    // Toggle selection
                    if (lvAssignees.getSelectionModel().isSelected(index)) {
                        lvAssignees.getSelectionModel().clearSelection(index);
                    } else {
                        lvAssignees.getSelectionModel().select(index);
                    }
                    lvAssignees.refresh(); // Cập nhật màu ngay
                }
            }
        });

        // 3. Cấu hình Giao diện (Chỉ hiển thị)
        lvAssignees.setCellFactory(lv -> new ListCell<UserDTO>() {
            @Override
            protected void updateItem(UserDTO user, boolean empty) {
                super.updateItem(user, empty);
                
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    boolean isSelected = lv.getSelectionModel().getSelectedItems().contains(user);
                    
                    if (isSelected) {
                        setText("✓ " + user.getFullName() + " (" + user.getRole() + ")");
                        setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
                    } else {
                        setText(user.getFullName() + " (" + user.getRole() + ")");
                        setStyle("-fx-padding: 8; -fx-background-color: transparent; -fx-text-fill: black;");
                    }
                }
            }
        });
        
        // Kiểm tra role và ẩn các control nếu user là MEMBER
        setupRoleBasedVisibility();
    }
    
    /**
     * Ẩn các control dựa trên role của user
     */
    private void setupRoleBasedVisibility() {
        AuthContext authContext = AuthContext.getInstance();
        List<String> roles = authContext.getRoles();
        boolean isMember = roles != null && roles.contains("MEMBER") && !roles.contains("ADMIN");
        
        if (isMember) {
            // Ẩn VBox chứa ComboBox trạng thái
            if (vboxStatus != null) {
                vboxStatus.setVisible(false);
                vboxStatus.setManaged(false);
            }
            
            // Ẩn VBox chứa ListView giao cho
            if (vboxAssignees != null) {
                vboxAssignees.setVisible(false);
                vboxAssignees.setManaged(false);
            }
            
            // Ẩn nút Lưu Task
            if (btnSave != null) {
                btnSave.setVisible(false);
                btnSave.setManaged(false);
            }
        }
    }

    /**
     * Set dự án và load danh sách thành viên
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
        loadProjectMembers();
    }

    /**
     * Set task để sửa (nếu là mode EDIT)
     */
    public void setTask(TaskDTO task) {
        this.currentTask = task;
        if (task != null) {
            txtTitle.setText(task.getTitle());
            txtDescription.setText(task.getDescription());
            cbStatus.setValue(statusToVietnamese(task.getStatus()));
            
            // Chọn nhiều assignees trong ListView
            if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
                for (UserDTO user : lvAssignees.getItems()) {
                    if (task.getAssigneeIds().contains((long) user.getId())) {
                        lvAssignees.getSelectionModel().select(user);
                    }
                }
            }
            // Fallback: nếu chỉ có một assignee 
            else if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                for (UserDTO user : lvAssignees.getItems()) {
                    if (user.getFullName().equals(task.getAssignee())) {
                        lvAssignees.getSelectionModel().select(user);
                        break;
                    }
                }
            }
            
            // Load comments nếu task đã tồn tại (có ID)
            if (task.getId() != null && task.getId() > 0) {
                loadComments();
            }
        }
    }

    /**
     * Load danh sách thành viên của dự án
     */
    private void loadProjectMembers() {
        if (projectId != null) {
            try {
                List<UserDTO> members = ProjectApi.getProjectMembers(projectId);
                lvAssignees.setItems(FXCollections.observableArrayList(members));
                System.out.println("DEBUG: Đã load " + members.size() + " thành viên");
            } catch (Exception e) {
                System.err.println("Lỗi load members: " + e.getMessage());
                lvAssignees.setItems(FXCollections.observableArrayList());
            }
        }
    }

    @FXML
    private void handleSave() {
        // Validate
        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tiêu đề task!");
            return;
        }

        // Tạo hoặc cập nhật task
        if (currentTask == null) {
            currentTask = new TaskDTO();
            currentTask.setProjectId(projectId);
        }

        currentTask.setTitle(txtTitle.getText().trim());
        currentTask.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : "");
        currentTask.setStatus(statusToEnglish(cbStatus.getValue()));
        
        // Lấy TOÀN BỘ danh sách người được chọn trong ListView
        List<UserDTO> selectedUsers = new ArrayList<>(lvAssignees.getSelectionModel().getSelectedItems());
        
        System.out.println("=== SAVE TASK DEBUG ===");
        System.out.println("Selected users count: " + selectedUsers.size());
        for (UserDTO u : selectedUsers) {
            System.out.println("  - " + u.getFullName() + " (ID: " + u.getId() + ")");
        }
        
        if (selectedUsers != null && !selectedUsers.isEmpty()) {
            List<Long> assigneeIds = selectedUsers.stream()
                .map(u -> (long) u.getId())
                .collect(Collectors.toList());
            List<String> assigneeNames = selectedUsers.stream()
                .map(UserDTO::getFullName)
                .collect(Collectors.toList());
            
            currentTask.setAssigneeIds(assigneeIds);
            currentTask.setAssigneeNames(assigneeNames);
            
            // Để tương thích với code set assignee đầu tiên
            currentTask.setAssignee(assigneeNames.get(0));
            currentTask.setAssigneeId(assigneeIds.get(0));
            
            System.out.println("AssigneeIds being sent: " + assigneeIds);
        } else {
            // Nếu không có ai được chọn, gửi danh sách rỗng
            currentTask.setAssigneeIds(new ArrayList<>());
            currentTask.setAssigneeNames(new ArrayList<>());
            currentTask.setAssignee("");
            currentTask.setAssigneeId(null);
            
            System.out.println("No assignees selected - sending empty list");
        }

        // Lưu task qua API
        try {
            if (currentTask.getId() == null || currentTask.getId() == 0L) {
                // GỌI API: POST /api/tasks (Tạo task mới - Chỉ Admin)
                System.out.println("Creating new task...");
                TaskApi.createTask(currentTask);
            } else {
                // GỌI API: PUT /api/tasks/{id} (Cập nhật task - Chỉ Admin)
                System.out.println("Updating task ID: " + currentTask.getId());
                TaskApi.updateTask(currentTask);
            }
            saved = true;
            notifyTaskSaved();
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể lưu task: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        stopAutoRefresh();
        closeWindow();
    }

    private void closeWindow() {
        stopAutoRefresh();
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getter để ProjectBoardController biết task đã được lưu chưa
    public boolean isSaved() {
        return saved;
    }

    public TaskDTO getTask() {
        return currentTask;
    }

    // Set callback để thông báo khi task được lưu
    public void setOnTaskSaved(Runnable callback) {
        this.onTaskSavedCallback = callback;
    }

    // Thực hiện callback sau khi lưu
    private void notifyTaskSaved() {
        if (onTaskSavedCallback != null) {
            onTaskSavedCallback.run();
        }
    }
    
    // ========== COMMENT METHODS ==========
    
    /**
     * Load comments từ server và hiển thị
     */
    private void loadComments() {
        if (currentTask == null || currentTask.getId() == null) {
            return;
        }
        
        vboxComments.getChildren().clear();
        existingCommentIds.clear();
        
        try {
            List<CommentDTO> comments = TaskApi.getComments(currentTask.getId());
            
            if (comments.isEmpty()) {
                Label lblEmpty = new Label("Chưa có bình luận nào");
                lblEmpty.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
                vboxComments.getChildren().add(lblEmpty);
                updateCommentCount(0);
            } else {
                for (CommentDTO comment : comments) {
                    existingCommentIds.add(comment.getId());
                    VBox commentBox = createCommentBox(comment, false);
                    vboxComments.getChildren().add(commentBox);
                }
                updateCommentCount(comments.size());
            }
            
            startAutoRefresh();
            
        } catch (Exception e) {
            System.err.println("Lỗi load comments: " + e.getMessage());
            Label lblError = new Label("Không thể tải bình luận");
            lblError.setStyle("-fx-text-fill: #f56565;");
            vboxComments.getChildren().add(lblError);
        }
    }
    
    /**
     * Tạo UI cho một comment
     * @param isNew true nếu là comment mới (để highlight)
     */
    private VBox createCommentBox(CommentDTO comment, boolean isNew) {
        VBox box = new VBox(4);
        
        // Highlight comment mới bằng màu xanh nhạt
        if (isNew) {
            box.setStyle("-fx-background-color: #E6F7FF; -fx-padding: 10; -fx-background-radius: 6; " +
                         "-fx-border-color: #1890FF; -fx-border-width: 2; -fx-border-radius: 6;");
        } else {
            box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 6; " +
                         "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 6;");
        }
        
        // Header: Username + Time + New badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblUsername = new Label(comment.getUsername());
        lblUsername.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 13px;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label lblTime = new Label(comment.getCreatedAt().format(formatter));
        lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        
        header.getChildren().addAll(lblUsername, lblTime);
        
        // Add "NEW" badge for new comments
        if (isNew) {
            Label lblNew = new Label("MỚI");
            lblNew.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white; " +
                           "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; " +
                           "-fx-background-radius: 3;");
            header.getChildren().add(lblNew);
        }
        
        // Content
        Label lblContent = new Label(comment.getContent());
        lblContent.setWrapText(true);
        lblContent.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        
        box.getChildren().addAll(header, lblContent);
        return box;
    }
    
    /**
     * Handler: Gửi comment mới
     */
    @FXML
    private void handleSendComment() {
        // Kiểm tra xem đã có task chưa
        if (currentTask == null || currentTask.getId() == null || currentTask.getId() == 0) {
            showAlert("Thông báo", "Vui lòng lưu task trước khi bình luận!");
            return;
        }
        
        String content = txtComment.getText();
        if (content == null || content.trim().isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập nội dung bình luận!");
            return;
        }
        
        try {
            // Gọi API gửi comment
            CommentDTO newComment = TaskApi.addComment(currentTask.getId(), content.trim());
            
            if (newComment != null) {
                // Thêm comment mới vào cuối danh sách
                VBox commentBox = createCommentBox(newComment, false);
                
                // Xóa label "Chưa có bình luận" nếu có
                if (vboxComments.getChildren().size() == 1 && 
                    vboxComments.getChildren().get(0) instanceof Label) {
                    vboxComments.getChildren().clear();
                }
                
                vboxComments.getChildren().add(commentBox);
                existingCommentIds.add(newComment.getId());
                updateCommentCount(existingCommentIds.size());
                
                // Clear input
                txtComment.clear();
                
                System.out.println("✓ Đã gửi comment thành công!");
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể gửi bình luận: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== AUTO-REFRESH MECHANISM ==========
    
    /**
     * Bắt đầu auto-refresh comments mỗi 5 giây
     */
    private void startAutoRefresh() {
        if (commentRefreshTimer != null) {
            commentRefreshTimer.cancel();
        }
        
        commentRefreshTimer = new Timer(true); // daemon thread
        commentRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkForNewComments());
            }
        }, 5000, 5000); // Check every 5 seconds
        
        System.out.println("✓ Auto-refresh bắt đầu cho task " + currentTask.getId());
    }
    
    /**
     * Kiểm tra và hiển thị comment mới
     */
    private void checkForNewComments() {
        if (currentTask == null || currentTask.getId() == null) {
            return;
        }
        
        try {
            List<CommentDTO> allComments = TaskApi.getComments(currentTask.getId());
            List<CommentDTO> newComments = new ArrayList<>();
            
            // Tìm comment mới (chưa có trong existingCommentIds)
            for (CommentDTO comment : allComments) {
                if (!existingCommentIds.contains(comment.getId())) {
                    newComments.add(comment);
                    existingCommentIds.add(comment.getId());
                }
            }
            
            // Nếu có comment mới, thêm vào danh sách với highlight
            if (!newComments.isEmpty()) {
                // Xóa label "Chưa có bình luận" nếu có
                if (vboxComments.getChildren().size() == 1 && 
                    vboxComments.getChildren().get(0) instanceof Label) {
                    vboxComments.getChildren().clear();
                }
                
                for (CommentDTO newComment : newComments) {
                    VBox commentBox = createCommentBox(newComment, true); // isNew = true
                    vboxComments.getChildren().add(commentBox);
                    System.out.println("Comment mới từ " + newComment.getUsername() + ": " + newComment.getContent());
                }
                
                updateCommentCount(existingCommentIds.size());
                
                // Scroll to bottom để hiển thị comment mới
                vboxComments.layout();
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi check new comments: " + e.getMessage());
        }
    }
    
    /**
     * Dừng auto-refresh khi đóng dialog
     */
    private void stopAutoRefresh() {
        if (commentRefreshTimer != null) {
            commentRefreshTimer.cancel();
            commentRefreshTimer = null;
            System.out.println("✓ Auto-refresh đã dừng");
        }
    }
    
    /**
     * Cập nhật số lượng comment hiển thị
     */
    private void updateCommentCount(int count) {
        if (lblCommentCount != null) {
            lblCommentCount.setText("(" + count + ")");
        }
    }
}
