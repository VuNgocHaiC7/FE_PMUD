package taskboard.auth;

import taskboard.api.ApiClient;
// import com.fasterxml.jackson.databind.ObjectMapper; // Vẫn comment nếu chưa có Jackson
import java.util.Arrays;

public class AuthApi {

    // >>> QUAN TRỌNG: ĐỔI THÀNH FALSE ĐỂ GỌI SERVER THẬT <<<
    private static final boolean IS_MOCK = true;
    
    // private static final ObjectMapper mapper = new ObjectMapper();

    // 1. Hàm Đăng Nhập (Login)
    public static LoginResponse login(String username, String password) throws Exception {
        if (IS_MOCK) {
            // --- LOGIC MOCK ---
            if ("admin".equals(username) && "123456".equals(password)) {
                 LoginResponse res = new LoginResponse();
                 res.token = "mock-token-admin"; 
                 res.fullName = "Admin Mock";
                 return res;
            } 
            else if ("pm".equals(username) && "123456".equals(password)) {
                LoginResponse res = new LoginResponse();
                res.token = "mock-token-pm"; 
                res.fullName = "Project Manager Mock";
                return res;
            }
            
            // Nếu không khớp user nào thì báo lỗi (Nằm TRONG khối if IS_MOCK)
            throw new RuntimeException("Sai thông tin (Mock) - Chỉ chấp nhận admin/123456 hoặc pm/123456");
            
        } else {
            // --- LOGIC REAL (GỌI SERVER) ---
            String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
            
            // Gọi API
            String responseJson = ApiClient.post("/auth/login", jsonBody);
            
            // Xử lý kết quả trả về (Tạm thời mock object trả về vì chưa có Jackson)
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