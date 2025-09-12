package service.library;

import entity.User;
import entity.library.Book;
import entity.library.Category;
import entity.library.LibraryProfile;
import view.BorrowRecordView;
import view.ReservationView;

import java.util.List;

/**
 * 图书馆模块业务逻辑服务接口 (重构版)
 * 定义了所有与图书馆相关的业务操作。
 * 这是一个纯粹的业务接口，与RMI或HTTPS等具体通信技术无关。
 */
public interface LibraryService {

    // --- 用户认证与状态 ---
    /**
     * 用户登录验证。
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回统一的用户实体，失败则抛出异常。
     * @throws Exception 登录验证失败时（如用户名或密码错误、无权限）抛出。
     */
    User login(String username, String password) throws Exception;

    /**
     * 刷新图书馆用户的状态信息。
     * @param mainUserId 统一用户ID
     * @return 最新的图书馆用户档案，包含罚款等信息。
     */
    LibraryProfile refreshLibraryProfile(String mainUserId);


    // --- 公共查询操作 ---
    /**
     * 根据多种条件搜索书籍。
     * @param title      书名 (模糊匹配)
     * @param author     作者 (模糊匹配)
     * @param publisher  出版社 (模糊匹配)
     * @param categoryId 分类ID (精确匹配)
     * @return 符合条件的书籍列表。
     */
    List<Book> searchBooks(String title, String author, String publisher, Integer categoryId);

    /**
     * 获取所有书籍分类。
     * @return 所有书籍分类的列表。
     */
    List<Category> getAllCategories();


    // --- 普通用户核心业务 ---
    /**
     * 用户借阅一本书。
     * @param mainUserId 借阅用户的统一ID
     * @param bookId     要借阅的书籍ID
     * @return 操作结果的描述信息。
     * @throws Exception 业务异常，如用户有罚款、书籍无库存等。
     */
    String borrowBook(String mainUserId, int bookId) throws Exception;

    /**
     * 用户归还一本指定的书籍副本。
     * @param copyId 要归还的书籍副本ID
     * @return 操作结果的描述信息，可能包含罚款通知。
     * @throws Exception 业务或数据库异常。
     */
    String returnBook(int copyId) throws Exception;

    /**
     * 用户续借一本尚未逾期的书籍。
     * @param recordId 要续借的借阅记录ID
     * @return 操作结果的描述信息。
     * @throws Exception 业务异常，如已超期、续借达上限等。
     */
    String renewBook(int recordId) throws Exception;

    /**
     * 用户预约一本当前无库存的书籍。
     * @param mainUserId 预约用户的统一ID
     * @param bookId     要预约的书籍ID
     * @return 操作结果的描述信息，包含排队位置。
     * @throws Exception 业务异常，如重复预约、书籍有库存等。
     */
    String reserveBook(String mainUserId, int bookId) throws Exception;

    /**
     * 用户支付罚款。
     * @param mainUserId 支付罚款的用户的统一ID
     * @param amount     支付的金额
     * @return 操作结果的描述信息。
     * @throws Exception 业务异常，如支付金额无效等。
     */
    String payFine(String mainUserId, double amount) throws Exception;

    /**
     * 获取用户的个人借阅记录视图列表。
     * @param mainUserId 用户的统一ID
     * @return 包含书名等信息的借阅记录视图列表。
     */
    List<BorrowRecordView> getMyBorrowRecords(String mainUserId);

    /**
     * 获取用户的个人有效预约记录视图列表。
     * @param mainUserId 用户的统一ID
     * @return 包含书名等信息的预约记录视图列表。
     */
    List<ReservationView> getMyReservations(String mainUserId);

    /**
     * 用户取消自己的预约。
     * @param reservationId 预约记录ID
     * @return 操作结果的描述信息。
     * @throws Exception 业务异常。
     */
    String cancelReservation(int reservationId) throws Exception;


    // --- 管理员业务 ---
    /**
     * 管理员添加一本新书，包含书籍信息和初始库存。
     * @param book 包含完整信息的书籍对象
     * @return 添加成功返回true，否则返回false。
     */
    boolean addBook(Book book);

    /**
     * 管理员更新书籍的基本信息（不包括库存）。
     * @param book 包含更新信息的书籍对象
     * @return 更新成功返回true，否则返回false。
     */
    boolean updateBook(Book book);
}
