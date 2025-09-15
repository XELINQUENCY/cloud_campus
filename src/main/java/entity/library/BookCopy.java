package entity.library;

import enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int copyId;
    private int bookId; // 关联的 Book
    private BookStatus status; // 状态: "在架" (available), "已借出" (borrowed), "已预约" (reserved), "遗失" (lost) 等

    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId=" + copyId +
                ", bookId=" + bookId +
                ", status='" + status + '\'' +
                '}';
    }
}
