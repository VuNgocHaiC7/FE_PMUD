package taskboard.auth;

import java.util.List;

public class LoginResponse {
    public String token;
    public Long userId;
    public String fullName;
    public List<String> roles;
}
