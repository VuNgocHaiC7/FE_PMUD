package taskboard.ui.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
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
        // loadView("/taskboard/ui/admin/UserManagementView.fxml");
    }

    @FXML
    void showProjectManagement(ActionEvent event) {
        System.out.println("Chuyển sang Quản lý dự án & thành viên");
        setActiveButton(event);
        // loadView("/taskboard/ui/project/ProjectListView.fxml");
    }

    @FXML
    void showKanban(ActionEvent event) {
        System.out.println("Chuyển sang Bảng công việc (Kanban)");
        setActiveButton(event);
        // loadView("/taskboard/ui/project/ProjectBoardView.fxml");
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
        // Xử lý đăng xuất, quay về màn hình Login
        System.out.println("Đăng xuất...");
        // Có thể thêm dialog xác nhận trước khi đăng xuất
        // AuthContext.getInstance().logout();
        // Chuyển về LoginView
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