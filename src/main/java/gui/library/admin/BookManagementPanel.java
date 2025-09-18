package gui.library.admin;

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
 * 书籍管理面板 (重构版)
 * 构造函数简化，服务通过ApiClientFactory获取。
 */
public class BookManagementPanel extends JPanel {

    private final User currentAdmin;
    // 将具体的服务接口替换为一个实现了所有接口的LibraryClient
    private final LibraryClient libraryClient;

    // --- UI 组件 ---
    private JTextField titleField, authorField;
    private JComboBox<Object> categoryComboBox;
    private JButton searchButton, refreshButton, addButton;
    private JTable bookTable;
    private AdminBookTableModel tableModel;

    public BookManagementPanel(User adminUser) {
        this.currentAdmin = adminUser;
        // 直接从工厂获取统一的客户端实例
        this.libraryClient = ApiClientFactory.getLibraryClient();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        loadCategories();
        refreshTableData(true); // 初始加载全部书籍
    }

    private void initUI() {
        // ... (initUI方法的内部代码保持不变) ...
        // --- 北部：搜索区 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("书籍查询"));
        titleField = new JTextField(15);
        authorField = new JTextField(15);
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setPreferredSize(new Dimension(120, (int)authorField.getPreferredSize().getHeight()));
        searchButton = new JButton("查询");

        searchPanel.add(new JLabel("书名:"));
        searchPanel.add(titleField);
        searchPanel.add(new JLabel("作者:"));
        searchPanel.add(authorField);
        searchPanel.add(new JLabel("分类:"));
        searchPanel.add(categoryComboBox);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // --- 中部：表格区 ---
        tableModel = new AdminBookTableModel();
        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);
        bookTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 设置居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bookTable.setDefaultRenderer(Object.class, centerRenderer);
        bookTable.setDefaultRenderer(Integer.class, centerRenderer);

        // 设置操作列的按钮渲染器和编辑器
        bookTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        bookTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // --- 南部：操作区 ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("添加新书");
        refreshButton = new JButton("刷新全表");
        actionPanel.add(addButton);
        actionPanel.add(refreshButton);
        add(actionPanel, BorderLayout.SOUTH);

        // --- 绑定事件 ---
        searchButton.addActionListener(e -> refreshTableData(false));
        refreshButton.addActionListener(e -> refreshTableData(true));
        addButton.addActionListener(e -> handleAddBook());
    }

    private void loadCategories() {
        final Vector<Object> categories = new Vector<>();
        categories.add("所有分类");

        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                // 使用统一的libraryClient
                return libraryClient.getAllCategories();
            }

            @Override
            protected void done() {
                try {
                    categories.addAll(get());
                    categoryComboBox.setModel(new DefaultComboBoxModel<>(categories));
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(BookManagementPanel.this, "加载分类失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshTableData(boolean showAll) {
        String title = showAll ? "" : titleField.getText();
        String author = showAll ? "" : authorField.getText();
        Object selected = categoryComboBox.getSelectedItem();
        Integer categoryId = (selected instanceof Category) ? ((Category) selected).getCategoryId() : null;

        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                // 使用统一的libraryClient
                return libraryClient.searchBooks(title, author, null, categoryId);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setBooks(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookManagementPanel.this, "加载书籍列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleAddBook() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        // BookEditDialog也应该被修改，不再需要传递Srv
        BookEditDialog dialog = new BookEditDialog(parentFrame, null);
        dialog.setVisible(true);

        Book newBook = dialog.getResultBook();
        if (newBook != null) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // 使用统一的libraryClient
                    return libraryClient.addBook(newBook);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(BookManagementPanel.this, "书籍添加成功！");
                            refreshTableData(true);
                        } else {
                            // addBook方法在失败时会抛出ApiException，所以这里一般不会执行到
                            JOptionPane.showMessageDialog(BookManagementPanel.this, "书籍添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BookManagementPanel.this, "添加书籍时发生异常: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // ... (内部类 AdminBookTableModel, ButtonRenderer, ButtonEditor 保持不变，但ButtonEditor内部调用adminSrv.updateBook需改为libraryClient.updateBook) ...
    static class AdminBookTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "书名", "作者", "ISBN", "总数", "可借", "操作"};
        private List<Book> books = new ArrayList<>();

        public void setBooks(List<Book> books) {
            this.books = books;
            fireTableDataChanged();
        }

        public Book getBookAt(int row) {
            return books.get(row);
        }

        @Override public int getRowCount() { return books.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }
        @Override public boolean isCellEditable(int row, int col) { return col == 6; }

        @Override
        public Object getValueAt(int row, int col) {
            Book b = books.get(row);
            switch (col) {
                case 0: return b.getBookId();
                case 1: return b.getTitle();
                case 2: return b.getAuthor();
                case 3: return b.getIsbn();
                case 4: return b.getTotalCopies();
                case 5: return b.getAvailableCopies();
                case 6: return "修改";
                default: return null;
            }
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            button.setText(value != null ? value.toString() : "");
            return button;
        }

        @Override public Object getCellEditorValue() {
            Book bookToEdit = tableModel.getBookAt(selectedRow);
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(BookManagementPanel.this);
            BookEditDialog dialog = new BookEditDialog(parentFrame, bookToEdit);
            dialog.setVisible(true);

            Book updatedBook = dialog.getResultBook();
            if (updatedBook != null) {
                new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() throws Exception {
                        // 使用统一的libraryClient
                        return libraryClient.updateBook(updatedBook);
                    }

                    @Override protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(BookManagementPanel.this, "书籍信息修改成功！");
                                refreshTableData(false);
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(BookManagementPanel.this, "修改书籍时发生异常: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
            return super.getCellEditorValue();
        }
    }
}
