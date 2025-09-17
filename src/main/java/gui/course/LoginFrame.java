package gui.course;

import client.ApiClientFactory;
import client.ApiException;
import client.library.LibraryClient; // 复用一个已有的、包含登录方法的客户端
import entity.User;
import enums.UserRole;

import javax.swing.*;
import java.awt.*;

/**
 * 选课系统的登录窗口。
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox adminCheckBox;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("选课系统 - 登录");
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
        panel.add(new JLabel("学号/工号:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("密  码:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        adminCheckBox = new JCheckBox("教务管理员登录");
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

        // 使用 SwingWorker 异步执行网络请求，避免界面卡死
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws ApiException {
                // 任何一个 Client 都可以用来登录，因为它们共享底层的 ApiClient 和 token
                // 这里我们复用 LibraryClient 的登录方法
                LibraryClient authClient = ApiClientFactory.getLibraryClient();
                return authClient.login(username, password, isAdminLogin);
            }

            @Override
            protected void done() {
                try {
                    User loggedInUser = get(); // 获取后台任务的结果
                    if (loggedInUser != null) {
                        statusLabel.setText("登录成功！");

                        // 权限检查：确保登录的用户是学生或教务管理员
                        if (loggedInUser.hasRole(UserRole.STUDENT) || loggedInUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
                            // 登录成功，打开选课系统主窗口
                            // 创建一个登出回调，当主窗口关闭时，重新显示登录窗口
                            Runnable onLogout = () -> SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                            new CourseSelectionMainFrame(loggedInUser, onLogout).setVisible(true);
                            LoginFrame.this.dispose(); // 关闭当前登录窗口
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                    "您的角色 (" + loggedInUser.getUserRoles() + ") 无权访问选课系统。",
                                    "权限不足", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    // 获取根本原因，这通常是我们自定义的 ApiException
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