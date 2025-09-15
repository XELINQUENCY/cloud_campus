package mapper;

import entity.library.BookCopy;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * BookCopyMapper 接口，定义所有对 'book_copies' 表的原子数据库操作
 */
public interface BookCopyMapper {

    /**
     * 【已有方法】为指定的书籍ID批量插入副本记录。
     * @param bookCopies 每个书籍副本
     * @return 成功插入的记录数
     */
    int insertCopies(List<BookCopy> bookCopies);

    /**
     * 【已有方法】根据书籍信息ID查找一个可用的副本。
     * @param bookId 书籍信息ID
     * @return 返回一个可用的 BookCopy 对象，或 null
     */
    BookCopy findAvailableCopyByBookId(int bookId);

    /**
     * 【新增】根据书籍ID查找一个为预约而保留的副本。
     * @param bookId 书籍信息ID
     * @return 返回一个状态为“保留”的 BookCopy 对象，或 null
     */
    BookCopy findReservedCopyByBookId(int bookId);

    /**
     * 【已有方法】根据副本ID查找其所属的书籍信息ID。
     * @param copyId 副本ID
     * @return 返回对应的 book_id，如果未找到则返回 null
     */
    Integer findBookIdByCopyId(int copyId);

    /**
     * 【已有方法】更新指定副本的状态。
     * @param copyId    要更新的副本ID
     * @param newStatus 新的状态
     * @return 受影响的行数
     */
    int updateCopyStatus(@Param("copyId") int copyId, @Param("newStatus") String newStatus);

    /**
     * 【已有方法】根据书籍信息ID查找其所有的副本列表。
     * @param bookId 书籍信息ID
     * @return 该书籍所有副本的列表
     */
    List<BookCopy> findCopiesByBookId(int bookId);
}
