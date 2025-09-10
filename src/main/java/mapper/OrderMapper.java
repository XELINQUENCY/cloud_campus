package mapper;

import entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    Order findById(@Param("orderId")int orderId);
    List<Order> findByUser(@Param("userId")String userId);
    List<Order> findAll();
    int insert(Order order);
    int updateStatus(@Param("orderId")int orderId,
                     @Param("status")String status);
    int delete(@Param("orderId")int orderId);
}
