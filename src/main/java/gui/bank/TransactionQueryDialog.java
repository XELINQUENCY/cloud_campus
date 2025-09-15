package gui.bank;

import client.ApiClientFactory;
import client.bank.BankClient;
import client.bank.IBankClientSrv;
import entity.bank.BankAccount;
import entity.bank.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("serial")
public class TransactionQueryDialog extends JDialog {
    private JComboBox<String> accountComboBox;
    private JButton queryButton;
    private JTable transactionTable;
    private JButton closeButton;

    private final IBankClientSrv bankClientSrv;

    /**
     * 【修改】构造函数不再接收 IBankClientSrv 实例。
     */
    public TransactionQueryDialog(JFrame parent) {
        super(parent, "交易记录查询", true);
        this.bankClientSrv = ApiClientFactory.getBankClient();
        initComponents();
        updateAccountComboBox();
    }

    // --- 未修改的纯UI方法 (已省略内部实现) ---
    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("交易记录查询");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(59, 130, 246));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 查询条件面板
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        queryPanel.setBackground(Color.WHITE);
        queryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel accountLabel = new JLabel("选择账户:");
        accountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryPanel.add(accountLabel);

        accountComboBox = new JComboBox<>();
        accountComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountComboBox.setPreferredSize(new Dimension(150, 30));
        queryPanel.add(accountComboBox);

        queryButton = new JButton("查询");
        queryButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryButton.setBackground(new Color(59, 130, 246));
        queryButton.setForeground(Color.BLACK);
        queryButton.setFocusPainted(false);
        queryButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        queryPanel.add(queryButton);

        mainPanel.add(queryPanel, BorderLayout.NORTH);

        // 交易记录表格
        String[] columnNames = {"交易ID", "转出账户", "转入账户", "金额", "类型", "时间", "备注"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class;
                return String.class;
            }
        };

        transactionTable = new JTable(model);
        transactionTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        transactionTable.setRowHeight(30);
        transactionTable.setShowGrid(false);
        transactionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(new Color(249, 250, 251));
        transactionTable.getTableHeader().setForeground(new Color(100, 110, 120));
        transactionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));
        transactionTable.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // 设置表格渲染器
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : new Color(255, 255, 255));

                if (column == 3) { // 金额列
                    double amount = Double.parseDouble(value.toString());
                    if (amount < 0) {
                        c.setForeground(new Color(239, 68, 68));
                    } else {
                        c.setForeground(new Color(16, 185, 129));
                    }
                    setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    c.setForeground(new Color(75, 85, 99));
                    setHorizontalAlignment(JLabel.LEFT);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        closeButton.setBackground(new Color(150, 150, 150));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 添加事件监听器
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryActionPerformed();
            }
        });

        closeButton.addActionListener(new ActionListener() {
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
                    JOptionPane.showMessageDialog(TransactionQueryDialog.this, "加载账户列表失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void queryActionPerformed() {
        String accountId = (String) accountComboBox.getSelectedItem();
        if (accountId == null) {
            JOptionPane.showMessageDialog(this, "请先选择账户", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        queryButton.setEnabled(false);

        // 【修改】使用 SwingWorker 异步执行查询交易记录的网络请求
        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return bankClientSrv.getTransactions(accountId, null, null);
            }

            @Override
            protected void done() {
                try {
                    updateTransactionsTable(get()); // 使用获取到的数据更新表格
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TransactionQueryDialog.this, "查询失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    queryButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void updateTransactionsTable(List<Transaction> transactions) {
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (transactions == null) return;

        for (Transaction transaction : transactions) {
            model.addRow(new Object[]{
                    transaction.getTransactionId(),
                    transaction.getFromAccount(),
                    transaction.getToAccount(),
                    transaction.getAmount(),
                    transaction.getType(),
                    formatter.format(transaction.getTimestamp()),
                    transaction.getMemo()
            });
        }
    }
}

