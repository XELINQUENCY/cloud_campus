package DAO;

import entity.Order;
import mapper.OrderMapper;

import java.util.List;

public class OrderDAO {
    public Order findById(int orderId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper -> mapper.findById(orderId));
    }

    public List<Order> findByUser(String userId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper -> mapper.findByUser(userId));
    }

    public List<Order> findAll() {
        return MyBatisUtil.executeQuery(OrderMapper.class, OrderMapper::findAll);
    }

    public boolean insert(Order order) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.insert(order)) > 0;
    }

    public boolean updateStatus(int orderId, String status) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.updateStatus(orderId, status)) > 0;
    }

    public boolean delete(int orderId) {
        return MyBatisUtil.executeUpdate(OrderMapper.class, mapper -> mapper.delete(orderId)) > 0;
    }
}
