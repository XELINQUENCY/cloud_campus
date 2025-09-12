package service.library.impl;

import DAO.MyBatisUtil;
import DAO.UserDAO;
import DAO.library.*;
import entity.User;
import entity.library.*;
import enums.UserRole;
import mapper.BookCopyMapper;
import mapper.BookMapper;
import mapper.BorrowRecordMapper;
import mapper.LibraryProfileMapper;
import org.apache.ibatis.session.SqlSession;
import service.library.LibraryService;
import view.BorrowRecordView;
import view.ReservationView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 图书馆模块业务逻辑实现类 (重构版)
 * 实现了 LibraryService 接口，使用 MyBatis 和新的 DAO 进行数据操作。
 */
public class LibraryServiceImpl implements LibraryService {

    // 引入所有需要用到的DAO
    private final UserDAO userDAO;
    private final LibraryProfileDAO libraryProfileDAO;
    private final BookDAO bookDAO;
    private final BookCopyDAO bookCopyDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final ReservationDAO reservationDAO;
    private final CategoryDAO categoryDAO;

    // 定义业务常量
    private static final int INITIAL_BORROW_DAYS = 90;
    private static final int RENEW_DAYS = 30;
    private static final int MAX_RENEWAL_COUNT = 3;
    private static final double FINE_PER_DAY = 0.5;
    private static final int RESERVATION_VALID_DAYS = 7;

    public LibraryServiceImpl() {
        this.userDAO = new UserDAO();
        this.libraryProfileDAO = new LibraryProfileDAO();
        this.bookDAO = new BookDAO();
        this.bookCopyDAO = new BookCopyDAO();
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.reservationDAO = new ReservationDAO();
        this.categoryDAO = new CategoryDAO();
    }

    @Override
    public User login(String username, String password) throws Exception {
        System.out.println("服务端日志: 接到登录请求 -> username=" + username);
        User user = userDAO.findByNameForAuth(username);

        if (user == null) {
            throw new Exception("用户名不存在。");
        }

        // 注意：在生产环境中，密码应该是加密存储的，这里需要用加密库进行比对
        // e.g., if (!passwordEncoder.matches(password, user.getPassword())) { ... }
        if (!password.equals(user.getPassword())) {
            throw new Exception("密码错误。");
        }

        // 检查用户是否拥有访问图书馆的权限
        if (!user.hasRole(UserRole.READER) && !user.hasRole(UserRole.LIBRARIAN)) {
            throw new Exception("该用户没有访问图书馆系统的权限。");
        }

        System.out.println("服务端日志: 用户 " + username + " 登录成功，角色: " + user.getUserRoles());
        return user;
    }

    @Override
    public LibraryProfile refreshLibraryProfile(String mainUserId) {
        return libraryProfileDAO.findProfileByMainUserId(mainUserId);
    }

    @Override
    public List<Book> searchBooks(String title, String author, String publisher, Integer categoryId) {
        return bookDAO.findBooksByCriteria(title, author, publisher, categoryId);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDAO.findAllCategories();
    }

    @Override
    public String borrowBook(String mainUserId, int bookId) throws Exception {
        System.out.println("服务端日志: 用户 " + mainUserId + " 请求借阅书籍 " + bookId);
        // 使用 MyBatis 手动管理事务
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                // 从当前事务的 session 中获取 Mapper
                LibraryProfileMapper profileMapper = sqlSession.getMapper(LibraryProfileMapper.class);
                BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
                BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);
                BorrowRecordMapper borrowRecordMapper = sqlSession.getMapper(BorrowRecordMapper.class);

                // --- 业务规则校验 ---
                LibraryProfile profile = profileMapper.findByMainUserId(mainUserId);
                if (profile != null && profile.getFineAmount() > 0) {
                    throw new Exception("借阅失败: 您有 " + profile.getFineAmount() + " 元罚款未缴清。");
                }

                Book book = bookMapper.findBookById(bookId);
                if (book == null || book.getAvailableCopies() <= 0) {
                    throw new Exception("借阅失败: 本书已全部借出或不存在。");
                }

                BookCopy copyToBorrow = copyMapper.findAvailableCopyByBookId(bookId);
                if (copyToBorrow == null) {
                    throw new Exception("借阅失败: 抱歉，最后一本刚刚被借走。");
                }

                // --- 执行数据库操作 ---
                copyMapper.updateCopyStatus(copyToBorrow.getCopyId(), "已借出");
                bookMapper.updateAvailableCopies(bookId, -1);

                BorrowRecord newRecord = new BorrowRecord();
                newRecord.setMainUserId(mainUserId);
                newRecord.setCopyId(copyToBorrow.getCopyId());
                Date borrowDate = new Date();
                newRecord.setBorrowDate(borrowDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(borrowDate);
                cal.add(Calendar.DATE, INITIAL_BORROW_DAYS);
                newRecord.setDueDate(cal.getTime());

                borrowRecordMapper.insert(newRecord);

                // --- 提交事务 ---
                sqlSession.commit();
                return "借阅成功！应还日期为: " + newRecord.getDueDate().toString();

            } catch (Exception e) {
                sqlSession.rollback(); // 发生任何异常，回滚事务
                throw e; // 将异常继续向上抛出
            }
        }
    }

    @Override
    public String returnBook(int copyId) throws Exception {
        System.out.println("服务端日志: 接到归还请求 -> copyId=" + copyId);
        String returnMessage;
        int bookId;
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                BorrowRecordMapper borrowRecordMapper = sqlSession.getMapper(BorrowRecordMapper.class);
                BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);
                BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
                LibraryProfileMapper profileMapper = sqlSession.getMapper(LibraryProfileMapper.class);

                BorrowRecord recordToReturn = borrowRecordMapper.findActiveByCopyId(copyId);
                if (recordToReturn == null) {
                    throw new Exception("还书失败: 未找到该书的有效借阅记录。");
                }

                Integer foundBookId = copyMapper.findBookIdByCopyId(copyId);
                if (foundBookId == null) {
                    throw new Exception("还书失败: 找不到副本对应的书籍信息。");
                }
                bookId = foundBookId;
                String mainUserId = recordToReturn.getMainUserId();

                Date today = new Date();
                double fine = 0.0;
                if (today.after(recordToReturn.getDueDate())) {
                    LocalDate dueDateLocal = recordToReturn.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate returnDateLocal = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    long overdueDays = ChronoUnit.DAYS.between(dueDateLocal, returnDateLocal);
                    if (overdueDays > 0) {
                        fine = overdueDays * FINE_PER_DAY;
                        recordToReturn.setIsOverdue(true);
                        returnMessage = "还书成功！书籍已逾期 " + overdueDays + " 天，产生罚款 " + fine + " 元。";
                    } else {
                        returnMessage = "还书成功！感谢您的使用。";
                    }
                } else {
                    returnMessage = "还书成功！感谢您的使用。";
                }
                recordToReturn.setReturnDate(today);
                recordToReturn.setFineAmount(fine);

                borrowRecordMapper.updateForReturn(recordToReturn);
                copyMapper.updateCopyStatus(copyId, "在馆");
                bookMapper.updateAvailableCopies(bookId, 1);

                if (fine > 0) {
                    LibraryProfile profile = profileMapper.findByMainUserId(mainUserId);
                    if (profile != null) {
                        profile.setFineAmount(profile.getFineAmount() + fine);
                        profile.setOverdue(true);
                        profileMapper.update(profile);
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new Exception("还书失败: 服务器内部错误。", e);
            }
        }
        // 事务外处理预约队列
        handleReservationQueue(bookId);
        return returnMessage;
    }

    private void handleReservationQueue(int bookId) {
        List<Reservation> reservations = reservationDAO.findActiveReservationsByBookId(bookId);
        if (!reservations.isEmpty()) {
            Reservation nextReservation = reservations.get(0);
            nextReservation.setStatus("可借阅");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, RESERVATION_VALID_DAYS);
            nextReservation.setExpirationDate(cal.getTime());

            if (reservationDAO.updateReservationStatus(nextReservation)) {
                System.out.println("服务端日志: 已通知用户 " + nextReservation.getMainUserId() + " 前来借阅书籍 " + bookId);
                // 在实际项目中，这里可以集成邮件或消息推送服务
            } else {
                System.err.println("服务端错误: 更新预约队列状态失败 for reservation ID " + nextReservation.getReservationId());
            }
        }
    }

    @Override
    public String renewBook(int recordId) throws Exception {
        BorrowRecord record = borrowRecordDAO.findRecordById(recordId);
        if (record == null) throw new Exception("续借失败: 未找到该借阅记录。");
        if (record.getReturnDate() != null) throw new Exception("续借失败: 该书已归还。");
        if (record.getRenewalCount() >= MAX_RENEWAL_COUNT) throw new Exception("续借失败: 续借次数已达上限。");
        if (new Date().after(record.getDueDate())) throw new Exception("续借失败: 书籍已超期。");

        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getDueDate());
        cal.add(Calendar.DATE, RENEW_DAYS);
        Date newDueDate = cal.getTime();

        if (borrowRecordDAO.updateRecordForRenewal(recordId, newDueDate)) {
            return "续借成功！新的应还日期为: " + newDueDate.toString();
        } else {
            throw new Exception("续借失败: 更新数据库失败。");
        }
    }

    @Override
    public String reserveBook(String mainUserId, int bookId) throws Exception {
        Book book = bookDAO.findBookById(bookId);
        if (book == null) throw new Exception("预约失败: 书籍不存在。");
        if (book.getAvailableCopies() > 0) throw new Exception("预约失败: 本书当前有库存，请直接借阅。");
        if (reservationDAO.hasActiveReservation(mainUserId, bookId)) throw new Exception("预约失败: 您已预约过本书。");

        Reservation newReservation = new Reservation();
        newReservation.setMainUserId(mainUserId);
        newReservation.setBookId(bookId);
        newReservation.setReservationDate(new Date());
        newReservation.setStatus("waiting");

        if (reservationDAO.addReservation(newReservation)) {
            List<Reservation> queue = reservationDAO.findActiveReservationsByBookId(bookId);
            return "预约成功！您当前排在第 " + queue.size() + " 位。";
        } else {
            throw new Exception("预约失败: 无法写入数据库。");
        }
    }

    @Override
    public String payFine(String mainUserId, double amount) throws Exception {
        LibraryProfile profile = libraryProfileDAO.findProfileByMainUserId(mainUserId);
        if (profile == null) throw new Exception("支付失败: 无效的用户。");

        double currentFine = profile.getFineAmount();
        if (currentFine <= 0) return "您当前没有罚款需要支付。";
        if (amount <= 0) throw new Exception("支付失败: 支付金额必须大于0。");
        if (amount > currentFine) throw new Exception("支付失败: 支付金额超过了您的总罚款。");

        double newFine = currentFine - amount;
        profile.setFineAmount(newFine);
        profile.setOverdue(newFine > 0);

        if (libraryProfileDAO.updateProfile(profile)) {
            String message = "支付成功 " + amount + " 元！";
            message += (newFine > 0) ? " 您还需支付 " + newFine + " 元。" : " 您的罚款已全部缴清！";
            return message;
        } else {
            throw new Exception("支付失败: 更新数据库时发生错误。");
        }
    }

    @Override
    public List<BorrowRecordView> getMyBorrowRecords(String mainUserId) {
        List<BorrowRecord> records = borrowRecordDAO.findRecordsByMainUserId(mainUserId);
        List<BorrowRecordView> views = new ArrayList<>();
        for (BorrowRecord record : records) {
            Integer bookId = bookCopyDAO.findBookIdByCopyId(record.getCopyId());
            String title = "未知书名 (ID:" + bookId + ")";
            if (bookId != null && bookId > 0) {
                Book book = bookDAO.findBookById(bookId);
                if (book != null) {
                    title = book.getTitle();
                }
            }
            views.add(new BorrowRecordView(record, title));
        }
        return views;
    }

    @Override
    public List<ReservationView> getMyReservations(String mainUserId) {
        List<Reservation> reservations = reservationDAO.findActiveReservationsByMainUserId(mainUserId);
        List<ReservationView> views = new ArrayList<>();
        for (Reservation res : reservations) {
            Book book = bookDAO.findBookById(res.getBookId());
            String title = (book != null) ? book.getTitle() : "未知书名";
            views.add(new ReservationView(res, title));
        }
        return views;
    }

    @Override
    public String cancelReservation(int reservationId) throws Exception {
        if (reservationDAO.deleteReservation(reservationId)) {
            return "预约已成功取消！";
        } else {
            throw new Exception("取消失败，可能该预约已不存在。");
        }
    }

    @Override
    public boolean addBook(Book book) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
                BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);

                bookMapper.insertBook(book); // 插入后 bookId 会被回填
                if (book.getTotalCopies() > 0) {
                    copyMapper.insertCopies(book.getBookId(), book.getTotalCopies());
                }

                sqlSession.commit();
                return true;
            } catch (Exception e) {
                sqlSession.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean updateBook(Book book) {
        return bookDAO.updateBook(book);
    }
}
