package service.shop;

import entity.shop.Coupon;
import java.util.List;

/**
 * 优惠券服务接口。
 * 同时管理优惠券模板和用户持有的优惠券。
 */
public interface CouponService {

    // --- 优惠券模板管理 (后台) ---
    List<Coupon> getAllCouponTemplates();
    Coupon getCouponById(String couponId);
    void addCouponTemplate(Coupon coupon);
    void updateCouponTemplate(Coupon coupon);
    void deleteCouponTemplate(String couponId);
    List<Coupon> getAvailableCouponTemplate();

    // --- 用户优惠券管理 (前台) ---
    List<Coupon> getMyCoupons(String userId);
    List<Coupon> getMyAvailableCoupons(String userId);
    void useMyCoupon(String couponId, String userId);
}
