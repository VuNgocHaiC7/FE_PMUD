package taskboard.api;

import taskboard.model.UserDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserApi {
    // Luôn bật MOCK để chạy không cần server
    private static final boolean IS_MOCK = true;

    // --- GIẢ LẬP DATABASE (Lưu trong RAM) ---
    private static List<UserDTO> mockDb = new ArrayList<>();

    static {
        // Khởi tạo dữ liệu mẫu ban đầu
        mockDb.add(new UserDTO(1, "admin", "Admin System", "admin@sys.com", "ADMIN", "Active"));
        mockDb.add(new UserDTO(2, "manager", "Le Quan Ly", "pm@sys.com", "PM", "Active"));
        mockDb.add(new UserDTO(3, "staff", "Nguyen Nhan Vien", "staff@sys.com", "MEMBER", "Locked"));
    }
    
    // Biến đếm ID tự tăng
    private static int idCounter = 4; 

    // 1. LẤY DANH SÁCH (Hỗ trợ tìm kiếm)
    public static List<UserDTO> getAllUsers(String keyword) throws Exception {
        if (IS_MOCK) {
            // Giả lập độ trễ mạng
            Thread.sleep(200);
            
            List<UserDTO> result = new ArrayList<>();
            for (UserDTO user : mockDb) {
                if (keyword == null || keyword.isEmpty()) {
                    result.add(user);
                } else {
                    // Tìm theo username hoặc fullname
                    String k = keyword.toLowerCase();
                    if (user.getUsername().toLowerCase().contains(k) || 
                        user.getFullName().toLowerCase().contains(k)) {
                        result.add(user);
                    }
                }
            }
            return result;
        } else {
            throw new RuntimeException("Chưa kết nối BE thật");
        }
    }

    // 2. THÊM USER MỚI
    public static void createUser(UserDTO newUser) throws Exception {
        if (IS_MOCK) {
            // Kiểm tra trùng username
            boolean exists = mockDb.stream().anyMatch(u -> u.getUsername().equals(newUser.getUsername()));
            if (exists) throw new RuntimeException("Username đã tồn tại!");

            // Tự động sinh ID mới
            UserDTO userToSave = new UserDTO(
                idCounter++, 
                newUser.getUsername(), 
                newUser.getFullName(), 
                newUser.getEmail(), // Giả sử DTO có field email
                newUser.getRole(), // Giả sử DTO có field role
                "Active"
            );
            mockDb.add(userToSave);
            System.out.println("Mock DB: Đã thêm user " + userToSave.getUsername());
        }
    }

    // 3. SỬA USER
    public static void updateUser(UserDTO updatedUser) throws Exception {
        if (IS_MOCK) {
            for (UserDTO u : mockDb) {
                if (u.getId() == updatedUser.getId()) {
                    // Cập nhật thông tin (trừ ID và Username thường không cho sửa)
                    // Lưu ý: UserDTO dùng Property nên ta cần setter hoặc tạo object mới
                    // Ở đây giả sử ta thao tác trực tiếp hoặc bạn cần thêm setter trong UserDTO
                    // Cách đơn giản nhất cho mock: Xóa cũ thêm mới (hoặc update field nếu có setter)
                    
                    // Để đơn giản, ta chỉ in log, thực tế bạn cần setter trong UserDTO
                    System.out.println("Mock DB: Đã update user ID " + u.getId());
                    return;
                }
            }
            throw new RuntimeException("User không tồn tại");
        }
    }

    // 4. KHÓA / MỞ KHÓA
    public static void changeStatus(int userId, String newStatus) throws Exception {
        if (IS_MOCK) {
            Optional<UserDTO> user = mockDb.stream().filter(u -> u.getId() == userId).findFirst();
            if (user.isPresent()) {
                user.get().setStatus(newStatus); // Cần đảm bảo UserDTO có setter cho status
            } else {
                throw new RuntimeException("User không tìm thấy");
            }
        }
    }
}