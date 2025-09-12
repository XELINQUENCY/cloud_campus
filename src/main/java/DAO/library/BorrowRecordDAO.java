package DAO.library;

import DAO.MyBatisUtil;
import entity.library.BorrowRecord;
import mapper.BorrowRecordMapper;

import java.util.Date;
import java.util.List;

/**
 * 借阅记录数据访问对象 (BorrowRecordDAO)
 * 负责封装所有对 'borrow_records' 数据表的数据库操作，使用MyBatis实现。
 */
public class BorrowRecordDAO {

    /**
     * 添加一条新的借阅记录。
     * 注意：此方法现在是自包含的，事务应由上层服务统一管理。
     * @param record 包含新借阅信息的 BorrowRecord 对象
     * @return 如果添加成功返回 true
     */
    public boolean addBorrowRecord(BorrowRecord record) {
        return MyBatisUtil.executeUpdate(BorrowRecordMapper.class, mapper -> mapper.insert(record)) > 0;
    }

    /**
     * 根据副本ID查找当前未归还的借阅记录。
     * @param copyId 副本ID
     * @return 如果找到，返回 BorrowRecord 对象；否则返回 null。
     */
    public BorrowRecord findActiveRecordByCopyId(int copyId) {
        return MyBatisUtil.executeQuery(BorrowRecordMapper.class, mapper -> mapper.findActiveByCopyId(copyId));
    }

    /**
     * 根据记录ID查找借阅记录。
     * @param recordId 记录ID
     * @return 如果找到，返回 BorrowRecord 对象；否则返回 null。
     */
    public BorrowRecord findRecordById(int recordId) {
        return MyBatisUtil.executeQuery(BorrowRecordMapper.class, mapper -> mapper.findById(recordId));
    }

    /**
     * 根据主系统用户ID查找该用户的所有借阅记录。
     * @param mainUserId 主系统用户ID (注意类型已从int变为String)
     * @return 包含该用户所有借阅记录的列表。
     */
    public List<BorrowRecord> findRecordsByMainUserId(String mainUserId) {
        return MyBatisUtil.executeQuery(BorrowRecordMapper.class, mapper -> mapper.findByMainUserId(mainUserId));
    }

    /**
     * 为还书操作更新借阅记录。
     * 注意：此方法现在是自包含的，事务应由上层服务统一管理。
     * @param record 包含更新信息的 BorrowRecord 对象 (如归还日期、罚款等)
     * @return 如果更新成功返回 true
     */
    public boolean updateRecordForReturn(BorrowRecord record) {
        return MyBatisUtil.executeUpdate(BorrowRecordMapper.class, mapper -> mapper.updateForReturn(record)) > 0;
    }

    /**
     * 为续借操作更新借阅记录。
     * 注意：此方法现在是自包含的，事务应由上层服务统一管理。
     * @param recordId   要续借的记录ID
     * @param newDueDate 新的应还日期
     * @return 如果更新成功返回 true
     */
    public boolean updateRecordForRenewal(int recordId, Date newDueDate) {
        return MyBatisUtil.executeUpdate(BorrowRecordMapper.class, mapper -> mapper.updateForRenewal(recordId, newDueDate)) > 0;
    }
}
