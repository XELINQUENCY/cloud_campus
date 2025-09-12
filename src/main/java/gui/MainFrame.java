package gui;

import entity.User;
import enums.UserRole;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User loggedInUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanelContainer = new JPanel(cardLayout);

    public MainFrame(User user) {
        this.loggedInUser = user;

        setTitle("虚拟校园系统");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        switchPanelBasedOnRole();
    }

    private void initComponents() {
        // --- 中部：卡片式主面板 ---
        // 不同的面板根据用户角色添加
        if (loggedInUser.hasRole(UserRole.LIBRARIAN)) {
            mainPanelContainer.add(new AdminDashboardPanel(loggedInUser), "ADMIN_PANEL");
        }
        if(loggedInUser.hasRole(UserRole.READER)){
            mainPanelContainer.add(new UserDashboardPanel(loggedInUser), "USER_PANEL");
        }

        // --- 底部：状态栏 ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel welcomeLabel = new JLabel("欢迎您, " + loggedInUser.getName());
        JButton logoutButton = new JButton("退出登录");
        statusBar.add(welcomeLabel, BorderLayout.WEST);
        statusBar.add(logoutButton, BorderLayout.EAST);

        logoutButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "您确定要退出登录吗？", "退出确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });

        setLayout(new BorderLayout());
        add(mainPanelContainer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void switchPanelBasedOnRole() {
        // 默认显示第一个适配的角色面板
        // 实际应用中可能需要根据角色优先级或默认角色来决定
        if (loggedInUser.hasRole(UserRole.LIBRARIAN)) {
            cardLayout.show(mainPanelContainer, "ADMIN_PANEL");
        } else if (loggedInUser.hasRole(UserRole.READER)) {
            cardLayout.show(mainPanelContainer, "USER_PANEL");
        } else {
            // 没有匹配的面板，可以显示一个默认的欢迎或错误页面
            mainPanelContainer.add(new JLabel("没有为您角色配置的视图", SwingConstants.CENTER));
        }
    }
}
