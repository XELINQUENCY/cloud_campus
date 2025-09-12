package mapper;

import entity.library.Book;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookMapper {

    /**
     * 根据多个条件动态查询书籍。
     * @param title      书名 (模糊匹配)
     * @param author     作者 (模糊匹配)
     * @param publisher  出版社 (模糊匹配)
     * @param categoryId 分类ID (精确匹配)
     * @return 包含查询结果的 Book 列表。
     */
    List<Book> findBooksByCriteria(
            @Param("title") String title,
            @Param("author") String author,
            @Param("publisher") String publisher,
            @Param("categoryId") Integer categoryId
    );

    /**
     * 根据书籍ID查找书籍。
     * @param bookId 书籍ID
     * @return 找到则返回 Book 对象，否则返回 null。
     */
    Book findBookById(int bookId);

    /**
     * 插入一本新的书籍信息。
     * 该方法会返回自增主键到传入的 book 对象中。
     * @param book 要添加的书籍对象 (不包含bookId)
     * @return 受影响的行数。
     */
    int insertBook(Book book);

    /**
     * 更新一本书籍的信息。
     * @param book 包含更新信息的 Book 对象。
     * @return 受影响的行数。
     */
    int updateBook(Book book);

    /**
     * 更新指定书籍的可借阅副本数量。
     * @param bookId 要更新的书籍ID
     * @param delta  数量变化值 (借书时为 -1, 还书时为 +1)
     * @return 受影响的行数。
     */
    int updateAvailableCopies(@Param("bookId") int bookId, @Param("delta") int delta);
}