package taskboard.model;

import javafx.beans.property.*;

public class UserDTO {
    // Dùng Property của JavaFX để bảng tự động cập nhật khi dữ liệu đổi
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty role;     // Admin, PM, Member
    private final StringProperty status;   // Active, Locked

    // Constructor đầy đủ
    public UserDTO(int id, String username, String fullName, String email, String role, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
    }
    
    // Constructor rỗng (Cần thiết sau này nếu dùng thư viện Jackson để parse JSON)
    public UserDTO() {
        this(0, "", "", "", "", "");
    }

    // --- 1. GETTERS TRẢ VỀ PROPERTY (Dùng cho TableView - cellValueFactory) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty roleProperty() { return role; }
    public StringProperty statusProperty() { return status; }
    
    // --- 2. GETTERS TRẢ VỀ GIÁ TRỊ THƯỜNG (Dùng để lấy dữ liệu xử lý logic) ---
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getFullName() { return fullName.get(); }
    
    // >>> ĐÃ BỔ SUNG THÊM <<<
    public String getEmail() { return email.get(); }
    public String getRole() { return role.get(); }
    public String getStatus() { return status.get(); }
    
    // --- 3. SETTERS (Dùng để cập nhật dữ liệu khi Sửa User) ---
    // ID và Username thường không cho sửa nên có thể không cần Setter
    
    public void setFullName(String newName) { this.fullName.set(newName); }
    public void setEmail(String newEmail) { this.email.set(newEmail); }
    public void setRole(String newRole) { this.role.set(newRole); }
    public void setStatus(String newStatus) { this.status.set(newStatus); }
    
    // --- 4. TOSTRING (Dùng để hiển thị trong ComboBox/ListView) ---
    @Override
    public String toString() {
        return getFullName() + " (" + getUsername() + ")";
    }
}