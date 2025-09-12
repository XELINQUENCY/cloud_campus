package gui;

import entity.User;
import gui.admin.BookManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 管理员主面板，使用 JTabbedPane 组织各项功能。
 * 不再负责创建Client服务，而是直接使用ApiClient单例。
 */
public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(User currentAdmin) {
        setLayout(new BorderLayout());
        initTabs(currentAdmin);
    }

    private void initTabs(User currentAdmin) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 1. 创建书籍管理面板
        BookManagementPanel bookManagementPanel = new BookManagementPanel(currentAdmin);
        tabbedPane.addTab("  书籍管理  ", bookManagementPanel);

        // 未来可以添加其他管理面板，例如：
        // JPanel userManagementPanel = new JPanel();
        // tabbedPane.addTab("  用户管理  ", userManagementPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
