package start;

import DAO.UserDAO;
import controller.ServerController;
import service.AuthService;
import service.impl.AuthServiceImpl;
import service.library.LibraryService;
import service.library.impl.LibraryServiceImpl;

import javax.swing.*;

/**
 * 服务器应用程序的唯一入口点 (重构版)
 */
public class Application {

    public static void main(String[] args) {
        // --- 1. 初始化核心业务组件 ---
        System.out.println("正在初始化核心服务...");

        // 创建DAO层实例
        UserDAO userDAO = new UserDAO();

        // 创建Service层实例，并将DAO注入其中
        AuthService authService = new AuthServiceImpl(userDAO);
        LibraryService libraryService = new LibraryServiceImpl();
        // 如果有其他Service, 也在这里一并创建...

        System.out.println("核心服务初始化完毕。");

        // --- 2. 启动网络控制层 ---
        // 将已经创建好的Service实例传递(注入)给Controller
        SwingUtilities.invokeLater(() -> {
            ServerController controller = new ServerController(libraryService, authService);
            controller.createFrame();
        });
    }
}
