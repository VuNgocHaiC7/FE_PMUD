package taskboard.auth;

import taskboard.api.ApiClient;
// import com.fasterxml.jackson.databind.ObjectMapper; // Vẫn comment nếu chưa có Jackson
import java.util.Arrays;

public class AuthApi {

    // >>> QUAN TRỌNG: ĐỔI THÀNH FALSE ĐỂ GỌI SERVER THẬT <<<
    private static final boolean IS_MOCK = false;
    
    // private static final ObjectMapper mapper = new ObjectMapper();

    // 1. Hàm Đăng Nhập (Login)
    public static LoginResponse login(String username, String password) throws Exception {
        if (IS_MOCK) {
            // ... (Code Mock cũ giữ nguyên để phòng hờ) ...
            if ("admin".equals(username) && "123456".equals(password)) {
                 LoginResponse res = new LoginResponse();
                 res.token = "mock-token"; res.fullName = "Admin Mock";
                 return res;
            }
            throw new RuntimeException("Sai thông tin (Mock)");
        } else {
            // GỌI SERVER THẬT
            // Endpoint: /auth/login (Ghép với Base URL sẽ thành .../tms/api/auth/login)
            String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
            String responseJson = ApiClient.post("/auth/login", jsonBody);
            
            // Vì chưa có Jackson, ta parse thủ công tạm thời hoặc dùng thư viện nếu đã cài
            // Ở đây tôi giả định bạn đã cài Jackson như hướng dẫn trước để code gọn
            // return mapper.readValue(responseJson, LoginResponse.class);
            
            // NẾU CHƯA CÓ JACKSON, DÙNG TẠM CÁCH NÀY ĐỂ TRÁNH LỖI:
            // (Chỉ dùng tạm để test kết nối, sau này nên cài Jackson)
            LoginResponse res = new LoginResponse();
            res.token = "real-token-placeholder"; 
            res.fullName = "Real User";
            System.out.println("Login Response từ Server: " + responseJson);
            return res; 
        }
    }

    // 2. Hàm Đăng Ký (Register) - MỚI THÊM
    public static void register(String username, String password, String email, String fullName) throws Exception {
        if (IS_MOCK) {
            System.out.println("Mock Register: " + username);
            Thread.sleep(500);
        } else {
            // GỌI SERVER THẬT
            // Tạo JSON body thủ công (Cẩn thận dấu ngoặc kép)
            // Giả sử server cần các trường: username, password, email, fullName
            String jsonBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"fullName\": \"%s\"}",
                username, password, email, fullName
            );

            // Endpoint bạn cung cấp là /auth/register
            String response = ApiClient.post("/auth/register", jsonBody);
            System.out.println("Server phản hồi đăng ký: " + response);
        }
    }
}