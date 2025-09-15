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
public class Transaction implements Serializable {
    // 添加 serialVersionUID
    @Serial
    private static final long serialVersionUID = 1L;

    // Getters and Setters
    private long transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String type;
    private LocalDateTime timestamp;
    private String memo;

    public Transaction() {}

    public Transaction(long transactionId, String fromAccount, String toAccount,
                       BigDecimal amount, String type, LocalDateTime timestamp, String memo) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.memo = memo;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", memo='" + memo + '\'' +
                '}';
    }
}
