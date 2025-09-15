package mapper;

import entity.shop.Coupon;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserCouponMapper {
    Coupon findById(@Param("couponId") String couponId);
    List<Coupon> findByUserId(@Param("mainUserId") String mainUserId);
    List<Coupon> findAvailableByUserId(@Param("mainUserId") String mainUserId);
    int insert(@Param("mainUserId") String userId, Coupon userCoupon);
    int useCoupon(@Param("couponId") String couponId, @Param("mainUserId") String mainUserId);
    int delete(@Param("couponId") String couponId);
}
