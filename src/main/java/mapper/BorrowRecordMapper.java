package mapper;

import entity.library.BorrowRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * MyBatis Mapper 接口，用于操作 borrow_records 表。
 */
public interface BorrowRecordMapper {

    /**
     * 插入一条新的借阅记录。
     * @param record 包含新借阅信息的 BorrowRecord 对象
     * @return 成功插入的行数 (通常是1)
     */
    int insert(BorrowRecord record);

    /**
     * 根据副本ID查找当前未归还的借阅记录。
     * @param copyId 副本ID
     * @return 找到则返回 BorrowRecord 对象，否则返回 null
     */
    BorrowRecord findActiveByCopyId(@Param("copyId") int copyId);

    /**
     * 根据记录ID查找借阅记录。
     * @param recordId 记录ID
     * @return 找到则返回 BorrowRecord 对象，否则返回 null
     */
    BorrowRecord findById(@Param("recordId") int recordId);

    /**
     * 根据主系统用户ID查找其所有借阅记录。
     * @param mainUserId 主系统用户ID
     * @return 该用户所有借阅记录的列表
     */
    List<BorrowRecord> findByMainUserId(@Param("mainUserId") String mainUserId);

    /**
     * 为还书操作更新记录。
     * @param record 包含更新信息的 BorrowRecord 对象 (归还日期、罚款等)
     * @return 成功更新的行数 (通常是1)
     */
    int updateForReturn(BorrowRecord record);

    /**
     * 为续借操作更新记录。
     * @param recordId 要续借的记录ID
     * @param newDueDate 新的应还日期
     * @return 成功更新的行数 (通常是1)
     */
    int updateForRenewal(@Param("recordId") int recordId, @Param("newDueDate") Date newDueDate);
}