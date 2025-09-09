package entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderItems {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal price;
}
