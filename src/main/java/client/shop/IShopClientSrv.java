package client.shop;

import client.ApiException;
import entity.User;
import entity.shop.*;

import java.util.List;
import java.util.Map;

/**
 * 商店模块客户端服务统一接口。
 * 定义了所有商店相关的客户端操作，包括普通用户和管理员。
 */
public interface IShopClientSrv {

    // --- 认证 ---
    User login(String username, String password, boolean isAdmin) throws ApiException;

    // --- 公共数据获取 (普通用户和管理员共用) ---
    String getCurrentUserId();
    List<Product> getAllProducts() throws ApiException;
    List<Product> getProductsByCategory(String category) throws ApiException; // 新增方法
    Map<String, List<Product>> getProductsGroupedByCategory() throws ApiException;
    Product getProductById(String productId) throws ApiException;
    List<String> getAllCategories() throws ApiException; // 新增方法
    List<Product> searchProductsByName(String name) throws ApiException; // 新增方法
    List<Product> searchProductsById(String id) throws ApiException; // 新增方法
    
    List<Coupon> getAllCouponTemplates() throws ApiException;
    List<Coupon> getAvailableCouponTemplates() throws ApiException;
    Coupon getCouponById(String couponId) throws ApiException;
    
    List<SalePromotion> getAllPromotions() throws ApiException;
    SalePromotion getPromotionById(String promotionId) throws ApiException;
    List<SalePromotion> getPromotionsByProductId(String productId) throws ApiException;
    
    void updateProductStock(String productId, int newStock) throws ApiException;
    void updateProductSales(String productId, int soldAmount) throws ApiException;

    // --- 普通用户操作 ---
    ShopProfile getMyShopProfile() throws ApiException;
    void initializeShopProfile() throws ApiException;
    void updateShopProfile(ShopProfile profile) throws ApiException;
    List<Address> getMyAddresses() throws ApiException;
    void addAddress(Address address) throws ApiException;
    void updateAddress(Address address) throws ApiException;
    void deleteAddress(Address address) throws ApiException;
    List<Order> getMyOrders() throws ApiException;
    Order getOrderById(String orderId) throws ApiException;
    Order createOrder(Order order) throws ApiException;
    List<Coupon> getMyCoupons() throws ApiException;
    List<Coupon> getMyAvailableCoupons() throws ApiException;
    void useMyCoupon(String couponId) throws ApiException;
    void addUserCoupon(Coupon coupon) throws ApiException;
    void updateUserCoupon(String couponId) throws ApiException;

    // --- 管理员操作 ---
    void addProduct(Product product) throws ApiException;
    void updateProduct(Product product) throws ApiException;
    void deleteProduct(String productId) throws ApiException;
    
    void addCouponTemplate(Coupon coupon) throws ApiException;
    void updateCouponTemplate(Coupon coupon) throws ApiException;
    void deleteCouponTemplate(String couponId) throws ApiException;
    
    void addSalePromotion(SalePromotion promotion) throws ApiException;
    void updateSalePromotion(SalePromotion promotion) throws ApiException;
    void deleteSalePromotion(String promotionId) throws ApiException;
    
    List<Order> getAllOrders() throws ApiException; // 管理员查看所有订单
    ShopProfile getUserProfile(String userId) throws ApiException; // 新增：管理员获取指定用户资料
    List<Order> getUserOrders(String userId) throws ApiException; // 新增：管理员获取指定用户订单
}