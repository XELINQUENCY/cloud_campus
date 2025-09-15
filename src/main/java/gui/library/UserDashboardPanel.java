package gui.library;

import entity.User;
import gui.library.user.BookSearchPanel;
import gui.library.user.MyBorrowsPanel;
import gui.library.user.MyProfilePanel;
import gui.library.user.MyReservationsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 普通用户主面板 (重构版)
 * 同样不再负责传递Client服务。
 */
public class UserDashboardPanel extends JPanel {

    public UserDashboardPanel(User currentUser) {
        setLayout(new BorderLayout());
        initTabs(currentUser);
    }

    private void initTabs(User currentUser) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 各个功能面板的构造函数已简化，不再需要传递ClientSrv
        tabbedPane.addTab("  书籍查询  ", new BookSearchPanel(currentUser));
        tabbedPane.addTab("  我的借阅  ", new MyBorrowsPanel(currentUser));
        tabbedPane.addTab("  我的预约  ", new MyReservationsPanel(currentUser));
        tabbedPane.addTab("  个人信息  ", new MyProfilePanel(currentUser));

        add(tabbedPane, BorderLayout.CENTER);
    }
}
