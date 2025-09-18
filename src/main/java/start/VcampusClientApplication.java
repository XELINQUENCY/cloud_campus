package start;

import gui.UnifiedLoginFrame; // 使用我们下面创建的统一登录窗口
import javax.swing.*;

/**
 * 虚拟校园系统客户端统一启动程序。
 * 这是整个应用程序的唯一入口点。
 */
public class VcampusClientApplication {

    public static void main(String[] args) {
        // 设置一个更现代的 Swing 界面外观 (可选)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在 Swing 的事件分发线程中创建并显示登录窗口
        SwingUtilities.invokeLater(() -> {
            new UnifiedLoginFrame().setVisible(true);
        });
    }
}