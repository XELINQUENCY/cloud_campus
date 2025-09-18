package controller;

import DAO.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.*;
import controller.handler.*;
import entity.User;
import service.AuthService;
import service.bank.IBankServerSrv;
import service.library.LibraryService;
import service.schoolroll.impl.StudentServiceImpl;
import service.shop.CouponService;
import service.shop.ProductService;
import service.shop.SalePromotionService;
import service.shop.ShopService;
import service.course.AdminCourseService;
import service.course.CourseBrowseService;
import service.course.StudentCourseService;
import service.user.UserManagementService;

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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 虚拟校园统一服务器控制器 (模块化重构版)
 * 职责: UI管理、服务器生命周期控制、请求分发。
 */
public class ServerController {

    private HttpsServer httpsServer = null;
    private ExecutorService threadPool = null;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private JFrame frame;
    private JTextArea logArea;
    private JButton startBtn, stopBtn;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();
    private final int port = 32777;

    private final AuthService authService;
    private final UserDAO userDAO;
    private final HttpHandler apiHandler; // 统一的API分发器

    // 2. 更新构造函数签名，添加 SchoolRollService
    public ServerController(LibraryService libraryService,
                            IBankServerSrv bankService,
                            AuthService authService,
                            UserManagementService userManagementService,
                            ShopService shopService,
                            ProductService productService,
                            CouponService couponService,
                            SalePromotionService salePromotionService,
                            StudentServiceImpl studentServiceImpl,
                            CourseBrowseService courseBrowseService,
                            StudentCourseService studentCourseService,
                            AdminCourseService adminCourseService,
                            UserDAO userDAO) {
        this.authService = authService;
        this.userDAO = userDAO;

        // 创建一个日志记录器实例，它将日志消息追加到UI的logArea
        ServerLogger uiLogger = this::appendLog;

        // 3. 初始化所有模块的处理器，并注入依赖（包括logger），新增 SchoolRollHandler
        this.apiHandler = new ApiHandler(
                new AuthHandler(authService, gson, uiLogger),
                new UserManagementHandler(userManagementService, gson, uiLogger),
                new LibraryHandler(libraryService, gson, uiLogger),
                new BankHandler(bankService, gson, uiLogger),
                new ShopHandler(shopService, productService, couponService, salePromotionService ,gson, uiLogger),
                new SchoolRollHandler(studentServiceImpl, gson, uiLogger),
                new CourseHandler(courseBrowseService, studentCourseService, adminCourseService, gson, uiLogger),
                uiLogger
        );
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

            HttpContext context = httpsServer.createContext("/api", this.apiHandler);
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
     * 认证过滤器
     */
    class AuthFilter extends Filter {
        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // 从白名单中移除 /api/bank/register，因为它需要知道当前登录的用户
            List<String> whitelist = List.of(
                    "/api/users/register",
                    "/api/auth/login", // 登录接口本身
                    // 公共查询接口，无需登录
                    "/api/bank/register",
                    "/api/library/books",
                    "/api/library/categories",
                    "/api/shop/products",
                    "/api/shop/categories",
                    "/api/shop/promotions",
                    "/api/course/browse"
            );

            if (whitelist.contains(path)) {
                chain.doFilter(exchange);
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = authService.validateToken(token);
                    User user = userDAO.findById(userId);

                    exchange.setAttribute("userId", userId);
                    exchange.setAttribute("user", user);
                    chain.doFilter(exchange);
                } catch (Exception e) {
                    appendLog("Token验证失败: " + e.getMessage());
                    sendJsonResponse(exchange, 401, Map.of("error", "认证失败: " + e.getMessage()));
                }
            } else {
                sendJsonResponse(exchange, 401, Map.of("error", "缺少认证令牌"));
            }
        }
        @Override
        public String description() { return "API Authentication Filter"; }
    }

    /**
     * 统一的API请求分发器 (Dispatcher)
     */
    class ApiHandler implements HttpHandler {
        private final HttpHandler authHandler;
        private final HttpHandler userManagementHandler;
        private final HttpHandler libraryHandler;
        private final HttpHandler bankHandler;
        private final HttpHandler shopHandler;
        private final HttpHandler schoolRollHandler;
        private final HttpHandler courseHandler;
        private final ServerLogger logger;

        // 5. 更新ApiHandler的构造函数
        public ApiHandler(HttpHandler auth, HttpHandler user, HttpHandler library, HttpHandler bank, HttpHandler shop, HttpHandler schoolRoll, HttpHandler course, ServerLogger logger) {
            this.authHandler = auth;
            this.userManagementHandler = user;
            this.libraryHandler = library;
            this.bankHandler = bank;
            this.shopHandler = shop;
            this.schoolRollHandler = schoolRoll;
            this.courseHandler = course;
            this.logger = logger;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                logger.log("分发请求: " + exchange.getRequestMethod() + " " + path);

                // 6. 在分发逻辑中添加对新路径的处理
                if (path.startsWith("/api/auth")) {
                    authHandler.handle(exchange);
                } else if(path.startsWith("/api/user")) {
                    userManagementHandler.handle(exchange);
                } else if (path.startsWith("/api/library")) {
                    libraryHandler.handle(exchange);
                } else if (path.startsWith("/api/bank")) {
                    bankHandler.handle(exchange);
                } else if (path.startsWith("/api/shop")) {
                    shopHandler.handle(exchange);
                } else if (path.startsWith("/api/schoolroll")) {
                    schoolRollHandler.handle(exchange);
                } else if (path.startsWith("/api/course")) {
                    courseHandler.handle(exchange);
                } else {
                    sendJsonResponse(exchange, 404, Map.of("error", "未知的API路径"));
                }
            } catch (Exception e) {
                logger.log("处理请求时发生严重错误: " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 500, Map.of("error", "服务器内部错误"));
            } finally {
                exchange.close();
            }
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
