package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import entity.User;
import enums.UserRole;
import service.user.UserManagementService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 负责处理所有与用户管理相关的API请求 (/api/users/**)
 */
public class UserManagementHandler extends BaseHandler {

    private final UserManagementService userService;

    public UserManagementHandler(UserManagementService userService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        User currentUser = (User) exchange.getAttribute("user");

        try {
            // --- 路由分发 ---
            if (path.equals("/api/users/register") && "POST".equalsIgnoreCase(method)) {
                handleRegister(exchange);
            }
            else if (path.equals("/api/users/profile") && "GET".equalsIgnoreCase(method)) {
                // GET自身的profile信息, currentUser从AuthFilter中来
                sendJsonResponse(exchange, 200, currentUser);
            }
            else if (path.equals("/api/users/profile") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateProfile(exchange, currentUser);
            }
            else if (path.equals("/api/users/admin/all") && "GET".equalsIgnoreCase(method)) {
                handleGetAllUsers(exchange, currentUser);
            }
            else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的用户管理API路径: " + path));
            }

        } catch (JsonSyntaxException e) {
            sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON请求体"));
        } catch (Exception e) {
            logger.log("用户管理服务错误: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误: " + e.getMessage()));
        }
    }

    private void handleRegister(HttpExchange exchange) throws Exception {
        User userToRegister = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                User.class
        );

        User newUser = userService.registerUser(userToRegister);

        logger.log("新用户注册成功: " + newUser.getName());
        sendJsonResponse(exchange, 201, newUser);
    }

    private void handleUpdateProfile(HttpExchange exchange, User currentUser) throws Exception {
        User updatedUserData = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                User.class
        );
        // 安全性：确保用户只能更新自己的信息，ID从Token解析出的currentUser中获取
        updatedUserData.setId(currentUser.getId());

        User result = userService.updateUser(updatedUserData);

        logger.log("用户 " + currentUser.getName() + " 更新了个人信息");
        sendJsonResponse(exchange, 200, result);
    }

    private void handleGetAllUsers(HttpExchange exchange, User currentUser) throws IOException {
        if (currentUser == null || !currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足"));
            return;
        }

        List<User> allUsers = userService.findAllUsers();

        sendJsonResponse(exchange, 200, allUsers);
    }
}

