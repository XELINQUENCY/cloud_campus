package controller.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import service.AuthService; // 你可能需要创建一个只包含BankAuthenticator的AuthService实例
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 专门处理银行认证请求 (/api/bank/auth/**)
 */
public class BankAuthHandler extends BaseHandler {
    private final AuthService bankAuthService;

    // 构造函数接收一个只包含 BankUserAuthenticator 的 AuthService 实例
    public BankAuthHandler(AuthService bankAuthService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.bankAuthService = bankAuthService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!path.equals("/api/bank/auth/login")) {
            sendJsonResponse(exchange, 404, Map.of("error", "未知的银行认证API"));
            return;
        }

        try {
            LoginRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), LoginRequest.class);
            logger.log("收到银行登录请求: user=" + req.getUsername());

            // 这个 bankAuthService 只会执行银行认证
            User user = bankAuthService.login(req.getUsername(), req.getPassword(), false);
            String token = bankAuthService.generateToken(user); // 为银行会话生成Token

            logger.log("银行用户 " + user.getName() + " 登录成功。");
            sendJsonResponse(exchange, 200, new LoginResponse(token, user));

        } catch (Exception e) {
            logger.log("银行认证失败: " + e.getMessage());
            sendJsonResponse(exchange, 401, Map.of("error", e.getMessage()));
        }
    }
}