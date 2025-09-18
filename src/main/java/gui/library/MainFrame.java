package gui.library;

import entity.User;
import enums.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private final User loggedInUser;
    private final boolean isAdminLogin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanelContainer = new JPanel(cardLayout);
    private final Runnable onExitCallback; // 【修改】添加回调成员变量

    /**
     * 【修改】构造函数增加了 Runnable 参数
     * @param user 登录用户
     * @param isAdminLogin 是否为管理员登录
     * @param onExitCallback 当窗口关闭或退出时执行的回调
     */
    public MainFrame(User user, boolean isAdminLogin, Runnable onExitCallback) {
        this.loggedInUser = user;
        this.isAdminLogin = isAdminLogin;
        this.onExitCallback = onExitCallback; // 【修改】保存回调

        setTitle("虚拟校园系统 - 图書館");
        setSize(1000, 700);
        // 【修改】关闭操作改为 DISPOSE，这样不会退出整个应用
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 【修改】添加窗口监听器，处理用户点击右上角'X'关闭按钮的事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onExitCallback != null) {
                    onExitCallback.run();
                }
            }
        });

        initComponents();
        switchPanelBasedOnRole();
    }

    private void initComponents() {
        // --- 中部：卡片式主面板 ---
        if (isAdminLogin && loggedInUser.hasRole(UserRole.LIBRARIAN)) {
            mainPanelContainer.add(new AdminDashboardPanel(loggedInUser), "ADMIN_PANEL");
        }
        if(loggedInUser.hasRole(UserRole.READER)){
            mainPanelContainer.add(new UserDashboardPanel(loggedInUser), "USER_PANEL");
        }

        // --- 底部：状态栏 ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel welcomeLabel = new JLabel("欢迎您, " + loggedInUser.getName());
        JButton logoutButton = new JButton("返回主界面"); // 【修改】按钮文本更明确
        statusBar.add(welcomeLabel, BorderLayout.WEST);
        statusBar.add(logoutButton, BorderLayout.EAST);

        // 【修改】按钮的 ActionListener 现在执行回调
        logoutButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "您确定要返回主界面吗？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // 不再创建新的LoginFrame，而是调用回调
                this.dispose(); // 关闭当前窗口，会触发 windowClosed 事件
            }
        });

        setLayout(new BorderLayout());
        add(mainPanelContainer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void switchPanelBasedOnRole() {
        if (loggedInUser.hasRole(UserRole.LIBRARIAN)) {
            cardLayout.show(mainPanelContainer, "ADMIN_PANEL");
        } else if (loggedInUser.hasRole(UserRole.READER)) {
            cardLayout.show(mainPanelContainer, "USER_PANEL");
        } else {
            mainPanelContainer.add(new JLabel("没有为您角色配置的视图", SwingConstants.CENTER));
        }
    }
}
