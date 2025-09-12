package gui.user;

import client.library.IUserClientSrv;
import entity.library.Reservation;
import entity.User;
import view.ReservationView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 我的预约面板 (重构版)
 * 显示用户的有效预约，并提供取消操作。
 */
public class MyReservationsPanel extends JPanel {

    private final User currentUser;
    private final IUserClientSrv userSrv;
    private JTable reservationsTable;
    private ReservationTableModel tableModel;
    private JButton refreshButton;

    public MyReservationsPanel(User user, IUserClientSrv userSrv) {
        this.currentUser = user;
        this.userSrv = userSrv;

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
                // 使用 String 类型的用户ID
                return userSrv.getMyReservations(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    tableModel.setReservations(get());
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(MyReservationsPanel.this, "加载预约记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
                    // 状态字段现在是 String
                    switch (res.getStatus()) {
                        case "waiting": return "等待中";
                        case "available": return "可借阅";
                        default: return res.getStatus();
                    }
                case 4: return "取消预约";
                default: return null;
            }
        }
        @Override public boolean isCellEditable(int row, int col) { return col == 4; }
        public Reservation getReservationAt(int row) { return reservations.get(row).getReservation(); }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            button.setText((value == null) ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            Reservation res = tableModel.getReservationAt(selectedRow);

            int choice = JOptionPane.showConfirmDialog(MyReservationsPanel.this,
                    "您确定要取消对《" + tableModel.getValueAt(selectedRow, 0) + "》的预约吗？",
                    "取消确认", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return userSrv.cancelReservation(res.getReservationId());
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
            return super.getCellEditorValue();
        }
    }

    static class ButtonColumnRenderer extends JButton implements TableCellRenderer {
        public ButtonColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(new Color(220, 220, 220));
            setForeground(Color.BLACK);
            return this;
        }
    }
}