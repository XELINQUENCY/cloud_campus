package gui;

import client.ApiClientFactory;
import client.ApiException;
import entity.User;
import enums.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.stream.Collectors;

/**
 * 虚拟校园平台统一主控制台。
 * 作为所有模块的入口。
 */
public class UnifiedMainDashboard extends JFrame {
    private final User currentUser;
    private final UnifiedLoginFrame loginFrame;

    public UnifiedMainDashboard(User user, UnifiedLoginFrame loginFrame) {
        this.currentUser = user;
        this.loginFrame = loginFrame;

        setTitle("虚拟校园服务平台 - 主界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private void openModule(String moduleName) {
        this.setVisible(false);
        Runnable onModuleExit = () -> this.setVisible(true);

        try {
            switch (moduleName) {
                case "图书馆":
                    if (currentUser.hasRole(UserRole.READER) || currentUser.hasRole(UserRole.LIBRARIAN)) {
                        new gui.library.MainFrame(currentUser, currentUser.hasRole(UserRole.LIBRARIAN)).setVisible(true);
                    } else {
                        showPermissionError("图书馆");
                    }
                    break;
                case "校园商店":
                    if (currentUser.hasRole(UserRole.STORE_CUSTOMER) || currentUser.hasRole(UserRole.STORE_ADMIN)) {
                        new gui.shop.ShopView(currentUser).setVisible(true);
                    } else {
                        showPermissionError("商店");
                    }
                    break;
                case "校园银行":
                    new gui.bank.BankLoginFrame().setVisible(true);
                    break;
                case "选课系统":
                    if (currentUser.hasRole(UserRole.STUDENT) || currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
                        new gui.course.CourseSelectionMainFrame(currentUser, onModuleExit).setVisible(true);
                    } else {
                        showPermissionError("选课系统");
                    }
                    break;
                case "学籍管理":
                    if (currentUser.hasRole(UserRole.STUDENT) || currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
                        new gui.schoolroll.SchoolRollMainFrame(currentUser, onModuleExit).setVisible(true);
                    } else {
                        showPermissionError("学籍管理");
                    }
                    break;
                // ... 其他模块 ...
                default:
                    JOptionPane.showMessageDialog(this, "模块 '"+ moduleName +"' 正在建设中...", "提示", JOptionPane.INFORMATION_MESSAGE);
                    this.setVisible(true);
                    break;
            }
        } catch (ApiException e) {
            JOptionPane.showMessageDialog(this, "启动模块失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            this.setVisible(true);
        }
    }

    private void showPermissionError(String module) {
        JOptionPane.showMessageDialog(this, "您的角色无权访问 " + module + "。");
        this.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(900, 80));
        headerPanel.setLayout(new BorderLayout());

        // 标题和用户信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(70, 130, 180));

        JLabel titleLabel = new JLabel("校园综合服务平台");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        infoPanel.add(titleLabel);

        String roleName = currentUser.hasRole(UserRole.ACADEMIC_ADMIN) ? "管理员" : "用户";
        JLabel userLabel = new JLabel("欢迎, " + currentUser.getName() + " (" + roleName + ")");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        infoPanel.add(userLabel);

        headerPanel.add(infoPanel, BorderLayout.CENTER);

        // 关闭和退出登录按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(70, 130, 180));

        JButton logoutButton = new JButton("退出登录");
        styleButton(logoutButton, new Color(245, 245, 245), new Color(100, 100, 100), 14);
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要退出登录吗?", "确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                this.setVisible(false);
                loginFrame.setVisible(true);
            }
        });
        buttonPanel.add(logoutButton);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(closeButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setLayout(new GridLayout(2, 3, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createModuleCard("个人信息", "维护个人基本资料", new Color(95, 189, 123), "👤"));
        mainPanel.add(createModuleCard("图书馆", "图书借阅和查询服务", new Color(149, 117, 205), "📖"));
        mainPanel.add(createModuleCard("校园商店", "购买学习用品和生活物品", new Color(237, 85, 101), "🛒"));
        mainPanel.add(createModuleCard("校园银行", "校园卡管理和消费记录", new Color(102, 102, 102), "💰"));

        // 根据用户角色显示不同功能
        if (currentUser.hasRole(UserRole.STUDENT)) {
            // 学生专属功能
            mainPanel.add(createModuleCard("选课系统", "选择课程和查看课表", new Color(74, 124, 246), "📚"));
            mainPanel.add(createModuleCard("学籍管理", "查看和管理学籍信息", new Color(247, 147, 39), "📊"));
            // mainPanel.add(createModuleCard("校园银行", "校园卡管理和消费记录", new Color(102, 102, 102), "💰"));
        } else if (currentUser.hasRole(UserRole.TEACHER)) {
            // 老师专属功能
            mainPanel.add(createModuleCard("课程管理", "管理教授课程和学生名单", new Color(74, 124, 246), "📚"));
//            mainPanel.add(createModuleCard("成绩录入", "录入和管理学生成绩", new Color(247, 147, 39), "📊"));
//            mainPanel.add(createModuleCard("科研项目", "管理科研项目和经费", new Color(102, 102, 102), "🔬"));
        } else if (currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            // 管理员专属功能
            mainPanel.add(createModuleCard("用户管理", "管理系统用户和权限", new Color(74, 124, 246), " 👥 "));

        }

        return mainPanel;
    }

    private JPanel createModuleCard(String title, String description, Color color, String icon) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // 添加悬停效果
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(245, 245, 245));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });

        // 图标
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(color);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        card.add(iconLabel, BorderLayout.NORTH);

        // 标题和描述
        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 0, 0, 0)); // 透明背景
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // 添加点击事件
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openModule(title);
            }
        });

        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        // 版权信息
        JLabel copyrightLabel = new JLabel("© 2023 校园综合服务平台 v2.0");
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        copyrightLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(copyrightLabel, BorderLayout.WEST);

        // 状态信息
        JLabel statusLabel = new JLabel("系统运行正常 | 最后更新: 2023-10-20");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(statusLabel, BorderLayout.EAST);

        return footerPanel;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor, int fontSize) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("微软雅黑", Font.PLAIN, fontSize));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void openUserProfile() {
        // 打开用户个人信息界面
        UserProfileDialog profileDialog = new UserProfileDialog(this, currentUser);
        profileDialog.setVisible(true);
    }

    private void openUserManagement() {
        // 打开用户管理界面（仅管理员）
        UserManagementDialog userManagementDialog = new UserManagementDialog(this);
        userManagementDialog.setVisible(true);
    }
    @Override
    public void dispose() {
        // 不需要断开网络连接，因为主登录界面还在使用同一个连接
        super.dispose();
    }
}
