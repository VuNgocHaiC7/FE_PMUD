package taskboard.auth;

import java.util.Arrays;

public class AuthApi {

    // login "fake" chỉ chạy FE
    // sau này có backend chỉ cần sửa lại hàm này
    public static LoginResponse login(String username, String password) throws Exception {
        // mô phỏng thời gian gọi API
        Thread.sleep(500);

        // TODO: bạn chỉnh lại rule tùy đề bài
        // Ví dụ: user = "admin", pass = "123456"
        if ("admin".equals(username) && "123456".equals(password)) {
            LoginResponse res = new LoginResponse();
            res.token = "fake-jwt-token-123";    // token giả
            res.userId = 1L;
            res.fullName = "Admin User";
            res.roles = Arrays.asList("ADMIN");
            return res;
        }

        // có thể thêm user khác nếu muốn
        if ("user".equals(username) && "123456".equals(password)) {
            LoginResponse res = new LoginResponse();
            res.token = "fake-token-user-456";
            res.userId = 2L;
            res.fullName = "Normal User";
            res.roles = Arrays.asList("USER");
            return res;
        }

        // sai thông tin đăng nhập
        throw new RuntimeException("Sai username hoặc password");
    }
}
