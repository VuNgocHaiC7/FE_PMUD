package taskboard.auth;

import java.util.List;

public class AuthContext {

    private static AuthContext instance;

    private String token;
    private Long userId;
    private String fullName;
    private List<String> roles;

    private AuthContext() { }

    public static AuthContext getInstance() {
        if (instance == null) {
            instance = new AuthContext();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    // getters & setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
