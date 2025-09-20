package gui.bank;

import client.ApiClientFactory;
import client.bank.IBankClientSrv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BankRegisterFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;

    private final IBankClientSrv bankClientSrv;

    /**
     * 构造函数不再接收 IBankClientSrv 实例。
     * 它现在通过 ApiClientFactory 自动获取网络客户端。
     */
    public BankRegisterFrame() {
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
    }

    private void initComponents() {
        setTitle("校园银行 - 注册");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 顶部标题
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(245, 247, 250));
        JLabel titleLabel = new JLabel("注册银行账户");
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
        formPanel.add(Box.createVerticalStrut(15));

        // 确认密码输入
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmPasswordLabel.setForeground(Color.BLACK); // 改为黑色
        confirmPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(Box.createVerticalStrut(5));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmPasswordField.setForeground(Color.BLACK); // 改为黑色
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        confirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(confirmPasswordField);
        formPanel.add(Box.createVerticalStrut(25));

        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setBackground(new Color(16, 185, 129));
        registerButton.setForeground(Color.BLACK); // 按钮文字保持白色
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(registerButton);
        formPanel.add(Box.createVerticalStrut(15));

        // 返回按钮
        backButton = new JButton("返回登录");
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

    // --- 适配网络通信的事件处理方法 ---
    private void registerActionPerformed() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        registerButton.setEnabled(false); // 禁用按钮

        // 使用 SwingWorker 异步执行注册的网络请求
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 在后台线程调用网络客户端的注册方法
                return bankClientSrv.register(userId, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BankRegisterFrame.this, "注册成功，请登录", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(BankRegisterFrame.this, "注册失败，用户名可能已存在", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(BankRegisterFrame.this, "注册失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    registerButton.setEnabled(true); // 恢复按钮
                }
            }
        }.execute();
    }
}

