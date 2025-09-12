package view;

import entity.library.BorrowRecord;
import java.io.Serializable;

/**
 * 用于界面显示的借阅记录视图模型
 * 封装了原始的 BorrowRecord 对象和客户端需要的额外信息（如书名）。
 */
public class BorrowRecordView implements Serializable {
    private static final long serialVersionUID = 1L;

    private BorrowRecord borrowRecord;
    private String bookTitle;

    public BorrowRecordView(BorrowRecord borrowRecord, String bookTitle) {
        this.borrowRecord = borrowRecord;
        this.bookTitle = bookTitle;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}