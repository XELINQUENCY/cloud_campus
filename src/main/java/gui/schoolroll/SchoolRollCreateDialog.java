package gui.schoolroll;

import client.ApiClientFactory;
import client.schoolroll.SchoolRollClient;
import entity.schoolroll.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建新学籍记录的对话框 (仅限管理员使用)
 */
public class SchoolRollCreateDialog extends JDialog {

    private final SchoolRollClient schoolRollClient;
    private final Map<String, JTextField> fields = new HashMap<>();
    private JButton createButton;

    public SchoolRollCreateDialog(Frame owner) {
        super(owner, "新建学籍档案", true);
        this.schoolRollClient = ApiClientFactory.getSchoolRollClient();
        initComponents();
    }

    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        mainPanel.add(new JLabel("请填写新学生的信息", SwingConstants.CENTER), BorderLayout.NORTH);

        // --- 表单面板 ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"学号*:", "姓名*:", "性别:", "班级ID:", "专业ID:", "入学日期(YYYY-MM-DD):", "学籍状态:"};
        String[] fieldKeys = {"studentId", "name", "gender", "classId", "majorId", "enrollDate", "status"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            JTextField textField = new JTextField(15);
            formPanel.add(textField, gbc);
            fields.put(fieldKeys[i], textField);
        }
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- 按钮面板 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("创建");
        createButton.addActionListener(e -> createStudent());
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createStudent() {
        // 数据校验
        String studentId = fields.get("studentId").getText();
        String name = fields.get("name").getText();
        if (studentId.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "学号和姓名是必填项。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student newStudent = new Student();
        newStudent.setStudentId(studentId);
        newStudent.setName(name);
        newStudent.setGender(fields.get("gender").getText());
        newStudent.setClassId(fields.get("classId").getText());
        newStudent.setMajorId(fields.get("majorId").getText());
        newStudent.setStatus(fields.get("status").getText());
        try {
            String dateText = fields.get("enrollDate").getText();
            if (dateText != null && !dateText.trim().isEmpty()) {
                newStudent.setEnrollDate(LocalDate.parse(dateText));
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "入学日期格式不正确，应为 YYYY-MM-DD。", "格式错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        createButton.setEnabled(false);

        // 使用 SwingWorker 执行创建操作
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                schoolRollClient.createStudent(newStudent);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SchoolRollCreateDialog.this, "学籍创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SchoolRollCreateDialog.this,
                            "创建失败: " + e.getCause().getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    createButton.setEnabled(true);
                }
            }
        }.execute();
    }
}
