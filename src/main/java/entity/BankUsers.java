package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BankUsers {
    private String userId;
    private String password;
    private LocalDateTime createdTime;
}
