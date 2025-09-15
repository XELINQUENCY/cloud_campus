package start.bank;

import gui.bank.BankLoginFrame;

import javax.swing.*;

/**
 * 银行系统客户端启动程序 (重构版)
 * 只负责启动GUI界面，不再创建任何服务实例。
 */
public class BankSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 【修改】构造函数不再需要传递IBankClientSrv
            BankLoginFrame loginFrame = new BankLoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
