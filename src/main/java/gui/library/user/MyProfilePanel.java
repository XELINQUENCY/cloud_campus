package gui.library.user;

import client.ApiClientFactory;
import client.library.LibraryClient;
import entity.library.LibraryProfile;
import entity.User;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;

/**
 * 个人信息面板 (重构版)
 * 显示用户的基本信息和图书馆罚款状况，并提供支付罚款的功能。
 */
public class MyProfilePanel extends JPanel {

    private final User currentUser;
    private LibraryProfile libraryProfile;
    private final LibraryClient libraryClient;

    // --- UI 组件 ---
    private JLabel userIdLabel;
    private JLabel usernameLabel;
    private JLabel fineAmountLabel;
    private JButton payFineButton;
    private JButton refreshButton;

    public MyProfilePanel(User user) {
        this.currentUser = user;
        this.libraryClient = ApiClientFactory.getLibraryClient();
        this.libraryProfile = null;

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(10, 20));

        initUI();
        userIdLabel.setText(currentUser.getId());
        usernameLabel.setText(currentUser.getName());
        refreshData();
    }

    private void initUI() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("个人信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.BOLD, 16);
        Font valueFont = new Font("微软雅黑", Font.PLAIN, 16);

        userIdLabel = new JLabel();
        userIdLabel.setFont(valueFont);
        usernameLabel = new JLabel();
        usernameLabel.setFont(valueFont);
        fineAmountLabel = new JLabel();
        fineAmountLabel.setFont(valueFont);

        gbc.gridx = 0; gbc.gridy = 0; JLabel idTitle = new JLabel("用户ID:"); idTitle.setFont(labelFont); infoPanel.add(idTitle, gbc);
        gbc.gridx = 1; gbc.gridy = 0; infoPanel.add(userIdLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; JLabel userTitle = new JLabel("用户名:"); userTitle.setFont(labelFont); infoPanel.add(userTitle, gbc);
        gbc.gridx = 1; gbc.gridy = 1; infoPanel.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; JLabel fineTitle = new JLabel("图书馆欠费:"); fineTitle.setFont(labelFont); infoPanel.add(fineTitle, gbc);
        gbc.gridx = 1; gbc.gridy = 2; infoPanel.add(fineAmountLabel, gbc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        payFineButton = new JButton("支付罚款");
        payFineButton.setFont(labelFont);
        refreshButton = new JButton("刷新状态");
        refreshButton.setFont(labelFont);

        payFineButton.addActionListener(e -> handlePayFine());
        refreshButton.addActionListener(e -> refreshData());

        actionPanel.add(payFineButton);
        actionPanel.add(refreshButton);

        add(infoPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void updateProfileView() {
        if (libraryProfile != null) {
            double fine = libraryProfile.getFineAmount();
            BigDecimal fineDecimal = BigDecimal.valueOf(fine).setScale(2, RoundingMode.HALF_UP);
            fineAmountLabel.setText(fineDecimal + " 元");

            if (fine > 0) {
                fineAmountLabel.setForeground(Color.RED);
                payFineButton.setEnabled(true);
            } else {
                fineAmountLabel.setForeground(new Color(0, 128, 0));
                payFineButton.setEnabled(false);
            }
        } else {
            fineAmountLabel.setText("无法获取图书馆信息");
            fineAmountLabel.setForeground(Color.GRAY);
            payFineButton.setEnabled(false);
        }
    }

    private void refreshData() {
        refreshButton.setEnabled(false);
        payFineButton.setEnabled(false);

        new SwingWorker<LibraryProfile, Void>() {
            @Override
            protected LibraryProfile doInBackground() throws Exception {
                return libraryClient.refreshLibraryProfile(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    LibraryProfile updatedProfile = get();
                    if (updatedProfile != null) {
                        libraryProfile = updatedProfile;
                        updateProfileView();
                        JOptionPane.showMessageDialog(MyProfilePanel.this, "图书馆档案已更新。");
                    } else {
                        JOptionPane.showMessageDialog(MyProfilePanel.this, "刷新失败，无法获取图书馆档案。", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this, "刷新时发生错误: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                    if(libraryProfile != null) {
                        payFineButton.setEnabled(libraryProfile.getFineAmount() > 0);
                    }
                }
            }
        }.execute();
    }

    private void handlePayFine() {
        if (libraryProfile == null || libraryProfile.getFineAmount() <= 0) return;
        double currentFine = libraryProfile.getFineAmount();
        String amountStr = JOptionPane.showInputDialog(this, "您当前欠费 " + currentFine + " 元，请输入支付金额:", "支付罚款", JOptionPane.PLAIN_MESSAGE);

        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amountToPay = Double.parseDouble(amountStr);
            if (amountToPay <= 0 || amountToPay > currentFine) {
                JOptionPane.showMessageDialog(this, "支付金额必须大于0且不能超过所欠总额！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return libraryClient.payFine(currentUser.getId(), amountToPay);
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MyProfilePanel.this, result);
                        refreshData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyProfilePanel.this, "支付失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字金额！", "输入错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
