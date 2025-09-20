package controller;

import DAO.UserDAO;
import DAO.bank.BankUserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.*;
import controller.handler.*;

import entity.User;
import service.AuthService;
import service.bank.BankServerSrvImpl;
import service.bank.IBankServerSrv;
import service.impl.AuthServiceImpl;
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
import service.auth.BankUserAuthenticator;     // 【新增】
import service.auth.GeneralUserAuthenticator; // 【新增】
import java.util.List;                        // 【新增】
import service.auth.Authenticator;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
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

    private final AuthService generalAuthService;
    private final AuthService bankAuthService;
    private final UserDAO userDAO;
    private ServerLogger uiLogger = this::appendLog;
    private final HttpHandler authHandler;
    private final HttpHandler bankAuthHandler; // 新增
    private final HttpHandler bankHandler;
    private final HttpHandler userManagementHandler;
    private final HttpHandler libraryHandler;
    private final HttpHandler shopHandler;
    private final HttpHandler schoolRollHandler;
    private final HttpHandler courseHandler;

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
                            UserDAO userDAO, BankUserDAO bankUserDAO) {
        // 构造函数参数列表保持不变
        this.userDAO = userDAO;

        // 创建两个独立的 AuthService 实例
        // 主系统认证服务，只包含通用认证器
        List<Authenticator> generalAuthenticators = List.of(new GeneralUserAuthenticator(userDAO));
        List<Authenticator> bankUserAuthenticator = List.of(new BankUserAuthenticator(bankUserDAO));
        this.generalAuthService = new AuthServiceImpl(generalAuthenticators);

        this.bankAuthService = new AuthServiceImpl(bankUserAuthenticator);

        // 分别初始化每个 Handler
        this.authHandler = new AuthHandler(generalAuthService, gson, uiLogger);
        this.bankAuthHandler = new BankAuthHandler(bankAuthService, gson, uiLogger);
        this.userManagementHandler = new UserManagementHandler(userManagementService, gson, uiLogger);
        this.libraryHandler = new LibraryHandler(libraryService, gson, uiLogger);

        this.bankHandler = new BankHandler(bankService, gson, uiLogger);
        this.shopHandler = new ShopHandler(shopService, productService, couponService, salePromotionService, gson, uiLogger);
        this.schoolRollHandler = new SchoolRollHandler(studentServiceImpl, gson, uiLogger);
        this.courseHandler = new CourseHandler(courseBrowseService, studentCourseService, adminCourseService, gson, uiLogger);
    }

    public void createFrame() {
        frame = new JFrame("VCampus Server");
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
            String KEYSTORE_RESOURCE_PATH = "/keystore.jks";
            String KEYSTORE_PASSWORD = "password";
            SSLContext sslContext = createSSLContext(KEYSTORE_RESOURCE_PATH, KEYSTORE_PASSWORD);

            httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

            // 银行认证上下文
            HttpContext bankAuthContext = httpsServer.createContext("/api/bank/auth", this.bankAuthHandler);

            // 银行API上下文，应用银行专用的过滤器
            HttpContext bankApiContext = httpsServer.createContext("/api/bank", this.bankHandler);
            bankApiContext.getFilters().add(new BankAuthFilter(bankAuthService, gson));

            // 主系统API上下文，应用通用的过滤器
            HttpContext mainApiContext = httpsServer.createContext("/api", new ApiHandler(uiLogger));
            mainApiContext.getFilters().add(new AuthFilter());

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

    private SSLContext createSSLContext(String resourcePath, String password) throws Exception {
        char[] pass = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");

        //使用 getResourceAsStream 从 JAR 包内部或 classpath 加载资源
        try (InputStream is = this.getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("无法在 Classpath 中找到资源: " + resourcePath);
            }
            ks.load(is, pass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, pass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        System.out.println("成功从类路径加载证书并创建SSLContext！");

        return sslContext;
    }

    /**
     * 认证过滤器
     */
    class AuthFilter extends Filter {
        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // 主系统的白名单
            List<String> whitelist = List.of(
                    "/api/auth/login",
                    "/api/users/register",
                    "/api/library/books",
                    "/api/library/categories",
                    "/api/shop/products",
                    "/api/shop/categories",
                    "/api/shop/promotions",
                    "/api/course/browse",
                    "/api/shop/coupons/templates/available",
                    "/api/shop/products/search"
            );

            // 如果请求是发往银行的，这个过滤器直接放行，让银行自己的上下文去处理
            if (path.startsWith("/api/bank")) {
                chain.doFilter(exchange);
                return;
            }

            if (whitelist.contains(path)) {
                chain.doFilter(exchange);
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // 使用 generalAuthService 验证
                    String userId = generalAuthService.validateToken(token);
                    User user = userDAO.findById(userId);

                    exchange.setAttribute("userId", userId);
                    exchange.setAttribute("user", user);
                    chain.doFilter(exchange);
                } catch (Exception e) {
                    appendLog("主系统Token验证失败: " + e.getMessage());
                    sendJsonResponse(exchange, 401, Map.of("error", "认证失败: " + e.getMessage()));
                }
            } else {
                sendJsonResponse(exchange, 401, Map.of("error", "缺少认证令牌"));
            }
        }
        @Override
        public String description() { return "Main API Authentication Filter"; }
    }

    /**
     * 统一的API请求分发器 (Dispatcher)
     */
    class ApiHandler implements HttpHandler {
        private final ServerLogger logger;

        public ApiHandler(ServerLogger logger) {
            this.logger = logger;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (exchange) {
                String path = exchange.getRequestURI().getPath();
                logger.log("主系统ApiHandler分发请求: " + exchange.getRequestMethod() + " " + path);

                // 这个Handler现在只处理非银行的API
                if (path.startsWith("/api/auth")) {
                    authHandler.handle(exchange);
                } else if (path.startsWith("/api/users")) {
                    userManagementHandler.handle(exchange);
                } else if (path.startsWith("/api/library")) {
                    libraryHandler.handle(exchange);
                } else if (path.startsWith("/api/shop")) {
                    shopHandler.handle(exchange);
                } else if (path.startsWith("/api/schoolroll")) {
                    schoolRollHandler.handle(exchange);
                } else if (path.startsWith("/api/course")) {
                    courseHandler.handle(exchange);
                } else if (path.startsWith("/api/bank")) {
                    sendJsonResponse(exchange, 404, Map.of("error", "银行API应由独立上下文处理"));
                } else {
                    sendJsonResponse(exchange, 404, Map.of("error", "未知的API路径"));
                }
            } catch (Exception e) {
                e.printStackTrace();
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
