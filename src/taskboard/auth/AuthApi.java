package taskboard.auth;

import taskboard.api.ApiClient;

public class AuthApi {

    // >>> QUAN TRỌNG: ĐỔI THÀNH FALSE ĐỂ GỌI SERVER THẬT <<<
    private static final boolean IS_MOCK = false; // Đổi thành true để test không cần server

    // 1. Hàm Đăng Nhập (Login) - Hỗ trợ cả Username và Email
    public static LoginResponse login(String usernameOrEmail, String password) throws Exception {
        if (IS_MOCK) {
            // Mock mode: Chấp nhận bất kỳ username/password nào
            System.out.println("MOCK MODE: Đăng nhập với username/email: " + usernameOrEmail);
            LoginResponse res = new LoginResponse();
            res.token = "mock-token-" + usernameOrEmail;
            res.userId = 1L;
            res.fullName = "User Mock - " + usernameOrEmail;
            res.roles = java.util.Arrays.asList("ADMIN"); // Hoặc "MEMBER"
            
            // Lưu vào context
            AuthContext.getInstance().setToken(res.token);
            AuthContext.getInstance().setUserId(res.userId);
            AuthContext.getInstance().setFullName(res.fullName);
            AuthContext.getInstance().setRoles(res.roles);
            
            return res;
        } else {
            // --- LOGIC REAL (GỌI SERVER) ---
            // Gửi giá trị vào field username, backend sẽ tự check xem đó là username hay email
            String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", usernameOrEmail, password);
            
            // Gọi API
            System.out.println("Đang gọi Login tới: " + jsonBody);
            String responseJson = ApiClient.post("/auth/login", jsonBody);
            System.out.println("=== SERVER TRẢ VỀ ===");
            System.out.println(responseJson);
            System.out.println("======================");
            
            // --- XỬ LÝ KẾT QUẢ ---
            LoginResponse res = new LoginResponse();
            
            // 1. Lấy Token
            if (responseJson.contains("\"token\"")) {
                String[] parts1 = responseJson.split("\"token\":");
                if (parts1.length > 1) {
                    String tokenPart = parts1[1].split("\"")[1];
                    res.token = tokenPart;
                    AuthContext.getInstance().setToken(tokenPart);
                }
            } else {
                throw new RuntimeException("Login thành công nhưng không thấy Token trả về!");
            }

            // 2. Lấy userId
            if (responseJson.contains("\"userId\"")) {
                String[] parts2 = responseJson.split("\"userId\":");
                if (parts2.length > 1) {
                    String idStr = parts2[1].split(",")[0].trim();
                    res.userId = Long.parseLong(idStr);
                    AuthContext.getInstance().setUserId(res.userId);
                }
            } else if (responseJson.contains("\"id\"")) {
                String[] parts2 = responseJson.split("\"id\":");
                if (parts2.length > 1) {
                    String idStr = parts2[1].split(",")[0].trim();
                    res.userId = Long.parseLong(idStr);
                    AuthContext.getInstance().setUserId(res.userId);
                }
            }

            // 3. Lấy FullName
            if (responseJson.contains("\"fullName\"")) {
                String[] parts3 = responseJson.split("\"fullName\":");
                if (parts3.length > 1) {
                    String fullNamePart = parts3[1].split("\"")[1];
                    res.fullName = fullNamePart;
                    System.out.println("✓ Đã parse fullName: " + fullNamePart);
                }
            } else if (responseJson.contains("\"full_name\"")) {
                String[] parts3 = responseJson.split("\"full_name\":");
                if (parts3.length > 1) {
                    String fullNamePart = parts3[1].split("\"")[1];
                    res.fullName = fullNamePart;
                    System.out.println("✓ Đã parse full_name: " + fullNamePart);
                }
            } else {
                res.fullName = usernameOrEmail;
                System.out.println("⚠ Không tìm thấy fullName, dùng usernameOrEmail: " + usernameOrEmail);
            }
            
            // 3.5 Lưu username (có thể là email nếu user dùng email để login)
            if (responseJson.contains("\"username\"")) {
                String[] usernameParts = responseJson.split("\"username\":");
                if (usernameParts.length > 1) {
                    String usernamePart = usernameParts[1].split("\"")[1];
                    res.username = usernamePart;
                    AuthContext.getInstance().setUsername(usernamePart);
                }
            } else {
                res.username = usernameOrEmail;
                AuthContext.getInstance().setUsername(usernameOrEmail);
            }
            
            // 4. Lấy roles
            if (responseJson.contains("\"role\"")) {
                String[] parts4 = responseJson.split("\"role\":");
                if (parts4.length > 1) {
                    String rolePart = parts4[1].split("\"")[1];
                    res.roles = java.util.Arrays.asList(rolePart);
                    AuthContext.getInstance().setRoles(res.roles);
                }
            } else {
                res.roles = java.util.Arrays.asList("MEMBER");
            }
            
            System.out.println("Final username: " + res.username);
            System.out.println("Final fullName: " + res.fullName);
            System.out.println("Final userId: " + res.userId);
            System.out.println("Final roles: " + res.roles);
            return res; 
        }
    }

    // 2. Hàm Đăng Ký (Register)
    public static void register(String username, String password, String email, String fullName) throws Exception {
        String jsonBody = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"fullName\": \"%s\", \"role\": \"MEMBER\"}",
            username, password, email, fullName
        );
        ApiClient.post("/auth/register", jsonBody);
    }
}
