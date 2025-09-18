package gui.library.user;

import client.ApiClientFactory;
import client.library.LibraryClient;
import entity.User;
import entity.library.BorrowRecord;
import view.BorrowRecordView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 我的借阅面板 (重构版)
 * 显示用户的借阅历史，并提供还书和续借操作。
 */
public class MyBorrowsPanel extends JPanel {

    private final User currentUser;
    private final LibraryClient libraryClient;
    private JTable borrowsTable;
    private BorrowRecordTableModel tableModel;
    private JButton refreshButton;

    public MyBorrowsPanel(User user) {
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

        tableModel = new BorrowRecordTableModel();
        borrowsTable = new JTable(tableModel);

        borrowsTable.setRowHeight(30);
        borrowsTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        borrowsTable.setDefaultRenderer(String.class, centerRenderer);
        borrowsTable.setDefaultRenderer(Date.class, centerRenderer);

        int actionColIndex = 5;
        borrowsTable.getColumnModel().getColumn(actionColIndex).setCellRenderer(new ActionPanelRenderer());
        borrowsTable.getColumnModel().getColumn(actionColIndex).setCellEditor(new ActionPanelEditor(new JCheckBox()));

        add(new JScrollPane(borrowsTable), BorderLayout.CENTER);
    }

    public void refreshData() {
        refreshButton.setEnabled(false);

        new SwingWorker<List<BorrowRecordView>, Void>() {
            @Override
            protected List<BorrowRecordView> doInBackground() throws Exception {
                return libraryClient.getMyBorrowRecords(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    List<BorrowRecordView> records = get();
                    tableModel.setRecords(records);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(MyBorrowsPanel.this, "加载借阅记录失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    // =================================================================================
    // ==                          内部类：用于JTable的自定义组件                         ==
    // =================================================================================

    static class BorrowRecordTableModel extends AbstractTableModel {
        private final String[] columnNames = {"书名", "借阅日期", "应还日期", "归还日期", "状态", "操作"};
        private List<BorrowRecordView> records = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public void setRecords(List<BorrowRecordView> records) {
            this.records = records;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return records.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            BorrowRecordView recordView = records.get(row);
            BorrowRecord record = recordView.getBorrowRecord();
            switch (col) {
                case 0: return recordView.getBookTitle();
                case 1: return dateFormat.format(record.getBorrowDate());
                case 2: return dateFormat.format(record.getDueDate());
                case 3: return record.getReturnDate() == null ? "—" : dateFormat.format(record.getReturnDate());
                case 4:
                    if (record.getReturnDate() != null) return "已归还";
                    if (record.isOverdue() || new Date().after(record.getDueDate())) return "已逾期";
                    return "借阅中";
                case 5: return record;
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 5;
        }
    }

    static class ActionPanelRenderer implements TableCellRenderer {
        private final JPanel panel;
        private final JButton returnButton;
        private final JButton renewButton;

        public ActionPanelRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            returnButton = new JButton("还书");
            renewButton = new JButton("续借");
            panel.add(returnButton);
            panel.add(renewButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof BorrowRecord record) {
                boolean isReturned = record.getReturnDate() != null;
                returnButton.setVisible(!isReturned);
                renewButton.setVisible(!isReturned);
            }
            return panel;
        }
    }

    class ActionPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton returnButton;
        private final JButton renewButton;
        private BorrowRecord currentRecord;

        public ActionPanelEditor(JCheckBox checkBox) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            returnButton = new JButton("还书");
            renewButton = new JButton("续借");
            panel.add(returnButton);
            panel.add(renewButton);

            returnButton.addActionListener(e -> {
                fireEditingStopped();
                handleReturnAction();
            });

            renewButton.addActionListener(e -> {
                fireEditingStopped();
                handleRenewAction();
            });
        }

        private void handleReturnAction() {
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return libraryClient.returnBook(currentRecord.getCopyId());
                }
                @Override protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MyBorrowsPanel.this, result);
                        refreshData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyBorrowsPanel.this, "还书失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }

        private void handleRenewAction() {
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return libraryClient.renewBook(currentRecord.getRecordId());
                }
                @Override protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MyBorrowsPanel.this, result);
                        refreshData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyBorrowsPanel.this, "续借失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof BorrowRecord) {
                currentRecord = (BorrowRecord) value;
                boolean isReturned = currentRecord.getReturnDate() != null;
                returnButton.setVisible(!isReturned);
                renewButton.setVisible(!isReturned);
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentRecord;
        }
    }
}
