package entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reader {
    private String readerId;
    private String Name;
    private int isOverdue;
}
