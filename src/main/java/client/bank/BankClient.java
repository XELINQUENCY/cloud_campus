package client.bank;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.bank.BankAccount;
import entity.bank.Transaction;
import lombok.Getter;

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
    @Getter
    private String currentUserId; // 用于在客户端会话期间保存当前登录的用户ID

    public BankClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public boolean login(String userId, String password) throws ApiException {
        LoginRequest loginRequest = new LoginRequest(userId, password, false);

        HttpRequest request = apiClient.newRequestBuilder("/bank/auth/login") // <--- 注意路径变化
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(loginRequest)))
                .build();

        System.out.println(request);

        LoginResponse response = apiClient.sendRequest(request, LoginResponse.class);

        if (response != null && response.getToken() != null && response.getUser() != null) {
            // 这里的 apiClient 是 bankApiClient，它会独立保存银行的 token
            apiClient.setAuthToken(response.getToken());
            this.currentUserId = response.getUser().getId();
            return true;
        }
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
                "amount", amount.toString()
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

}
