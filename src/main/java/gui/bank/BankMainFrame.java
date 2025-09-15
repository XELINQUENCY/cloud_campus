package gui.bank;

import client.ApiClientFactory;
import client.bank.BankClient;
import client.bank.IBankClientSrv;
import entity.bank.BankAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class BankMainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton queryTransactionsButton;
    private JButton createAccountButton;
    private JTable accountsTable;
    private JButton toggleFullscreenButton;
    private boolean isFullscreen = false;
    private JLabel lastUpdateLabel;
    private final Runnable onLogoutCallback;
    private final IBankClientSrv bankClientSrv;

    /**
     * 【修改】构造函数不再直接接收 IBankClientSrv 实例。
     * 它现在通过 ApiClientFactory 自动获取网络客户端。
     * @param onLogoutCallback 当用户退出登录时要执行的回调操作。
     */
    public BankMainFrame(Runnable onLogoutCallback) {
        this.onLogoutCallback = onLogoutCallback;
        this.bankClientSrv = ApiClientFactory.getBankClient(); // 从工厂获取
        initComponents();
        updateWelcomeLabel();
        updateAccountsTable();
    }

    /**
     * 【新增】一个辅助方法，用于安全地从BankClient获取当前用户名并更新界面。
     */
    private void updateWelcomeLabel() {
        if (bankClientSrv instanceof BankClient) {
            String userId = ((BankClient) bankClientSrv).getCurrentUserId();
            welcomeLabel.setText("欢迎, " + userId);
        } else {
            welcomeLabel.setText("欢迎");
        }
    }

    // --- 未修改的纯UI方法 (已省略内部实现) ---
    private void initComponents() {
        setTitle("校园银行 - 账户管理");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // 使用现代外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部面板 - 欢迎信息和退出按钮
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        BankClient clientSrv = (BankClient) bankClientSrv;
        String userId = clientSrv.getCurrentUserId();

        welcomeLabel = new JLabel("欢迎, " + userId);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(60, 70, 85));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        // 顶部按钮面板
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topButtonPanel.setOpaque(false);

        // 全屏切换按钮
        toggleFullscreenButton = new JButton("全屏");
        toggleFullscreenButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        toggleFullscreenButton.setBackground(new Color(240, 240, 240));
        toggleFullscreenButton.setForeground(new Color(80, 90, 100));
        toggleFullscreenButton.setFocusPainted(false);
        toggleFullscreenButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // 退出按钮
        logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logoutButton.setBackground(new Color(239, 68, 68));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        topButtonPanel.add(toggleFullscreenButton);
        topButtonPanel.add(logoutButton);
        topPanel.add(topButtonPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(new Color(245, 247, 250));

        // 左侧功能面板
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(255, 255, 255));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        leftPanel.setPreferredSize(new Dimension(220, 0));

        // 功能按钮 - 添加创建新账户按钮
        depositButton = createMenuButton("存款", new Color(59, 130, 246));
        withdrawButton = createMenuButton("取款", new Color(139, 92, 246));
        transferButton = createMenuButton("转账", new Color(16, 185, 129));
        queryTransactionsButton = createMenuButton("交易记录", new Color(245, 158, 11));
        createAccountButton = createMenuButton("创建新账户", new Color(236, 72, 153)); // 替换为创建账户按钮

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(depositButton);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(withdrawButton);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(transferButton);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(queryTransactionsButton);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(createAccountButton); // 添加创建账户按钮
        leftPanel.add(Box.createVerticalGlue());

        contentPanel.add(leftPanel, BorderLayout.WEST);

        // 中央内容面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(255, 255, 255));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // 账户信息标题
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel tableTitle = new JLabel("我的账户");
        tableTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        tableTitle.setForeground(new Color(60, 70, 85));
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        //将lastUpdateLabel从局部变量改为成员变量
        lastUpdateLabel = new JLabel("最后更新: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        lastUpdateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lastUpdateLabel.setForeground(new Color(150, 150, 150));
        tableHeaderPanel.add(lastUpdateLabel, BorderLayout.EAST);

        centerPanel.add(tableHeaderPanel, BorderLayout.NORTH);

        // 账户信息表格
        String[] columnNames = {"账户ID", "余额", "状态", "开户时间"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Double.class;
                return String.class;
            }
        };

        accountsTable = new JTable(model);
        accountsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        accountsTable.setRowHeight(35);
        accountsTable.setIntercellSpacing(new Dimension(0, 5));
        accountsTable.setShowGrid(false);
        accountsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        accountsTable.getTableHeader().setBackground(new Color(249, 250, 251));
        accountsTable.getTableHeader().setForeground(new Color(100, 110, 120));
        accountsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));
        accountsTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // 设置表格渲染器，美化表格外观
        accountsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : new Color(255, 255, 255));

                if (column == 1) { // 余额列
                    double balance = Double.parseDouble(value.toString());
                    if (balance < 0) {
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

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(255, 255, 255));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel statusLabel = new JLabel("系统运行正常");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(150, 150, 150));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JLabel copyrightLabel = new JLabel("© 2023 校园综合服务平台 - 银行模块 v2.0");
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        copyrightLabel.setForeground(new Color(150, 150, 150));
        statusPanel.add(copyrightLabel, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 添加事件监听器
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depositActionPerformed();
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                withdrawActionPerformed();
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transferActionPerformed();
            }
        });

        queryTransactionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryTransactionsActionPerformed();
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAccountActionPerformed();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutActionPerformed();
            }
        });

        toggleFullscreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });
    }

    private JButton createMenuButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(75, 85, 99));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 50));

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(249, 250, 251));
                button.setForeground(color);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(75, 85, 99));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
        });

        return button;
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            // 进入全屏
            dispose();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            toggleFullscreenButton.setText("退出全屏");
            isFullscreen = true;
        } else {
            // 退出全屏
            dispose();
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            setSize(1000, 700);
            setLocationRelativeTo(null);
            toggleFullscreenButton.setText("全屏");
            isFullscreen = false;
        }
        setVisible(true);
    }

    private void logoutActionPerformed() {
        int result = JOptionPane.showConfirmDialog(this, "确定要登出吗?", "确认", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            dispose(); // 关闭当前窗口
            if (onLogoutCallback != null) {
                onLogoutCallback.run(); // 执行回调，返回登录界面
            }}
    }

    // --- 已修改为适配网络通信的事件处理方法 ---

    private void depositActionPerformed() {
        // 【修改】Dialogs现在使用无参构造函数，它们内部会自己从工厂获取服务
        DepositDialog dialog = new DepositDialog(this);
        dialog.setVisible(true);
        updateAccountsTable(); // 操作完成后刷新主界面表格
    }

    private void withdrawActionPerformed() {
        WithdrawDialog dialog = new WithdrawDialog(this);
        dialog.setVisible(true);
        updateAccountsTable();
    }

    private void transferActionPerformed() {
        TransferDialog dialog = new TransferDialog(this);
        dialog.setVisible(true);
        updateAccountsTable();
    }

    private void queryTransactionsActionPerformed() {
        TransactionQueryDialog dialog = new TransactionQueryDialog(this);
        dialog.setVisible(true);
    }

    private void createAccountActionPerformed() {
        int result = JOptionPane.showConfirmDialog(
                this, "确定要创建新的银行账户吗？", "创建新账户",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        // 【修改】使用 SwingWorker 异步执行创建账户的网络请求
        createAccountButton.setEnabled(false); // 禁用按钮防止重复点击
        new SwingWorker<BankAccount, Void>() {
            @Override
            protected BankAccount doInBackground() throws Exception {
                // 在后台线程调用网络客户端的方法
                return bankClientSrv.createAccount();
            }

            @Override
            protected void done() {
                try {
                    BankAccount newAccount = get(); // 获取网络请求的结果
                    if (newAccount != null) {
                        JOptionPane.showMessageDialog(
                                BankMainFrame.this, "账户创建成功！\n账户ID: " + newAccount.getAccountId(),
                                "成功", JOptionPane.INFORMATION_MESSAGE
                        );
                        updateAccountsTable(); // 成功后刷新账户列表
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(BankMainFrame.this, "账户创建失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    createAccountButton.setEnabled(true); // 恢复按钮
                }
            }
        }.execute();
    }

    public void updateAccountsTable() {
        // 【修改】使用 SwingWorker 异步加载账户列表
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
                        DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
                        model.setRowCount(0); // 清空旧数据
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        for (BankAccount account : accounts) {
                            model.addRow(new Object[]{
                                    account.getAccountId(),
                                    account.getBalance(),
                                    account.getStatus(),
                                    formatter.format(account.getCreateTime())
                            });
                        }
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(BankMainFrame.this, "加载账户列表失败: " + cause.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    lastUpdateLabel.setText("最后更新: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));
                }
            }
        }.execute();
    }
}

