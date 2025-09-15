package DAO.library;

import DAO.MyBatisUtil;
import mapper.ReservationMapper;
import entity.library.Reservation;

import java.util.List;

/**
 * 预约记录数据访问对象 (ReservationDAO)
 * 负责封装所有对 'reservations' 数据表的数据库操作，使用MyBatis实现。
 */
public class ReservationDAO {

    /**
     * 添加一条新的预约记录。
     * @param reservation 包含预约信息的 Reservation 对象
     * @return 添加成功返回 true，失败返回 false。
     */
    public boolean addReservation(Reservation reservation) {
        return MyBatisUtil.executeUpdate(ReservationMapper.class,
                mapper -> mapper.insert(reservation)) > 0;
    }

    /**
     * 根据书籍ID查找所有有效的预约记录，并按预约时间升序排序。
     * @param bookId 书籍ID
     * @return 返回一个按时间排序的预约记录列表。
     */
    public List<Reservation> findActiveReservationsByBookId(int bookId) {
        return MyBatisUtil.executeQuery(ReservationMapper.class, mapper -> mapper.findActiveByBookId(bookId));
    }

    /**
     * 根据主系统用户ID查找该用户的所有有效预约记录。
     * @param mainUserId 主系统用户ID (注意类型已从int变为String)
     * @return 返回该用户的所有有效预约记录列表。
     */
    public List<Reservation> findActiveReservationsByMainUserId(String mainUserId) {
        return MyBatisUtil.executeQuery(ReservationMapper.class, mapper -> mapper.findActiveByMainUserId(mainUserId));
    }

    /**
     * 检查特定用户是否已经对某本书有有效的预约。
     * @param mainUserId 主系统用户ID (注意类型已从int变为String)
     * @param bookId 书籍ID
     * @return 如果存在有效预约返回 true，否则返回 false。
     */
    public boolean hasActiveReservation(String mainUserId, int bookId) {
        Long count = MyBatisUtil.executeQuery(ReservationMapper.class, mapper -> mapper.countActiveByUserAndBook(mainUserId, bookId));
        return count != null && count > 0;
    }

    /**
     * 更新预约记录的状态。
     * 注意：事务应由上层服务统一管理。
     * @param reservation 包含更新信息的 Reservation 对象
     * @return 更新成功返回 true
     */
    public boolean updateReservationStatus(Reservation reservation) {
        return MyBatisUtil.executeUpdate(ReservationMapper.class, mapper -> mapper.updateStatus(reservation)) > 0;
    }

    /**
     * 根据ID删除一条预约记录。
     * @param reservationId 要删除的预约记录ID
     * @return 删除成功返回 true，失败返回 false。
     */
    public boolean deleteReservation(int reservationId) {
        return MyBatisUtil.executeUpdate(ReservationMapper.class, mapper -> mapper.delete(reservationId)) > 0;
    }
}
