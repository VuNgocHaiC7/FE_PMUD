package taskboard.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
// Import thêm
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class LoginController {

    @FXML
    private StackPane rootPane; // Khung chứa chính
    @FXML
    private ImageView bgImage;  // Ảnh nền

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    // Hàm tự động chạy khi màn hình mở
    @FXML
    public void initialize() {
        // Ràng buộc kích thước ảnh theo cửa sổ
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.equals("admin") && password.equals("123")) {
            showAlert("Thành công", "Đăng nhập thành công!");
            // Code chuyển màn hình chính sau này
        } else {
            showAlert("Thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleSwitchToSignUp(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/SignUpView.fxml"));
            javafx.scene.Parent signUpRoot = loader.load();
            
            // Lấy Scene hiện tại
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            
            // Thay vì tạo Scene mới, ta chỉ thay thế nội dung bên trong
            // Điều này giúp giữ nguyên kích thước cửa sổ đang có
            currentScene.setRoot(signUpRoot);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}