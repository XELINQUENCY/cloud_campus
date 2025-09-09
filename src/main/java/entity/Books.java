package entity;

import enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Books {
    private String bookId;

    private String title;
    private String author;
    private String publisher;
    private BookStatus status;
}
