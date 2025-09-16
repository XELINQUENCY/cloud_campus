package service.library.impl;

import DAO.MyBatisUtil;
import DAO.UserDAO;
import DAO.library.*;
import entity.User;
import entity.library.*;
import enums.BookStatus;
import enums.UserRole;
import mapper.*;
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

    // ... (DAO 和常量的定义保持不变) ...
    private final UserDAO userDAO;
    private final LibraryProfileDAO libraryProfileDAO;
    private final BookDAO bookDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final ReservationDAO reservationDAO;
    private final CategoryDAO categoryDAO;
    private static final int INITIAL_BORROW_DAYS = 90;
    private static final int RENEW_DAYS = 30;
    private static final int MAX_RENEWAL_COUNT = 3;
    private static final double FINE_PER_DAY = 0.5;
    private static final int RESERVATION_VALID_DAYS = 7;


    public LibraryServiceImpl() {
        this.userDAO = new UserDAO();
        this.libraryProfileDAO = new LibraryProfileDAO();
        this.bookDAO = new BookDAO();
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.reservationDAO = new ReservationDAO();
        this.categoryDAO = new CategoryDAO();
    }

    // ... (login, refreshLibraryProfile, searchBooks, getAllCategories 等方法保持不变) ...
    @Override
    public User login(String username, String password) throws Exception {
        System.out.println("服务端日志: 接到登录请求 -> username=" + username);
        User user = userDAO.findByNameForAuth(username);

        if (user == null) {
            throw new Exception("用户名不存在。");
        }
        if (!password.equals(user.getPassword())) {
            throw new Exception("密码错误。");
        }
        if (!user.hasRole(UserRole.READER) && !user.hasRole(UserRole.LIBRARIAN)) {
            throw new Exception("该用户没有访问图书馆系统的权限。");
        }

        System.out.println("服务端日志: 用户 " + username + " 登录成功，角色: " + user.getUserRoles());
        return user;
    }

    @Override
    public LibraryProfile refreshLibraryProfile(String mainUserId) {
        LibraryProfile profile = libraryProfileDAO.findProfileByMainUserId(mainUserId);
        if (profile == null) {
            System.out.println("服务端日志: 用户 " + mainUserId + " 的图书馆档案不存在，将为其自动创建。");
            profile = new LibraryProfile();
            profile.setMainUserId(mainUserId);
            profile.setOverdue(false);
            profile.setFineAmount(0.0);
            libraryProfileDAO.createProfile(profile);
        }
        return profile;
    }

    @Override
    public List<Book> searchBooks(String title, String author, String publisher, Integer categoryId) {
        return bookDAO.findBooksByCriteria(title, author, publisher, categoryId);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDAO.findAllCategories();
    }


    /**
     * 【已重构】处理借书逻辑，区分预约者和普通用户。
     */
    @Override
    public String borrowBook(String mainUserId, int bookId) throws Exception {
        System.out.println("服务端日志: 用户 " + mainUserId + " 请求借阅书籍 " + bookId);

        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                // ... (获取所有需要的Mapper的代码保持不变) ...
                LibraryProfileMapper profileMapper = sqlSession.getMapper(LibraryProfileMapper.class);
                BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
                BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);
                BorrowRecordMapper borrowRecordMapper = sqlSession.getMapper(BorrowRecordMapper.class);
                ReservationMapper reservationMapper = sqlSession.getMapper(ReservationMapper.class);

                // ... (检查罚款的代码保持不变) ...
                LibraryProfile profile = profileMapper.findByMainUserId(mainUserId);
                if (profile != null && profile.getFineAmount() > 0) {
                    throw new Exception("借阅失败: 您有 " + profile.getFineAmount() + " 元罚款未缴清。");
                }

                Reservation userReservation = reservationMapper.findAvailableReservationByUserAndBook(mainUserId, bookId);

                BookCopy copyToBorrow;
                if (userReservation != null) {
                    // --- 预约者借书流程 ---
                    System.out.println("服务端日志: 用户 " + mainUserId + " 是预约者，查找保留副本...");
                    copyToBorrow = copyMapper.findReservedCopyByBookId(bookId);
                    if (copyToBorrow == null) {
                        throw new Exception("借阅失败: 为您保留的书籍状态异常，请联系管理员。");
                    }
                    reservationMapper.updateStatusToCompleted(userReservation.getReservationId());
                    // 【修复】预约者借书时，也需要将书籍的可借阅数量减 1
                    bookMapper.updateAvailableCopies(bookId, -1);
                } else {
                    // --- 普通用户借书流程 ---
                    // ... (这部分代码保持不变) ...
                    System.out.println("服务端日志: 用户 " + mainUserId + " 是普通读者，查找在架副本...");
                    Book book = bookMapper.findBookById(bookId); // 锁定书籍信息行
                    if (book == null || book.getAvailableCopies() <= 0) {
                        throw new Exception("借阅失败: 本书已全部借出或不存在。");
                    }
                    copyToBorrow = copyMapper.findAvailableCopyByBookId(bookId);
                    if (copyToBorrow == null) {
                        throw new Exception("借阅失败: 抱歉，最后一本刚刚被借走。");
                    }
                    bookMapper.updateAvailableCopies(bookId, -1);
                }

                // ... (通用借阅操作的代码保持不变) ...
                copyMapper.updateCopyStatus(copyToBorrow.getCopyId(), BookStatus.CHECKED_OUT.getDisplayName());
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

                sqlSession.commit();
                return "借阅成功！应还日期为: " + newRecord.getDueDate().toString();

            } catch (Exception e) {
                sqlSession.rollback();
                throw e;
            }
        }
    }


    /**
     * 【已重构】处理还书逻辑，并智能处理预约队列。
     */
    @Override
    public String returnBook(int copyId) throws Exception {
        // ... (方法签名和日志部分保持不变) ...
        String returnMessage;
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(false)) {
            try {
                // ... (获取Mappers部分保持不变) ...
                BorrowRecordMapper borrowRecordMapper = sqlSession.getMapper(BorrowRecordMapper.class);
                BookCopyMapper copyMapper = sqlSession.getMapper(BookCopyMapper.class);
                BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
                LibraryProfileMapper profileMapper = sqlSession.getMapper(LibraryProfileMapper.class);
                ReservationMapper reservationMapper = sqlSession.getMapper(ReservationMapper.class);

                // ... (查找并更新借阅记录，计算罚款等逻辑保持不变) ...
                BorrowRecord recordToReturn = borrowRecordMapper.findActiveByCopyId(copyId);
                if (recordToReturn == null) throw new Exception("还书失败: 未找到该书的有效借阅记录。");

                Integer bookId = copyMapper.findBookIdByCopyId(copyId);
                if (bookId == null) throw new Exception("还书失败: 找不到副本对应的书籍信息。");

                String mainUserId = recordToReturn.getMainUserId();
                double fine = 0.0;
                Date today = new Date();
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

                if (fine > 0) {
                    LibraryProfile profile = profileMapper.findByMainUserId(mainUserId);
                    if (profile != null) {
                        profile.setFineAmount(profile.getFineAmount() + fine);
                        profile.setOverdue(true);
                        profileMapper.update(profile);
                    }
                }

                // 检查是否有等待的预约
                List<Reservation> reservations = reservationMapper.findActiveByBookId(bookId);

                if (reservations.isEmpty()) {
                    // --- 无预约流程 ---
                    System.out.println("服务端日志: 书籍 " + bookId + " 无预约，直接上架。");
                    copyMapper.updateCopyStatus(copyId, BookStatus.ON_SHELF.getDisplayName());
                    bookMapper.updateAvailableCopies(bookId, 1);
                } else {
                    // --- 有预约流程 ---
                    System.out.println("服务端日志: 书籍 " + bookId + " 有预约，为队首用户保留。");
                    copyMapper.updateCopyStatus(copyId, BookStatus.RESERVED.getDisplayName());
                    // 【修复】即使书被预约，也应该先增加可借阅数量，因为它已经“还回”了。
                    // 借书时无论是预约者还是普通读者，都会减去可借阅数，从而保持平衡。
                    bookMapper.updateAvailableCopies(bookId, 1);
                    if(!reservations.get(0).getStatus().equals("可借阅")){
                        handleReservationQueue(reservations.get(0), reservationMapper);
                        Reservation temp = reservations.get(0);
                        reservations.remove(0);
                        reservations.add(temp);
                    }

                }

                sqlSession.commit();
                return returnMessage;

            } catch (Exception e) {
                sqlSession.rollback();
                throw new Exception("还书失败: 服务器内部错误。", e);
            }
        }
    }

    /**
     * 【已重构】此方法现在只负责更新预约记录的状态，不再处理数据库对象。
     */
    private void handleReservationQueue(Reservation nextReservation, ReservationMapper reservationMapper) {
        // 【修复】使用 "available" 关键字，而不是用于显示的中文
        nextReservation.setStatus("可借阅");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, RESERVATION_VALID_DAYS);
        nextReservation.setExpirationDate(cal.getTime());

        if (reservationMapper.updateStatus(nextReservation) > 0) {
            System.out.println("服务端日志: 已通知用户 " + nextReservation.getMainUserId() + " 前来借阅书籍 " + nextReservation.getBookId());
        } else {
            System.err.println("服务端错误: 更新预约队列状态失败 for reservation ID " + nextReservation.getReservationId());
        }
    }

    // ... (renewBook, reserveBook, payFine, getMyBorrowRecords, getMyReservations, cancelReservation, addBook, updateBook 等方法保持不变) ...
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
        newReservation.setStatus("等待中");

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
        if (records == null) {
            System.err.println("服务端警告: 从数据库获取用户 " + mainUserId + " 的借阅记录时返回了null，可能存在数据问题。已返回一个空列表。");
            return new ArrayList<>();
        }
        List<BorrowRecordView> views = new ArrayList<>();
        for (BorrowRecord record : records) {
            Integer bookId = BookCopyDAO.findBookIdByCopyId(record.getCopyId());
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
                bookMapper.insertBook(book);

                if (book.getTotalCopies() > 0) {
                    List<BookCopy> copiesToInsert = new ArrayList<>();
                    for (int i = 0; i < book.getTotalCopies(); i++) {
                        BookCopy copy = new BookCopy();
                        copy.setBookId(book.getBookId());
                        copy.setStatus(BookStatus.ON_SHELF);
                        copiesToInsert.add(copy);
                    }
                    copyMapper.insertCopies(copiesToInsert);
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

