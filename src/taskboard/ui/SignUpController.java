package taskboard.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
// Import thêm
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class SignUpController {

    @FXML private StackPane rootPane; // Khung chứa chính
    @FXML private ImageView bgImage;  // Ảnh nền

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;

    // Hàm tự động chạy
    @FXML
    public void initialize() {
        // Ràng buộc kích thước ảnh theo cửa sổ
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    @FXML
    public void handleSignUp(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!pass.equals(confirm)) {
            showAlert("Lỗi", "Mật khẩu nhập lại không khớp!");
            return;
        }

        showAlert("Thành công", "Đăng ký tài khoản thành công!");
    }

    // Xử lý khi bấm chữ "Login" để quay lại màn cũ
    @FXML
    public void handleSwitchToLogin(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/LoginView.fxml"));
            javafx.scene.Parent loginRoot = loader.load();
            
            // Lấy Scene hiện tại
            Scene currentScene = ((Node) event.getSource()).getScene();
            
            // Thay thế nội dung (giữ nguyên kích thước cửa sổ)
            currentScene.setRoot(loginRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}