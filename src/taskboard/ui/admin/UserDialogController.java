package taskboard.ui.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import taskboard.model.UserDTO;

public class UserDialogController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbRole;

    @FXML
    public void initialize() {
        // Khởi tạo danh sách quyền (chỉ ADMIN và MEMBER)
        cbRole.getItems().addAll("ADMIN", "MEMBER");
        cbRole.getSelectionModel().select("MEMBER"); // Mặc định chọn Member
    }

    // --- Hàm dùng cho việc TẠO MỚI (Lấy dữ liệu ra) ---
    public UserDTO getNewUser() {
        String user = txtUsername.getText().trim();
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cbRole.getValue();

        // ID để là 0 (API sẽ tự tăng), Status mặc định Active
        return new UserDTO(0, user, name, email, role, "Active");
    }

    // --- Hàm dùng cho việc SỬA (Đổ dữ liệu vào) ---
    public void setEditData(UserDTO user) {
        txtUsername.setText(user.getUsername());
        txtUsername.setDisable(true); // Không cho phép sửa Username (thường là khóa chính)
        
        txtFullName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        cbRole.getSelectionModel().select(user.getRole());
    }
}