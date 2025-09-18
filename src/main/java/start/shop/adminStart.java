package start.shop;

import gui.bank.BankLoginFrame;
import gui.shop.AdminView;

import javax.swing.*;

public class adminStart {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminView loginFrame = new AdminView(null);
            loginFrame.setVisible(true);
        });
    }
}
