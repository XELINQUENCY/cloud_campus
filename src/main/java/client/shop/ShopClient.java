package client.shop;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import entity.shop.*;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商店模块的专用网络客户端实现。
 * 实现了 IShopClientSrv 接口，将业务请求转换为对服务器的HTTPS API调用。
 */
public class ShopClient implements IShopClientSrv {

    private final ApiClient apiClient;
    private String currentUserId;

    public ShopClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public User login(String username, String password, boolean isAdmin) throws ApiException {
        LoginRequest loginRequest = new LoginRequest(username, password, isAdmin);
        HttpRequest request = apiClient.newRequestBuilder("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(loginRequest)))
                .build();
        LoginResponse response = apiClient.sendRequest(request, LoginResponse.class);

        if (response != null && response.getToken() != null) {
            apiClient.setAuthToken(response.getToken());
            this.currentUserId = response.getUser().getId();
            return response.getUser();
        }
        throw new ApiException("登录失败，服务器未返回有效数据。");
    }

    // --- 公共数据获取 ---
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    @Override
    public List<Product> getAllProducts() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products").GET().build();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Product> getProductsByCategory(String category) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products/category?category=" + category).GET().build();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public Map<String, List<Product>> getProductsGroupedByCategory() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products/grouped").GET().build();
        Type mapType = new TypeToken<Map<String, List<Product>>>() {}.getType();
        return apiClient.sendRequest(request, mapType);
    }

    @Override
    public Product getProductById(String productId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/product?id=" + productId).GET().build();
        return apiClient.sendRequest(request, Product.class);
    }

    @Override
    public List<String> getAllCategories() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/categories").GET().build();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Product> searchProductsByName(String name) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products/search?name=" + name).GET().build();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Product> searchProductsById(String id) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products/search?id=" + id).GET().build();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Coupon> getAllCouponTemplates() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupons/templates").GET().build();
        Type listType = new TypeToken<List<Coupon>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Coupon> getAvailableCouponTemplates() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupons/templates/available").GET().build();
        Type listType = new TypeToken<List<Coupon>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public Coupon getCouponById(String couponId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupon/template?id=" + couponId).GET().build();
        return apiClient.sendRequest(request, Coupon.class);
    }

    @Override
    public List<SalePromotion> getAllPromotions() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotions").GET().build();
        Type listType = new TypeToken<List<SalePromotion>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public SalePromotion getPromotionById(String promotionId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotion?id=" + promotionId).GET().build();
        return apiClient.sendRequest(request, SalePromotion.class);
    }

    @Override
    public List<SalePromotion> getPromotionsByProductId(String productId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotions/product?productId=" + productId).GET().build();
        Type listType = new TypeToken<List<SalePromotion>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }
    
    @Override
    public void updateProductStock(String productId, int newStock) throws ApiException {
        Map<String, Object> body = Map.of("productId", productId, "newStock", newStock);
        HttpRequest request = apiClient.newRequestBuilder("/shop/product/stock")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateProductSales(String productId, int soldAmount) throws ApiException {
        Map<String, Object> body = Map.of("productId", productId, "soldAmount", soldAmount);
        HttpRequest request = apiClient.newRequestBuilder("/shop/product/sales")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    // --- 普通用户操作 ---

    @Override
    public ShopProfile getMyShopProfile() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/profile").GET().build();
        return apiClient.sendRequest(request, ShopProfile.class);
    }

    @Override
    public void initializeShopProfile() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/profile/initialize")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateShopProfile(ShopProfile profile) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/profile")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(profile)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public List<Address> getMyAddresses() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/addresses").GET().build();
        Type listType = new TypeToken<List<Address>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public void addAddress(Address address) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/addresses")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(address)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateAddress(Address address) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/addresses")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(address)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void deleteAddress(Address address) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/address")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(address)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public List<Order> getMyOrders() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/orders").GET().build();
        Type listType = new TypeToken<List<Order>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public Order getOrderById(String orderId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/order?id=" + orderId).GET().build();
        return apiClient.sendRequest(request, Order.class);
    }

    @Override
    public Order createOrder(Order order) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/orders")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(order)))
                .build();
        return apiClient.sendRequest(request, Order.class);
    }

    @Override
    public List<Coupon> getMyCoupons() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupons").GET().build();
        Type listType = new TypeToken<List<Coupon>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Coupon> getMyAvailableCoupons() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupons/available").GET().build();
        Type listType = new TypeToken<List<Coupon>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public void useMyCoupon(String couponId) throws ApiException {
        Map<String, String> body = Map.of("couponId", couponId);
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupon/use")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void addUserCoupon(Coupon coupon) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupon")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(coupon)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateUserCoupon(String couponId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupon?couponId=" + couponId)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    // --- 管理员操作 ---

    @Override
    public void addProduct(Product product) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(product)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateProduct(Product product) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/products")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(product)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void deleteProduct(String productId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/product?id=" + productId)
                .DELETE()
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void addCouponTemplate(Coupon coupon) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupon/template")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(coupon)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateCouponTemplate(Coupon coupon) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupon/template")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(coupon)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void deleteCouponTemplate(String couponId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupon/template?id=" + couponId)
                .DELETE()
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void addSalePromotion(SalePromotion promotion) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotion")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(promotion)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void updateSalePromotion(SalePromotion promotion) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotion")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(promotion)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void deleteSalePromotion(String promotionId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotion?id=" + promotionId)
                .DELETE()
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public List<Order> getAllOrders() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/orders").GET().build();
        Type listType = new TypeToken<List<Order>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }
    @Override
    public ShopProfile getUserProfile(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/profile?userId=" + userId).GET().build();
        return apiClient.sendRequest(request, ShopProfile.class);
    }

    @Override
    public List<Order> getUserOrders(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/orders?userId=" + userId).GET().build();
        Type listType = new TypeToken<List<Order>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }
}