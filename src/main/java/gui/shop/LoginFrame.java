package gui.shop;

import client.ApiClientFactory;
import client.ApiException;
import client.shop.*;
import entity.User;
import enums.UserRole;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox adminCheckBox;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("虚拟校园系统 - 登录");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
        initListeners();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("密  码:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        adminCheckBox = new JCheckBox("管理员登录");
        panel.add(adminCheckBox, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(100, 30));
        panel.add(loginButton, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        panel.add(statusLabel, gbc);

        this.add(panel);
    }

    private void initListeners() {
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        final String username = usernameField.getText();
        final String password = new String(passwordField.getPassword());
        final boolean isAdminLogin = adminCheckBox.isSelected();

        if (username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("正在登录，请稍候...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws ApiException {
                IShopClientSrv userSrv = ApiClientFactory.getShopClient();
                return userSrv.login(username, password, isAdminLogin);
            }

            @Override
            protected void done() {
                try {
                    User loggedInUser = get();
                    if (loggedInUser != null) {
                        statusLabel.setText("登录成功！");
                        // 登录成功，打开主窗口
                        if (isAdminLogin && loggedInUser.hasRole(UserRole.STORE_ADMIN)) {
                        	
                        	AdminView av = new AdminView();
                            av.setVisible(true);
                        }
                        if(loggedInUser.hasRole(UserRole.STORE_CUSTOMER)){
                        	ShopView sv = new ShopView(loggedInUser);
                            sv.setVisible(true);
                        }
                        LoginFrame.this.dispose();
                    }
                    // 如果loggedInUser为null，get()会抛出异常，在catch块中处理
                } catch (Exception e) {
                    // 获取根本原因，这通常是我们的ApiException
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "登录失败！\n" + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText(" ");
                } finally {
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}

