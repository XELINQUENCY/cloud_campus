package entity.library;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 书籍信息实体类
 * 描述一种书的通用信息，例如《Java编程思想》。
 */
@Setter
@Getter
public class Book implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Getter 和 Setter 方法
    private int bookId;
    private String title;       // 书名
    private String author;      // 作者
    private String publisher;   // 出版社
    private String isbn;
    private int categoryId;     // 分类ID

    // 新增属性
    private int totalCopies;     // 总本数
    private int availableCopies; // 可借本数

    // 构造函数
    public Book() {}

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", availableCopies=" + availableCopies +
                '/' + totalCopies +
                '}';
    }
}
