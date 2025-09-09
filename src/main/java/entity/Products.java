package entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Products {
    private int id;
    private String name;
    private String description;

    private String imageUrl;
    private BigDecimal price;
    private int stock;
}
