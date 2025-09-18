package gui.bank;

import client.ApiClientFactory;
import client.bank.BankClient;
import client.bank.IBankClientSrv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BankLoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel; // 用于显示登录状态

    private final IBankClientSrv bankClientSrv;
    // 【修改】添加一个回调成员变量，用于通知主框架返回
    private final Runnable onBackCallback;

    /**
     * 【修改】构造函数现在接收一个 Runnable 回调。
     * @param onBackCallback 当窗口关闭或点击返回时要执行的操作。
     */
    public BankLoginFrame(Runnable onBackCallback) {
        this.onBackCallback = onBackCallback;
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
    }

    private void initComponents() {
        setTitle("校园银行 - 登录");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // 【修改】添加窗口监听器，处理用户点击 'X' 关闭按钮的事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 当关闭窗口时，执行回调以返回主界面
                if (onBackCallback != null) {
                    onBackCallback.run();
                }
            }
        });

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
        titleLabel.setForeground(Color.BLACK);
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
        userIdLabel.setForeground(Color.BLACK);
        userIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userIdLabel);
        formPanel.add(Box.createVerticalStrut(5));

        userIdField = new JTextField();
        userIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userIdField.setForeground(Color.BLACK);
        userIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        userIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userIdField);
        formPanel.add(Box.createVerticalStrut(15));

        // 密码输入
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setForeground(Color.BLACK);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(25));

        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setBackground(new Color(59, 130, 246));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(15));

        // 注册按钮
        registerButton = new JButton("注册新账户");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setBackground(new Color(16, 185, 129));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(registerButton);
        formPanel.add(Box.createVerticalStrut(15));

        // 返回按钮
        backButton = new JButton("返回主界面");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setBackground(new Color(150, 150, 150));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(backButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 底部版权信息
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(245, 247, 250));
        JLabel footerLabel = new JLabel("© 2025 校园综合服务平台 - 银行模块");
        footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 添加事件监听器
        loginButton.addActionListener(e -> loginActionPerformed());
        registerButton.addActionListener(e -> registerActionPerformed());
        backButton.addActionListener(e -> backActionPerformed());
    }

    /**
     * 【修改】返回按钮的逻辑现在会执行回调函数。
     */
    private void backActionPerformed() {
        if (onBackCallback != null) {
            onBackCallback.run();
        }
        dispose();
    }

    private void loginActionPerformed() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        if (statusLabel != null) statusLabel.setText("正在登录...");

        // 使用 SwingWorker 异步执行登录的网络请求
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
                        // 【修改】登录成功, 打开主界面, 并将返回主控制台的回调传递给它
                        BankMainFrame mainFrame = new BankMainFrame(onBackCallback);
                        mainFrame.setVisible(true);
                        dispose(); // 关闭当前登录窗口
                    }
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
        // 注册窗口是一个独立的临时窗口，关闭后不影响主流程，因此无需修改
        BankRegisterFrame registerFrame = new BankRegisterFrame();
        registerFrame.setVisible(true);
    }
}
