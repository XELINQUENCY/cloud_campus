package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import entity.bank.BankAccount;
import service.AuthService;
import service.bank.IBankServerSrv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 负责处理所有银行相关的API请求 (/api/bank/**)
 * 实现了对服务层抛出的业务异常的精确捕获和处理。
 */
public class BankHandler extends BaseHandler {
    private final IBankServerSrv bankService;

    public BankHandler(IBankServerSrv bankService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.bankService = bankService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String authenticatedUserId = (String) exchange.getAttribute("userId");

        try {
            // --- 银行账户注册 (需要系统级认证) ---
            if (path.equals("/api/bank/register") && "POST".equalsIgnoreCase(method)) {
                Map<String, String> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType());
                boolean success = bankService.register(body.get("userId"), body.get("password"));
                sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success, "message", success ? "注册成功" : "用户已存在"));
            }
            // --- 创建银行卡 (需要系统级认证) ---
            else if (path.equals("/api/bank/accounts") && "POST".equalsIgnoreCase(method)) {
                if (authenticatedUserId == null) {
                    sendJsonResponse(exchange, 401, Map.of("error", "银行用户未登录"));return;
                }
                BankAccount newAccount = bankService.createAccount(authenticatedUserId);
                sendJsonResponse(exchange, newAccount != null ? 201 : 400, newAccount);
            }
            // --- 获取用户所有银行卡 (需要系统级认证) ---
            else if (path.matches("/api/bank/accounts/user/[^/]+") && "GET".equalsIgnoreCase(method)) {
                String[] pathParts = path.split("/");
                String requestedUserId = pathParts[pathParts.length - 1];

                if (authenticatedUserId == null) {
                    sendJsonResponse(exchange, 401, Map.of("error", "用户未登录"));
                    return;
                }
                // 用户只能获取自己的账户列表
                if (!authenticatedUserId.equals(requestedUserId)) {
                    sendJsonResponse(exchange, 403, Map.of("error", "权限不足，无法查看他人账户列表"));
                    return;
                }
                sendJsonResponse(exchange, 200, bankService.getUserAccounts(authenticatedUserId));
            }
            // --- 存款 (常规操作) ---
            else if (path.equals("/api/bank/deposit") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>(){}.getType());
                String accountId = (String) body.get("accountId");
                BigDecimal amount = new BigDecimal(body.get("amount").toString());
                boolean success = bankService.deposit(accountId, amount);
                sendJsonResponse(exchange, 200, Map.of("success", success));
            }
            // --- 取款 (敏感操作) ---
            else if (path.equals("/api/bank/withdraw") && "POST".equalsIgnoreCase(method)) {
                if (authenticatedUserId == null) {
                    sendJsonResponse(exchange, 401, Map.of("error", "用户未登录"));
                    return;
                }
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>(){}.getType());
                String accountId = (String) body.get("accountId");
                BigDecimal amount = new BigDecimal(body.get("amount").toString());
                String password = (String) body.get("password");

                boolean success = bankService.withdraw(authenticatedUserId, accountId, amount, password);
                sendJsonResponse(exchange, 200, Map.of("success", success));
            }
            // --- 转账 (敏感操作) ---
            else if (path.equals("/api/bank/transfer") && "POST".equalsIgnoreCase(method)) {
                if (authenticatedUserId == null) {
                    sendJsonResponse(exchange, 401, Map.of("error", "用户未登录"));
                    return;
                }
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>(){}.getType());
                String fromAccountId = (String) body.get("fromAccountId");
                String toAccountId = (String) body.get("toAccountId");
                BigDecimal amount = new BigDecimal(body.get("amount").toString());
                String password = (String) body.get("password");

                boolean success = bankService.transfer(authenticatedUserId, fromAccountId, toAccountId, amount, password);
                sendJsonResponse(exchange, 200, Map.of("success", success));
            }else if(path.matches("/api/bank/transactions/[^/]+") && "GET".equalsIgnoreCase(method)){

            }else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的银行API路径: " + path));
            }
        } catch (JsonSyntaxException e) {
            logger.log("银行请求JSON格式错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON格式: " + e.getMessage()));
        } catch (Exception e) {
            logger.log("银行服务业务逻辑错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        }
    }
}
