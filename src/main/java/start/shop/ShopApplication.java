package start.shop;

import gui.shop.*;

import javax.swing.*;

public class ShopApplication {
    public static void main(String[] args) {

        // 使用 SwingUtilities.invokeLater 来确保所有GUI操作都在事件分发线程（EDT）中执行
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
