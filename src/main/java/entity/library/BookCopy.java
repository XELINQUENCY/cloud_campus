package entity.library;

import enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class BookCopy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int copyId;
    private int bookId; // 关联的 Book
    private BookStatus status; // 状态: "在馆" (available), "已借出" (borrowed), "已预约" (reserved), "遗失" (lost) 等


    public BookCopy(int copyId, int bookId, BookStatus status) {
        this.copyId = copyId;
        this.bookId = bookId;
        this.status = status;
    }


    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId=" + copyId +
                ", bookId=" + bookId +
                ", status='" + status + '\'' +
                '}';
    }
}
