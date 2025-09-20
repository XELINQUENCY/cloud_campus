package gui;

import client.ApiClientFactory;
import client.ApiException;
import client.user.IUserManagementClient;
import entity.User;
import enums.UserRole;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class UserManagementDialog extends JDialog {
    private final IUserManagementClient userManagementClient;
    private JTable userTable;
    private UserTableModel tableModel;
    private JButton refreshButton;
    private JButton deleteButton;
    private JButton toggleStatusButton;
    private JButton changeRoleButton;
    private final Runnable onWindowClosedCallback;

    public UserManagementDialog(JFrame parent, Runnable onWindowClosedCallback) {
        super(parent, "用户管理", true);
        this.userManagementClient = ApiClientFactory.getUserManagementClient();
        this.onWindowClosedCallback = onWindowClosedCallback;

        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadUserData();

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
        tableModel = new UserTableModel();
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadUserData());

        deleteButton = new JButton("删除用户");
        deleteButton.addActionListener(e -> deleteSelectedUser());

        changeRoleButton = new JButton("更改角色");
        changeRoleButton.addActionListener(e -> changeUserRole());

        // toggleStatusButton 未在布局中使用，但在此处初始化以避免潜在的 NullPointerException
        toggleStatusButton = new JButton("切换状态");
    }

    private JScrollPane createTablePanel() {
        return new JScrollPane(userTable);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose()); // 点击时关闭对话框

        panel.add(refreshButton);
        panel.add(changeRoleButton);
        panel.add(deleteButton);
        panel.add(Box.createHorizontalStrut(20)); // 分隔符
        panel.add(closeButton);
        return panel;
    }

    private void setButtonsEnabled(boolean enabled) {
        refreshButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        changeRoleButton.setEnabled(enabled);
    }

    private void loadUserData() {
        setButtonsEnabled(false);
        refreshButton.setText("刷新中...");

        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws ApiException {
                return userManagementClient.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setUsers(users);
                } catch (Exception e) {
                    handleApiError(e, "加载用户数据失败");
                } finally {
                    setButtonsEnabled(true);
                    refreshButton.setText("刷新");
                }
            }
        }.execute();
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User selectedUser = tableModel.getUserAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除用户 '" + selectedUser.getName() + "' 吗?", "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setButtonsEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws ApiException {
                    userManagementClient.deleteUser(selectedUser.getId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // 检查是否有异常
                        JOptionPane.showMessageDialog(UserManagementDialog.this, "用户删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadUserData(); // 刷新数据
                    } catch (Exception e) {
                        handleApiError(e, "删除失败");
                    } finally {
                        setButtonsEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User selectedUser = tableModel.getUserAt(selectedRow);
        UserRole[] allRoles = UserRole.values();
        UserRole newRole = (UserRole) JOptionPane.showInputDialog(
                this, "请为用户 '" + selectedUser.getName() + "' 选择新角色:", "更改角色",
                JOptionPane.PLAIN_MESSAGE, null, allRoles, selectedUser.getUserRoles().stream().findFirst().orElse(null));

        if (newRole != null) {
            setButtonsEnabled(false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws ApiException {
                    userManagementClient.changeUserRoles(selectedUser.getId(), new HashSet<>(Collections.singletonList(newRole)));
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(UserManagementDialog.this, "角色更改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadUserData(); // 刷新
                    } catch (Exception e) {
                        handleApiError(e, "操作失败");
                    } finally {
                        setButtonsEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void handleApiError(Exception e, String title) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        JOptionPane.showMessageDialog(this, title + ": " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
    }

    private static class UserTableModel extends AbstractTableModel {
        private List<User> users = new ArrayList<>();
        private final String[] columnNames = {"ID", "用户名", "邮箱", "性别", "年龄", "角色", "创建时间", "最后登录"};
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public void setUsers(List<User> users) {
            this.users = (users != null) ? users : new ArrayList<>();
            fireTableDataChanged();
        }

        public User getUserAt(int row) {
            return users.get(row);
        }

        @Override public int getRowCount() { return users.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            User user = users.get(rowIndex);
            switch (columnIndex) {
                case 0: return user.getId();
                case 1: return user.getName();
                case 2: return user.getEmail();
                case 3: return user.getGender() != null ? user.getGender(): "未知";
                case 4: return user.getAge();
                case 5: return user.getUserRoles().stream().findFirst().map(UserRole::getDisplayName).orElse("无角色");
                case 6: return user.getCreatedDate() != null ? user.getCreatedDate().format(formatter) : "N/A";
                case 7: return user.getLastLogin() != null ? user.getLastLogin().format(formatter) : "N/A";
                default: return "";
            }
        }
    }
}