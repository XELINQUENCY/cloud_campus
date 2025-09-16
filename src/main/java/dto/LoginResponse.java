package dto;

import entity.User;
import lombok.Getter;

/**
 * 数据传输对象 (DTO) - 用于封装登录成功的响应数据。
 */
@Getter
public class LoginResponse {
    private final String token;
    private final User user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

	public String getToken() {
		// TODO Auto-generated method stub
		return token;
	}

	public User getUser() {
		return user;
	}
}
