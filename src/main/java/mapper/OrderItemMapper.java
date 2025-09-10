package mapper;

import entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int batchInsert(List<OrderItem> item);
    List<OrderItem> getByOrderId(@Param("orderId")int orderId);
}
