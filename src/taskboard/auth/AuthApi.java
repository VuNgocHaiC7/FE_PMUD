package taskboard.auth;

import taskboard.api.ApiClient;

public class AuthApi {

    // >>> QUAN TRỌNG: ĐỔI THÀNH FALSE ĐỂ GỌI SERVER THẬT <<<
    private static final boolean IS_MOCK = false; 

    // 1. Hàm Đăng Nhập (Login)
    public static LoginResponse login(String username, String password) throws Exception {
        if (IS_MOCK) {
            // ... (Giữ nguyên logic Mock cũ nếu muốn, hoặc xóa đi cũng được) ...
            LoginResponse res = new LoginResponse();
            res.token = "mock-token";
            return res;
        } else {
            // --- LOGIC REAL (GỌI SERVER) ---
            String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
            
            // Gọi API
            System.out.println("Đang gọi Login tới: " + jsonBody);
            String responseJson = ApiClient.post("/auth/login", jsonBody);
            System.out.println("=== SERVER TRẢ VỀ ===");
            System.out.println(responseJson);
            System.out.println("======================");
            
            // --- XỬ LÝ KẾT QUẢ (Cắt chuỗi thủ công để lấy Token) ---
            LoginResponse res = new LoginResponse();
            
            // 1. Lấy Token (Tìm đoạn "token":"...")
            if (responseJson.contains("\"token\"")) {
                // Cắt chuỗi đơn giản: Tách sau chữ "token":" và lấy đến dấu " tiếp theo
                String[] parts = responseJson.split("\"token\":");
                if (parts.length > 1) {
                    String tokenPart = parts[1].split("\"")[1]; // Lấy nội dung trong ngoặc kép
                    res.token = tokenPart;
                    
                    // LƯU TOKEN VÀO CONTEXT ĐỂ DÙNG CHO CÁC API KHÁC
                    AuthContext.getInstance().setToken(tokenPart);
                    AuthContext.getInstance().setUserId(1L); // Tạm thời hardcode ID, sau này parse sau
                }
            } else {
                throw new RuntimeException("Login thành công nhưng không thấy Token trả về!");
            }

            // 2. Lấy FullName từ response JSON
            if (responseJson.contains("\"fullName\"")) {
                String[] parts = responseJson.split("\"fullName\":");
                if (parts.length > 1) {
                    String fullNamePart = parts[1].split("\"")[1];
                    res.fullName = fullNamePart;
                    System.out.println("✓ Đã parse fullName (camelCase): " + fullNamePart);
                }
            } else if (responseJson.contains("\"full_name\"")) {
                // Trường hợp backend trả về là full_name (snake_case)
                String[] parts = responseJson.split("\"full_name\":");
                if (parts.length > 1) {
                    String fullNamePart = parts[1].split("\"")[1];
                    res.fullName = fullNamePart;
                    System.out.println("✓ Đã parse full_name (snake_case): " + fullNamePart);
                }
            } else {
                res.fullName = username; // Fallback về username nếu không có fullName
                System.out.println("⚠ Không tìm thấy fullName trong response, dùng username: " + username);
            }
            
            System.out.println("Final fullName được set: " + res.fullName);
            return res; 
        }
    }

    // 2. Hàm Đăng Ký (Register)
    public static void register(String username, String password, String email, String fullName) throws Exception {
        // Tạo JSON body
        String jsonBody = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"fullName\": \"%s\", \"role\": \"MEMBER\"}",
            username, password, email, fullName
        );

        // Gọi API
        ApiClient.post("/auth/register", jsonBody);
    }
}