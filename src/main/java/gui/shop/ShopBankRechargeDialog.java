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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopBankRechargeDialog extends JDialog {
    private JComboBox<String> accountComboBox;
    private JLabel selectedAmountLabel;
    private JLabel bonusLabel;
    private JLabel totalLabel;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private final IBankClientSrv bankClientSrv;
    private final String shopAccountId; // 商店的固定银行账户
    private final RechargeCallback rechargeCallback;
    
    private BigDecimal selectedAmount = BigDecimal.ZERO;
    private BigDecimal bonusAmount = BigDecimal.ZERO;
    private final Map<BigDecimal, BigDecimal> bonusRules = new HashMap<>();
    
    // 充值金额按钮
    private JButton[] amountButtons;
    private final BigDecimal[] amounts = {
        new BigDecimal("10.0"),
        new BigDecimal("20.0"),
        new BigDecimal("50.0"),
        new BigDecimal("100.0"),
        new BigDecimal("200.0"),
        new BigDecimal("500.0")
    };

    public interface RechargeCallback {
        void onRechargeSuccess(BigDecimal amount, BigDecimal bonus);
        void onRechargeFailure(String errorMessage);
    }

    public ShopBankRechargeDialog(JFrame parent, RechargeCallback callback) {
        super(parent, "商店余额充值", true);
        this.rechargeCallback = callback;
        this.bankClientSrv = ApiClientFactory.getBankClient();
        this.shopAccountId = ApiClientFactory.getShopClient().getCurrentUserId();
        
        // 设置充值优惠规则
        bonusRules.put(new BigDecimal("100.0"), new BigDecimal("11.0"));
        bonusRules.put(new BigDecimal("200.0"), new BigDecimal("22.0"));
        bonusRules.put(new BigDecimal("500.0"), new BigDecimal("55.0"));
        
        initComponents();
        updateAccountComboBox();
    }

    private void initComponents() {
        setSize(500, 550);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("商店余额充值");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // 账户选择
        JLabel accountLabel = new JLabel("支付账户:");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountLabel.setForeground(Color.BLACK);
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(accountLabel);
        formPanel.add(Box.createVerticalStrut(5));

        accountComboBox = new JComboBox<>();
        accountComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        accountComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(accountComboBox);
        formPanel.add(Box.createVerticalStrut(20));

        // 金额选择标题
        JLabel amountTitleLabel = new JLabel("选择充值金额:");
        amountTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        amountTitleLabel.setForeground(Color.BLACK);
        amountTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(amountTitleLabel);
        formPanel.add(Box.createVerticalStrut(10));

        // 金额按钮面板
        JPanel amountButtonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        amountButtonPanel.setBackground(Color.WHITE);
        amountButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        amountButtons = new JButton[amounts.length];
        for (int i = 0; i < amounts.length; i++) {
            final BigDecimal amount = amounts[i];
            JButton button = new JButton(amount + "元");
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setBackground(new Color(240, 240, 240));
            button.setFocusPainted(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectAmount(amount);
                    // 重置所有按钮样式
                    for (JButton btn : amountButtons) {
                        btn.setBackground(new Color(240, 240, 240));
                        btn.setForeground(Color.BLACK);
                    }
                    // 设置选中按钮样式
                    button.setBackground(new Color(59, 130, 246));
                    button.setForeground(Color.WHITE);
                }
            });
            amountButtons[i] = button;
            amountButtonPanel.add(button);
        }
        
        formPanel.add(amountButtonPanel);
        formPanel.add(Box.createVerticalStrut(20));

        // 金额信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 248, 225));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        selectedAmountLabel = new JLabel("充值金额: 0.00元");
        selectedAmountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        selectedAmountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(selectedAmountLabel);
        
        bonusLabel = new JLabel("赠送金额: 0.00元");
        bonusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bonusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(bonusLabel);
        
        totalLabel = new JLabel("实际到账: 0.00元");
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(totalLabel);
        
        formPanel.add(infoPanel);
        formPanel.add(Box.createVerticalStrut(20));

        // 密码输入
        JLabel passwordLabel = new JLabel("支付密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.BLACK);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(20));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmButton = new JButton("确认充值");
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        confirmButton.setBackground(new Color(16, 185, 129));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new Dimension(120, 35));
        confirmButton.setEnabled(false); // 初始状态禁用

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(150, 150, 150));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(confirmButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());

        formPanel.add(buttonPanel);

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
                if (rechargeCallback != null) {
                    rechargeCallback.onRechargeFailure("用户取消充值");
                }
            }
        });
    }

    private void selectAmount(BigDecimal amount) {
        this.selectedAmount = amount;
        
        // 计算赠送金额
        this.bonusAmount = bonusRules.getOrDefault(amount, BigDecimal.ZERO);
        
        // 更新显示
        selectedAmountLabel.setText("充值金额: " + selectedAmount + "元");
        bonusLabel.setText("赠送金额: " + bonusAmount + "元");
        totalLabel.setText("实际到账: " + selectedAmount.add(bonusAmount) + "元");
        
        // 启用确认按钮
        confirmButton.setEnabled(true);
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
                    JOptionPane.showMessageDialog(ShopBankRechargeDialog.this, 
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
        
        if (selectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "请选择充值金额", "错误", JOptionPane.ERROR_MESSAGE);
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
                return bankClientSrv.transfer(accountId, shopAccountId, selectedAmount, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ShopBankRechargeDialog.this, 
                                "充值成功！您获得了" + bonusAmount + "元赠送金额", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        if (rechargeCallback != null) {
                            rechargeCallback.onRechargeSuccess(selectedAmount, bonusAmount);
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ShopBankRechargeDialog.this, 
                            "充值失败: " + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                    if (rechargeCallback != null) {
                        rechargeCallback.onRechargeFailure(errorMsg);
                    }
                } finally {
                    confirmButton.setEnabled(true);
                }
            }
        }.execute();
    }
}