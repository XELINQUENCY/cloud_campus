package mapper;

import entity.shop.Order;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderMapper {
    Order findById(@Param("orderId") String orderId);
    List<Order> findByUserId(@Param("userId") String userId);
    List<Order> findAll();
    int insert(@Param("userId") String userId, Order order);
}
