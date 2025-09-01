package server;

import DAO.ConnectToDatabase;
import com.google.gson.Gson;
import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

public class HttpServer {

    private static final int PORT = 8443;
    private static final String KEYSTORE_PATH = "D:/Learning/Java/IDEA/cloud_campus/src/main/java/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password"; // 测试用，请改为安全密码

    private final ConnectToDatabase dao;
    private final Gson gson = new Gson();

    public  HttpServer(ConnectToDatabase dao) {
        this.dao = dao;
    }

    public void start() throws Exception {
        // 1. 准备 SSLContext
        SSLContext sslContext = createSSLContext();

        // 2. 创建 HttpsServer
        HttpsServer server = HttpsServer.create(new InetSocketAddress(PORT), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                try {
                    // 初始化 SSL 参数（使用默认）
                    SSLContext c = getSSLContext();
                    SSLEngine engine = c.createSSLEngine();
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    defaultSSLParameters.setNeedClientAuth(false);
                    defaultSSLParameters.setCipherSuites(engine.getEnabledCipherSuites());
                    defaultSSLParameters.setProtocols(engine.getEnabledProtocols());
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception ex) {
                    System.err.println("Failed to configure HTTPS parameters: " + ex.getMessage());
                }
            }
        });

        // 3. 注册上下文（路由）
        // GET /user?id=123
        server.createContext("/user", new UserHandler());

        // 4. 线程池
        server.setExecutor(Executors.newFixedThreadPool(10));

        // 5. 启动
        server.start();
        System.out.println("HTTPS server started on port " + PORT);
    }

    // SSLContext 从 keystore 构建
    private SSLContext createSSLContext()
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException, KeyManagementException {
        char[] pass = HttpServer.KEYSTORE_PASSWORD.toCharArray();

        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(HttpServer.KEYSTORE_PATH)) {
            ks.load(fis, pass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pass);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    // Handler: 解析查询参数，调用 DAO，返回 JSON
    private class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只允许 GET
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, gson.toJson(new SimpleResp("error", "Only GET allowed")));
                return;
            }

            String query = exchange.getRequestURI().getQuery(); // e.g. "id=1"
            Integer id = parseIdFromQuery(query);

            if (id == null) {
                sendJson(exchange, 400, gson.toJson(new SimpleResp("error", "missing or invalid id")));
                return;
            }

            try {
                // 调用数据库（注意：这里在工作线程中）
                common.User user = dao.getUserById(id);
                if (user == null) {
                    sendJson(exchange, 404, gson.toJson(new SimpleResp("not_found", null)));
                } else {
                    sendJson(exchange, 200, gson.toJson(new SuccessUserResp("ok", user)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, gson.toJson(new SimpleResp("error", "server error")));
            }
        }

        private Integer parseIdFromQuery(String q) {
            if (q == null) return null;
            for (String part : q.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && "id".equalsIgnoreCase(kv[0])) {
                    try {
                        return Integer.parseInt(kv[1]);
                    } catch (NumberFormatException ignored) {}
                }
            }
            return null;
        }

        private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
            byte[] bytes = json.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // 简单的响应类（用于 Gson 序列化）
    private static class SimpleResp {
        final String status;
        final String message;
        SimpleResp(String s, String m) { status = s; message = m; }
    }

    private static class SuccessUserResp {
        final String status;
        final common.User user;
        SuccessUserResp(String s, common.User u) { status = s; user = u; }
    }

    // main 启动示例（在 GUI 的 startServer 里也可以调用）
    public static void main(String[] args) throws Exception {
        ConnectToDatabase dao = new ConnectToDatabase();
        HttpServer server = new HttpServer(dao);
        server.start();
    }
}

