package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.Coupon;
import mapper.CouponMapper;

import java.util.List;

/**
 * 优惠券模板数据访问对象。
 * 用于管理和维护系统中的优惠券类型。
 */
public class CouponDAO {

    /**
     * 根据ID查找优惠券模板。
     * @param couponId 优惠券的唯一标识符。
     * @return 找到的 Coupon 对象，如果不存在则返回 null。
     */
    public Coupon findById(String couponId) {
        return MyBatisUtil.executeQuery(CouponMapper.class, mapper->mapper.findById(couponId));
    }

    /**
     * 获取所有优惠券模板。
     * @return 包含所有优惠券模板的列表。
     */
    public List<Coupon> findAll() {
        return MyBatisUtil.executeQuery(CouponMapper.class, CouponMapper::findAll);
    }

    /**
     * 插入一个新的优惠券模板。
     * @param coupon 要插入的优惠券模板对象。
     */
    public int insert(Coupon coupon) {
        return MyBatisUtil.executeUpdate(CouponMapper.class, mapper->mapper.insert(coupon));
    }

    /**
     * 更新一个优惠券模板。
     * @param coupon 包含更新信息的优惠券模板对象。
     */
    public int update(Coupon coupon) {
        return MyBatisUtil.executeUpdate(CouponMapper.class, mapper->mapper.update(coupon));
    }

    /**
     * 根据ID删除一个优惠券模板。
     * @param couponId 要删除的优惠券模板的ID。
     */
    public int delete(String couponId) {
        return MyBatisUtil.executeUpdate(CouponMapper.class, mapper->mapper.delete(couponId));
    }

    /**
     * 查询所有可用（未使用且未过期）的优惠券。
     *
     * @return 返回可用的优惠券列表。
     */
    public List<Coupon> findAvailableCoupons(){
        return MyBatisUtil.executeQuery(CouponMapper.class, CouponMapper::findAvailableCoupons);
    }
}
