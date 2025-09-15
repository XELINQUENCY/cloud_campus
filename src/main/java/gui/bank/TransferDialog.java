package gui.bank;

import client.ApiClientFactory;
import client.bank.BankClient;
import client.bank.IBankClientSrv;
import entity.bank.BankAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

public class TransferDialog extends JDialog {

    private JComboBox<String> fromAccountComboBox;
    private JTextField toAccountField;
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private final IBankClientSrv bankClientSrv;

    /**
     * 【修改】构造函数不再接收 IBankClientSrv 实例。
     */
    public TransferDialog(JFrame parent) {
        super(parent, "转账", true);
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
        updateFromAccountComboBox();
    }

    // --- 未修改的纯UI方法 (已省略内部实现) ---
    private void initComponents() {
        setSize(500, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("转账操作");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板 - 使用GridBagLayout确保组件居中
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 15, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // 转出账户输入 - 改为下拉框
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel fromAccountLabel = new JLabel("转出账户:");
        fromAccountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        fromAccountLabel.setForeground(Color.BLACK);
        formPanel.add(fromAccountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        fromAccountComboBox = new JComboBox<>(); // 使用下拉框
        fromAccountComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        fromAccountComboBox.setForeground(Color.BLACK);
        fromAccountComboBox.setPreferredSize(new Dimension(200, 35));
        formPanel.add(fromAccountComboBox, gbc);

        // 转入账户输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel toAccountLabel = new JLabel("转入账户:");
        toAccountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        toAccountLabel.setForeground(Color.BLACK);
        formPanel.add(toAccountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        toAccountField = new JTextField();
        toAccountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        toAccountField.setForeground(Color.BLACK);
        toAccountField.setPreferredSize(new Dimension(200, 35));
        formPanel.add(toAccountField, gbc);

        // 金额输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel amountLabel = new JLabel("转账金额:");
        amountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        amountLabel.setForeground(Color.BLACK);
        formPanel.add(amountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        amountField = new JTextField();
        amountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        amountField.setForeground(Color.BLACK);
        amountField.setPreferredSize(new Dimension(200, 35));
        formPanel.add(amountField, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setForeground(Color.BLACK);
        passwordField.setPreferredSize(new Dimension(200, 35));
        formPanel.add(passwordField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        confirmButton = new JButton("确认转账");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setBackground(new Color(16, 185, 129));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new Dimension(120, 35));

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // 添加事件监听器
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmActionPerformed();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * 【修改】异步从服务器加载用户的银行账户列表。
     */
    private void updateFromAccountComboBox() {
        new SwingWorker<List<BankAccount>, Void>() {
            @Override
            protected List<BankAccount> doInBackground() throws Exception {
                // 需要一个方式来获取当前用户ID
                if (bankClientSrv instanceof BankClient) {
                    String userId = ((BankClient) bankClientSrv).getCurrentUserId();
                    return bankClientSrv.getUserAccounts(userId);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    List<BankAccount> accounts = get();
                    if (accounts != null) {
                        fromAccountComboBox.removeAllItems();
                        for (BankAccount account : accounts) {
                            fromAccountComboBox.addItem(account.getAccountId());
                        }
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TransferDialog.this, "加载账户列表失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void confirmActionPerformed() {
        String fromAccountId = (String) fromAccountComboBox.getSelectedItem();
        String toAccountId = toAccountField.getText();
        String amountText = amountField.getText();
        String password = new String(passwordField.getPassword());

        // ... (输入验证代码，例如检查空值、金额格式等，保持不变) ...
        if (fromAccountId == null || toAccountId.isEmpty() || amountText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // ... 其他验证 ...

        confirmButton.setEnabled(false);

        // 【修改】使用 SwingWorker 异步执行转账的网络请求
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                BigDecimal amount = new BigDecimal(amountText);
                return bankClientSrv.transfer(fromAccountId, toAccountId, amount, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(TransferDialog.this, "转账成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TransferDialog.this, "转账失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    confirmButton.setEnabled(true);
                }
            }
        }.execute();
    }
}

