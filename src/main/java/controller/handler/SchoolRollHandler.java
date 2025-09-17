package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import dto.schoolroll.StudentDetailDTO;
import entity.StudentQueryCriteria;
import entity.User;
import entity.schoolroll.Student; // 假设您的学籍实体类路径
import enums.UserRole; // 假设您有角色枚举
import service.schoolroll.impl.StudentServiceImpl; // 假设您的学籍服务类路径
import service.schoolroll.exception.ForbiddenException; // 假设的自定义异常
import service.schoolroll.exception.NotFoundException;   // 假设的自定义异常
import service.schoolroll.exception.BadRequestException; // 假设的自定义异常

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责处理所有学籍管理相关的API请求 (/api/schoolroll/**)
 */
public class SchoolRollHandler extends BaseHandler {

    private final StudentServiceImpl studentServiceImpl;

    // 使用正则表达式匹配包含ID的路径，例如 /api/schoolroll/records/S001
    // 匹配到学号ID
    private static final Pattern RECORD_ID_PATTERN = Pattern.compile("^/api/schoolroll/records/([^/]+)$");
    // 匹配 /api/schoolroll/records/details/{id}
    private static final Pattern DETAIL_ID_PATTERN = Pattern.compile("^/api/schoolroll/records/details/([^/]+)$");


    public SchoolRollHandler(StudentServiceImpl studentServiceImpl, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.studentServiceImpl = studentServiceImpl;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        User authenticatedUser = (User) exchange.getAttribute("user"); // 从AuthFilter获取用户信息

        // 权限检查：所有学籍操作都需要用户先登录
        if (authenticatedUser == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证，请先登录"));
            return;
        }

        try {
            // --- 路由分发 ---

            // 匹配 GET /api/schoolroll/records/{id}
            Matcher recordIdMatcher = RECORD_ID_PATTERN.matcher(path);
            if (recordIdMatcher.matches() && "GET".equalsIgnoreCase(method)) {
                String studentId = recordIdMatcher.group(1);
                handleGetStudent(exchange, studentId, authenticatedUser);
                return;
            }

            // 匹配 GET /api/schoolroll/records/details/{id}
            Matcher detailIdMatcher = DETAIL_ID_PATTERN.matcher(path);
            if (detailIdMatcher.matches() && "GET".equalsIgnoreCase(method)) {
                String studentId = detailIdMatcher.group(1);
                handleGetStudentDetails(exchange, studentId, authenticatedUser); // 调用新方法
                return;
            }

            // --- 所有写操作（创建、更新、删除）都使用 POST ---
            if ("POST".equalsIgnoreCase(method)) {
                switch (path) {
                    case "/api/schoolroll/records/create" -> handleCreateRecord(exchange, authenticatedUser);
                    case "/api/schoolroll/records/update" -> handleUpdateRecord(exchange, authenticatedUser);
                    case "/api/schoolroll/records/delete" -> handleDeleteRecord(exchange, authenticatedUser);
                    case "/api/schoolroll/records/search" -> handleSearchRecords(exchange, authenticatedUser);
                    case "/api/schoolroll/records/detail/serach" ->handleSearchRecordDetails(exchange, authenticatedUser);
                    default -> sendJsonResponse(exchange, 404, Map.of("error", "未知的学籍API路径: " + path));
                }
                return;
            }

            // 如果方法不是GET或POST，或者路径不匹配任何POST路由
            sendJsonResponse(exchange, 404, Map.of("error", "未知的API路径或不支持的请求方法"));

        } catch (JsonSyntaxException e) {
            logger.log("学籍模块请求JSON格式错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON格式"));
        } catch (ForbiddenException e) {
            logger.log("权限不足: " + e.getMessage());
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足: " + e.getMessage()));
        } catch (NotFoundException e) {
            logger.log("资源未找到: " + e.getMessage());
            sendJsonResponse(exchange, 404, Map.of("error", e.getMessage()));
        } catch (BadRequestException e) {
            logger.log("无效请求: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "无效请求: " + e.getMessage()));
        } catch (Exception e) {
            logger.log("学籍模块发生未知错误: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误"));
        }
    }


    // [新增] 处理获取单个学生详细信息的请求
    private void handleGetStudentDetails(HttpExchange exchange, String studentId, User currentUser) throws Exception {
        StudentDetailDTO record = studentServiceImpl.getStudentDetails(studentId, currentUser);
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "record", record));
    }

    // [新增] 处理搜索学生详细信息的请求
    private void handleSearchRecordDetails(HttpExchange exchange, User currentUser) throws Exception {
        StudentQueryCriteria criteria = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                StudentQueryCriteria.class
        );
        List<StudentDetailDTO> results = studentServiceImpl.searchStudentDetails(criteria, currentUser);
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "records", results));
    }

    private void handleGetStudent(HttpExchange exchange, String studentId, User currentUser) throws Exception {
        Student record = studentServiceImpl.getStudent(studentId, currentUser);
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "record", record));
    }

    private void handleCreateRecord(HttpExchange exchange, User currentUser) throws Exception {
        // 权限检查也可以放在Controller/Handler层
        if (!hasRole(exchange, UserRole.ACADEMIC_ADMIN)) { // 假设 UserRole.ADMIN 是管理员角色
            throw new ForbiddenException("仅管理员可创建学籍");
        }
        Student newRecord = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Student.class);
        studentServiceImpl.createStudent(newRecord, currentUser);
        sendJsonResponse(exchange, 201, Map.of("status", "ok", "message", "学籍创建成功"));
    }

    private void handleUpdateRecord(HttpExchange exchange, User currentUser) throws Exception {
        Student recordToUpdate = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Student.class);
        studentServiceImpl.updateStudent(recordToUpdate, currentUser);
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "message", "学籍更新成功"));
    }

    private void handleDeleteRecord(HttpExchange exchange, User currentUser) throws Exception {
        if (!hasRole(exchange, UserRole.ACADEMIC_ADMIN)) {
            throw new ForbiddenException("仅管理员可删除学籍");
        }
        Map<String, String> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType());
        String studentId = body.get("studentId");
        if (studentId == null || studentId.isEmpty()) {
            throw new BadRequestException("请求体中必须包含 'studentId'");
        }
        studentServiceImpl.deleteStudent(studentId, currentUser);
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "message", "学籍删除成功"));
    }

    private void handleSearchRecords(HttpExchange exchange, User currentUser) throws Exception {
        // 从请求体中解析出查询条件对象
        StudentQueryCriteria criteria = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                StudentQueryCriteria.class
        );

        // 调用 Service 层的新方法
        List<Student> results = studentServiceImpl.searchStudent(criteria, currentUser);

        // 将查询结果以JSON格式返回给客户端
        sendJsonResponse(exchange, 200, Map.of("status", "ok", "records", results));
    }
}
