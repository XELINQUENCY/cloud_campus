package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import entity.User;
import entity.studentRecord.StudentRecord;
import service.studentRecord.StudentRecordService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 负责处理所有学籍管理相关的API请求 (/api/studentrecord/**)
 */
public class StudentRecordHandler extends BaseHandler {

    private final StudentRecordService studentRecordService;

    public StudentRecordHandler(StudentRecordService studentRecordService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.studentRecordService = studentRecordService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();
        User authenticatedUser = (User) exchange.getAttribute("user");

        try {
            // 路由分发
            if (path.endsWith("/api/studentrecord/completed")) {
                switch (method) {
                    case "GET" -> handleListCompleted(exchange, authenticatedUser);
                    case "POST" -> handleAddCompleted(exchange, authenticatedUser);
                    case "DELETE" -> handleRemoveCompleted(exchange, authenticatedUser);
                    default -> sendJsonResponse(exchange, 405, Map.of("error", "方法不允许"));
                }
            } else if (path.startsWith("/api/studentrecord")) {
                switch (method) {
                    case "GET" -> handleGetRecord(exchange, authenticatedUser);
                    case "POST" -> handleCreateRecord(exchange, authenticatedUser);
                    case "PUT" -> handleUpdateRecord(exchange, authenticatedUser);
                    case "DELETE" -> handleDeleteRecord(exchange, authenticatedUser);
                    default -> sendJsonResponse(exchange, 405, Map.of("error", "方法不允许"));
                }
            } else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的学籍API路径"));
            }
        } catch (JsonSyntaxException e) {
            logger.log("请求体JSON格式错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "请求体JSON格式错误"));
        } catch (IllegalArgumentException e) {
            logger.log("请求参数错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.log("学籍服务业务逻辑错误: " + e.getMessage());
            // 业务异常（如权限不足）通常返回 403 Forbidden
            sendJsonResponse(exchange, 403, Map.of("error", e.getMessage()));
        }
    }

    private void handleGetRecord(HttpExchange exchange, User user) throws Exception {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String studentId = params.get("id");
        if (studentId == null || studentId.isEmpty()) {
            throw new IllegalArgumentException("必须提供 'id' 查询参数");
        }
        StudentRecord record = studentRecordService.getRecord(user, studentId);
        sendJsonResponse(exchange, 200, record);
    }

    private void handleCreateRecord(HttpExchange exchange, User user) throws Exception {
        StudentRecord record = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), StudentRecord.class);
        boolean success = studentRecordService.createRecord(user, record);
        sendJsonResponse(exchange, 201, Map.of("success", success, "message", "创建成功"));
    }

    private void handleUpdateRecord(HttpExchange exchange, User user) throws Exception {
        StudentRecord record = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), StudentRecord.class);
        boolean success = studentRecordService.updateRecord(user, record);
        sendJsonResponse(exchange, 200, Map.of("success", success, "message", "更新成功"));
    }

    private void handleDeleteRecord(HttpExchange exchange, User user) throws Exception {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String studentId = params.get("id");
        if (studentId == null || studentId.isEmpty()) {
            throw new IllegalArgumentException("必须提供 'id' 查询参数");
        }
        boolean success = studentRecordService.deleteRecord(user, studentId);
        sendJsonResponse(exchange, 200, Map.of("success", success, "message", "删除成功"));
    }

    private void handleListCompleted(HttpExchange exchange, User user) throws Exception {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String studentId = params.get("id");
        if (studentId == null || studentId.isEmpty()) {
            throw new IllegalArgumentException("必须提供 'id' 查询参数");
        }
        sendJsonResponse(exchange, 200, studentRecordService.listCompletedCourses(user, studentId));
    }

    private void handleAddCompleted(HttpExchange exchange, User user) throws Exception {
        Map<String, String> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, String>>(){}.getType());
        String studentId = body.get("studentId");
        String courseId = body.get("courseId");
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("请求体必须包含 'studentId' 和 'courseId'");
        }
        boolean success = studentRecordService.addCompletedCourse(user, studentId, courseId);
        sendJsonResponse(exchange, 200, Map.of("success", success, "message", "添加成功"));
    }

    private void handleRemoveCompleted(HttpExchange exchange, User user) throws Exception {
        Map<String, String> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, String>>(){}.getType());
        String studentId = body.get("studentId");
        String courseId = body.get("courseId");
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("请求体必须包含 'studentId' 和 'courseId'");
        }
        boolean success = studentRecordService.removeCompletedCourse(user, studentId, courseId);
        sendJsonResponse(exchange, 200, Map.of("success", success, "message", "移除成功"));
    }
}