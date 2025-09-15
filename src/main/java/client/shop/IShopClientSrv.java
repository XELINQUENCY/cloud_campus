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
    Map<String, List<Product>> getProductsGroupedByCategory() throws ApiException;
    Product getProductById(String productId) throws ApiException;
    List<Coupon> getAllCouponTemplates() throws ApiException;
    List<SalePromotion> getAllPromotions() throws ApiException;

    // --- 普通用户操作 ---
    ShopProfile getMyShopProfile() throws ApiException;
    List<Address> getMyAddresses() throws ApiException;
    void addAddress(Address address) throws ApiException;
    void deleteAddress(Address address) throws ApiException;
    List<Order> getMyOrders() throws ApiException;
    Order createOrder(Order order) throws ApiException;
    List<Coupon> getMyCoupons() throws ApiException;
    List<Coupon> getMyAvailableCoupons() throws ApiException;
    void claimCoupon(String couponId) throws ApiException; // 假设有领取优惠券的接口

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
    List<Order> getAllOrdersForAdmin() throws ApiException; // 管理员查看所有订单
}
