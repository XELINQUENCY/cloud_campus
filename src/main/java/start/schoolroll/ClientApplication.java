package start.schoolroll;

import gui.schoolroll.LoginFrame;

import javax.swing.*;

/**
 * 客户端应用程序的唯一入口点。
 */
public class ClientApplication {

    public static void main(String[] args) {
        // 设置Swing界面的外观和感觉，使其更现代化
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 使用 SwingUtilities.invokeLater 来确保所有GUI操作都在事件分发线程（EDT）中执行
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

