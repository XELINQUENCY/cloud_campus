package gui;

import client.ApiClientFactory;
import client.ApiException;
import client.user.IUserManagementClient;
import entity.User;
import enums.UserRole;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 虚拟校园平台统一注册窗口。
 */
public class UnifiedRegisterFrame extends JFrame {

    private final UnifiedLoginFrame loginFrame;
    private final IUserManagementClient userManagementClient;

    // UI Components
    private JTextField usernameField, emailField, ageField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private JComboBox<String> genderComboBox;
    private JButton registerButton, backButton;
    private FlowLayout flowLayout;

    public UnifiedRegisterFrame(UnifiedLoginFrame loginFrame) {
        this.userManagementClient = ApiClientFactory.getUserManagementClient();
        this.loginFrame = loginFrame;

        // 设置窗口属性
        setTitle("用户管理系统 - 注册");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 750); //
        setLocationRelativeTo(null); // 居中显示
        setUndecorated(true); // 去除窗口边框
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20)); // 圆角窗口

        // 设置背景颜色
        getContentPane().setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout());

        // 创建UI组件
        initComponents();

        // 添加组件到窗口
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private void attemptRegister() {
        // --- 1. 数据校验 (与您原代码类似) ---
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "必填字段不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
        }

        UserRole role = (UserRole) roleComboBox.getSelectedItem();
        String gender = (String) genderComboBox.getSelectedItem();
        Integer age = ageField.getText().isEmpty() ? null : Integer.parseInt(ageField.getText());


        // --- 2. 使用SwingWorker执行注册 ---
        registerButton.setEnabled(false);
        registerButton.setText("注册中...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws ApiException {
                return userManagementClient.register(username, password, emailField.getText(), role, gender, age);
            }

            @Override
            protected void done() {
                try {
                    User newUser = get();
                    JOptionPane.showMessageDialog(UnifiedRegisterFrame.this,
                            "用户 " + newUser.getName() + " 注册成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                    returnToLogin();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(UnifiedRegisterFrame.this, "注册失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("注册账户");
                }
            }
        }.execute();
    }

    private void returnToLogin() {
        this.dispose();
        loginFrame.setVisible(true);
    }

    private void initComponents() {
        // 用户名输入框
        usernameField = new JTextField();
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 密码输入框
        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 确认密码输入框
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 邮箱输入框
        emailField = new JTextField();
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        emailField.setFont(new Font("微软雅黑", Font.PLAIN, 14));


        // 角色选择框 - 只包含学生和老师
        java.util.List<UserRole> allowedRoles = Arrays.stream(UserRole.values())
                .filter(role -> role == UserRole.STUDENT || role == UserRole.TEACHER)
                .toList();

        roleComboBox = new JComboBox<>(allowedRoles.toArray(new UserRole[0]));
        roleComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        roleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserRole) {
                    setText(((UserRole) value).getDisplayName());
                }
                return this;
            }
        });
        roleComboBox.setSelectedItem(UserRole.STUDENT);

        String[] genders = {"男", "女", "保密"};

        // 性别选择框
        genderComboBox = new JComboBox<>(genders);
        genderComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        genderComboBox.setBackground(Color.WHITE);
        genderComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // 年龄输入框
        ageField = new JTextField();
        ageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 注册按钮
        registerButton = new JButton("注册账户");
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 返回按钮
        backButton = new JButton("返回登录");
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setBackground(new Color(245, 245, 245));
        backButton.setForeground(new Color(100, 100, 100));
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        flowLayout = new FlowLayout(FlowLayout.CENTER, 10, 10);

        // 添加事件监听器
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptRegister();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnToLogin();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(500, 100));
        headerPanel.setLayout(new BorderLayout());

        // 标题
        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
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
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(240, 240, 240));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 创建账户信息面板
        JPanel accountPanel = createGroupPanel("账户信息", createAccountFields());
        formPanel.add(accountPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 创建个人信息面板
        JPanel infoPanel = createGroupPanel("个人信息", createPersonalInfoFields());
        formPanel.add(infoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 创建角色选择面板
        JPanel rolePanel = createGroupPanel("角色选择", createRoleSelectionFields());
        formPanel.add(rolePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(240, 240, 240));

        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(registerButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(backButton);

        formPanel.add(buttonPanel);

        return formPanel;
    }

    private JPanel createGroupPanel(String title, JPanel contentPanel) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BorderLayout());
        groupPanel.setBackground(new Color(240, 240, 240));

        // 标题标签
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 内容面板
        contentPanel.setBackground(new Color(240, 240, 240));

        groupPanel.add(titleLabel, BorderLayout.NORTH);
        groupPanel.add(contentPanel, BorderLayout.CENTER);

        return groupPanel;
    }

    private JPanel createAccountFields() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 用户名标签
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameLabel, gbc);

        // 用户名输入框
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        // 密码标签
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        // 密码输入框
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // 确认密码标签
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(confirmPasswordLabel, gbc);

        // 确认密码输入框
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(confirmPasswordField, gbc);

        // 邮箱标签
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);

        // 邮箱输入框
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);

        return panel;
    }

    private JPanel createPersonalInfoFields() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 性别标签
        JLabel genderLabel = new JLabel("性别:");
        genderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(genderLabel, gbc);

        // 性别选择框
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(genderComboBox, gbc);

        // 年龄标签
        JLabel ageLabel = new JLabel("年龄:");
        ageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(ageLabel, gbc);

        // 年龄输入框
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(ageField, gbc);

        return panel;
    }

    private JPanel createRoleSelectionFields() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 角色标签
        JLabel roleLabel = new JLabel("角色:");
        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(roleLabel, gbc);

        // 角色选择框
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(roleComboBox, gbc);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();

        footerPanel.setLayout(flowLayout);//设置布局方式
        footerPanel.add(registerButton);
        footerPanel.add(backButton);

        return footerPanel;
    }
}
