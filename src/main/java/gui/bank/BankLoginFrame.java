package gui.bank;

import client.ApiClientFactory;
import client.bank.BankClient;
import client.bank.IBankClientSrv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BankLoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel; // 用于显示登录状态

    private final IBankClientSrv bankClientSrv;

    /**
     * 【修改】构造函数无参化，自动从工厂获取网络客户端。
     */
    public BankLoginFrame() {
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
    }

    // --- 未修改的纯UI方法 (已省略内部实现) ---
    private void initComponents() {
        setTitle("校园银行 - 登录");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // 使用现代外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 顶部标题
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(245, 247, 250));
        JLabel titleLabel = new JLabel("校园银行登录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK); // 改为黑色
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 中心表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // 用户ID输入
        JLabel userIdLabel = new JLabel("用户ID:");
        userIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userIdLabel.setForeground(Color.BLACK); // 改为黑色
        userIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userIdLabel);
        formPanel.add(Box.createVerticalStrut(5));

        userIdField = new JTextField();
        userIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userIdField.setForeground(Color.BLACK); // 改为黑色
        userIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        userIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userIdField);
        formPanel.add(Box.createVerticalStrut(15));

        // 密码输入
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK); // 改为黑色
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setForeground(Color.BLACK); // 改为黑色
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(25));

        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setBackground(new Color(59, 130, 246));
        loginButton.setForeground(Color.BLACK); // 按钮文字保持白色
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(15));

        // 注册按钮
        registerButton = new JButton("注册新账户");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setBackground(new Color(16, 185, 129));
        registerButton.setForeground(Color.BLACK); // 按钮文字保持白色
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(registerButton);
        formPanel.add(Box.createVerticalStrut(15));

        // 返回按钮
        backButton = new JButton("返回主界面");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backButton.setBackground(new Color(150, 150, 150));
        backButton.setForeground(Color.BLACK); // 按钮文字保持白色
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(backButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 底部版权信息
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(245, 247, 250));
        JLabel footerLabel = new JLabel("© 2023 校园综合服务平台 - 银行模块");
        footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        footerLabel.setForeground(Color.BLACK); // 改为黑色
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 添加事件监听器
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginActionPerformed();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerActionPerformed();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backActionPerformed();
            }
        });
    }

    private void backActionPerformed() {
        dispose();
    }

    // --- 已修改为适配网络通信的事件处理方法 ---

    private void loginActionPerformed() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        if (statusLabel != null) statusLabel.setText("正在登录...");

        // 【修改】使用 SwingWorker 异步执行登录的网络请求
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 在后台线程调用网络客户端的登录方法
                return bankClientSrv.login(userId, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) { // 获取后台任务的结果
                        // 登录成功, 打开主界面
                        // 传入一个登出回调, 当主界面退出时, 可以重新显示一个新的登录窗口
                        BankMainFrame mainFrame = new BankMainFrame(() -> new BankLoginFrame().setVisible(true));
                        mainFrame.setVisible(true);
                        dispose(); // 关闭当前登录窗口
                    }
                    // 如果登录失败，get() 会在 doInBackground 抛出异常时重新抛出该异常
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(BankLoginFrame.this, "登录失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loginButton.setEnabled(true);
                    if (statusLabel != null) statusLabel.setText("");
                }
            }
        }.execute();
    }

    private void registerActionPerformed() {
        // 【修改】注册窗口现在也使用无参构造函数
        BankRegisterFrame registerFrame = new BankRegisterFrame();
        registerFrame.setVisible(true);
    }
}

