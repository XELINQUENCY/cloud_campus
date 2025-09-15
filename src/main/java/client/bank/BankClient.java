package client.bank;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.bank.BankAccount;
import entity.bank.Transaction;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 银行模块的专用网络客户端。
 * 实现了 IBankClientSrv 接口，负责将业务请求转换为对服务器的HTTPS API调用。
 */
public class BankClient implements IBankClientSrv {

    private final ApiClient apiClient;
    private String currentUserId; // 用于在客户端会话期间保存当前登录的用户ID

    public BankClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public boolean login(String userId, String password) throws ApiException {
        // 复用通用的登录接口 /api/auth/login
        LoginRequest loginRequest = new LoginRequest(userId, password, false); // 银行用户不是管理员
        HttpRequest request = apiClient.newRequestBuilder("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(loginRequest)))
                .build();

        // 发送请求并获取包含Token和User信息的响应
        LoginResponse response = apiClient.sendRequest(request, LoginResponse.class);

        if (response != null && response.getToken() != null && response.getUser() != null) {
            apiClient.setAuthToken(response.getToken()); // 在核心客户端中设置全局Token
            this.currentUserId = response.getUser().getId(); // 保存当前用户ID
            return true;
        }
        // 如果失败，sendRequest会抛出ApiException，这里无需额外处理
        return false;
    }

    @Override
    public boolean register(String userId, String password) throws ApiException {
        Map<String, String> body = Map.of("userId", userId, "password", password);
        HttpRequest request = apiClient.newRequestBuilder("/bank/register")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        // 对于返回 success:true/false 的简单响应
        Map<String, Boolean> response = apiClient.sendRequest(request, new TypeToken<Map<String, Boolean>>() {}.getType());
        return response.getOrDefault("success", false);
    }

    @Override
    public BankAccount createAccount() throws ApiException {
        // userId 会从服务器端的Token中解析，所以请求体为空
        HttpRequest request = apiClient.newRequestBuilder("/bank/accounts")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        return apiClient.sendRequest(request, BankAccount.class);
    }

    @Override
    public boolean deposit(String accountId, BigDecimal amount) throws ApiException {
        Map<String, String> body = Map.of(
                "accountId", accountId,
                "amount", amount.toString() // 将BigDecimal转为字符串传输
        );
        HttpRequest request = apiClient.newRequestBuilder("/bank/deposit")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, Boolean> response = apiClient.sendRequest(request, new TypeToken<Map<String, Boolean>>() {}.getType());
        return response.getOrDefault("success", false);
    }

    @Override
    public boolean withdraw(String accountId, BigDecimal amount, String password) throws ApiException {
        Map<String, String> body = Map.of(
                "accountId", accountId,
                "amount", amount.toString(),
                "password", password
        );
        HttpRequest request = apiClient.newRequestBuilder("/bank/withdraw")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, Boolean> response = apiClient.sendRequest(request, new TypeToken<Map<String, Boolean>>() {}.getType());
        return response.getOrDefault("success", false);
    }

    @Override
    public boolean transfer(String fromAccountId, String toAccountId, BigDecimal amount, String password) throws ApiException {
        Map<String, String> body = Map.of(
                "fromAccountId", fromAccountId,
                "toAccountId", toAccountId,
                "amount", amount.toString(),
                "password", password
        );
        HttpRequest request = apiClient.newRequestBuilder("/bank/transfer")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, Boolean> response = apiClient.sendRequest(request, new TypeToken<Map<String, Boolean>>() {}.getType());
        return response.getOrDefault("success", false);
    }

    @Override
    public List<Transaction> getTransactions(String accountId, LocalDateTime start, LocalDateTime end) throws ApiException {
        // 注意：时间范围查询暂未在服务端实现，这里先获取全部
        HttpRequest request = apiClient.newRequestBuilder("/bank/transactions/" + accountId).GET().build();
        Type listType = new TypeToken<List<Transaction>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<BankAccount> getUserAccounts(String userId) throws ApiException {
        // userId 从Token中获取，所以路径中不需要再传
        HttpRequest request = apiClient.newRequestBuilder("/bank/accounts/user/" + userId).GET().build();
        Type listType = new TypeToken<List<BankAccount>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    // --- 为了兼容旧GUI代码的辅助方法 ---

    @Override
    public BankAccount createAccount(String userId) throws ApiException {
        // 这个方法在新的API模式下不再需要，因为服务器会从Token中识别用户。
        // 调用无参版本即可。
        return createAccount();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}
