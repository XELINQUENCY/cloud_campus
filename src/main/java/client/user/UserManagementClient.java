package client.user;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import enums.UserRole;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IUserManagementClient接口的实现。
 * 使用统一的ApiClient发送HTTPS请求到后端API。
 */
public class UserManagementClient implements IUserManagementClient {

    private final ApiClient apiClient;

    public UserManagementClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
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

    @Override
    public User register(String username, String password, String email, UserRole role, String gender, Integer age) throws ApiException {
        Map<String, Object> requestBody = Map.of(
                "name", username,
                "password", password,
                "email", email,
                "userRoles", Set.of(role, UserRole.READER, UserRole.STORE_CUSTOMER),
                "gender", gender,
                "age", age
        );

        HttpRequest request = apiClient.newRequestBuilder("/users/register")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(requestBody)))
                .build();

        return apiClient.sendRequest(request, User.class);
    }

    @Override
    public User getMyProfile() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/users/profile").GET().build();
        return apiClient.sendRequest(request, User.class);
    }

    @Override
    public User updateMyProfile(User userToUpdate) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/users/profile")
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(userToUpdate)))
                .build();
        return apiClient.sendRequest(request, User.class);
    }

    @Override
    public List<User> getAllUsers() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/users/admin/all").GET().build();
        Type userListType = new TypeToken<List<User>>() {}.getType();
        return apiClient.sendRequest(request, userListType);
    }

    @Override
    public void deleteUser(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/users/admin/" + userId)
                .DELETE()
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void toggleUserStatus(String userId, boolean newStatus) throws ApiException {
        Map<String, Boolean> requestBody = Map.of("active", newStatus);
        HttpRequest request = apiClient.newRequestBuilder("/users/admin/status/" + userId)
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(requestBody)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }

    @Override
    public void changeUserRoles(String userId, Set<UserRole> newRoles) throws ApiException {
        Map<String, Set<UserRole>> requestBody = Map.of("roles", newRoles);
        HttpRequest request = apiClient.newRequestBuilder("/users/admin/roles/" + userId)
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(requestBody)))
                .build();
        apiClient.sendRequest(request, Void.class);
    }
}
