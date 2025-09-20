package gui.library.user;

import client.ApiClientFactory;
import client.library.LibraryClient;
import entity.library.Reservation;
import entity.User;
import view.ReservationView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 我的预约面板
 * 显示用户的有效预约，并提供取消或借阅操作。
 */
public class MyReservationsPanel extends JPanel {

    private final User currentUser;
    private final LibraryClient libraryClient;
    private JTable reservationsTable;
    private ReservationTableModel tableModel;
    private JButton refreshButton;

    public MyReservationsPanel(User user) {
        this.currentUser = user;
        this.libraryClient = ApiClientFactory.getLibraryClient();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        refreshData();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("刷新列表");
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new ReservationTableModel();
        reservationsTable = new JTable(tableModel);

        reservationsTable.setRowHeight(30);
        reservationsTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        reservationsTable.setDefaultRenderer(String.class, centerRenderer);

        int actionColIndex = 4;
        reservationsTable.getColumnModel().getColumn(actionColIndex).setCellRenderer(new ButtonColumnRenderer());
        reservationsTable.getColumnModel().getColumn(actionColIndex).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);
    }

    public void refreshData() {
        refreshButton.setEnabled(false);
        new SwingWorker<List<ReservationView>, Void>() {
            @Override
            protected List<ReservationView> doInBackground() throws Exception {
                return libraryClient.getMyReservations(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    tableModel.setReservations(get());
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(MyReservationsPanel.this, "加载预约记录失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    static class ReservationTableModel extends AbstractTableModel {
        private final String[] columnNames = {"书名", "预约日期", "失效日期", "状态", "操作"};
        private List<ReservationView> reservations = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public void setReservations(List<ReservationView> reservations) {
            this.reservations = reservations;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return reservations.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ReservationView resView = reservations.get(row);
            Reservation res = resView.getReservation();
            switch (col) {
                case 0: return resView.getBookTitle();
                case 1: return dateFormat.format(res.getReservationDate());
                case 2: return res.getExpirationDate() == null ? "—" : dateFormat.format(res.getExpirationDate());
                case 3:
                    return switch (res.getStatus()) {
                        case "waiting" -> "等待中";
                        case "available" -> "可借阅";
                        default -> res.getStatus();
                    };
                case 4:
                    // 根据状态动态显示按钮文本
                    if (res.getStatus().equals("available")) {
                        return "借 阅";
                    }
                    return "取消预约";
                default: return null;
            }
        }
        @Override public boolean isCellEditable(int row, int col) { return col == 4; }
        public ReservationView getReservationViewAt(int row) { return reservations.get(row); }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;
        private String actionLabel;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            this.actionLabel = (value == null) ? "" : value.toString();
            button.setText(actionLabel);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            ReservationView resView = tableModel.getReservationViewAt(selectedRow);
            Reservation res = resView.getReservation();

            // 根据按钮文本执行不同操作
            if ("借 阅".equals(actionLabel)) {
                handleBorrowAction(res);
            } else { // "取消预约"
                handleCancelAction(res, resView.getBookTitle());
            }
            return actionLabel;
        }

        private void handleBorrowAction(Reservation res) {
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return libraryClient.borrowBook(currentUser.getId(), res.getBookId());
                }
                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MyReservationsPanel.this, result);
                        refreshData(); // 成功后刷新列表
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyReservationsPanel.this, "借阅失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }

        private void handleCancelAction(Reservation res, String bookTitle) {
            int choice = JOptionPane.showConfirmDialog(MyReservationsPanel.this,
                    "您确定要取消对《" + bookTitle + "》的预约吗？",
                    "取消确认", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return libraryClient.cancelReservation(res.getReservationId());
                    }
                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            JOptionPane.showMessageDialog(MyReservationsPanel.this, result);
                            refreshData();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(MyReservationsPanel.this, "取消预约失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        }
    }

    static class ButtonColumnRenderer extends JButton implements TableCellRenderer {
        public ButtonColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());

            ReservationTableModel model = (ReservationTableModel) table.getModel();
            Reservation res = model.getReservationViewAt(row).getReservation();

            // 根据状态渲染不同颜色的按钮
            if ("available".equals(res.getStatus())) {
                setBackground(new Color(76, 175, 80));
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(220, 220, 220));
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
