package taskboard.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

// >>> IMPORT MỚI CẦN THÊM <<<
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
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim(); // Thêm .trim() để cắt khoảng trắng thừa
    String password = passwordField.getText();

    // >>> THÊM 2 DÒNG NÀY ĐỂ DEBUG <<<
    System.out.println("Username nhập vào: [" + username + "]");
    System.out.println("Password nhập vào: [" + password + "]");

        // 1. Validate sơ bộ
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            // 2. GỌI API ĐĂNG NHẬP (Kết nối AuthApi)
            // Hàm này sẽ tự check Mock hoặc Real dựa trên config bên trong nó
            LoginResponse response = AuthApi.login(username, password);

            // 3. LƯU THÔNG TIN VÀO CONTEXT (Quan trọng nhất)
            // Nếu không có bước này, ApiClient sẽ không có token để dùng
            AuthContext.getInstance().setToken(response.token);
            AuthContext.getInstance().setUserId(response.userId);
            AuthContext.getInstance().setFullName(response.fullName);
            AuthContext.getInstance().setRoles(response.roles);

            // 4. Thông báo và chuyển màn hình
            // showAlert("Thành công", "Xin chào " + response.fullName); 
            // (Thường thì login xong sẽ chuyển cảnh luôn chứ không hiện popup)
            
            switchToMainView();

        } catch (Exception e) {
            // Nếu sai pass hoặc lỗi mạng, AuthApi sẽ throw Exception
            // Ta bắt lỗi đó để hiển thị lên màn hình
            e.printStackTrace(); // In lỗi ra console để debug
            showAlert("Đăng nhập thất bại", e.getMessage());
        }
    }

    // Hàm chuyển sang màn hình chính (Dashboard/UserManagement)
    private void switchToMainView() {
        try {
            // Ví dụ chuyển sang màn hình Admin User mà bạn sắp làm
            // Đường dẫn này tùy thuộc vào file FXML bạn tạo tiếp theo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/admin/UserManagementView.fxml"));
            javafx.scene.Parent mainRoot = loader.load();
            
            // Lấy Stage hiện tại và set Scene mới
            javafx.scene.Scene currentScene = loginButton.getScene();
            currentScene.setRoot(mainRoot);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể tải màn hình chính: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR); // Đổi thành ERROR cho dễ nhìn
        if (title.equals("Thành công")) alert.setAlertType(AlertType.INFORMATION);
        
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleSwitchToSignUp(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskboard/ui/SignUpView.fxml"));
            javafx.scene.Parent signUpRoot = loader.load();
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            currentScene.setRoot(signUpRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}