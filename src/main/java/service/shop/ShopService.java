package service.shop;

import entity.shop.Address;
import entity.shop.Coupon;
import entity.shop.Order;
import entity.shop.ShopProfile;
import java.util.List;

import javax.swing.DefaultListModel;

/**
 * 商店核心业务服务接口。
 * 整合了用户资料、地址、订单等核心操作。
 */
public interface ShopService {

    /**
     * 获取用户的商店资料。
     * @param userId 用户的唯一ID。
     * @return 用户的 ShopProfile 对象。
     */
    ShopProfile getShopProfile(String userId);

    /**
     * 更新用户的商店资料。
     * @param profile 包含更新信息的 ShopProfile 对象。
     */
    
    int initializeShopProfile(String userId);
    
    void updateShopProfile(ShopProfile profile);

    /**
     * 获取用户的所有收货地址。
     * @param userId 用户的唯一ID。
     * @return 地址列表。
     */
    List<Address> getAddressesByUserId(String userId);

    /**
     * 为用户添加新地址。
     * @param address 要添加的地址对象。
     */
    void addAddress(String userId, Address address);

    void updateAddress(String userId, Address address);
    
    /**
     * 删除用户的地址。
     * @param address 要删除的地址对象。
     */
    void deleteAddress(String userId, Address address);

    /**
     * 获取用户的所有历史订单。
     * @param userId 用户的唯一ID。
     * @return 订单列表。
     */
    List<Order> getOrdersByUserId(String userId);
    
    Order getOrdersById(String id);
    
    List<Order> getAllOrders();

    /**
     * 创建一个新订单。
     * 这是一个事务性操作，会同时创建订单和订单项。
     * @param order 包含所有信息的订单对象。
     */
    void createOrder(String userId, Order order);
    
    int addCoupon(String userId, Coupon coupon);
    
    int updateCoupon(String couponId, String userId);
    
    List<Coupon>getCouponsByUserId(String userId);
}
