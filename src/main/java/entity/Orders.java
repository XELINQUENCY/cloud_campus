package entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class Orders {
    private int id;
    private String userId;
    private BigDecimal totalAmount;

    private String status;
    private LocalDateTime createTime;
}
