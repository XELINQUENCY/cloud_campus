package service.shop.impl;

import DAO.shop.AddressDAO;
import DAO.shop.OrderDAO;
import DAO.shop.ShopProfileDAO;
import DAO.shop.UserCouponDAO;
import entity.shop.Address;
import entity.shop.Coupon;
import entity.shop.Order;
import entity.shop.ShopProfile;
import service.shop.ShopService;

import java.util.List;

public class ShopServiceImpl implements ShopService {

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final AddressDAO addressDAO = new AddressDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserCouponDAO userCouponDAO = new UserCouponDAO();

    @Override
    public ShopProfile getShopProfile(String userId) {
        return shopProfileDAO.findById(userId);
    }
    
    @Override
    public int initializeShopProfile(String userId) {
    	return shopProfileDAO.insert(userId);
    }

    @Override
    public void updateShopProfile(ShopProfile profile) {
        shopProfileDAO.update(profile);
    }

    @Override
    public List<Address> getAddressesByUserId(String userId) {
        return addressDAO.findByUserId(userId);
    }

    @Override
    public void addAddress(String userId, Address address) {
        addressDAO.insert(userId, address);
    }
    
    @Override
	public void updateAddress(String userId, Address address) {
    	addressDAO.update(userId, address);
    }

    @Override
    public void deleteAddress(String userId, Address address) {
        addressDAO.delete(userId, address);
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        return orderDAO.findByUserId(userId);
    }
    
    @Override
    public List<Order> getAllOrders() {
    	return orderDAO.findAll();
    }
    
    @Override 
    public Order getOrdersById(String id){
    	return orderDAO.findById(id);
    }

    @Override
    public void createOrder(String userId, Order order) {
        // 调用我们之前特别处理过的事务性DAO方法
        orderDAO.insertOrderWithItems(userId, order);
    }
    
    @Override
    public int addCoupon(String userId, Coupon coupon) {
    	return userCouponDAO.insert(userId, coupon);
    }
    
    @Override
    public int updateCoupon(String couponId, String userId) {
    	return userCouponDAO.useCoupon(couponId, userId);
    }
}
