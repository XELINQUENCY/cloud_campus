package mapper;

import entity.shop.OrderItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderItemMapper {
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);
    int insert(OrderItem orderItem);
    int insertBatch(@Param("items") List<OrderItem> items);
}
