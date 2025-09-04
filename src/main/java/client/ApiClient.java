package client;

import com.google.gson.Gson;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/*请求格式统一如下：
* GET https://localhost:12345/<表名>/id=<用户id>
* */

public class ApiClient {

    private static final String TARGET_URL = "https://localhost:12349/user";
    private static final HttpClient secureHttpClient = createSecureHttpClient();

    /**
     * 根据用户 ID 获取用户信息 (GET请求)。
     *
     * @param userId 要查询的用户 ID
     * @return 服务器返回的包含用户信息的 JSON 字符串
     * @throws IOException          如果发生 I/O 错误
     * @throws InterruptedException 如果操作被中断
     */
    public static String getUserById(int userId) throws IOException, InterruptedException {
        // 1. 构建带有查询参数的 URL
        String urlWithQuery = TARGET_URL + "?id=" + userId;

        // 2. 构建 HttpRequest，方法为 GET()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery)) // 使用带有参数的 URL
                .timeout(Duration.ofSeconds(10))
                .GET() // GET 请求没有请求体
                .build();

        // 3. 发送请求并获取响应 (这部分和 postUser 一样)
        HttpResponse<String> response = secureHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response Status Code: " + response.statusCode());
        return response.body();
    }

    public static String postUser(String name, String token) throws IOException, InterruptedException {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("name", name);
        requestData.put("token", token);
        String jsonBody = new Gson().toJson(requestData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TARGET_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = secureHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response Status Code: " + response.statusCode());
        return response.body();
    }

    /**
     * 更新一个已存在的用户信息 (PUT请求)。
     *
     * @param userId   要更新的用户的 ID
     * @param newName  新的用户名
     * @param newToken 新的 token
     * @return 服务器返回的响应体
     * @throws IOException          如果发生 I/O 错误
     * @throws InterruptedException 如果操作被中断
     */
    public static String updateUser(int userId, String newName, String newToken) throws IOException, InterruptedException {
        // 1. 构建请求体 JSON，非常重要：必须包含 id
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("id", userId); // PUT 请求需要指定要更新的实体ID
        requestData.put("name", newName);
        requestData.put("token", newToken);
        String jsonBody = new Gson().toJson(requestData);

        // 2. 构建 HttpRequest，方法为 PUT()
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TARGET_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                // 使用 PUT 方法并附上请求体
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 3. 发送请求并获取响应
        HttpResponse<String> response = secureHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response Status Code: " + response.statusCode());
        return response.body();
    }

    /**
     * 创建一个安全的 HttpClient，它加载自定义的 TrustStore 来验证服务器证书。
     *
     * @return 配置好的安全 HttpClient
     */
    private static HttpClient createSecureHttpClient() {
        try {
            // 1. 加载我们自己的 TrustStore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            // 确保证书文件路径正确，这里假设它在项目根目录
            try (FileInputStream fis = new FileInputStream("src/main/java/client_truststore.jks")) {
                // 加载时需要提供在第2步中设置的密码
                trustStore.load(fis, "clientpassword".toCharArray());
            }

            // 2. 使用 TrustStore 初始化 TrustManagerFactory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // 3. 使用 TrustManagerFactory 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // 第一个参数是 KeyManagers (用于客户端证书，这里不需要)，第二个是 TrustManagers
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

            // 4. 构建 HttpClient 并应用我们自定义的、安全的 SSLContext
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create secure HttpClient", e);
        }
    }

    // (请将这个 main 方法替换掉您 ApiClientSecure 类中旧的 main 方法)
    public static void main(String[] args) {
        // 确保你的 ServerController 正在运行!
        // 确保 client_truststore.jks 文件在正确的位置!
        try {
            // !!! 重要：请在这里修改为您数据库中一个实际存在的用户 ID !!!
            int userIdToTest = 2;

            // 1. 首先，获取这个 ID 的当前信息
            System.out.println("--- 1. Fetching user with ID: " + userIdToTest + " ---");
            String initialUserInfo = getUserById(userIdToTest);
            System.out.println("Initial User Info: " + initialUserInfo);
            System.out.println(); // 打印一个空行

            // 2. 然后，更新这个用户的信息
            String newName = "Eason" + System.currentTimeMillis();
            String newToken = "02d739de836c9a0d6" + System.currentTimeMillis();
            System.out.println("--- 2. Updating user with ID: " + userIdToTest + " to new name: " + newName + " ---");
            String updateResponse = updateUser(userIdToTest, newName, newToken);
            System.out.println("Update Response: " + updateResponse);
            System.out.println(); // 打印一个空行

            // 3. 最后，再次获取用户信息，验证是否已更新
            System.out.println("--- 3. Fetching user with ID: " + userIdToTest + " again to verify update ---");
            String updatedUserInfo = getUserById(userIdToTest);
            System.out.println("Updated User Info: " + updatedUserInfo);

        } catch (Exception e) {
            System.err.println("An error occurred during the test workflow:");
            e.printStackTrace();
        }
    }
}