package mapper;

import entity.shop.Coupon;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 管理优惠券模板的Mapper
 */
public interface CouponMapper {
    Coupon findById(@Param("couponId") String couponId);
    List<Coupon> findAll();
    int insert(Coupon coupon);
    int update(Coupon coupon);
    int delete(@Param("couponId") String couponId);
    List<Coupon> findAvailableCoupons();
}
