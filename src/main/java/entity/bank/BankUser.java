package entity.bank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class BankUser implements Serializable {
    // 添加 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    // Getters and Setters
    private String userId;
    private String password; // 加密后的密码
    private LocalDateTime createTime;


    public BankUser(String userId, String password, LocalDateTime createTime) {
        this.userId = userId;
        this.password = password;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "BankUser{" +
                "userId='" + userId + '\'' +
                ", createTime=" + createTime +
                '}';}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	
}
