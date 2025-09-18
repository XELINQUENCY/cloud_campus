package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import entity.User;
import enums.UserRole;
import service.course.AdminCourseService;
import service.course.CourseBrowseService;
import service.course.StudentCourseService;
import service.course.exception.CourseSelectionException; // 假设您将自定义异常放在此包下

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 负责处理所有选课相关的API请求 (/api/course/**)
 */
public class CourseHandler extends BaseHandler {

    private final CourseBrowseService courseBrowseService;
    private final StudentCourseService studentCourseService;
    private final AdminCourseService adminCourseService;

    public CourseHandler(CourseBrowseService courseBrowseService,
                         StudentCourseService studentCourseService,
                         AdminCourseService adminCourseService,
                         Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.courseBrowseService = courseBrowseService;
        this.studentCourseService = studentCourseService;
        this.adminCourseService = adminCourseService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        User authenticatedUser = (User) exchange.getAttribute("user");

        try {
            // --- 公共接口 (无需登录或特定角色) ---
            if (path.equals("/api/course/browse") && "GET".equalsIgnoreCase(method)) {
                handleBrowseCourses(exchange);
            }

            // --- 学生接口 (需要学生角色) ---
            else if (path.equals("/api/course/my-courses") && "GET".equalsIgnoreCase(method)) {
                handleGetMyCourses(exchange, authenticatedUser);
            } else if (path.equals("/api/course/select") && "POST".equalsIgnoreCase(method)) {
                handleSelectCourse(exchange, authenticatedUser);
            } else if (path.equals("/api/course/drop") && "POST".equalsIgnoreCase(method)) {
                handleDropCourse(exchange, authenticatedUser);
            }

            // --- 管理员接口 (需要教务管理员角色) ---
            else if (path.equals("/api/course/admin/add-for-student") && "POST".equalsIgnoreCase(method)) {
                handleAddCourseForStudent(exchange, authenticatedUser);
            } else if (path.equals("/api/course/admin/remove-for-student") && "POST".equalsIgnoreCase(method)) {
                handleRemoveCourseForStudent(exchange, authenticatedUser);
            } else if (path.equals("/api/course/admin/capacity") && "PUT".equalsIgnoreCase(method)) {
                handleUpdateCapacity(exchange, authenticatedUser);
            }

            else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的选课API路径: " + path));
            }
        } catch (JsonSyntaxException e) {
            logger.log("选课模块请求JSON格式错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON格式"));
        } catch (CourseSelectionException e) {
            // 精确捕获来自Service层的业务异常
            logger.log("选课业务逻辑错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.log("选课模块发生未知错误: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误"));
        }
    }

    private void handleBrowseCourses(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String semester = params.get("semester");
        if (semester == null || semester.isEmpty()) {
            throw new IllegalArgumentException("必须提供 'semester' 参数");
        }
        var courses = courseBrowseService.findAvailableCourses(
                semester,
                params.get("courseName"),
                params.get("teacherName"),
                params.get("department")
        );
        sendJsonResponse(exchange, 200, courses);
    }

    private void handleGetMyCourses(HttpExchange exchange, User user) throws IOException {
        if (user == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String semester = params.get("semester");
        if (semester == null || semester.isEmpty()) {
            throw new IllegalArgumentException("必须提供 'semester' 参数");
        }
        var myCourses = studentCourseService.findMyCourses(user.getId(), semester);
        sendJsonResponse(exchange, 200, myCourses);
    }

    private void handleSelectCourse(HttpExchange exchange, User user) throws IOException {
        if (user == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
        int teachingId = ((Double) body.get("teachingId")).intValue();
        String semester = (String) body.get("semester");
        studentCourseService.selectCourse(user.getId(), teachingId, semester);
        sendJsonResponse(exchange, 200, Map.of("message", "选课成功"));
    }

    private void handleDropCourse(HttpExchange exchange, User user) throws IOException {
        if (user == null) {
            sendJsonResponse(exchange, 401, Map.of("error", "用户未认证"));
            return;
        }
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
        int teachingId = ((Double) body.get("teachingId")).intValue();
        studentCourseService.dropCourse(user.getId(), teachingId);
        sendJsonResponse(exchange, 200, Map.of("message", "退课成功"));
    }

    private void handleAddCourseForStudent(HttpExchange exchange, User user) throws IOException {
        if (!hasRole(exchange, UserRole.ACADEMIC_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅教务管理员可操作"));
            return;
        }
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
        String studentId = (String) body.get("studentId");
        int teachingId = ((Double) body.get("teachingId")).intValue();
        String semester = (String) body.get("semester");
        adminCourseService.addCourseForStudent(studentId, teachingId, semester);
        sendJsonResponse(exchange, 200, Map.of("message", "为学生添加课程成功"));
    }

    private void handleRemoveCourseForStudent(HttpExchange exchange, User user) throws IOException {
        if (!hasRole(exchange, UserRole.ACADEMIC_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅教务管理员可操作"));
            return;
        }
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
        String studentId = (String) body.get("studentId");
        int teachingId = ((Double) body.get("teachingId")).intValue();
        adminCourseService.removeCourseForStudent(studentId, teachingId);
        sendJsonResponse(exchange, 200, Map.of("message", "为学生移除课程成功"));
    }

    private void handleUpdateCapacity(HttpExchange exchange, User user) throws IOException {
        if (!hasRole(exchange, UserRole.ACADEMIC_ADMIN)) {
            sendJsonResponse(exchange, 403, Map.of("error", "权限不足，仅教务管理员可操作"));
            return;
        }
        Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
        int teachingId = ((Double) body.get("teachingId")).intValue();
        int newCapacity = ((Double) body.get("newCapacity")).intValue();
        adminCourseService.updateCourseCapacity(teachingId, newCapacity);
        sendJsonResponse(exchange, 200, Map.of("message", "课程容量更新成功"));
    }
}