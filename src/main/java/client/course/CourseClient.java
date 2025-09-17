package client.course;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import view.CourseOfferingVO;
import view.StudentCourseDetailVO;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 选课模块的专用网络客户端实现。
 */
public class CourseClient implements ICourseClientSrv {

    private final ApiClient apiClient;

    public CourseClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<CourseOfferingVO> browseCourses(String semester, String courseName, String teacherName, String department) throws ApiException {
        Map<String, String> params = new HashMap<>();
        params.put("semester", semester);
        if (courseName != null && !courseName.isEmpty()) params.put("courseName", courseName);
        if (teacherName != null && !teacherName.isEmpty()) params.put("teacherName", teacherName);
        if (department != null && !department.isEmpty()) params.put("department", department);

        String query = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = apiClient.newRequestBuilder("/course/browse?" + query).GET().build();
        Type listType = new TypeToken<List<CourseOfferingVO>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<StudentCourseDetailVO> getMyCourses(String semester) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/course/my-courses?semester=" + semester).GET().build();
        Type listType = new TypeToken<List<StudentCourseDetailVO>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public String selectCourse(int teachingId, String semester) throws ApiException {
        Map<String, Object> body = Map.of("teachingId", teachingId, "semester", semester);
        HttpRequest request = apiClient.newRequestBuilder("/course/select")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String dropCourse(int teachingId) throws ApiException {
        Map<String, Object> body = Map.of("teachingId", teachingId);
        HttpRequest request = apiClient.newRequestBuilder("/course/drop")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String addCourseForStudent(String studentId, int teachingId, String semester) throws ApiException {
        Map<String, Object> body = Map.of("studentId", studentId, "teachingId", teachingId, "semester", semester);
        HttpRequest request = apiClient.newRequestBuilder("/course/admin/add-for-student")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String removeCourseForStudent(String studentId, int teachingId) throws ApiException {
        Map<String, Object> body = Map.of("studentId", studentId, "teachingId", teachingId);
        HttpRequest request = apiClient.newRequestBuilder("/course/admin/remove-for-student")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String updateCourseCapacity(int teachingId, int newCapacity) throws ApiException {
        Map<String, Object> body = Map.of("teachingId", teachingId, "newCapacity", newCapacity);
        HttpRequest request = apiClient.newRequestBuilder("/course/admin/capacity")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }
}
