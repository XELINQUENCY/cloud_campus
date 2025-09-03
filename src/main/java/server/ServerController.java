package server;

import DAO.ConnectToDatabase;
import com.google.gson.JsonSyntaxException;
import common.User;

import com.google.gson.Gson;
import com.sun.net.httpserver.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLParameters;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerController {

    // HttpsServer 实例
    private HttpsServer httpsServer = null;
    private ExecutorService threadPool = null;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private JFrame frame;
    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;

    private final ConnectToDatabase dao;
    private final Gson gson = new Gson();

    private final int port = 12345;
    private final int poolSize = 10;

    public ServerController() {
        this.dao = new ConnectToDatabase();
    }

    private void createFrame() {
        frame = new JFrame("Server (HTTPS)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setPreferredSize(new Dimension(900, 620));

        JLabel label = new JLabel("这是一个 HTTPS Server!", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(22f));
        content.add(label, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        content.add(scroll, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        startBtn = new JButton("Start (HTTPS)");
        stopBtn = new JButton("Stop");
        stopBtn.setEnabled(false);
        controls.add(startBtn);
        controls.add(stopBtn);
        content.add(controls, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startServer());
        stopBtn.addActionListener(e -> stopServer());

        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
            }
        });
    }

    private void startServer() {
        if (running.get()) {
            appendLog("Server already running.");
            return;
        }

        try {
            // 1. 初始化 SSLContext（从 keystore 加载证书）
            // 配置：替换为你的 keystore 路径与密码
            String KEYSTORE_PATH = "src/main/java/server.jks";
            String KEYSTORE_PASSWORD = "password";
            SSLContext sslContext = createSSLContext(KEYSTORE_PATH, KEYSTORE_PASSWORD);

            // 2. 创建 HttpsServer
            httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);

            // 配置 HTTPS
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext c = getSSLContext();
                        SSLParameters sslParams = c.getDefaultSSLParameters();
                        // 仅允许 TLSv1.2/1.3（依据你的 JVM 支持情况）
                        sslParams.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
                        // 你也可以设置 cipher suites: sslParams.setCipherSuites(...);
                        params.setSSLParameters(sslParams);
                    } catch (Exception ex) {
                        appendLog("Error configuring SSL params: " + ex.getMessage());
                    }
                }
            });

            // 3. 创建 context：GET /user?id=123, POST /user
            httpsServer.createContext("/user", new UserHttpsHandler());
            // 如需更多接口，可新增 context

            threadPool = Executors.newFixedThreadPool(poolSize);
            httpsServer.setExecutor(threadPool);

            httpsServer.start();
            running.set(true);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            appendLog("HTTPS server started on port " + port);

        } catch (Exception e) {
            appendLog("Failed to start HTTPS server: " + e.getMessage());
            cleanupResources();
        }
    }

    private void stopServer() {
        if (!running.get()) return;
        running.set(false);
        appendLog("Stopping server...");
        cleanupResources();
        SwingUtilities.invokeLater(() -> {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
    }

    private void cleanupResources() {
        try {
            if (httpsServer != null) {
                httpsServer.stop(1); // 停止服务（延迟 1 秒）
            }
        } catch (Exception ignored) {}
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        httpsServer = null;
    }

    private void appendLog(String s) {
        String message = "[" + java.time.LocalTime.now().withNano(0) + "] " + s + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * 创建 SSLContext（从 JKS keystore 加载）
     */
    private SSLContext createSSLContext(String keystorePath, String keystorePassword) throws Exception {
        char[] pass = keystorePassword.toCharArray();

        // 加载 keystore (JKS)
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, pass);
        }

        // KeyManager（用于服务器持有私钥）
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, pass);

        // TrustManager（可选择使用同一个 keystore 或默认信任库）
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks); // 这里用同一个 keystore，使客户端若信任该证书即可校验（自签名场景）
        // 生产：应使用由受信 CA 签发的证书或更严谨的 truststore

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    /**
     * Https Handler：支持 GET /user?id=123 和 POST /user (JSON)
     */
    private class UserHttpsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String remote = exchange.getRemoteAddress().toString();
            appendLog("HTTPS " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " from " + remote);

            try {
                String method = exchange.getRequestMethod().toUpperCase();
                switch (method) {
                    case "GET" -> handleGet(exchange);
                    case "POST" -> handlePost(exchange);
                    case "PUT" -> handlePut(exchange);
                    default -> sendJson(exchange, 405, mapOf("status", "error", "message", "method_not_allowed"));
                }
            } catch (Exception e) {
                appendLog("Handler error: " + e.getMessage());
                sendJson(exchange, 500, mapOf("status", "error", "message", "server_error"));
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            Map<String, String> q = parseQuery(rawQuery);
            String idStr = q.get("id");
            if (idStr == null || idStr.isEmpty()) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "missing_id"));
                return;
            }
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "invalid_id"));
                return;
            }
            User u = dao.getUserById(id);
            if (u != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "ok");
                resp.put("user", u); // Gson 会把 POJO 转为 JSON
                sendJson(exchange, 200, resp);
            } else {
                sendJson(exchange, 200, mapOf("status", "not_found"));
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "expected_application_json"));
                return;
            }

            // 读取请求体并用 Gson 解析为 User（需保证 User 有无参构造器和 getter/setter）
            try (InputStream is = exchange.getRequestBody();
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                User incoming = gson.fromJson(br, User.class);//应为缺少id的user
                if (incoming == null) {
                    sendJson(exchange, 400, mapOf("status", "error", "message", "invalid_json"));
                    return;
                }
                //销毁误传入的id参数。id由数据库自动生成
                if (incoming.getId() != null){
                    incoming.setId(null);
                }
                // 这里可以做验证（例如 name/password 非空等）
                // 调用 DAO 创建新用户（dao.createUser 返回新 user 的 id）
                int ok = dao.createUser(incoming);
                if (ok != 0) {
                    sendJson(exchange, 200, mapOf("status", "ok"));
                } else {
                    sendJson(exchange, 500, mapOf("status", "error", "message", "db_error"));
                }

            } catch (JsonSyntaxException jse) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "json_syntax_error"));
            }
        }

        private void handlePut(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "expected_application_json"));
                return;
            }

            // 读取请求体并用 Gson 解析为 User（需保证 User 有无参构造器和 getter/setter）
            try (InputStream is = exchange.getRequestBody();
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                User incoming = gson.fromJson(br, User.class);
                if (incoming == null) {
                    sendJson(exchange, 400, mapOf("status", "error", "message", "invalid_json"));
                    return;
                }
                if (incoming.getId() == null || incoming.getName().isEmpty() || incoming.getToken().isEmpty()) {
                    sendJson(exchange, 400, mapOf("status", "error", "message", "missing_information"));
                }

                // 这里可以做验证（例如 name/password 非空等）
                // 调用 DAO 保存用户（假设 dao.createUser 返回新 user 的 id 或 boolean）
                int ok = dao.updateUser(incoming); // 假设 ConnectToDatabase 有该方法
                if (ok != 0) {
                    sendJson(exchange, 200, mapOf("status", "ok"));
                } else {
                    sendJson(exchange, 500, mapOf("status", "error", "message", "db_error"));
                }

            } catch (JsonSyntaxException jse) {
                sendJson(exchange, 400, mapOf("status", "error", "message", "json_syntax_error"));
            }
        }

        private void sendJson(HttpExchange exchange, int statusCode, Map<String, ?> obj) throws IOException {
            String body = gson.toJson(obj);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            } catch (IOException ignored) {}
        }

        private Map<String, String> parseQuery(String rawQuery) {
            Map<String, String> map = new HashMap<>();
            if (rawQuery == null || rawQuery.isEmpty()) return map;
            String[] pairs = rawQuery.split("&");
            for (String p : pairs) {
                int idx = p.indexOf('=');
                try {
                    if (idx > 0) {
                        String k = java.net.URLDecoder.decode(p.substring(0, idx), "UTF-8");
                        String v = java.net.URLDecoder.decode(p.substring(idx + 1), "UTF-8");
                        map.put(k, v);
                    } else {
                        String k = java.net.URLDecoder.decode(p, "UTF-8");
                        map.put(k, "");
                    }
                } catch (UnsupportedEncodingException ignored) {}
            }
            return map;
        }

        // 快捷构造 Map
        private Map<String, Object> mapOf(String k1, Object v1) {
            Map<String, Object> m = new HashMap<>();
            m.put(k1, v1);
            return m;
        }

        private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
            Map<String, Object> m = new HashMap<>();
            m.put(k1, v1);
            m.put(k2, v2);
            return m;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerController controller = new ServerController();
            controller.createFrame();
        });
    }
}
