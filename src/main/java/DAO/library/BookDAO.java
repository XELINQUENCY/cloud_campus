package DAO.library; // 包名与您的MyBatisUtil保持一致

import DAO.MyBatisUtil;
import entity.library.Book;
import entity.library.BookCopy;
import mapper.BookCopyMapper;
import mapper.BookMapper;

import java.util.List;

public class BookDAO {

    /**
     * 根据多个条件动态查询书籍。
     * @param title      书名 (模糊匹配)
     * @param author     作者 (模糊匹配)
     * @param publisher  出版社 (模糊匹配)
     * @param categoryId 分类ID (精确匹配)
     * @return 包含查询结果的 Book 列表。
     */
    public List<Book> findBooksByCriteria(String title, String author, String publisher, Integer categoryId) {
        return MyBatisUtil.executeQuery(BookMapper.class, mapper -> mapper.findBooksByCriteria(title, author, publisher, categoryId));
    }

    /**
     * 根据书籍ID查找书籍。
     * @param bookId 书籍ID
     * @return 找到则返回 Book 对象，否则返回 null。
     */
    public Book findBookById(int bookId) {
        return MyBatisUtil.executeQuery(BookMapper.class, mapper -> mapper.findBookById(bookId));
    }


    /**
     * 更新一本书籍的信息。
     * @param book 包含更新信息的 Book 对象。
     * @return 更新成功返回 true，失败返回 false。
     */
    public boolean updateBook(Book book) {
        int result = MyBatisUtil.executeUpdate(BookMapper.class, mapper -> mapper.updateBook(book));
        return result > 0;
    }


    /**
     * 添加一本书籍的基本信息。
     * @param book 要添加的书籍对象
     * @return 操作成功返回 true，失败返回 false。成功后，book对象的ID会被自动填充。
     */
    public boolean addBookInfo(Book book) {
        // executeUpdate 内部处理了 commit/rollback
        return MyBatisUtil.executeUpdate(BookMapper.class, mapper -> mapper.insertBook(book)) > 0;
    }

    /**
     * 为一本书批量添加副本记录。
     * @param copies 书籍副本的列表
     * @return 操作成功返回 true，失败返回 false。
     */
    public boolean addBookCopies(List<BookCopy> copies) {
        if (copies.isEmpty()) return true; // 如果不需要添加副本，直接返回成功
        return MyBatisUtil.executeUpdate(BookCopyMapper.class, mapper -> mapper.insertCopies(copies)) > 0;
    }
}