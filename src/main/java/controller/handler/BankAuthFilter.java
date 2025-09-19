// controller/BankAuthFilter.java (新文件)
package controller.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import entity.User;
import service.AuthService; // 保持不变

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 银行模块专用的认证过滤器。
 * 只验证由 BankAuthService 签发的令牌。
 */
public class BankAuthFilter extends Filter {

    private final AuthService bankAuthService; // 1. 【修改】明确这是银行的AuthService
    private final Gson gson;

    // 2. 【修改】构造函数，接收银行专用的AuthService和Gson实例
    public BankAuthFilter(AuthService bankAuthService, Gson gson) {
        this.bankAuthService = bankAuthService;
        this.gson = gson;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // 3. 【修改】定义银行模块的白名单
        List<String> whitelist = List.of(
                "/api/bank/auth/login", // 银行登录接口本身
                "/api/bank/register"    // 银行注册接口
        );

        if (whitelist.contains(path)) {
            chain.doFilter(exchange); // 白名单请求直接放行
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 4. 【修改】使用 bankAuthService 来验证令牌
                String userId = bankAuthService.validateToken(token);
                // 注意：银行的token解码后可能只包含userId，不包含完整的User对象
                // 我们只将 userId 放入属性中，BankHandler 将依赖这个ID进行操作
                exchange.setAttribute("userId", userId);
                chain.doFilter(exchange);
            } catch (Exception e) {
                sendJsonResponse(exchange, 401, Map.of("error", "银行令牌无效或已过期: " + e.getMessage()));
            }
        } else {
            sendJsonResponse(exchange, 401, Map.of("error", "缺少银行认证令牌"));
        }
    }

    @Override
    public String description() {
        return "Bank API Authentication Filter";
    }

    // 辅助方法，用于发送JSON响应
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}