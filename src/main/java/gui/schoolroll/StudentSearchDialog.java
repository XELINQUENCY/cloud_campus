package gui.schoolroll;

import client.ApiClientFactory;
import client.schoolroll.SchoolRollClient;
import dto.schoolroll.StudentDetailDTO;
import entity.StudentQueryCriteria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 学生信息搜索对话框 (仅限管理员使用)
 */
public class StudentSearchDialog extends JDialog {

    private final SchoolRollClient schoolRollClient;
    private final Map<String, JTextField> searchFields = new HashMap<>();
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private final Consumer<String> onStudentSelectedCallback;

    /**
     * 构造函数
     * @param owner 父窗口
     * @param onStudentSelectedCallback 当用户选择一个学生后要执行的回调函数，
     * 该函数接收一个 studentId 作为参数。
     */
    public StudentSearchDialog(Frame owner, Consumer<String> onStudentSelectedCallback) {
        super(owner, "查找学生学籍", true);
        this.schoolRollClient = ApiClientFactory.getSchoolRollClient();
        this.onStudentSelectedCallback = onStudentSelectedCallback;
        initComponents();
    }

    private void initComponents() {
        setSize(900, 650);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // --- 顶部搜索条件面板 ---
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // --- 中间结果表格 ---
        JScrollPane tableScrollPane = createResultsTable();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // --- 底部操作按钮 ---
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索条件"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"学号:", "姓名:", "班级ID:", "专业ID:", "入学年份:", "学籍状态:"};
        String[] fieldKeys = {"studentId", "name", "classId", "majorId", "enrollYear", "status"};

        int row = 0, col = 0;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = col++;
            gbc.gridy = row;
            searchPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = col++;
            JTextField textField = new JTextField(10);
            searchFields.put(fieldKeys[i], textField);
            searchPanel.add(textField, gbc);

            if (col > 3) {
                col = 0;
                row++;
            }
        }

        gbc.gridx = 4;
        gbc.gridy = 1;
        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton, gbc);

        return searchPanel;
    }

    private JScrollPane createResultsTable() {
        String[] columnNames = {"学号", "姓名", "性别", "专业", "班级", "状态", "入学日期"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(25);

        return new JScrollPane(resultsTable);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectButton = new JButton("查看详情");
        selectButton.addActionListener(e -> selectStudent());
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(selectButton);
        bottomPanel.add(closeButton);
        return bottomPanel;
    }

    private void performSearch() {
        StudentQueryCriteria criteria = new StudentQueryCriteria();
        criteria.setStudentId(searchFields.get("studentId").getText());
        criteria.setName(searchFields.get("name").getText());
        criteria.setClassId(searchFields.get("classId").getText());
        criteria.setMajorId(searchFields.get("majorId").getText());
        String yearText = searchFields.get("enrollYear").getText();
        if (yearText != null && !yearText.isEmpty()) {
            try {
                criteria.setEnrollYear(Integer.parseInt(yearText));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "入学年份必须是有效的数字。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        criteria.setStatus(searchFields.get("status").getText());

        // 使用 SwingWorker 执行搜索
        new SwingWorker<List<StudentDetailDTO>, Void>() {
            @Override
            protected List<StudentDetailDTO> doInBackground() throws Exception {
                return schoolRollClient.searchStudentDetails(criteria);
            }

            @Override
            protected void done() {
                try {
                    List<StudentDetailDTO> results = get();
                    updateTable(results);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StudentSearchDialog.this,
                            "搜索失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateTable(List<StudentDetailDTO> studentList) {
        tableModel.setRowCount(0); // 清空旧数据
        if (studentList == null) return;
        for (StudentDetailDTO dto : studentList) {
            tableModel.addRow(new Object[]{
                    dto.getStudentId(),
                    dto.getName(),
                    dto.getGender(),
                    dto.getMajorName(),
                    dto.getClassName(),
                    dto.getStatus(),
                    dto.getEnrollDate()
            });
        }
        if (studentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到符合条件的学生记录。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void selectStudent() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先在表格中选择一名学生。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedStudentId = (String) tableModel.getValueAt(selectedRow, 0);

        if (onStudentSelectedCallback != null) {
            onStudentSelectedCallback.accept(selectedStudentId); // 执行回调
        }
        dispose();
    }
}
