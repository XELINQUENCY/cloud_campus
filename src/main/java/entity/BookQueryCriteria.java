package entity;

import enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookQueryCriteria {
    private String title;
    private String author;
    private String publisher;
    private BookStatus status;
}
