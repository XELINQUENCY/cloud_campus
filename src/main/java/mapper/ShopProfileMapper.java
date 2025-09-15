package mapper;

import entity.shop.ShopProfile;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

public interface ShopProfileMapper {
    ShopProfile findById(@Param("userId") String userId);
    int insert(ShopProfile shopProfile);
    int update(ShopProfile shopProfile);
    int updateBalance(@Param("userId") String userId, @Param("newBalance") BigDecimal newBalance);
}
