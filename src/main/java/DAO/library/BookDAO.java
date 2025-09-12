package DAO.library; // 包名与您的MyBatisUtil保持一致

import DAO.MyBatisUtil;
import entity.library.Book;
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
     * 【重要】关于事务操作 addBook 的说明：
     * 您提供的 MyBatisUtil 是为单个操作设计的，它会自动提交事务。
     * 而 addBook 需要“先插入Book，再批量插入BookCopy”，这两个操作必须在同一个事务中。
     * 因此，这个事务逻辑不应该在DAO层实现，而应该提升到 Service 层。
     * * DAO层只提供原子的数据库操作。下面是这两个原子操作的DAO方法。
     */

    /**
     * (原子操作) 添加一本书籍的基本信息。
     * @param book 要添加的书籍对象
     * @return 操作成功返回 true，失败返回 false。成功后，book对象的ID会被自动填充。
     */
    public boolean addBookInfo(Book book) {
        // executeUpdate 内部处理了 commit/rollback
        return MyBatisUtil.executeUpdate(BookMapper.class, mapper -> mapper.insertBook(book)) > 0;
    }

    /**
     * (原子操作) 为一本书批量添加副本记录。
     * @param bookId 书籍ID
     * @param count  副本数量
     * @return 操作成功返回 true，失败返回 false。
     */
    public boolean addBookCopies(int bookId, int count) {
        if (count <= 0) return true; // 如果不需要添加副本，直接返回成功
        return MyBatisUtil.executeUpdate(BookCopyMapper.class, mapper -> mapper.insertCopies(bookId, count)) > 0;
    }

    /*
     * 在您的 Service 层中，应该这样调用来保证事务性：
     *
     * public boolean createNewBook(Book book) {
     * try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession()) {
     * try {
     * BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
     * BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);
     *
     * // 1. 插入书籍信息
     * int bookResult = bookMapper.insertBook(book);
     * if (bookResult == 0) {
     * throw new RuntimeException("插入书籍信息失败！");
     * }
     *
     * // book.getBookId() 此时已经有值了
     * int newBookId = book.getBookId();
     *
     * // 2. 插入副本信息
     * if (book.getTotalCopies() > 0) {
     * int copyResult = copyMapper.insertCopies(newBookId, book.getTotalCopies());
     * if (copyResult == 0) {
     * throw new RuntimeException("插入书籍副本失败！");
     * }
     * }
     *
     * sqlSession.commit(); // 两个操作都成功，提交事务
     * return true;
     * } catch (Exception e) {
     * sqlSession.rollback(); // 任何一步失败，回滚事务
     * e.printStackTrace();
     * return false;
     * }
     * }
     * }
     */
}