package mapper;

import entity.library.Reservation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于操作 reservations 表。
 */
public interface ReservationMapper {

    /**
     * 插入一条新的预约记录。
     * @param reservation 包含预约信息的 Reservation 对象
     * @return 成功插入的行数 (通常是1)
     */
    int insert(Reservation reservation);

    /**
     * 根据书籍ID查找所有有效的预约记录，按预约时间升序排序。
     * @param bookId 书籍ID
     * @return 按时间排序的有效预约记录列表
     */
    List<Reservation> findActiveByBookId(@Param("bookId") int bookId);

    /**
     * 根据主系统用户ID查找其所有有效的预约记录。
     * @param mainUserId 主系统用户ID
     * @return 该用户所有有效预约记录的列表
     */
    List<Reservation> findActiveByMainUserId(@Param("mainUserId") String mainUserId);

    /**
     * 【新增】查找特定用户对特定书籍的“可借阅”状态的预约。
     * @param mainUserId 用户ID
     * @param bookId 书籍ID
     * @return 如果找到，返回该 Reservation 对象，否则返回 null
     */
    Reservation findAvailableReservationByUserAndBook(@Param("mainUserId") String mainUserId, @Param("bookId") int bookId);

    /**
     * 检查特定用户是否已经对某本书有有效的预约。
     * @param mainUserId 主系统用户ID
     * @param bookId 书籍ID
     * @return 如果存在有效预约，返回记录数 (大于0)，否则返回0
     */
    long countActiveByUserAndBook(@Param("mainUserId") String mainUserId, @Param("bookId") int bookId);

    /**
     * 更新预约记录的状态和失效日期。
     * @param reservation 包含更新信息的 Reservation 对象
     * @return 成功更新的行数 (通常是1)
     */
    int updateStatus(Reservation reservation);

    /**
     * 【新增】将预约记录的状态更新为“已完成”。
     * @param reservationId 预约记录ID
     * @return 成功更新的行数 (通常是1)
     */
    int updateStatusToCompleted(@Param("reservationId") int reservationId);

    /**
     * 根据预约ID删除一条记录。
     * @param reservationId 要删除的预约记录ID
     * @return 成功删除的行数 (通常是1)
     */
    int delete(@Param("reservationId") int reservationId);

}
