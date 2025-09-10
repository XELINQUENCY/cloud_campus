package mapper;

import entity.BorrowRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowRecordMapper {
    /**
     * 插入一条新的借阅记录。借阅时没有实际归还日期和是否逾期。
     * @param record 包含借阅信息的记录对象
     * @return 受影响的行数
     */
    int insert(BorrowRecord record);

    /**
     * 更新借阅记录的归还信息（还书时调用）
     * @param recordId 要更新的记录ID
     * @param actualReturnDate 实际归还日期
     * @return 受影响的行数
     */
    int updateReturnInfo(@Param("recordId") String recordId, @Param("actualReturnDate")LocalDateTime actualReturnDate);

    /**
     * 查找某个读者当前未归还的借阅记录
     * @param readerId 读者ID
     * @return 找到的活动借阅记录，可能为null
     */
    List<BorrowRecord> findActiveRecordByReaderId(String readerId);

    /**
     * 查找某个读者的所有借阅历史
     * @param readerId 读者ID
     * @return 该读者的借阅记录列表
     */
    List<BorrowRecord> findByReaderId(String readerId);
}
