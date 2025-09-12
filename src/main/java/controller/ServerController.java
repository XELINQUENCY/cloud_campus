package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.*;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import entity.library.Book;
import service.AuthService;
import service.library.LibraryService;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟校园统一服务器控制器 (重构版)
 * 采用基于HTTPS的API服务器，取代了原有的RMI实现，成为所有模块的唯一入口。
 */
public class ServerController {

    private HttpsServer httpsServer = null;
    private ExecutorService threadPool = null;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private JFrame frame;
    private JTextArea logArea;
    private JButton startBtn, stopBtn;

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    private final int port = 32777;

    // --- Service层引用 (通过依赖注入) ---
    private final LibraryService libraryService;
    private final AuthService authService;

    public ServerController(LibraryService libraryService, AuthService authService) {
        this.libraryService = libraryService;
        this.authService = authService;
    }

    public void createFrame() {
        frame = new JFrame("VCampus Server (HTTPS)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setPreferredSize(new Dimension(900, 620));

        JLabel label = new JLabel("虚拟校园统一服务器", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(22f));
        content.add(label, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        content.add(scroll, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        startBtn = new JButton("启动服务");
        stopBtn = new JButton("停止服务");
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
            appendLog("服务器已在运行中。");
            return;
        }
        try {
            String KEYSTORE_PATH = "src/main/java/server.jks";
            String KEYSTORE_PASSWORD = "password";
            SSLContext sslContext = createSSLContext(KEYSTORE_PATH, KEYSTORE_PASSWORD);

            httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            // 创建统一的API处理器，并添加认证过滤器
            HttpContext context = httpsServer.createContext("/api", new ApiHandler());
            context.getFilters().add(new AuthFilter());

            threadPool = Executors.newFixedThreadPool(10);
            httpsServer.setExecutor(threadPool);
            httpsServer.start();

            running.set(true);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            appendLog("HTTPS 服务器已在端口 " + port + " 启动");

        } catch (Exception e) {
            appendLog("错误: 启动服务器失败 - " + e.getMessage());
            e.printStackTrace();
            cleanupResources();
        }
    }

    private void stopServer() {
        if (!running.compareAndSet(true, false)) return;
        appendLog("正在停止服务器...");
        cleanupResources();
        SwingUtilities.invokeLater(() -> {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
        appendLog("服务器已停止。");
    }

    private void cleanupResources() {
        if (httpsServer != null) {
            httpsServer.stop(1);
            httpsServer = null;
        }
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
            threadPool = null;
        }
    }

    private void appendLog(String s) {
        String message = "[" + java.time.LocalTime.now().withNano(0) + "] " + s + "\n";
        SwingUtilities.invokeLater(() -> logArea.append(message));
    }

    private SSLContext createSSLContext(String keystorePath, String password) throws Exception {
        char[] pass = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, pass);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, pass);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    /**
     * 认证过滤器，用于检查需要认证的API请求
     */
    class AuthFilter extends Filter {
        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // 定义白名单，这些路径不需要认证
            if (path.equals("/api/auth/login") || path.equals("/api/library/books") || path.equals("/api/library/categories")) {
                chain.doFilter(exchange); // 放行
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = authService.validateToken(token);
                    exchange.setAttribute("userId", userId); // 将认证后的用户ID存入请求属性
                    chain.doFilter(exchange); // 认证通过，继续处理
                } catch (Exception e) {
                    sendJsonResponse(exchange, 401, Map.of("error", "认证失败: " + e.getMessage()));
                }
            } else {
                sendJsonResponse(exchange, 401, Map.of("error", "缺少认证令牌"));
            }
        }

        @Override
        public String description() {
            return "Handles API authentication";
        }
    }

    /**
     * 统一的API请求处理器
     */
    class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                appendLog("处理请求: " + exchange.getRequestMethod() + " " + path);

                if (path.startsWith("/api/auth")) {
                    handleAuthRequest(exchange);
                } else if (path.startsWith("/api/library")) {
                    handleLibraryRequest(exchange);
                } else {
                    sendJsonResponse(exchange, 404, Map.of("error", "未知的API路径"));
                }
            } catch (Exception e) {
                appendLog("处理请求时发生严重错误: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误"));
            } finally {
                exchange.close();
            }
        }

        private void handleAuthRequest(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                    LoginRequest req = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), LoginRequest.class);
                    User user = authService.login(req.getUsername(), req.getPassword(), req.isAdmin());
                    String token = authService.generateToken(user);
                    sendJsonResponse(exchange, 200, new LoginResponse(token, user));
                } else {
                    sendJsonResponse(exchange, 404, Map.of("error", "未知的认证API"));
                }
            } catch (Exception e) {
                appendLog("认证失败: " + e.getMessage());
                sendJsonResponse(exchange, 401, Map.of("error", e.getMessage()));
            }
        }

        private void handleLibraryRequest(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String authenticatedUserId = (String) exchange.getAttribute("userId");

            try {
                // --- 公共接口 (无需认证) ---
                if (path.equals("/api/library/books") && "GET".equalsIgnoreCase(method)) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    Integer categoryId = params.containsKey("categoryId") ? Integer.parseInt(params.get("categoryId")) : null;
                    sendJsonResponse(exchange, 200, libraryService.searchBooks(params.get("title"), params.get("author"), params.get("publisher"), categoryId));
                } else if (path.equals("/api/library/categories") && "GET".equalsIgnoreCase(method)) {
                    sendJsonResponse(exchange, 200, libraryService.getAllCategories());
                }
                // --- 用户接口 (需要认证) ---
                else if (path.matches("/api/library/user/[^/]+/profile") && "GET".equalsIgnoreCase(method)) {
                    sendJsonResponse(exchange, 200, libraryService.refreshLibraryProfile(authenticatedUserId));
                } else if (path.matches("/api/library/user/[^/]+/borrows") && "GET".equalsIgnoreCase(method)) {
                    sendJsonResponse(exchange, 200, libraryService.getMyBorrowRecords(authenticatedUserId));
                } else if (path.matches("/api/library/user/[^/]+/reservations") && "GET".equalsIgnoreCase(method)) {
                    sendJsonResponse(exchange, 200, libraryService.getMyReservations(authenticatedUserId));
                } else if (path.equals("/api/library/user/borrows") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    int bookId = ((Double) body.get("bookId")).intValue();
                    String message = libraryService.borrowBook(authenticatedUserId, bookId);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                } else if (path.equals("/api/library/user/returns") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    int copyId = ((Double) body.get("copyId")).intValue();
                    String message = libraryService.returnBook(copyId);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                } else if (path.equals("/api/library/user/renews") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    int recordId = ((Double) body.get("recordId")).intValue();
                    String message = libraryService.renewBook(recordId);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                } else if (path.equals("/api/library/user/reservations") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    int bookId = ((Double) body.get("bookId")).intValue();
                    String message = libraryService.reserveBook(authenticatedUserId, bookId);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                } else if (path.equals("/api/library/user/reservations/cancel") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    int reservationId = ((Double) body.get("reservationId")).intValue();
                    String message = libraryService.cancelReservation(reservationId);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                } else if (path.equals("/api/library/user/fines/pay") && "POST".equalsIgnoreCase(method)) {
                    Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                    double amount = (Double) body.get("amount");
                    String message = libraryService.payFine(authenticatedUserId, amount);
                    sendJsonResponse(exchange, 200, Map.of("message", message));
                }
                // --- 管理员接口 (需要认证) ---
                else if (path.equals("/api/library/admin/books") && "POST".equalsIgnoreCase(method)) {
                    Book book = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Book.class);
                    libraryService.addBook(book);
                    sendJsonResponse(exchange, 201, Map.of("message", "书籍添加成功"));
                } else if (path.matches("/api/library/admin/books/\\d+") && "PUT".equalsIgnoreCase(method)) {
                    Book book = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Book.class);
                    libraryService.updateBook(book);
                    sendJsonResponse(exchange, 200, Map.of("message", "书籍更新成功"));
                }
                else {
                    sendJsonResponse(exchange, 404, Map.of("error", "未知的图书馆API路径: " + path));
                }
            } catch (JsonSyntaxException e) {
                sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON格式"));
            } catch (Exception e) {
                appendLog("业务逻辑错误: " + e.getMessage());
                sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
            }
        }


        private Map<String, String> parseQuery(String query) {
            if (query == null || query.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> result = new java.util.HashMap<>();
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
