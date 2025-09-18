package gui;

import client.ApiClientFactory;
import client.ApiException;
import client.library.LibraryClient;
import client.user.UserManagementClient;
import entity.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 虚拟校园平台统一登录窗口。
 * 本窗口对接成熟框架的统一认证服务。
 */
public class UnifiedLoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JCheckBox adminCheckBox;

    public UnifiedLoginFrame() {
        setTitle("虚拟校园服务平台 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        initComponents();
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        boolean isAdmin = adminCheckBox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("正在登录...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws ApiException {
                UserManagementClient authClient = ApiClientFactory.getUserManagementClient();
                return authClient.login(username, password, isAdmin);
            }

            @Override
            protected void done() {
                try {
                    User loggedInUser = get();
                    JOptionPane.showMessageDialog(UnifiedLoginFrame.this, "登录成功！欢迎您, " + loggedInUser.getName(), "成功", JOptionPane.INFORMATION_MESSAGE);
                    openMainDashboard(loggedInUser);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(UnifiedLoginFrame.this, "登录失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("登录");
                }
            }
        }.execute();
    }

    private void openMainDashboard(User user) {
        new UnifiedMainDashboard(user, this).setVisible(true);
        this.setVisible(false);
    }

    private void openRegisterFrame() {
        new UnifiedRegisterFrame(this).setVisible(true);
        this.setVisible(false);
    }

    private void initComponents() {
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        adminCheckBox = new JCheckBox("管理员登录");
        loginButton = new JButton("登录");
        registerButton = new JButton("注册新账户");

        // ... 样式设置 ...
        usernameField.setPreferredSize(new Dimension(250, 40));
        passwordField.setPreferredSize(new Dimension(250, 40));
        loginButton.setPreferredSize(new Dimension(250, 45));
        registerButton.setPreferredSize(new Dimension(250, 45));
        // ... 其他样式设置 ...

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> openRegisterFrame());
    }

    private JPanel createHeaderPanel() {
        // ... 和您之前的代码相同 ...
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(400, 120));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("虚拟校园统一认证", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setOpaque(true);
        closeButton.setBorderPainted(false);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setBackground(new Color(70, 130, 180));
        closePanel.add(closeButton);
        headerPanel.add(closePanel, BorderLayout.NORTH);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        // ... 和您之前的代码类似，增加了adminCheckBox ...
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridy = 1; formPanel.add(usernameField, gbc);
        gbc.gridy = 2; formPanel.add(new JLabel("密码:"), gbc);
        gbc.gridy = 3; formPanel.add(passwordField, gbc);
        gbc.gridy = 4; formPanel.add(adminCheckBox, gbc);
        gbc.gridy = 5; formPanel.add(loginButton, gbc);
        gbc.gridy = 6; formPanel.add(new JSeparator(), gbc);
        gbc.gridy = 7; formPanel.add(registerButton, gbc);

        return formPanel;
    }

    private JPanel createFooterPanel() {
        // ... 和您之前的代码相同 ...
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        JLabel footerLabel = new JLabel("© 2025 虚拟校园服务平台");
        footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(footerLabel);
        return footerPanel;
    }
}
