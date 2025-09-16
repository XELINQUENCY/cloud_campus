package gui.shop;

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

public class ShopBankPaymentDialog extends JDialog {
    private JComboBox<String> accountComboBox;
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private final IBankClientSrv bankClientSrv;
    private final String shopAccountId; // 商店的固定银行账户
    private final BigDecimal paymentAmount;
    private final PaymentCallback paymentCallback;

    public interface PaymentCallback {
        void onPaymentSuccess(BigDecimal amount);
        void onPaymentFailure(String errorMessage);
    }

    public ShopBankPaymentDialog(JFrame parent, BigDecimal amount, PaymentCallback callback) {
        super(parent, "银行支付", true);
        this.paymentAmount = amount;
        this.paymentCallback = callback;
        this.bankClientSrv = ApiClientFactory.getBankClient();
        this.shopAccountId = ApiClientFactory.getShopClient().getCurrentUserId();
        
        initComponents();
        updateAccountComboBox();
    }

    private void initComponents() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("银行支付");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 信息面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(255, 248, 225));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel infoLabel = new JLabel("<html><center>支付金额: <b>" + paymentAmount + "</b> 元<br>收款方: <b>校园商店</b></center></html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // 表单面板
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
        JLabel accountLabel = new JLabel("支付账户:");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountLabel.setForeground(Color.BLACK);
        formPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        accountComboBox = new JComboBox<>();
        accountComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountComboBox.setPreferredSize(new Dimension(200, 35));
        formPanel.add(accountComboBox, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("支付密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 35));
        formPanel.add(passwordField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        confirmButton = new JButton("确认支付");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setBackground(new Color(16, 185, 129));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new Dimension(120, 35));

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.WHITE);
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
                if (paymentCallback != null) {
                    paymentCallback.onPaymentFailure("用户取消支付");
                }
            }
        });
    }

    private void updateAccountComboBox() {
        new SwingWorker<List<BankAccount>, Void>() {
            @Override
            protected List<BankAccount> doInBackground() throws Exception {
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
                            accountComboBox.addItem(account.getAccountId() + " (余额: " + account.getBalance() + ")");
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ShopBankPaymentDialog.this, 
                            "加载账户列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void confirmActionPerformed() {
        String selectedAccount = (String) accountComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        
        if (selectedAccount == null) {
            JOptionPane.showMessageDialog(this, "请选择支付账户", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入支付密码", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 从选项文本中提取账户ID
        String accountId = selectedAccount.split(" ")[0];
        
        confirmButton.setEnabled(false);
        
        // 执行转账操作
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankClientSrv.transfer(accountId, shopAccountId, paymentAmount, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ShopBankPaymentDialog.this, 
                                "支付成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        if (paymentCallback != null) {
                            paymentCallback.onPaymentSuccess(paymentAmount);
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ShopBankPaymentDialog.this, 
                            "支付失败: " + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                    if (paymentCallback != null) {
                        paymentCallback.onPaymentFailure(errorMsg);
                    }
                } finally {
                    confirmButton.setEnabled(true);
                }
            }
        }.execute();
    }
}