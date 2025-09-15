package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import entity.shop.*;
import enums.UserRole;
import service.shop.CouponService;
import service.shop.ProductService;
import service.shop.SalePromotionService;
import service.shop.ShopService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 商店模块的统一请求处理器 (基于已有Service的最终版)。
 * 负责处理所有以 "/api/shop/" 开头的API请求。
 * 本版本精确地调用已提供的Service接口，整合了旧项目中
 * Product, Coupon, SalePro, Student 四个 Controller 的全部功能。
 */
public class ShopHandler extends BaseHandler {

    private final ShopService shopService;
    private final ProductService productService;
    private final CouponService couponService;
    private final SalePromotionService salePromotionService;

    public ShopHandler(ShopService shopService, ProductService productService, CouponService couponService, SalePromotionService salePromotionService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.shopService = shopService;
        this.productService = productService;
        this.couponService = couponService;
        this.salePromotionService = salePromotionService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String method = exchange.getRequestMethod();
        String authenticatedUserId = (String) exchange.getAttribute("userId");

        try {
            // --- 路由分发 ---
            // --- 商品管理 (来自 ProductController) ---
            if (path.equals("/api/shop/products") && "GET".equalsIgnoreCase(method)) {
                handleGetAllProducts(exchange);
            } else if (path.equals("/api/shop/product") && "GET".equalsIgnoreCase(method)) {
                handleGetProductById(exchange, params);
            } else if (path.equals("/api/shop/products/category") && "GET".equalsIgnoreCase(method)) {
                handleGetProductsByCategory(exchange, params);
            } else if (path.equals("/api/shop/products/search") && "GET".equalsIgnoreCase(method)) {
                handleSearchProducts(exchange, params);
            } else if (path.equals("/api/shop/categories") && "GET".equalsIgnoreCase(method)) {
                handleGetAllCategories(exchange);
            } else if (path.equals("/api/shop/products/grouped") && "GET".equalsIgnoreCase(method)) {
                handleGetProductsGroupedByCategory(exchange);
            } else if (path.equals("/api/shop/products") && "POST".equalsIgnoreCase(method)) {
                handleAddProduct(exchange);
            } else if (path.equals("/api/shop/products") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateProduct(exchange);
            } else if (path.equals("/api/shop/product/stock") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateProductStock(exchange);
            } else if (path.equals("/api/shop/product/sales") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateProductSales(exchange);
            } else if (path.equals("/api/shop/product") && "DELETE".equalsIgnoreCase(method)) {
                handleDeleteProduct(exchange, params);
            }

            // --- 优惠券管理 (来自 CouponController) ---
            else if (path.equals("/api/shop/coupons/templates") && "GET".equalsIgnoreCase(method)) {
                handleGetAllCouponTemplates(exchange);
            } else if (path.equals("/api/shop/coupons/templates/available") && "GET".equalsIgnoreCase(method)) {
                handleGetAvailableCouponTemplates(exchange);
            } else if (path.equals("/api/shop/coupon/template") && "GET".equalsIgnoreCase(method)) {
                handleGetCouponById(exchange, params);
            } else if (path.equals("/api/shop/coupon/template") && "POST".equalsIgnoreCase(method)) {
                handleAddCouponTemplate(exchange);
            } else if (path.equals("/api/shop/coupon/template") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateCouponTemplate(exchange);
            } else if (path.equals("/api/shop/coupon/template") && "DELETE".equalsIgnoreCase(method)) {
                handleDeleteCouponTemplate(exchange, params);
            } else if (path.equals("/api/shop/user/coupons") && "GET".equalsIgnoreCase(method)) {
                handleGetMyCoupons(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/coupons/available") && "GET".equalsIgnoreCase(method)) {
                handleGetMyAvailableCoupons(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/coupon/use") && "POST".equalsIgnoreCase(method)) {
                handleUseMyCoupon(exchange, authenticatedUserId);
            }

            // --- 促销活动管理 (来自 SaleProController) ---
            else if (path.equals("/api/shop/promotions") && "GET".equalsIgnoreCase(method)) {
                handleGetAllPromotions(exchange);
            } else if (path.equals("/api/shop/promotion") && "GET".equalsIgnoreCase(method)) {
                handleGetPromotionById(exchange, params);
            } else if (path.equals("/api/shop/promotions/product") && "GET".equalsIgnoreCase(method)) {
                handleGetPromotionsByProductId(exchange, params);
            } else if (path.equals("/api/shop/promotion") && "POST".equalsIgnoreCase(method)) {
                handleAddPromotion(exchange);
            } else if (path.equals("/api/shop/promotion") && "PUT".equalsIgnoreCase(method)) {
                handleUpdatePromotion(exchange);
            } else if (path.equals("/api/shop/promotion") && "DELETE".equalsIgnoreCase(method)) {
                handleDeletePromotion(exchange, params);
            }

            // --- 用户中心 (来自 StudentController, 由 ShopService 实现) ---
            else if (path.equals("/api/shop/user/profile") && "GET".equalsIgnoreCase(method)) {
                handleGetUserProfile(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/profile/initialize") && "POST".equalsIgnoreCase(method)) {
                handleInitializeShopProfile(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/profile") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateShopProfile(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/orders") && "GET".equalsIgnoreCase(method)) {
                handleGetUserOrders(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/order") && "GET".equalsIgnoreCase(method)) {
                handleGetOrderById(exchange, params, authenticatedUserId);
            } else if (path.equals("/api/shop/orders") && "GET".equalsIgnoreCase(method)) {
                handleGetAllOrders(exchange);
            } else if (path.equals("/api/shop/user/orders") && "POST".equalsIgnoreCase(method)) {
                handleCreateOrder(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/addresses") && "GET".equalsIgnoreCase(method)) {
                handleGetUserAddresses(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/addresses") && "POST".equalsIgnoreCase(method)) {
                handleAddAddress(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/addresses") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateAddress(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/address") && "DELETE".equalsIgnoreCase(method)) {
                handleDeleteAddress(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/coupon") && "POST".equalsIgnoreCase(method)) {
                handleAddUserCoupon(exchange, authenticatedUserId);
            } else if (path.equals("/api/shop/user/coupon") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateUserCoupon(exchange, authenticatedUserId, params);
            }

            else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的商店API路径: " + path));
            }
        } catch (JsonSyntaxException e) {
            logger.log("请求体JSON格式错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "请求体JSON格式错误"));
        } catch (IllegalArgumentException e) {
            logger.log("请求参数错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.log("商店服务业务逻辑错误: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误: " + e.getMessage()));
        }
    }

    // ===================================================================
    // Private Handler Methods - 严格按照 Service 接口实现
    // ===================================================================

    // --- Product Logic ---
    private void handleGetAllProducts(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, productService.getAllProducts());
    }

    private void handleGetProductById(HttpExchange exchange, Map<String, String> params) throws IOException {
        String productId = params.get("id");
        if (productId == null) throw new IllegalArgumentException("必须提供 'id' 参数");

        Product product = productService.getProductById(productId);
        if (product != null) {
            sendJsonResponse(exchange, 200, product);
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "商品未找到"));
        }
    }

    private void handleGetProductsByCategory(HttpExchange exchange, Map<String, String> params) throws IOException {
        String category = params.get("category");
        if (category == null) throw new IllegalArgumentException("必须提供 'category' 参数");
        sendJsonResponse(exchange, 200, productService.getProductsByCategory(category));
    }

    private void handleSearchProducts(HttpExchange exchange, Map<String, String> params) throws IOException {
        String name = params.get("name");
        String id = params.get("id");
        if (name != null) {
            sendJsonResponse(exchange, 200, productService.searchProductsByName(name));
        } else if (id != null) {
            sendJsonResponse(exchange, 200, productService.searchProductsById(id));
        } else {
            throw new IllegalArgumentException("必须提供 'name' 或 'id' 参数");
        }
    }

    private void handleGetAllCategories(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, productService.getAllCategories());
    }

    private void handleGetProductsGroupedByCategory(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, productService.getProductsGroupedByCategory());
    }

    private void handleAddProduct(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Product product = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Product.class);
        productService.addProduct(product);
        sendJsonResponse(exchange, 201, Map.of("message", "商品添加成功", "productId", product.getProductId()));
    }

    private void handleUpdateProduct(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Product product = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Product.class);
        productService.updateProduct(product);
        sendJsonResponse(exchange, 200, Map.of("message", "商品更新成功"));
    }

    private void handleUpdateProductStock(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Map<String, Object> request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Map.class);
        String productId = (String) request.get("productId");
        Integer newStock = ((Double) request.get("newStock")).intValue();
        if (productId == null || newStock == null) throw new IllegalArgumentException("必须提供 'productId' 和 'newStock'");
        productService.updateStock(productId, newStock);
        sendJsonResponse(exchange, 200, Map.of("message", "库存更新成功"));
    }

    private void handleUpdateProductSales(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Map<String, Object> request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Map.class);
        String productId = (String) request.get("productId");
        Integer soldAmount = ((Double) request.get("soldAmount")).intValue();
        if (productId == null || soldAmount == null) throw new IllegalArgumentException("必须提供 'productId' 和 'soldAmount'");
        productService.updateSales(productId, soldAmount);
        sendJsonResponse(exchange, 200, Map.of("message", "销量更新成功"));
    }

    private void handleDeleteProduct(HttpExchange exchange, Map<String, String> params) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        String productId = params.get("id");
        if (productId == null) throw new IllegalArgumentException("必须在URL参数中提供商品 'id'");

        productService.deleteProduct(productId);
        sendJsonResponse(exchange, 200, Map.of("message", "商品删除成功"));
    }

    // --- Coupon Logic ---
    private void handleGetAllCouponTemplates(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, couponService.getAllCouponTemplates());
    }

    private void handleGetAvailableCouponTemplates(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, couponService.getAvailableCouponTemplate());
    }

    private void handleGetCouponById(HttpExchange exchange, Map<String, String> params) throws IOException {
        String couponId = params.get("id");
        if (couponId == null) throw new IllegalArgumentException("必须提供 'id' 参数");
        Coupon coupon = couponService.getCouponById(couponId);
        if (coupon != null) {
            sendJsonResponse(exchange, 200, coupon);
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "优惠券未找到"));
        }
    }

    private void handleAddCouponTemplate(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Coupon coupon = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Coupon.class);
        couponService.addCouponTemplate(coupon);
        sendJsonResponse(exchange, 201, Map.of("message", "优惠券模板添加成功"));
    }

    private void handleUpdateCouponTemplate(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        Coupon coupon = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Coupon.class);
        couponService.updateCouponTemplate(coupon);
        sendJsonResponse(exchange, 200, Map.of("message", "优惠券模板更新成功"));
    }

    private void handleDeleteCouponTemplate(HttpExchange exchange, Map<String, String> params) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        String couponId = params.get("id");
        if (couponId == null) throw new IllegalArgumentException("必须在URL参数中提供优惠券 'id'");
        couponService.deleteCouponTemplate(couponId);
        sendJsonResponse(exchange, 200, Map.of("message", "优惠券模板删除成功"));
    }

    private void handleGetMyCoupons(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        sendJsonResponse(exchange, 200, couponService.getMyCoupons(userId));
    }

    private void handleGetMyAvailableCoupons(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        sendJsonResponse(exchange, 200, couponService.getMyAvailableCoupons(userId));
    }

    private void handleUseMyCoupon(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Map<String, String> request = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Map.class);
        String couponId = request.get("couponId");
        if (couponId == null) throw new IllegalArgumentException("必须提供 'couponId'");
        couponService.useMyCoupon(couponId, userId);
        sendJsonResponse(exchange, 200, Map.of("message", "优惠券使用成功"));
    }

    // --- SalePromotion Logic ---
    private void handleGetAllPromotions(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, salePromotionService.getAllPromotions());
    }

    private void handleGetPromotionById(HttpExchange exchange, Map<String, String> params) throws IOException {
        String promotionId = params.get("id");
        if (promotionId == null) throw new IllegalArgumentException("必须提供 'id' 参数");
        SalePromotion promotion = salePromotionService.getPromotionById(promotionId);
        if (promotion != null) {
            sendJsonResponse(exchange, 200, promotion);
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "促销活动未找到"));
        }
    }

    private void handleGetPromotionsByProductId(HttpExchange exchange, Map<String, String> params) throws IOException {
        String productId = params.get("productId");
        if (productId == null) throw new IllegalArgumentException("必须提供 'productId' 参数");
        sendJsonResponse(exchange, 200, salePromotionService.getPromotionsByProductId(productId));
    }

    private void handleAddPromotion(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        SalePromotion promotion = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), SalePromotion.class);
        salePromotionService.addPromotion(promotion);
        sendJsonResponse(exchange, 201, Map.of("message", "促销活动添加成功"));
    }

    private void handleUpdatePromotion(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        SalePromotion promotion = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), SalePromotion.class);
        salePromotionService.updatePromotion(promotion);
        sendJsonResponse(exchange, 200, Map.of("message", "促销活动更新成功"));
    }

    private void handleDeletePromotion(HttpExchange exchange, Map<String, String> params) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        String promotionId = params.get("id");
        if (promotionId == null) throw new IllegalArgumentException("必须在URL参数中提供促销活动 'id'");
        salePromotionService.deletePromotion(promotionId);
        sendJsonResponse(exchange, 200, Map.of("message", "促销活动删除成功"));
    }

    // --- User-Centric Logic (via ShopService) ---
    private void handleGetUserProfile(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        sendJsonResponse(exchange, 200, shopService.getShopProfile(userId));
    }

    private void handleInitializeShopProfile(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        int result = shopService.initializeShopProfile(userId);
        if (result > 0) {
            sendJsonResponse(exchange, 201, Map.of("message", "用户资料初始化成功"));
        } else {
            sendJsonResponse(exchange, 500, Map.of("error", "用户资料初始化失败"));
        }
    }

    private void handleUpdateShopProfile(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        ShopProfile profile = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), ShopProfile.class);
        profile.setUserId(userId); // 确保 profile 的 userId 与认证用户一致
        shopService.updateShopProfile(profile);
        sendJsonResponse(exchange, 200, Map.of("message", "用户资料更新成功"));
    }

    private void handleGetUserOrders(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        sendJsonResponse(exchange, 200, shopService.getOrdersByUserId(userId));
    }

    private void handleGetOrderById(HttpExchange exchange, Map<String, String> params, String userId) throws IOException {
        String orderId = params.get("id");
        if (orderId == null) throw new IllegalArgumentException("必须提供 'id' 参数");
        Order order = shopService.getOrdersById(orderId);
        if (order != null) {
            // 安全检查：普通用户只能获取自己的订单，管理员可以获取所有订单
            if (!hasRole(exchange, UserRole.STORE_ADMIN) && !order.getUserId().equals(userId)) {
                sendJsonResponse(exchange, 403, Map.of("error", "无权访问此订单"));
                return;
            }
            sendJsonResponse(exchange, 200, order);
        } else {
            sendJsonResponse(exchange, 404, Map.of("error", "订单未找到"));
        }
    }

    private void handleGetAllOrders(HttpExchange exchange) throws IOException {
        if (!hasRole(exchange, UserRole.STORE_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅管理员可操作"));
            return;
        }
        sendJsonResponse(exchange, 200, shopService.getAllOrders());
    }

    private void handleCreateOrder(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Order order = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Order.class);
        shopService.createOrder(userId, order);
        sendJsonResponse(exchange, 201, Map.of("message", "订单创建成功"));
    }

    private void handleGetUserAddresses(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        sendJsonResponse(exchange, 200, shopService.getAddressesByUserId(userId));
    }

    private void handleAddAddress(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Address address = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Address.class);
        shopService.addAddress(userId, address);
        sendJsonResponse(exchange, 201, Map.of("message", "地址添加成功"));
    }

    private void handleUpdateAddress(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Address address = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Address.class);
        shopService.updateAddress(userId, address);
        sendJsonResponse(exchange, 200, Map.of("message", "地址更新成功"));
    }

    private void handleDeleteAddress(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Address address = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Address.class);
        shopService.deleteAddress(userId, address);
        sendJsonResponse(exchange, 200, Map.of("message", "地址删除成功"));
    }

    private void handleAddUserCoupon(HttpExchange exchange, String userId) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Coupon coupon = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Coupon.class);
        int result = shopService.addCoupon(userId, coupon);
        if (result > 0) {
            sendJsonResponse(exchange, 201, Map.of("message", "优惠券添加成功"));
        } else {
            sendJsonResponse(exchange, 500, Map.of("error", "优惠券添加失败"));
        }
    }

    private void handleUpdateUserCoupon(HttpExchange exchange, String userId, Map<String, String> params) throws IOException {
        if (userId == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        String couponId = params.get("couponId");
        if (couponId == null) throw new IllegalArgumentException("必须提供 'couponId' 参数");
        int result = shopService.updateCoupon(couponId, userId);
        if (result > 0) {
            sendJsonResponse(exchange, 200, Map.of("message", "优惠券更新成功"));
        } else {
            sendJsonResponse(exchange, 500, Map.of("error", "优惠券更新失败"));
        }
    }
}
