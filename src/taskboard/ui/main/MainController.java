package taskboard.ui.main;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import taskboard.api.NotificationApi;
import taskboard.auth.AuthContext;
import taskboard.model.NotificationDTO;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label userLabel;
    
    @FXML
    private Label lblNotificationBadge;
    
    // Notification polling
    private Timer notificationTimer;
    private long lastNotificationCount = 0;
    private Popup currentNotificationPopup;

    @FXML
    public void initialize() {
        // Hiển thị tên người dùng từ AuthContext
        String fullName = AuthContext.getInstance().getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            userLabel.setText(fullName);
        } else {
            userLabel.setText("Người dùng");
        }
        
        // Bắt đầu notification polling
        startNotificationPolling();
        
        // Có thể load mặc định màn hình Dashboard khi vừa vào
        // showDashboard(null);
    }

    // Hàm tiện ích để load view vào vùng Center
    private void loadView(String fxmlPath) {
        try {
            System.out.println("=== Loading view: " + fxmlPath + " ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainBorderPane.setCenter(view); // Thay thế nội dung cũ bằng view mới
            System.out.println("=== Successfully loaded: " + fxmlPath + " ===");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("!!! KHÔNG THỂ LOAD FILE FXML: " + fxmlPath);
            System.err.println("!!! Lỗi: " + e.getMessage());
            
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải giao diện");
            alert.setContentText("Không thể load: " + fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("!!! LỖI KHÁC KHI LOAD: " + fxmlPath);
            System.err.println("!!! Lỗi: " + e.getMessage());
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        System.out.println("Chuyển sang Dashboard / Báo cáo");
        setActiveButton(event);
        loadView("/taskboard/ui/dashboard/DashboardView.fxml");
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
    void showTaskManagement(ActionEvent event) {
        System.out.println("Chuyển sang Quản lý task");
        setActiveButton(event);
        loadView("/taskboard/ui/task/TaskListView.fxml");
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
    
    // ========== NOTIFICATION METHODS ==========
    
    /**
     * Bắt đầu polling để check notifications mới
     */
    private void startNotificationPolling() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
        
        notificationTimer = new Timer(true);
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkNotifications());
            }
        }, 0, 10000); // Check mỗi 10 giây
        
        System.out.println("✓ Notification polling started");
    }
    
    /**
     * Kiểm tra và cập nhật badge notifications
     */
    private void checkNotifications() {
        try {
            long count = NotificationApi.getUnreadCount();
            
            if (count > 0) {
                lblNotificationBadge.setText(String.valueOf(count));
                lblNotificationBadge.setVisible(true);
                
                // Nếu có notification mới (count tăng), hiển thị popup
                if (count > lastNotificationCount) {
                    showNewNotificationPopup();
                }
                lastNotificationCount = count;
            } else {
                lblNotificationBadge.setVisible(false);
                lastNotificationCount = 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi check notifications: " + e.getMessage());
        }
    }
    
    /**
     * Hiển thị popup khi có notification mới
     */
    private void showNewNotificationPopup() {
        try {
            List<NotificationDTO> unreadNotifications = NotificationApi.getUnreadNotifications();
            if (unreadNotifications.isEmpty()) return;
            
            // Chỉ hiển thị notification mới nhất
            NotificationDTO latest = unreadNotifications.get(0);
            
            Platform.runLater(() -> {
                VBox popupContent = new VBox(10);
                popupContent.setStyle("-fx-background-color: white; -fx-padding: 15; " +
                                    "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
                popupContent.setPrefWidth(350);
                
                // Header
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label iconLabel = new Label("🔔");
                iconLabel.setStyle("-fx-font-size: 20px;");
                Label titleLabel = new Label("Thông báo mới");
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                header.getChildren().addAll(iconLabel, titleLabel);
                
                // Message
                Label messageLabel = new Label(latest.getMessage());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
                
                // Time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
                Label timeLabel = new Label(latest.getCreatedAt().format(formatter));
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
                
                // Button
                Button btnView = new Button("Xem chi tiết");
                btnView.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                               "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
                btnView.setOnAction(e -> {
                    if (currentNotificationPopup != null) {
                        currentNotificationPopup.hide();
                    }
                    handleShowNotifications(null);
                });
                
                popupContent.getChildren().addAll(header, messageLabel, timeLabel, btnView);
                
                // Tạo và hiển thị popup
                Popup popup = new Popup();
                popup.getContent().add(popupContent);
                currentNotificationPopup = popup;
                
                // Hiển thị ở góc phải trên
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                popup.show(stage, stage.getX() + stage.getWidth() - 370, stage.getY() + 80);
                
                // Fade in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popupContent);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                
                // Tự động ẩn sau 5 giây
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (popup.isShowing()) {
                                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), popupContent);
                                fadeOut.setFromValue(1);
                                fadeOut.setToValue(0);
                                fadeOut.setOnFinished(ev -> popup.hide());
                                fadeOut.play();
                            }
                        });
                    }
                }, 5000);
            });
            
        } catch (Exception e) {
            System.err.println("Lỗi hiển thị popup: " + e.getMessage());
        }
    }
    
    /**
     * Hiển thị danh sách tất cả notifications
     */
    @FXML
    public void handleShowNotifications(javafx.scene.input.MouseEvent event) {
        System.out.println(">>> handleShowNotifications được gọi!");
        try {
            System.out.println(">>> Đang gọi API lấy notifications...");
            List<NotificationDTO> notifications = NotificationApi.getAllNotifications();
            System.out.println(">>> Nhận được " + (notifications != null ? notifications.size() : "null") + " notifications");
            
            // Tạo dialog hiển thị danh sách
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Thông báo");
            dialog.setHeaderText("Danh sách thông báo của bạn");
            
            VBox content = new VBox(10);
            content.setPadding(new Insets(10));
            
            if (notifications == null || notifications.isEmpty()) {
                Label emptyLabel = new Label("Bạn chưa có thông báo nào");
                emptyLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
                content.getChildren().add(emptyLabel);
            } else {
                for (NotificationDTO notif : notifications) {
                    VBox notifBox = createNotificationItem(notif);
                    content.getChildren().add(notifBox);
                }
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setPrefWidth(500);
            
            // Đánh dấu tất cả đã đọc khi mở dialog
            dialog.setOnCloseRequest(e -> {
                try {
                    NotificationApi.markAllAsRead();
                    checkNotifications(); // Refresh badge
                } catch (Exception ex) {
                    System.err.println("Lỗi mark all as read: " + ex.getMessage());
                }
            });
            
            System.out.println(">>> Hiển thị dialog...");
            dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("!!! LỖI hiển thị notifications: " + e.getMessage());
            e.printStackTrace();
            
            // Hiển thị thông báo lỗi cho user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi");
            errorAlert.setHeaderText("Không thể tải thông báo");
            errorAlert.setContentText("Lỗi: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    
    /**
     * Tạo UI cho một notification item
     */
    private VBox createNotificationItem(NotificationDTO notif) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle(notif.isRead() ? 
            "-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;" :
            "-fx-background-color: #E6F7FF; -fx-border-color: #1890FF; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with actor
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label actorLabel = new Label(notif.getActorFullName() != null ? notif.getActorFullName() : "Hệ thống");
        actorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM");
        Label timeLabel = new Label(notif.getCreatedAt().format(formatter));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        
        if (!notif.isRead()) {
            Label newBadge = new Label("MỚI");
            newBadge.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white; " +
                            "-fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 3;");
            header.getChildren().addAll(actorLabel, newBadge, timeLabel);
        } else {
            header.getChildren().addAll(actorLabel, timeLabel);
        }
        
        // Message
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        
        box.getChildren().addAll(header, messageLabel);
        return box;
    }
    
    /**
     * Cleanup khi đóng app
     */
    public void cleanup() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
        if (currentNotificationPopup != null && currentNotificationPopup.isShowing()) {
            currentNotificationPopup.hide();
        }
    }
}