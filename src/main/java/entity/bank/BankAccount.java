package entity.bank;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
public class BankAccount implements Serializable {
    // 添加 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    // Getters and Setters
    private String accountId;
    private String userId;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createTime;

    public BankAccount() {}

    public BankAccount(String accountId, String userId, BigDecimal balance, String status, LocalDateTime createTime) {
        this.accountId = accountId;
        this.userId = userId;
        this.balance = balance;
        this.status = status;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "accountId='" + accountId + '\'' +
                ", userId='" + userId + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
