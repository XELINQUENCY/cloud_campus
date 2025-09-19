package start;

import DAO.UserDAO;
import DAO.bank.BankUserDAO;
import controller.ServerController;
import service.AuthService;
import service.auth.Authenticator;
import service.auth.BankUserAuthenticator;
import service.auth.GeneralUserAuthenticator;
import service.bank.BankServerSrvImpl;
import service.bank.IBankServerSrv;
import service.course.AdminCourseService;
import service.course.CourseBrowseService;
import service.course.ServiceFactory;
import service.course.StudentCourseService;
import service.course.impl.AdminCourseServiceImpl;
import service.course.impl.CourseBrowseServiceImpl;
import service.course.impl.StudentCourseServiceImpl;
import service.impl.AuthServiceImpl;
import service.library.LibraryService;
import service.library.impl.LibraryServiceImpl;
import service.schoolroll.impl.StudentServiceImpl;
import service.shop.CouponService;
import service.shop.ProductService;
import service.shop.SalePromotionService;
import service.shop.ShopService;
import service.shop.impl.CouponServiceImpl;
import service.shop.impl.ProductServiceImpl;
import service.shop.impl.SalePromotionServiceImpl;
import service.shop.impl.ShopServiceImpl;
import service.user.UserManagementService;
import service.user.impl.UserManagementServiceImpl;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器应用程序的唯一入口点 (重构版)
 * 采用策略模式对认证服务进行解耦，提高可扩展性。
 */
public class Application {

    public static void main(String[] args) {
        // --- 1. 初始化数据访问层 (DAO) ---
        System.out.println("正在初始化DAO层组件...");
        UserDAO userDAO = new UserDAO();
        BankUserDAO bankUserDAO = new BankUserDAO();
        System.out.println("当前工作目录 (CWD): " + new java.io.File(".").getAbsolutePath());

        // --- 2. 组装认证策略 (Strategy Pattern) ---
        // 创建一个认证策略的列表，AuthService将按顺序尝试这些策略
        List<Authenticator> authenticators = new ArrayList<>();

        // 添加银行用户认证策略
        authenticators.add(new BankUserAuthenticator(bankUserDAO));
        // 添加通用用户(图书馆等)认证策略
        authenticators.add(new GeneralUserAuthenticator(userDAO));
        // 未来若有学生模块，只需在此处添加新的认证策略即可
        // authenticators.add(new StudentAuthenticator(studentDAO));

        System.out.println("认证策略组装完毕。");

        // --- 3. 初始化核心业务服务 (Service) ---
        System.out.println("正在初始化核心服务...");

        // 创建AuthService，并将上面组装好的策略列表注入进去
        AuthService authService = new AuthServiceImpl(authenticators);

        // 创建其他模块的服务实例
        LibraryService libraryService = new LibraryServiceImpl();
        UserManagementService userManagementService = new UserManagementServiceImpl();
        IBankServerSrv bankService = new BankServerSrvImpl();
        ShopService shopService = new ShopServiceImpl();
        CouponService couponService = new CouponServiceImpl();
        SalePromotionService salePromotionService = new SalePromotionServiceImpl();
        ProductService productService = new ProductServiceImpl();
        StudentServiceImpl studentServiceImpl = new StudentServiceImpl();
        ServiceFactory serviceFactory = ServiceFactory.getInstance();


        // 未来可在此处添加其他服务...

        System.out.println("核心服务初始化完毕。");

        // --- 4. 启动网络控制层 (Controller) ---
        // 将所有创建好的Service实例传递(注入)给Controller
        SwingUtilities.invokeLater(() -> {
            ServerController controller =
                    new ServerController(libraryService, bankService,
                            authService, userManagementService, shopService,
                            productService, couponService,
                            salePromotionService, studentServiceImpl,
                            serviceFactory.getCourseBrowseService(),
                            serviceFactory.getStudentCourseService(),
                            serviceFactory.getAdminCourseService(), userDAO, bankUserDAO);
            controller.createFrame();
        });
    }
}