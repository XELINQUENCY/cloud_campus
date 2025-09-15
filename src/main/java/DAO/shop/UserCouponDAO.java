package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.Coupon;
import mapper.UserCouponMapper;

import java.util.List;

/**
 * 用户持有的优惠券数据访问对象 (DAO)。
 * 管理每个用户所拥有的优惠券实例。
 */
public class UserCouponDAO {

    /**
     * 查找用户持有的特定优惠券。
     * @param couponId 优惠券实例的唯一ID。
     * @return 找到的 UserCoupon 对象，如果不存在则返回 null。
     */
    public Coupon findById(String couponId) {
        return MyBatisUtil.executeQuery(UserCouponMapper.class, mapper->mapper.findById(couponId));
    }

    /**
     * 查找一个用户持有的所有优惠券。
     * @param userId 用户的唯一标识符。
     * @return 该用户的所有优惠券列表。
     */
    public List<Coupon> findByUserId(String userId) {
        return MyBatisUtil.executeQuery(UserCouponMapper.class, mapper->mapper.findByUserId(userId));
    }

    /**
     * 查找一个用户所有可用的优惠券 (未过期且未使用)。
     * @param userId 用户的唯一标识符。
     * @return 该用户所有可用的优惠券列表。
     */
    public List<Coupon> findAvailableByUserId(String userId) {
        return MyBatisUtil.executeQuery(UserCouponMapper.class, mapper->mapper.findAvailableByUserId(userId));
    }

    /**
     * 为用户添加一张优惠券。
     * @param userCoupon 要添加的优惠券实例。
     */
    public int insert(String userId, Coupon userCoupon) {
        return MyBatisUtil.executeUpdate(UserCouponMapper.class, mapper->mapper.insert(userId, userCoupon));
    }

    /**
     * 将一张优惠券标记为已使用。
     * @param couponId 要使用的优惠券实例ID。
     * @param userId   优惠券所属的用户ID。
     */
    public int useCoupon(String couponId, String userId) {
        return MyBatisUtil.executeUpdate(UserCouponMapper.class, mapper->mapper.useCoupon(couponId,userId));
    }
}
