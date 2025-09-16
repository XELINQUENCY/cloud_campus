package DAO.shop;

import DAO.MyBatisUtil;
import entity.shop.Order;
import entity.shop.OrderItem;
import mapper.OrderItemMapper;
import mapper.OrderMapper;

import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 订单数据访问对象 (DAO)。
 * 封装了对订单及其关联的订单项的操作。
 */
public class OrderDAO {

    /**
     * 根据订单ID查找订单。
     * 这通常会一并加载订单下的所有订单项 (OrderItem)。
     * @param orderId 订单的唯一标识符。
     * @return 找到的 Order 对象，如果不存在则返回 null。
     */
    public Order findById(String orderId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper->mapper.findById(orderId));
    }

    /**
     * 根据用户ID查找该用户的所有历史订单。
     * @param userId 用户的唯一标识符。
     * @return 该用户的订单列表。
     */
    public List<Order> findByUserId(String userId) {
        return MyBatisUtil.executeQuery(OrderMapper.class, mapper->mapper.findByUserId(userId));
    }
    
    public List<Order> findAll(){
    	 return MyBatisUtil.executeQuery(OrderMapper.class, mapper->mapper.findAll());
    }

    /**
     * 插入一个新订单及其所有订单项 (事务性操作)。
     * 【已修改】: 使用手动管理的 SqlSession 模式来确保事务的原子性。
     * @param order 要插入的订单对象，应包含其 items 列表。
     */
    public void insertOrderWithItems(String userId, Order order) {
        // 手动获取 SqlSession
        SqlSession session = MyBatisUtil.getSqlSessionFactory().openSession();
        try {
            // 获取 Mapper 接口
            OrderMapper orderMapper = session.getMapper(OrderMapper.class);
            OrderItemMapper orderItemMapper = session.getMapper(OrderItemMapper.class);

            // 步骤 1: 插入主订单
            orderMapper.insert(order);

            // 步骤 2: 批量插入订单项
            if (order.items != null && !order.items.isEmpty()) {
                // 确保每个 item 都有正确的 orderId
                for (OrderItem item : order.items) {
                    item.orderId = order.orderId;
                }
                orderItemMapper.insertBatch(order.items);
            }

            // 如果所有操作都成功，提交事务
            session.commit();
        } catch (Exception e) {
            // 如果在任何步骤中发生异常，回滚事务
            session.rollback();
            // 抛出异常，让上层调用者知道操作失败
            throw new RuntimeException("插入订单时发生错误", e);
        } finally {
            // 无论成功与否，最后都要确保关闭 SqlSession
            session.close();
        }
    }
}
