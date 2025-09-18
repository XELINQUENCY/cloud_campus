package client.schoolroll;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.schoolroll.StudentDetailDTO;
import entity.StudentQueryCriteria;
import entity.User;
import entity.schoolroll.Student;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

/**
 * 学籍管理模块的专用网络客户端。
 * 实现了所有与学籍管理相关的客户端服务接口，
 * 内部使用核心ApiClient来发送HTTPS请求。
 */
public class SchoolRollClient {

    private final ApiClient apiClient;

    public SchoolRollClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 根据学号查询学籍信息。
     * 服务端会根据Token验证用户权限。
     * @param studentId 要查询的学号
     * @return 学生学籍信息
     * @throws ApiException 如果API调用失败
     */
    public Student getStudent(String studentId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/" + studentId)
                .GET()
                .build();
        // 响应体结构是 {"status":"ok", "record":{...}}，我们需要从中提取 record
        Type responseType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = apiClient.sendRequest(request, responseType);
        // 使用 Gson 将 Map 转换为 Student 对象
        return apiClient.getGson().fromJson(apiClient.getGson().toJson(response.get("record")), Student.class);
    }

    public String getStudentId() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/query")
                .GET()
                .build();
        // 响应体结构是 {"status":"ok", "record":{...}}，我们需要从中提取 record
        Type responseType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = apiClient.sendRequest(request, responseType);
        // 使用 Gson 将 Map 转换为 String 对象
        return apiClient.getGson().fromJson(apiClient.getGson().toJson(response.get("studentId")), String.class);
    }

    /**
     * 创建一条新的学籍记录 (仅管理员)。
     * @param student 要创建的学籍对象
     * @return 包含成功信息的Map
     * @throws ApiException 如果API调用失败
     */
    public Map<String, String> createStudent(Student student) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/create")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(student)))
                .build();
        Type responseType = new TypeToken<Map<String, String>>() {}.getType();
        return apiClient.sendRequest(request, responseType);
    }

    /**
     * 更新一条已有的学籍记录。
     * 服务端会根据Token和请求体中的学号验证用户权限。
     * @param student 要更新的学籍对象
     * @return 包含成功信息的Map
     * @throws ApiException 如果API调用失败
     */
    public Map<String, String> updateStudent(Student student) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/update")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(student)))
                .build();
        Type responseType = new TypeToken<Map<String, String>>() {}.getType();
        return apiClient.sendRequest(request, responseType);
    }

    /**
     * 根据学号删除一条学籍记录 (仅管理员)。
     * @param studentId 要删除的学号
     * @return 包含成功信息的Map
     * @throws ApiException 如果API调用失败
     */
    public Map<String, String> deleteStudent(String studentId) throws ApiException {
        Map<String, String> body = Map.of("studentId", studentId);
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/delete")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Type responseType = new TypeToken<Map<String, String>>() {}.getType();
        return apiClient.sendRequest(request, responseType);
    }

    /**
     * 根据复杂的查询条件搜索学籍记录 (仅管理员)。
     * @param criteria 包含所有查询条件的封装对象
     * @return 符合条件的学生学籍列表
     * @throws ApiException 如果API调用失败
     */
    public List<Student> searchStudents(StudentQueryCriteria criteria) throws ApiException {
        // 将查询条件对象序列化为JSON字符串
        String requestBody = apiClient.getGson().toJson(criteria);

        // 构建一个POST请求，并将JSON作为请求体
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/search")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 响应体结构是 {"status":"ok", "records":[...]}，我们需要从中提取 records
        Type responseType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = apiClient.sendRequest(request, responseType);

        // 定义一个 List<Student> 的类型
        Type studentListType = new TypeToken<List<Student>>() {}.getType();

        // 使用 Gson 将 records 部分 (它是一个List<Map>) 转换为 List<Student>
        return apiClient.getGson().fromJson(apiClient.getGson().toJson(response.get("records")), studentListType);
    }

    // [新增] 根据学号查询单个学生的详细学籍信息 (返回 DTO)
    public StudentDetailDTO getStudentDetails(String studentId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/details/" + studentId)
                .GET()
                .build();
        Type responseType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = apiClient.sendRequest(request, responseType);
        return apiClient.getGson().fromJson(apiClient.getGson().toJson(response.get("record")), StudentDetailDTO.class);
    }

    // [新增] 根据复杂条件搜索学生详细学籍信息列表 (返回 DTO 列表)
    public List<StudentDetailDTO> searchStudentDetails(StudentQueryCriteria criteria) throws ApiException {
        String requestBody = apiClient.getGson().toJson(criteria);
        HttpRequest request = apiClient.newRequestBuilder("/schoolroll/records/details/search")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        Type responseType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = apiClient.sendRequest(request, responseType);

        Type dtoListType = new TypeToken<List<StudentDetailDTO>>() {}.getType();
        return apiClient.getGson().fromJson(apiClient.getGson().toJson(response.get("records")), dtoListType);
    }

    public User login(String username, String password, boolean isAdmin) throws ApiException {
        LoginRequest loginRequest = new LoginRequest(username, password, isAdmin);
        HttpRequest request = apiClient.newRequestBuilder("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(loginRequest)))
                .build();

        LoginResponse response = apiClient.sendRequest(request, LoginResponse.class);

        if (response != null && response.getToken() != null) {
            apiClient.setAuthToken(response.getToken()); // 登录成功后，在核心客户端中设置令牌
            return response.getUser();
        }
        throw new ApiException("登录失败，服务器未返回有效数据。");
    }
}