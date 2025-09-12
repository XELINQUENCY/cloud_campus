package gui;

import entity.User;
import gui.user.BookSearchPanel;
import gui.user.MyBorrowsPanel;
import gui.user.MyProfilePanel;
import gui.user.MyReservationsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 普通用户主面板，使用 JTabbedPane 组织功能。
 */
public class UserDashboardPanel extends JPanel {

    public UserDashboardPanel(User currentUser) {
        setLayout(new BorderLayout());
        initTabs(currentUser);
    }

    private void initTabs(User currentUser) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 各个功能面板不再需要传递ClientSrv，它们将直接使用ApiClient.getInstance()
        tabbedPane.addTab("  书籍查询  ", new BookSearchPanel(currentUser));
        tabbedPane.addTab("  我的借阅  ", new MyBorrowsPanel(currentUser));
        tabbedPane.addTab("  我的预约  ", new MyReservationsPanel(currentUser));
        tabbedPane.addTab("  个人信息  ", new MyProfilePanel(currentUser));

        add(tabbedPane, BorderLayout.CENTER);
    }
}
