package taskboard.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Import các class Auth của bạn
import taskboard.auth.AuthApi;
import taskboard.auth.AuthContext;
import taskboard.auth.LoginResponse;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        // Binding hình nền cho đẹp, responsive theo cửa sổ
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        // 1. Validate đầu vào
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", AlertType.WARNING);
            return;
        }

        try {
            // 2. GỌI API: POST /api/auth/login (hỗ trợ cả username và email)
            LoginResponse response = AuthApi.login(usernameOrEmail, password);

            // 3. XỬ LÝ THÀNH CÔNG:
            // - Lưu Token và FullName vào biến toàn cục (Session)
            AuthContext.getInstance().setToken(response.token);
            AuthContext.getInstance().setUserId(response.userId);            AuthContext.getInstance().setUsername(response.username);            AuthContext.getInstance().setFullName(response.fullName);
            AuthContext.getInstance().setRoles(response.roles);

            System.out.println("✓ Đăng nhập thành công!");
            System.out.println("  User: " + response.fullName);
            System.out.println("  Roles: " + response.roles);

            // 4. Kiểm tra Role để phân quyền (nếu cần)
            // Có thể dùng để hiển thị menu khác nhau cho Admin/PM/Member

            // 5. Chuyển sang màn hình Danh sách dự án (MainView)
            switchToMainView();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu!\n" + e.getMessage(), AlertType.ERROR);
        }
    }

    // --- HÀM CHUYỂN HƯỚNG QUAN TRỌNG ---
    private void switchToMainView() {
        try {
            System.out.println(">>> Bắt đầu chuyển sang MainView...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/main/MainView.fxml"));
            System.out.println(">>> Đường dẫn FXML: " + getClass().getResource("/taskboard/ui/main/MainView.fxml"));
            
            Parent mainRoot = loader.load();
            System.out.println(">>> Load FXML thành công!");

            // 1. Đóng cửa sổ Login cũ lại
            Stage oldStage = (Stage) loginButton.getScene().getWindow();
            oldStage.close();
            System.out.println(">>> Đã đóng cửa sổ login cũ");

            // 2. Tạo cửa sổ mới cho Main App
            Stage newStage = new Stage();
            newStage.setScene(new Scene(mainRoot));
            newStage.setTitle("TaskBoard - Hệ thống quản lý công việc");
            
            // Mở lên là full màn hình luôn
            newStage.setMaximized(true);
            newStage.show();
            System.out.println(">>> Hiển thị MainView thành công!");

        } catch (Exception e) {
            System.err.println("!!! LỖI KHI CHUYỂN SANG MAINVIEW !!!");
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển sang màn hình chính!\n" + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    public void handleSwitchToSignUp(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/SignUpView.fxml"));
            Parent signUpRoot = loader.load();
            Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            currentScene.setRoot(signUpRoot);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển sang trang đăng ký.", AlertType.ERROR);
        }
    }

    // Hàm hiển thị thông báo chung
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}