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
    @Serial
    private static final long serialVersionUID = 1L;

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


}
