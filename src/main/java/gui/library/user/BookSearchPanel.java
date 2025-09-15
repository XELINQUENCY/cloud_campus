package gui.library.user;

import client.ApiClientFactory;
import client.library.LibraryClient;
import entity.library.Book;
import entity.library.Category;
import entity.User;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * 书籍查询面板 (重构版)
 * 构造函数简化，服务通过ApiClientFactory获取。
 */
public class BookSearchPanel extends JPanel {

    private final User currentUser;
    private final LibraryClient libraryClient;

    private JTextField titleField, authorField, publisherField;
    private JComboBox<Object> categoryComboBox;
    private JButton searchButton;
    private JTable resultTable;
    private BookTableModel tableModel;
    private JLabel statusLabel;

    public BookSearchPanel(User user) {
        this.currentUser = user;
        this.libraryClient = ApiClientFactory.getLibraryClient();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initSearchControls();
        initResultTable();
        initListeners();

        loadCategories();
    }

    private void initSearchControls() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索条件"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        titleField = new JTextField(15);
        authorField = new JTextField(15);
        publisherField = new JTextField(15);
        categoryComboBox = new JComboBox<>();
        searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 0; searchPanel.add(new JLabel("书名:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; searchPanel.add(titleField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; searchPanel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; searchPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; searchPanel.add(new JLabel("出版社:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; searchPanel.add(publisherField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; searchPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; searchPanel.add(categoryComboBox, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2; gbc.fill = GridBagConstraints.VERTICAL;
        searchPanel.add(searchButton, gbc);

        statusLabel = new JLabel("欢迎使用图书查询系统。");

        add(searchPanel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void initResultTable() {
        tableModel = new BookTableModel();
        resultTable = new JTable(tableModel);

        resultTable.setRowHeight(30);
        resultTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultTable.setDefaultRenderer(Object.class, centerRenderer);

        int actionColumnIndex = 5;
        resultTable.getColumnModel().getColumn(actionColumnIndex).setCellRenderer(new ButtonColumnRenderer());
        resultTable.getColumnModel().getColumn(actionColumnIndex).setCellEditor(new ButtonColumnEditor(new JCheckBox()));

        resultTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initListeners() {
        searchButton.addActionListener(e -> performSearch());
    }

    private void loadCategories() {
        final Vector<Object> categories = new Vector<>();
        categories.add("所有分类");

        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return libraryClient.getAllCategories();
            }

            @Override
            protected void done() {
                try {
                    categories.addAll(get());
                    categoryComboBox.setModel(new DefaultComboBoxModel<>(categories));
                } catch (InterruptedException | ExecutionException e) {
                    statusLabel.setText("错误: 无法加载书籍分类。");
                }
            }
        }.execute();
    }

    private void performSearch() {
        final String title = titleField.getText();
        final String author = authorField.getText();
        final String publisher = publisherField.getText();
        final Object selectedCategory = categoryComboBox.getSelectedItem();
        final Integer categoryId = (selectedCategory instanceof Category) ? ((Category) selectedCategory).getCategoryId() : null;

        searchButton.setEnabled(false);
        statusLabel.setText("正在搜索，请稍候...");

        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return libraryClient.searchBooks(title, author, publisher, categoryId);
            }

            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    tableModel.setBooks(books);
                    statusLabel.setText("搜索完成，共找到 " + books.size() + " 条结果。");
                } catch (Exception e) {
                    statusLabel.setText("错误: 搜索时发生异常。");
                } finally {
                    searchButton.setEnabled(true);
                }
            }
        }.execute();
    }

    // --- 内部类 ---

    class ButtonColumnEditor extends DefaultCellEditor {
        // ... (代码保持不变，但内部的userSrv调用需改为libraryClient) ...
        private JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;

        public ButtonColumnEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                Book book = tableModel.getBookAt(selectedRow);
                String actionType = button.getText();

                statusLabel.setText("正在处理您的请求...");

                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        if ("借 阅".equals(actionType)) {
                            // 使用统一的libraryClient
                            return libraryClient.borrowBook(currentUser.getId(), book.getBookId());
                        } else {
                            // 使用统一的libraryClient
                            return libraryClient.reserveBook(currentUser.getId(), book.getBookId());
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            JOptionPane.showMessageDialog(BookSearchPanel.this, result);
                            performSearch(); // 操作成功后刷新列表
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(BookSearchPanel.this, "操作失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
            isPushed = false;
            return label;
        }
    }

    // ... 其他内部类 (BookTableModel, ButtonColumnRenderer) 保持不变 ...
    static class BookTableModel extends AbstractTableModel {
        private final String[] columnNames = {"书名", "作者", "出版社", "总数", "可借", "操作"};
        private List<Book> books = new ArrayList<>();

        public void setBooks(List<Book> books) {
            this.books = books;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return books.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Book book = books.get(rowIndex);
            switch (columnIndex) {
                case 0: return book.getTitle();
                case 1: return book.getAuthor();
                case 2: return book.getPublisher();
                case 3: return book.getTotalCopies();
                case 4: return book.getAvailableCopies();
                case 5: return book.getAvailableCopies() > 0 ? "借 阅" : "预 约";
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 5;
        }

        public Book getBookAt(int row) {
            return books.get(row);
        }
    }

    static class ButtonColumnRenderer extends JButton implements TableCellRenderer {
        public ButtonColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            BookTableModel model = (BookTableModel) table.getModel();
            Book book = model.getBookAt(row);
            if (book.getAvailableCopies() > 0) {
                setBackground(new Color(30, 144, 255));
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(255, 165, 0));
                setForeground(Color.WHITE);
            }
            return this;
        }
    }
}
