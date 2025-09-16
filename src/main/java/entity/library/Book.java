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

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getTotalCopies() {
		return totalCopies;
	}

	public void setTotalCopies(int totalCopies) {
		this.totalCopies = totalCopies;
	}

	public int getAvailableCopies() {
		return availableCopies;
	}

	public void setAvailableCopies(int availableCopies) {
		this.availableCopies = availableCopies;
	}

	
}
