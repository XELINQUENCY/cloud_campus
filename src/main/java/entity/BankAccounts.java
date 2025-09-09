package entity;

import enums.BankAccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class BankAccounts {
    private String accountId;
    private String userId;
    private BigDecimal balance;
    private BankAccountStatus status;
    private LocalDateTime createdTime;

}
