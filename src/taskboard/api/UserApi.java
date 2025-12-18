package taskboard.api;

import taskboard.model.UserDTO;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserApi {
    // Chuyển sang false để kết nối server thật
    private static final boolean IS_MOCK = false;

    // --- GIẢ LẬP DATABASE (Lưu trong RAM) - Chỉ dùng khi IS_MOCK = true ---
    private static List<UserDTO> mockDb = new ArrayList<>();
    static {
        // Khởi tạo dữ liệu mẫu ban đầu
        mockDb.add(new UserDTO(1, "admin", "Admin System", "admin@sys.com", "ADMIN", "Active"));
        mockDb.add(new UserDTO(2, "manager", "Le Quan Ly", "pm@sys.com", "PM", "Active"));
        mockDb.add(new UserDTO(3, "staff", "Nguyen Nhan Vien", "staff@sys.com", "MEMBER", "Locked"));
    }
    private static int idCounter = 4; 

    // 1. GET /api/users - Lấy danh sách tất cả người dùng
    public static List<UserDTO> getAllUsers() throws Exception {
        if (IS_MOCK) {
            Thread.sleep(200);
            return new ArrayList<>(mockDb);
        } else {
            String responseJson = ApiClient.get("/users");
            return parseUserList(responseJson);
        }
    }

    // 2. GET /api/users (với keyword tìm kiếm)
    public static List<UserDTO> getAllUsers(String keyword) throws Exception {
        if (IS_MOCK) {
            Thread.sleep(200);
            List<UserDTO> result = new ArrayList<>();
            for (UserDTO user : mockDb) {
                if (keyword == null || keyword.isEmpty()) {
                    result.add(user);
                } else {
                    String k = keyword.toLowerCase();
                    if (user.getUsername().toLowerCase().contains(k) || 
                        user.getFullName().toLowerCase().contains(k)) {
                        result.add(user);
                    }
                }
            }
            return result;
        } else {
            String endpoint = "/users";
            // QUAN TRỌNG: Gửi keyword dưới dạng query parameter ?keyword=xxx
            if (keyword != null && !keyword.trim().isEmpty()) {
                endpoint += "?keyword=" + java.net.URLEncoder.encode(keyword.trim(), "UTF-8");
            }
            String responseJson = ApiClient.get(endpoint);
            return parseUserList(responseJson);
        }
    }

    // 3. POST /api/users - Admin tạo User mới
    public static void createUser(UserDTO newUser) throws Exception {
        if (IS_MOCK) {
            boolean exists = mockDb.stream().anyMatch(u -> u.getUsername().equals(newUser.getUsername()));
            if (exists) throw new RuntimeException("Username đã tồn tại!");

            UserDTO userToSave = new UserDTO(
                idCounter++, 
                newUser.getUsername(), 
                newUser.getFullName(), 
                newUser.getEmail(),
                newUser.getRole(),
                "Active"
            );
            mockDb.add(userToSave);
            System.out.println("Mock DB: Đã thêm user " + userToSave.getUsername());
        } else {
            String jsonBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"fullName\": \"%s\", \"role\": \"%s\"}",
                newUser.getUsername(),
                "default123", // Mật khẩu mặc định hoặc lấy từ form
                newUser.getEmail(),
                newUser.getFullName(),
                newUser.getRole()
            );
            ApiClient.post("/users", jsonBody);
        }
    }

    // 4. PUT /api/users/{id}/role - Admin thay đổi quyền hạn của User
    public static void updateUserRole(int userId, String newRole) throws Exception {
        if (IS_MOCK) {
            for (UserDTO u : mockDb) {
                if (u.getId() == userId) {
                    u.setRole(newRole);
                    System.out.println("Mock DB: Đã update role của user " + userId + " thành " + newRole);
                    return;
                }
            }
            throw new RuntimeException("User không tồn tại");
        } else {
            String jsonBody = String.format("{\"role\": \"%s\"}", newRole);
            ApiClient.put("/users/" + userId + "/role", jsonBody);
        }
    }

    // 5. PUT /api/users/{id}/status - Khóa hoặc Mở khóa user theo ID (Toggle)
    public static void updateUserStatus(int userId, String newStatus) throws Exception {
        if (IS_MOCK) {
            for (UserDTO u : mockDb) {
                if (u.getId() == userId) {
                    u.setStatus(newStatus);
                    System.out.println("Mock DB: Đã update status của user " + userId + " thành " + newStatus);
                    return;
                }
            }
            throw new RuntimeException("User không tìm thấy");
        } else {
            // Backend chỉ toggle, không cần gửi body
            // Chỉ cần gọi PUT /users/{id}/status là backend tự đảo trạng thái
            ApiClient.put("/users/" + userId + "/status", "");
        }
    }

    // Alias for backward compatibility
    public static void changeStatus(int userId, String newStatus) throws Exception {
        updateUserStatus(userId, newStatus);
    }

    // 6. Cập nhật thông tin User (BONUS - nếu cần)
    public static void updateUser(UserDTO updatedUser) throws Exception {
        if (IS_MOCK) {
            for (UserDTO u : mockDb) {
                if (u.getId() == updatedUser.getId()) {
                    u.setFullName(updatedUser.getFullName());
                    u.setEmail(updatedUser.getEmail());
                    u.setRole(updatedUser.getRole());
                    System.out.println("Mock DB: Đã update user ID " + u.getId());
                    return;
                }
            }
            throw new RuntimeException("User không tồn tại");
        } else {
            String jsonBody = String.format(
                "{\"fullName\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}",
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
            );
            ApiClient.put("/users/" + updatedUser.getId(), jsonBody);
        }
    }
    
    // 6. DELETE /api/users/{id} - Xóa người dùng (Chỉ admin)
    public static void deleteUser(int userId) throws Exception {
        if (IS_MOCK) {
            mockDb.removeIf(user -> user.getId() == userId);
            System.out.println("API: Đã xóa user ID " + userId);
        } else {
            System.out.println(">>> Calling DELETE /api/users/" + userId);
            ApiClient.delete("/users/" + userId);
            System.out.println(">>> DELETE request completed successfully");
        }
    }

    // Helper: Parse JSON array thành List<UserDTO>
    private static List<UserDTO> parseUserList(String jsonResponse) {
        List<UserDTO> users = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                // QUAN TRỌNG: Backend có field "boolean isActive" 
                // Nhưng Jackson serialize thành JSON với tên "active" (không phải "isActive")
                // Vì vậy phải dùng "active" thay vì "isActive"
                boolean isActive = obj.optBoolean("active", true);
                String status = isActive ? "Active" : "Locked";
                
                UserDTO user = new UserDTO(
                    obj.getInt("id"),
                    obj.getString("username"),
                    obj.optString("fullName", ""),
                    obj.optString("email", ""),
                    obj.optString("role", "MEMBER"),
                    status
                );
                users.add(user);
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON users: " + e.getMessage());
        }
        return users;
    }
}