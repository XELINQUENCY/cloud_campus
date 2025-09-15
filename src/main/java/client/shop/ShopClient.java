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
import java.util.ArrayList;
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
    public Map<String, List<Product>> getProductsGroupedByCategory() throws ApiException {
        List<Product> allProducts = getAllProducts();
        if (allProducts == null) {
            return new HashMap<>();
        }
        return allProducts.stream().collect(Collectors.groupingBy(Product::getCategory));
    }

    @Override
    public Product getProductById(String productId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/product?id=" + productId).GET().build();
        return apiClient.sendRequest(request, Product.class);
    }

    @Override
    public List<Coupon> getAllCouponTemplates() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/coupons/templates").GET().build();
        Type listType = new TypeToken<List<Coupon>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<SalePromotion> getAllPromotions() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/promotions").GET().build();
        Type listType = new TypeToken<List<SalePromotion>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    // --- 普通用户操作 ---

    @Override
    public ShopProfile getMyShopProfile() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/profile").GET().build();
        return apiClient.sendRequest(request, ShopProfile.class);
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
    public void claimCoupon(String couponId) throws ApiException {
        // 假设服务器端有一个 /api/shop/user/coupons/claim 接口
        Map<String, String> body = Map.of("couponId", couponId);
        HttpRequest request = apiClient.newRequestBuilder("/shop/user/coupons/claim")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
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

    // 其他管理员方法的实现...
    @Override
    public void addCouponTemplate(Coupon coupon) throws ApiException {
        // 假设接口为 POST /api/shop/coupons/templates
    }

    @Override
    public void updateCouponTemplate(Coupon coupon) throws ApiException {
        // 假设接口为 PUT /api/shop/coupons/templates
    }

    @Override
    public void deleteCouponTemplate(String couponId) throws ApiException {
        // 假设接口为 DELETE /api/shop/coupons/templates/{id}
    }

    @Override
    public void addSalePromotion(SalePromotion promotion) throws ApiException {
        // 假设接口为 POST /api/shop/promotions
    }

    @Override
    public void updateSalePromotion(SalePromotion promotion) throws ApiException {
        // 假设接口为 PUT /api/shop/promotions
    }

    @Override
    public void deleteSalePromotion(String promotionId) throws ApiException {
        // 假设接口为 DELETE /api/shop/promotions/{id}
    }
    @Override
    public List<Order> getAllOrdersForAdmin() throws ApiException {
        // 假设有一个管理员专用接口获取所有订单
        HttpRequest request = apiClient.newRequestBuilder("/shop/admin/orders").GET().build();
        Type listType = new TypeToken<List<Order>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }
}
