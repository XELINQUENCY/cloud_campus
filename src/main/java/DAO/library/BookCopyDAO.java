package DAO.library; // 同样，包名与MyBatisUtil保持一致

import DAO.MyBatisUtil;
import mapper.BookCopyMapper;
import entity.library.BookCopy;

import java.util.List;

/**
 * 书籍副本数据访问对象 (BookCopyDAO) - MyBatis 实现
 * 负责调用 BookCopyMapper 来执行数据库操作。
 */
public class BookCopyDAO {

    /**
     * 根据书籍信息ID查找一个当前可供借阅的副本。
     * @param bookId 书籍信息ID
     * @return 如果找到可用的副本，返回该 BookCopy 对象；否则返回 null。
     */
    public static BookCopy findAvailableCopyByBookId(int bookId) {
        return MyBatisUtil.executeQuery(BookCopyMapper.class, mapper -> mapper.findAvailableCopyByBookId(bookId));
    }

    /**
     * 根据副本ID查找其所属的书籍信息ID。
     * @param copyId 副本ID
     * @return 如果找到，返回对应的 book_id；否则返回 -1。
     */
    public static int findBookIdByCopyId(int copyId) {
        Integer bookId = MyBatisUtil.executeQuery(BookCopyMapper.class, mapper -> mapper.findBookIdByCopyId(copyId));
        return (bookId != null) ? bookId : -1; // 保持与原代码相同的返回值约定
    }

    /**
     * 查找指定书籍的所有副本列表。
     * @param bookId 书籍信息ID
     * @return 包含该书籍所有副本的列表。
     */
    public static List<BookCopy> findCopiesByBookId(int bookId) {
        return MyBatisUtil.executeQuery(BookCopyMapper.class, mapper -> mapper.findCopiesByBookId(bookId));
    }


    /**
     * 更新指定副本的状态。
     * @param copyId    要更新的副本ID
     * @param newStatus 新的状态
     * @return 更新成功返回 true，失败返回 false
     */
    public static boolean updateCopyStatus(int copyId, String newStatus) {
        int result = MyBatisUtil.executeUpdate(BookCopyMapper.class, mapper -> mapper.updateCopyStatus(copyId, newStatus));
        return result > 0;
    }
}