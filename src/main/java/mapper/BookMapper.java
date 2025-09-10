package mapper;

import entity.Book;
import entity.BookQueryCriteria;
import enums.BookStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BookMapper {
    /**
     * 根据图书ID查找图书
     * @param bookId 图书的唯一编号
     * @return 找到的图书对象，未找到则返回null
     */
    Book findById(@Param("bookId")String bookId);

    /**
     * 根据条件模糊查询图书（如书名、作者）
     * @param criteria 查询关键字
     * @return 符合条件的图书列表
     */
    List<Book> searchBooks(BookQueryCriteria criteria);

    /**
     * 更新图书的状态（如 "在馆" -> "借出"）
     * @param bookId 要更新的图书ID
     * @param status 新的状态
     * @return 受影响的行数
     */
    int updateStatus(@Param("bookId")String bookId, @Param("status") BookStatus status);

    /**
     * (管理员) 新增一本图书
     * @param book 要新增的图书对象
     * @return 受影响的行数
     */
    int insert(Book book);

    /**
     * (管理员) 更新图书信息
     * @param book 包含更新信息的图书对象
     * @return 受影响的行数
     */
    int update(Book book);
}