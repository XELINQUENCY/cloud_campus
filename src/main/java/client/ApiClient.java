package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import controller.LocalDateTimeTypeAdapter;
import controller.LocalDateTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 核心API客户端
 * 这是一个通用的HTTPS请求引擎，采用单例模式。
 * 它不实现任何业务接口，只负责处理底层的HTTP通信和认证。
 */
public class ApiClient {

    private static final String BASE_URL = "https://localhost:32777/api";
    private final HttpClient secureHttpClient;
    /**
     * -- GETTER --
     *  获取Gson实例，用于在模块客户端中处理JSON序列化。
     *
     * @return Gson实例
     */
    @Getter
    private final Gson gson;
    // --- 认证管理 ---
    @Setter
    private String authToken; // 用于保存登录后获取的认证令牌

    public ApiClient() {
        this.secureHttpClient = createSecureHttpClient();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .create();
    }

    public void clearAuthToken() {
        this.authToken = null;
    }

    // --- 通用请求方法 ---

    /**
     * 发送HTTP请求并处理响应的通用方法。
     * @param request      构建好的HttpRequest
     * @param responseType 期望的响应体反序列化后的类型
     * @param <T>          响应类型的泛型
     * @return 反序列化后的响应对象
     * @throws ApiException API调用失败时抛出
     */
    public <T> T sendRequest(HttpRequest request, Type responseType) throws ApiException {
        try {
            HttpResponse<String> response = secureHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (responseType.equals(Void.class) || response.body() == null || response.body().isEmpty()) {
                    return null;
                }
                return gson.fromJson(response.body(), responseType);
            } else {
                String errorMessage = "未知错误，状态码: " + response.statusCode();
                if (response.body() != null && !response.body().isEmpty()) {
                    try {
                        Map<String, String> errorMap = gson.fromJson(response.body(), new TypeToken<Map<String, String>>() {}.getType());
                        if (errorMap != null && errorMap.containsKey("error")) {
                            errorMessage = errorMap.get("error");
                        } else {
                            errorMessage = response.body();
                        }
                    } catch (Exception e) {
                        errorMessage = response.body();
                    }
                }
                throw new ApiException(errorMessage);
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiException("网络连接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建一个附加了认证头和通用设置的 HttpRequest.Builder。
     * @param path API的相对路径
     * @return HttpRequest.Builder 实例
     */
    public HttpRequest.Builder newRequestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=UTF-8");

        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }


    /**
     * 创建一个配置了自定义信任证书的、安全的HttpClient。
     * @return 配置好的 HttpClient 实例
     */
    private static HttpClient createSecureHttpClient() {
        try {
            // 1. 定义证书在 classpath 中的资源路径
            String trustStoreResourcePath = "/client_truststore.jks";
            char[] password = "clientpassword".toCharArray();

            KeyStore trustStore = KeyStore.getInstance("JKS");

            // 2. 使用 getResourceAsStream 从 JAR 包内部或 classpath 加载资源
            //    这是核心改动。我们用 ApiClient.class，也可以用任何一个在此代码上下文中的类
            try (InputStream is = ApiClient.class.getResourceAsStream(trustStoreResourcePath)) {
                if (is == null) {
                    // 如果资源找不到，立即抛出清晰的异常
                    throw new RuntimeException("无法在 Classpath 中找到资源: " + trustStoreResourcePath);
                }
                trustStore.load(is, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

            System.out.println("客户端成功从类路径加载信任证书！");

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

        } catch (Exception e) {
            // 可以保留外层的catch块，但内部的错误会更具体
            throw new RuntimeException("创建安全HttpClient失败!", e);
        }
    }

	public Gson getGson() {
		return gson;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public static String getBaseUrl() {
		return BASE_URL;
	}

	public HttpClient getSecureHttpClient() {
		return secureHttpClient;
	}
}