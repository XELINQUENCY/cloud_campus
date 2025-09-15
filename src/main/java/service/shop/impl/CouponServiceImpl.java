package service.shop.impl;

import DAO.shop.CouponDAO;
import DAO.shop.UserCouponDAO;
import entity.shop.Coupon;
import service.shop.CouponService;

import java.util.List;

public class CouponServiceImpl implements CouponService {

    private final CouponDAO couponDAO = new CouponDAO();
    private final UserCouponDAO userCouponDAO = new UserCouponDAO();

    @Override
    public List<Coupon> getAllCouponTemplates() {
        return couponDAO.findAll();
    }
    
    @Override
    public Coupon getCouponById(String couponId) {
    	return couponDAO.findById(couponId);
    }

    @Override
    public void addCouponTemplate(Coupon coupon) {
        couponDAO.insert(coupon);
    }

    @Override
    public void updateCouponTemplate(Coupon coupon) {
        couponDAO.update(coupon);
    }

    @Override
    public void deleteCouponTemplate(String couponId) {
        couponDAO.delete(couponId);
    }
    
    @Override
    public List<Coupon> getAvailableCouponTemplate() {
    	return couponDAO.findAvailableCoupons();
    }

    @Override
    public List<Coupon> getMyCoupons(String userId) {
        return userCouponDAO.findByUserId(userId);
    }

    @Override
    public List<Coupon> getMyAvailableCoupons(String userId) {
        return userCouponDAO.findAvailableByUserId(userId);
    }

    @Override
    public void useMyCoupon(String couponId, String userId) {
        userCouponDAO.useCoupon(couponId, userId);
    }
}
