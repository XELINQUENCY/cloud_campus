package client;

import com.google.gson.Gson; // 您服务器端已在使用的 JSON 库
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    // 要请求的目标 URL
    private static final String TARGET_URL = "https://localhost:12345/user";

    // 创建一个可以重用的 HttpClient 实例
    private static final HttpClient insecureHttpClient = createInsecureHttpClient();

    /**
     * 向服务器发送 POST 请求来创建用户，功能等同于 curl 命令。
     *
     * @param name  要创建的用户名
     * @param token 用户的 token
     * @return 服务器返回的响应体 (String)
     * @throws IOException          如果发生 I/O 错误
     * @throws InterruptedException 如果操作被中断
     */
    public static String postUser(String name, String token) throws IOException, InterruptedException {
        // 1. 使用 Map 和 Gson 构建 JSON 请求体，这比手动拼接字符串更健壮
        Map<String, String> requestData = new HashMap<>();
        requestData.put("name", name);
        requestData.put("token", token);
        String jsonBody = new Gson().toJson(requestData);

        // 2. 构建 HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TARGET_URL))
                .timeout(Duration.ofSeconds(10)) // 设置10秒超时
                .header("Content-Type", "application/json") // 设置请求头
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)) // 设置方法为 POST 并附上请求体
                .build();

        // 3. 发送请求并获取响应
        // 使用预先创建好的、忽略证书验证的 HttpClient
        HttpResponse<String> response = insecureHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. 打印状态码并返回响应体
        System.out.println("Response Status Code: " + response.statusCode());
        return response.body();
    }

    /**
     * 创建一个 HttpClient，它会忽略所有 SSL 证书验证。
     * 这模拟了 curl 的 -k 或 --insecure 选项。
     *
     * @return 配置好的 HttpClient
     */
    private static HttpClient createInsecureHttpClient() {
        try {
            // 创建一个信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            // 使用该 TrustManager 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 构建 HttpClient 并应用自定义的 SSLContext
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // 在实际应用中，这里应该有更完善的异常处理
            throw new RuntimeException("Failed to create insecure HttpClient", e);
        }
    }

    // === 主方法：用于演示如何调用 ===
    public static void main(String[] args) {
        try {
            String name = "javaClientUser";
            String token = "a1b2c3d4e5f6_from_java";

            System.out.println("Sending POST request for user: " + name);

            // 调用封装好的函数
            String responseBody = postUser(name, token);

            System.out.println("Server Response: " + responseBody);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending request: " + e.getMessage());
            // 如果看到 "Connection refused" 错误，请确保您的 Java HTTPS 服务器正在运行。
            // 如果看到其他 SSL 相关的错误，请检查 createInsecureHttpClient() 方法的实现。
            e.printStackTrace();
        }
    }
}