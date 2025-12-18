package taskboard.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

// Import AuthApi để gọi chức năng đăng ký
import taskboard.auth.AuthApi;

public class SignUpController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;

    // Các trường nhập liệu (Khớp với fx:id trong SignUpView.fxml)
    @FXML private TextField fullNameField;       // Mới thêm
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;
    @FXML private Button signupButton;

    @FXML
    public void initialize() {
        // Làm ảnh nền co giãn theo cửa sổ
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    /**
     * Xử lý khi bấm nút SIGN UP
     */
    @FXML
    public void handleSignUp(ActionEvent event) {
        // 1. Lấy dữ liệu và xóa khoảng trắng thừa
        String fullName = fullNameField.getText().trim();
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // 2. Kiểm tra dữ liệu rỗng
        if (fullName.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ tất cả thông tin!");
            return;
        }

        // 3. Kiểm tra mật khẩu khớp nhau
        if (!pass.equals(confirm)) {
            showAlert("Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // 4. GỌI API: POST /api/auth/register
        try {
            AuthApi.register(user, pass, email, fullName);

            // 5. XỬ LÝ THÀNH CÔNG:
            // - Thông báo thành công
            showAlert("Thành công", "Đăng ký thành công! Bạn sẽ được chuyển về màn hình đăng nhập.");
            
            // - Chuyển về form Login
            handleSwitchToLogin(null);

        } catch (Exception e) {
            // Hiển thị lỗi từ server (ví dụ: Username đã tồn tại)
            e.printStackTrace();
            showAlert("Đăng ký thất bại", "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Chuyển về màn hình Login (khi bấm chữ Login hoặc đăng ký xong)
     */
    @FXML
    public void handleSwitchToLogin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/LoginView.fxml"));
            javafx.scene.Parent loginRoot = loader.load();
            
            // Lấy Scene hiện tại để thay thế nội dung
            Scene currentScene;
            if (event != null) {
                // Nếu được gọi từ sự kiện chuột click
                currentScene = ((Node) event.getSource()).getScene();
            } else {
                // Nếu được gọi tự động từ code (event = null), lấy qua rootPane
                currentScene = rootPane.getScene();
            }
            
            if (currentScene != null) {
                currentScene.setRoot(loginRoot);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể quay lại màn hình đăng nhập: " + e.getMessage());
        }
    }

    /**
     * Hàm hiển thị thông báo chung
     */
    private void showAlert(String title, String content) {
        AlertType type = AlertType.INFORMATION;
        
        // Nếu tiêu đề chứa chữ "Lỗi" hoặc "thất bại" thì đổi icon thành Error
        if (title.contains("Lỗi") || title.contains("thất bại")) {
            type = AlertType.ERROR;
        }
        
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}