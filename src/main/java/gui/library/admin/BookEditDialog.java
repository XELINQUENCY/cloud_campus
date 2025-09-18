package gui.library.admin;

import client.ApiClientFactory;
import client.library.LibraryClient;
import entity.library.Book;
import entity.library.Category;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * 新增/修改书籍信息的对话框 (重构版)
 * 构造函数已简化，不再需要传递服务实例。
 */
public class BookEditDialog extends JDialog {

    // --- UI 组件 ---
    private JTextField titleField, authorField, publisherField, isbnField;
    private JComboBox<Object> categoryComboBox;
    private JSpinner totalCopiesSpinner;
    private JButton saveButton, cancelButton;

    // --- 成员变量 ---
    private final Book bookToEdit;
    private Book resultBook = null;
    private final LibraryClient libraryClient;

    /**
     * @param parent     父窗口
     * @param bookToEdit 要编辑的书籍对象。如果为null，则为新增模式。
     */
    public BookEditDialog(Frame parent, Book bookToEdit) {
        super(parent, true);
        this.libraryClient = ApiClientFactory.getLibraryClient(); // 直接从工厂获取
        this.bookToEdit = bookToEdit;

        setTitle(isEditMode() ? "修改书籍信息" : "新增书籍");
        setSize(450, 400);
        setLocationRelativeTo(parent);

        initComponents();
        initListeners();
        loadCategories();

        if (isEditMode()) {
            populateData();
        }
    }

    private boolean isEditMode() {
        return bookToEdit != null;
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        titleField = new JTextField(20);
        authorField = new JTextField(20);
        publisherField = new JTextField(20);
        isbnField = new JTextField(20);
        categoryComboBox = new JComboBox<>();
        totalCopiesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("书名:"), gbc);
        gbc.gridx = 1; formPanel.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 1; formPanel.add(authorField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("出版社:"), gbc);
        gbc.gridx = 1; formPanel.add(publisherField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1; formPanel.add(isbnField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(categoryComboBox, gbc);

        if (!isEditMode()) {
            gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("入库数量:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(totalCopiesSpinner, gbc);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> handleSave());
    }

    private void loadCategories() {
        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                return libraryClient.getAllCategories();
            }

            @Override
            protected void done() {
                try {
                    List<Category> categories = get();
                    Vector<Object> categoryVector = new Vector<>(categories);
                    categoryComboBox.setModel(new DefaultComboBoxModel<>(categoryVector));
                    // 如果是编辑模式，需要在分类加载后设置选中项
                    if (isEditMode()) {
                        populateCategorySelection();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookEditDialog.this, "加载分类失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void populateData() {
        titleField.setText(bookToEdit.getTitle());
        authorField.setText(bookToEdit.getAuthor());
        publisherField.setText(bookToEdit.getPublisher());
        isbnField.setText(bookToEdit.getIsbn());
    }

    private void populateCategorySelection() {
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            if (categoryComboBox.getItemAt(i) instanceof Category cat) {
                if (cat.getCategoryId() == bookToEdit.getCategoryId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void handleSave() {
        if (titleField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "书名和作者不能为空！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!(categoryComboBox.getSelectedItem() instanceof Category)) {
            JOptionPane.showMessageDialog(this, "请选择一个有效的分类！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book book = isEditMode() ? bookToEdit : new Book();
        book.setTitle(titleField.getText().trim());
        book.setAuthor(authorField.getText().trim());
        book.setPublisher(publisherField.getText().trim());
        book.setIsbn(isbnField.getText().trim());
        book.setCategoryId(((Category) categoryComboBox.getSelectedItem()).getCategoryId());

        if (!isEditMode()) {
            int copies = (Integer) totalCopiesSpinner.getValue();
            book.setTotalCopies(copies);
            book.setAvailableCopies(copies);
        }

        this.resultBook = book;
        dispose();
    }

    public Book getResultBook() {
        return resultBook;
    }
}
