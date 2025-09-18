package gui;

import client.ApiClientFactory;
import client.ApiException;
import client.user.IUserManagementClient;
import entity.User;

import javax.swing.*;
import java.awt.*;

public class UserProfileDialog extends JDialog {
    private final IUserManagementClient userManagementClient;
    private User currentUser;

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JButton saveButton;
    private JButton cancelButton;

    private JComboBox<String> genderComboBox;
    private JTextField ageField;

    public UserProfileDialog(JFrame parent, User user) {
        super(parent, "个人信息管理", true);
        this.userManagementClient = ApiClientFactory.getUserManagementClient(); // 使用新的ApiClientFactory
        this.currentUser = user;

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadUserData();
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        usernameField.setEditable(false); // 用户名不可编辑

        emailField = new JTextField(20);

        String[] genders = {"男", "女", "保密"};
        genderComboBox = new JComboBox<>(genders);

        ageField = new JTextField(10);

        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        saveButton = new JButton("保存");
        saveButton.addActionListener(_ -> saveUserData());

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(_ -> dispose());
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(usernameField, gbc);

        // 邮箱
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(emailField, gbc);

        // 性别
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(genderComboBox, gbc);

        // 年龄
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("年龄:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(ageField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(passwordField, gbc);

        // 确认密码
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
        usernameField.setText(currentUser.getName()); // 使用 getName()
        emailField.setText(currentUser.getEmail());
        if (currentUser.getGender() != null) {
            genderComboBox.setSelectedItem(currentUser.getGender());
        }
        if (currentUser.getAge() != null) {
            ageField.setText(currentUser.getAge().toString());
        }
    }

    private void saveUserData() {
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

        // 创建一个User对象来承载更新的信息
        User userToUpdate = new User();
        userToUpdate.setId(currentUser.getId());
        userToUpdate.setName(currentUser.getName()); // 用户名不能修改
        userToUpdate.setEmail(email);
        userToUpdate.setGender(gender);
        userToUpdate.setAge(age);
        // 如果输入了新密码，也设置它（需要后端API支持密码更新）
        if (!password.isEmpty()) {
            userToUpdate.setPassword(password);
        }

        saveButton.setEnabled(false);
        saveButton.setText("保存中...");

        // 使用SwingWorker执行更新操作
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws ApiException {
                // 调用新的API客户端方法
                return userManagementClient.updateMyProfile(userToUpdate);
            }

            @Override
            protected void done() {
                try {
                    User updatedUser = get();
                    JOptionPane.showMessageDialog(UserProfileDialog.this,
                            "用户信息更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                    // 更新本地的currentUser对象
                    currentUser = updatedUser;

                    // 如果修改了密码，单独提示（因为updateMyProfile可能不处理密码）
                    if (!password.isEmpty()) {
                        JOptionPane.showMessageDialog(UserProfileDialog.this,
                                "密码更新功能需要后端API专门支持。\n请确认后端是否已实现密码修改接口。", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }

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