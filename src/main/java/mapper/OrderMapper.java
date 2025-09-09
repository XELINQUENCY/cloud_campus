package mapper;

import entity.Order;

import java.util.List;

public interface OrderMapper {
    Order findById(int orderId);
    List<Order> findByUser(String userId);
    List<Order> findAll();
    int insert(Order order);
    int updateStatus(int orderId, String status);
    int delete(int orderId);
}
