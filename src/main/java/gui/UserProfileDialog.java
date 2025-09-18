package gui;

import client.ApiClientFactory;
import client.ApiException;
import client.user.IUserManagementClient;
import entity.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserProfileDialog extends JDialog {
    private final IUserManagementClient userManagementClient;
    private User currentUser;
    private final Runnable onWindowClosedCallback; // 【修改】添加回调成员变量

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JButton saveButton;
    private JButton cancelButton;

    private JComboBox<String> genderComboBox;
    private JTextField ageField;

    /**
     * 【修改】构造函数增加了 Runnable 参数
     * @param parent 父窗口
     * @param user 当前用户
     * @param onWindowClosedCallback 当对话框关闭时执行的回调
     */
    public UserProfileDialog(JFrame parent, User user, Runnable onWindowClosedCallback) {
        super(parent, "个人信息管理", true);
        this.userManagementClient = ApiClientFactory.getUserManagementClient();
        this.currentUser = user;
        this.onWindowClosedCallback = onWindowClosedCallback; // 【修改】保存回调

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadUserData();

        // 【修改】添加窗口监听器以处理关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onWindowClosedCallback != null) {
                    onWindowClosedCallback.run();
                }
            }
        });
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        usernameField.setEditable(false);

        emailField = new JTextField(20);

        String[] genders = {"男", "女", "保密"};
        genderComboBox = new JComboBox<>(genders);

        ageField = new JTextField(10);

        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        saveButton = new JButton("保存");
        saveButton.addActionListener(_ -> saveUserData());

        cancelButton = new JButton("取消");
        // 【修改】取消按钮现在也只负责关闭对话框，关闭事件由监听器统一处理
        cancelButton.addActionListener(_ -> dispose());
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(genderComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("年龄:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(ageField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("确认密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(confirmPasswordField, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void loadUserData() {
        usernameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        if (currentUser.getGender() != null) {
            genderComboBox.setSelectedItem(currentUser.getGender());
        }
        if (currentUser.getAge() != null) {
            ageField.setText(currentUser.getAge().toString());
        }
    }

    private void saveUserData() {
        // ... (数据校验逻辑保持不变)
        String email = emailField.getText();
        String gender = (String) genderComboBox.getSelectedItem();
        Integer age = null;
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ageField.getText().isEmpty()) {
            try {
                age = Integer.parseInt(ageField.getText());
                if (age < 1 || age > 150) {
                    JOptionPane.showMessageDialog(this, "年龄必须在1-150之间", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "年龄必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "密码长度不能少于6位", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        User userToUpdate = new User();
        userToUpdate.setId(currentUser.getId());
        userToUpdate.setName(currentUser.getName());
        userToUpdate.setEmail(email);
        userToUpdate.setGender(gender);
        userToUpdate.setAge(age);
        if (!password.isEmpty()) {
            userToUpdate.setPassword(password);
        }

        saveButton.setEnabled(false);
        saveButton.setText("保存中...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws ApiException {
                return userManagementClient.updateMyProfile(userToUpdate);
            }

            @Override
            protected void done() {
                try {
                    User updatedUser = get();
                    JOptionPane.showMessageDialog(UserProfileDialog.this,
                            "用户信息更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                    currentUser = updatedUser;

                    // 【修改】成功后，关闭对话框，这将自动触发 windowClosed 事件和回调
                    dispose();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(UserProfileDialog.this,
                            "更新失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    saveButton.setEnabled(true);
                    saveButton.setText("保存");
                }
            }
        }.execute();
    }
}