package dto;

/**
 * 数据传输对象 (DTO) - 用于封装登录请求的数据。
 */
public class LoginRequest {
    private final String username;
    private final String password;
    private final boolean isAdmin; // 增加一个字段用于区分管理员登录

    public LoginRequest(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

