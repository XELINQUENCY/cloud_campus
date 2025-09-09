package entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Transaction {
    private int transactionId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime timestamp;
    private String memo;

}
