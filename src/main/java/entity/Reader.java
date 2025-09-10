package entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reader {
    private String readerId;
    private String name;
    private int isOverdue;
}
