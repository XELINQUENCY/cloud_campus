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

@SuppressWarnings("serial")
public class DepositDialog extends JDialog {

    private JComboBox<String> accountComboBox;
    private JTextField amountField;
    private JButton confirmButton;
    private JButton cancelButton;
    private final IBankClientSrv bankClientSrv;

    /**
     * 【修改】构造函数不再接收 IBankClientSrv 实例。
     */
    public DepositDialog(JFrame parent) {
        super(parent, "存款", true);
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
        updateAccountComboBox();
    }

    // --- 未修改的纯UI方法 (已省略内部实现) ---
    private void initComponents() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("存款操作");
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

        // 账户选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel accountLabel = new JLabel("选择账户:");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountLabel.setForeground(Color.BLACK);
        formPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        accountComboBox = new JComboBox<>();
        accountComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountComboBox.setPreferredSize(new Dimension(200, 35));
        formPanel.add(accountComboBox, gbc);

        // 金额输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel amountLabel = new JLabel("存款金额:");
        amountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        amountLabel.setForeground(Color.BLACK);
        formPanel.add(amountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        amountField = new JTextField();
        amountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        amountField.setForeground(Color.BLACK);
        amountField.setPreferredSize(new Dimension(200, 35));
        formPanel.add(amountField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        confirmButton = new JButton("确认存款");
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
    private void updateAccountComboBox() {
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
                        accountComboBox.removeAllItems();
                        for (BankAccount account : accounts) {
                            accountComboBox.addItem(account.getAccountId());
                        }
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(DepositDialog.this, "加载账户列表失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void confirmActionPerformed() {
        String accountId = (String) accountComboBox.getSelectedItem();
        String amountText = amountField.getText();

        if (accountId == null) {
            JOptionPane.showMessageDialog(this, "请先选择账户", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "金额不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "存款金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        confirmButton.setEnabled(false);

        // 【修改】使用 SwingWorker 异步执行存款的网络请求
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankClientSrv.deposit(accountId, amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(DepositDialog.this, "存款成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(DepositDialog.this, "存款失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    confirmButton.setEnabled(true);
                }
            }
        }.execute();
    }
}

