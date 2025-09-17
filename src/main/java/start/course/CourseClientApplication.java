package start.course;

import gui.course.LoginFrame;
import javax.swing.*;

public class CourseClientApplication {

    public static void main(String[] args) {
        // 设置一个更现代的 Swing 界面外观 (可选)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在 Swing 的事件分发线程中创建并显示登录窗口
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}