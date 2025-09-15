package controller.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import service.AuthService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 负责处理所有认证相关的API请求 (/api/auth/**)
 */
public class AuthHandler extends BaseHandler {
    private final AuthService authService;

    public AuthHandler(AuthService authService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                LoginRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), LoginRequest.class);
                logger.log("收到登录请求: user=" + req.getUsername() + ", isAdmin=" + req.isAdmin());
                User user = authService.login(req.getUsername(), req.getPassword(), req.isAdmin());
                String token = authService.generateToken(user);
                logger.log("用户 " + user.getName() + " 登录成功, 生成Token。");
                sendJsonResponse(exchange, 200, new LoginResponse(token, user));
            } else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的认证API"));
            }
        } catch (Exception e) {
            logger.log("认证失败: " + e.getMessage());
            sendJsonResponse(exchange, 401, Map.of("error", e.getMessage()));
        }
    }
}

