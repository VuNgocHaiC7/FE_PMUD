package taskboard.ui.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import taskboard.auth.AuthContext;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label userLabel;

    @FXML
    public void initialize() {
        // Hiển thị tên người dùng từ AuthContext
        String fullName = AuthContext.getInstance().getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            userLabel.setText(fullName);
        } else {
            userLabel.setText("Người dùng");
        }
        
        // Có thể load mặc định màn hình Dashboard khi vừa vào
        // showDashboard(null);
    }

    // Hàm tiện ích để load view vào vùng Center
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainBorderPane.setCenter(view); // Thay thế nội dung cũ bằng view mới
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể load file FXML: " + fxmlPath);
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        System.out.println("Chuyển sang Dashboard / Báo cáo");
        setActiveButton(event);
        // loadView("/taskboard/ui/dashboard/DashboardView.fxml");
    }

    @FXML
    void showUserManagement(ActionEvent event) {
        System.out.println("Chuyển sang Quản lý người dùng");
        setActiveButton(event);
        loadView("/taskboard/ui/admin/UserManagementView.fxml");
    }

    @FXML
    void showProjectManagement(ActionEvent event) {
        System.out.println("Chuyển sang Quản lý dự án & thành viên");
        setActiveButton(event);
        loadView("/taskboard/ui/project/ProjectListView.fxml");
    }

    @FXML
    void showKanban(ActionEvent event) {
        System.out.println("Chuyển sang Bảng công việc (Kanban)");
        setActiveButton(event);
        // Load màn hình danh sách project để chọn project xem board
        loadView("/taskboard/ui/kanbanBoard/KanbanProjectListView.fxml");
    }

    @FXML
    void showTaskDetail(ActionEvent event) {
        System.out.println("Chuyển sang Chi tiết task & bình luận");
        setActiveButton(event);
        // loadView("/taskboard/ui/task/TaskDetailView.fxml");
    }

    @FXML
    void showNotifications(ActionEvent event) {
        System.out.println("Chuyển sang Thông báo");
        setActiveButton(event);
        // loadView("/taskboard/ui/notification/NotificationView.fxml");
    }

    @FXML
    void showMyTasks(ActionEvent event) {
        System.out.println("Chuyển sang My Tasks (Công việc của tôi)");
        setActiveButton(event);
        // loadView("/taskboard/ui/mytasks/MyTasksView.fxml");
    }
    
    @FXML
    void showFilter(ActionEvent event) {
        System.out.println("Mở Filter");
        // Mở dialog hoặc panel filter
    }

    @FXML
    void handleCreate(ActionEvent event) {
        System.out.println("Tạo mới task/project");
        // Mở dialog tạo mới
    }

    @FXML
    void showConfig(ActionEvent event) {
        System.out.println("Chuyển sang Cấu hình hệ thống");
        setActiveButton(event);
        // loadView("/taskboard/ui/config/ConfigView.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // Hiển thị dialog xác nhận đăng xuất
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đăng xuất");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        confirmAlert.setContentText("Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Xóa thông tin đăng nhập
            AuthContext.getInstance().logout();
            System.out.println("Đăng xuất thành công!");
            
            // Chuyển về màn hình đăng nhập
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/LoginView.fxml"));
                Parent loginRoot = loader.load();
                Scene loginScene = new Scene(loginRoot);
                
                // Lấy Stage hiện tại từ BorderPane
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                stage.setScene(loginScene);
                stage.setTitle("TaskBoard - Login");
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Không thể load màn hình đăng nhập: " + e.getMessage());
                
                // Hiển thị thông báo lỗi cho người dùng
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi");
                errorAlert.setHeaderText("Không thể chuyển về màn hình đăng nhập");
                errorAlert.setContentText("Vui lòng khởi động lại ứng dụng.");
                errorAlert.showAndWait();
            }
        }
    }

    // Phương thức để highlight nút đang được chọn
    private void setActiveButton(ActionEvent event) {
        // Xóa style active khỏi tất cả các nút
        mainBorderPane.lookupAll(".menu-btn").forEach(node -> {
            node.getStyleClass().remove("menu-btn-active");
        });
        
        // Thêm style active cho nút được click
        if (event.getSource() instanceof javafx.scene.control.Button) {
            javafx.scene.control.Button clickedButton = (javafx.scene.control.Button) event.getSource();
            clickedButton.getStyleClass().add("menu-btn-active");
        }
    }
}