package DAO;

import entity.OrderItem;
import mapper.OrderItemMapper;

import java.util.List;

public class OrderItemDAO {
    public int batchInsert(List<OrderItem> items){
        return MyBatisUtil.executeUpdate(OrderItemMapper.class, mapper->mapper.batchInsert(items));
    }

    public List<OrderItem> getByOrderId(int orderId){
        return MyBatisUtil.executeQuery(OrderItemMapper.class, mapper->mapper.getByOrderId(orderId));
    }
}
