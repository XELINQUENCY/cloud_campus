package controller.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entity.User;
import enums.UserRole;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * 所有请求处理器的抽象基类，提供通用功能，如日志记录和JSON响应。
 */
public abstract class BaseHandler implements HttpHandler {

    protected final Gson gson;
    protected final ServerLogger logger;

    public BaseHandler(Gson gson, ServerLogger logger) {
        this.gson = gson;
        this.logger = logger;
    }

    /**
     * 辅助方法，用于发送JSON响应
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * 辅助方法，用于解析URL查询参数
     */
    protected Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new java.util.HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], java.net.URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    /**
     * 辅助方法，用于检查用户角色。
     */
    protected boolean hasRole(HttpExchange exchange, UserRole role) {
        Object userObj = exchange.getAttribute("user");
        if (userObj instanceof User) {
            return ((User) userObj).hasRole(role);
        }
        return false;
    }
}

