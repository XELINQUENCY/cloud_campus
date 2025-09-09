package DAO;

import entity.Order;
import mapper.OrderMapper;

import java.util.List;

public class OrderDAO {
    Order findById(int orderId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper -> mapper.findById(orderId));
    }

    List<Order> findByUser(String userId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper -> mapper.findByUser(userId));
    }

    List<Order> findAll() {
        return MyBatisUtil.executeQuery(OrderMapper.class, OrderMapper::findAll);
    }

    boolean insert(Order order) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.insert(order)) > 0;
    }

    boolean updateStatus(int orderId, String status) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.updateStatus(orderId, status)) > 0;
    }

    boolean delete(int orderId) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.delete(orderId)) > 0;
    }
}
