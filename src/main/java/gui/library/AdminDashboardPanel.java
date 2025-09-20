package gui.library;

import entity.User;
import gui.library.admin.BookManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 管理员主面板
 * 不再负责创建和传递Client服务，子面板将通过工厂自行获取。
 */
public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(User currentAdmin) {
        setLayout(new BorderLayout());
        initTabs(currentAdmin);
    }

    private void initTabs(User currentAdmin) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        BookManagementPanel bookManagementPanel = new BookManagementPanel(currentAdmin);
        tabbedPane.addTab("  书籍管理  ", bookManagementPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
