package gui.schoolroll;

import client.ApiClientFactory;
import client.ApiException;
import client.schoolroll.SchoolRollClient;
import dto.schoolroll.StudentDetailDTO;
import entity.User;
import entity.schoolroll.Student;
import enums.UserRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 学籍管理主界面。
 * 根据用户角色（学生/管理员）动态调整显示和功能。
 */
public class SchoolRollMainFrame extends JFrame {

    private final User currentUser;
    private final SchoolRollClient schoolRollClient;
    private final Runnable onLogoutCallback;

    // UI Components
    private final Map<String, JTextField> detailFields = new HashMap<>();
    private JButton saveButton, refreshButton;
    private JButton searchButton, createButton, deleteButton; // Admin only
    private JPanel adminButtonPanel;
    private JLabel statusLabel;

    private StudentDetailDTO currentStudentDTO; // 保存当前正在显示的学籍信息

    /**
     * 构造函数
     * @param user 登录的当前用户
     * @param onLogoutCallback 登出时执行的回调
     */
    public SchoolRollMainFrame(User user, Runnable onLogoutCallback) throws ApiException {
        this.currentUser = user;
        this.schoolRollClient = ApiClientFactory.getSchoolRollClient();
        this.onLogoutCallback = onLogoutCallback;

        initComponents();
        setupRoleBasedUI();

        // 学生登录后自动加载自己的信息
        if (currentUser.hasRole(UserRole.STUDENT)) {
            loadStudentDetails(schoolRollClient.getStudentId());
        }
    }

    private void initComponents() {
        setTitle("学籍管理系统");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 500));

        // --- 主面板 ---
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // --- 顶部面板 ---
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- 中央表单面板 ---
        JPanel centerPanel = createCenterFormPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- 底部按钮面板 ---
        JPanel bottomPanel = createBottomButtonPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("欢迎, " + currentUser.getName());
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(60, 70, 85));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("返回主界面");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.setBackground(new Color(239, 68, 68));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(_ -> logoutActionPerformed());
        topPanel.add(logoutButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createCenterFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 40, 20, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"学号:", "姓名:", "性别:", "专业:", "班级:", "学籍状态:", "入学日期:"};
        String[] fieldKeys = {"studentId", "name", "gender", "majorName", "className", "status", "enrollDate"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            JTextField textField = new JTextField(20);
            textField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            textField.setMargin(new Insets(5, 5, 5, 5));
            formPanel.add(textField, gbc);
            detailFields.put(fieldKeys[i], textField);
        }
        return formPanel;
    }

    private JPanel createBottomButtonPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // 左侧状态标签
        statusLabel = new JLabel("准备就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        // 右侧按钮面板
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonContainer.setOpaque(false);

        // 学生和管理员共有的按钮
        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshActionPerformed());
        saveButton = new JButton("保存更改");
        saveButton.addActionListener(e -> saveChangesActionPerformed());

        // 仅管理员可见的按钮面板
        adminButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        adminButtonPanel.setOpaque(false);
        searchButton = new JButton("查找学生");
        searchButton.addActionListener(e -> openSearchDialog());
        createButton = new JButton("新建学籍");
        createButton.addActionListener(e -> openCreateDialog());
        deleteButton = new JButton("删除学籍");
        deleteButton.addActionListener(e -> deleteActionPerformed());
        adminButtonPanel.add(searchButton);
        adminButtonPanel.add(createButton);
        adminButtonPanel.add(deleteButton);

        buttonContainer.add(adminButtonPanel);
        buttonContainer.add(refreshButton);
        buttonContainer.add(saveButton);
        bottomPanel.add(buttonContainer, BorderLayout.EAST);

        return bottomPanel;
    }

    /**
     * 根据用户角色设置UI元素的可见性和可编辑性
     */
    private void setupRoleBasedUI() {
        boolean isAdmin = currentUser.hasRole(UserRole.ACADEMIC_ADMIN);

        // 控制管理员按钮的可见性
        adminButtonPanel.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);

        // 控制字段的可编辑性
        detailFields.get("studentId").setEditable(false); // 学号始终不可编辑
        detailFields.get("majorName").setEditable(isAdmin);
        detailFields.get("className").setEditable(isAdmin);
        detailFields.get("enrollDate").setEditable(isAdmin);
        detailFields.get("status").setEditable(isAdmin);

        // 假设学生只能修改姓名和性别，管理员可以修改所有
        detailFields.get("name").setEditable(isAdmin);
        detailFields.get("gender").setEditable(isAdmin);

        // 学生不能保存专业、班级等信息，因此对学生隐藏保存按钮
        saveButton.setVisible(isAdmin);

        if (!isAdmin) {
            statusLabel.setText("您好，同学！此处显示您的个人学籍信息。");
            // 明确禁用学生不可编辑的字段的背景色，使其看起来像标签
            detailFields.forEach((key, field) -> {
                if (!field.isEditable()) {
                    field.setBackground(new Color(235, 235, 235));
                    field.setBorder(BorderFactory.createEtchedBorder());
                }
            });
        } else {
            statusLabel.setText("管理员模式：请通过'查找学生'加载学籍信息。");
        }
    }

    /**
     * 加载指定学号的学生详细信息到界面
     * @param studentId 要加载的学号
     */
    public void loadStudentDetails(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return;
        }
        statusLabel.setText("正在加载学号 " + studentId + " 的信息...");
        setFormEnabled(false);

        new SwingWorker<StudentDetailDTO, Void>() {
            @Override
            protected StudentDetailDTO doInBackground() throws Exception {
                return schoolRollClient.getStudentDetails(studentId);
            }

            @Override
            protected void done() {
                try {
                    StudentDetailDTO dto = get();
                    currentStudentDTO = dto; // 保存当前加载的数据
                    populateForm(dto);
                    statusLabel.setText("已成功加载 " + dto.getName() + " 的学籍信息。");
                } catch (Exception e) {
                    currentStudentDTO = null; // 加载失败，清空当前数据
                    clearForm();
                    statusLabel.setText("加载失败");
                    showErrorDialog("加载学籍信息失败", e);
                } finally {
                    setFormEnabled(true);
                }
            }
        }.execute();
    }

    private void saveChangesActionPerformed() {
        if (currentStudentDTO == null) {
            JOptionPane.showMessageDialog(this, "没有加载任何学籍信息，无法保存。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 从界面收集数据构建一个 Student 对象用于更新
        Student studentToUpdate = new Student();
        studentToUpdate.setStudentId(detailFields.get("studentId").getText());
        studentToUpdate.setName(detailFields.get("name").getText());
        studentToUpdate.setGender(detailFields.get("gender").getText());
        studentToUpdate.setStatus(detailFields.get("status").getText());
        // ... 其他字段可以类似地从 DTO 中获取，因为它们不能直接在UI上修改
        studentToUpdate.setClassId(currentStudentDTO.getClassId());
        studentToUpdate.setMajorId(currentStudentDTO.getMajorId());
        try {
            studentToUpdate.setEnrollDate(LocalDate.parse(detailFields.get("enrollDate").getText()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "入学日期格式不正确，应为 YYYY-MM-DD", "格式错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setText("正在保存更改...");
        setFormEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                schoolRollClient.updateStudent(studentToUpdate);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // 检查是否有异常抛出
                    JOptionPane.showMessageDialog(SchoolRollMainFrame.this, "学籍信息更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText("信息已保存");
                } catch (Exception e) {
                    statusLabel.setText("保存失败");
                    showErrorDialog("保存学籍信息失败", e);
                } finally {
                    setFormEnabled(true);
                }
            }
        }.execute();
    }

    private void refreshActionPerformed() {
        if (currentStudentDTO != null) {
            loadStudentDetails(currentStudentDTO.getStudentId());
        } else {
            JOptionPane.showMessageDialog(this, "请先加载一条学籍记录再刷新。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteActionPerformed() {
        if (currentStudentDTO == null) {
            JOptionPane.showMessageDialog(this, "请先加载一条学籍记录再删除。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "您确定要删除学号为 " + currentStudentDTO.getStudentId() + " (" + currentStudentDTO.getName() + ") 的学籍吗？\n此操作通常是不可逆的！",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        statusLabel.setText("正在删除学籍...");
        setFormEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                schoolRollClient.deleteStudent(currentStudentDTO.getStudentId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SchoolRollMainFrame.this, "学籍删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText("学籍已删除");
                    clearForm();
                    currentStudentDTO = null;
                } catch (Exception e) {
                    statusLabel.setText("删除失败");
                    showErrorDialog("删除学籍失败", e);
                } finally {
                    setFormEnabled(true);
                }
            }
        }.execute();
    }

    private void openSearchDialog() {
        // 创建并显示搜索对话框，传入一个回调函数，当在对话框中选择一个学生时，主界面会加载该学生的信息
        StudentSearchDialog searchDialog = new StudentSearchDialog(this, this::loadStudentDetails);
        searchDialog.setVisible(true);
    }

    private void openCreateDialog() {
        SchoolRollCreateDialog createDialog = new SchoolRollCreateDialog(this);
        createDialog.setVisible(true);
    }

    private void logoutActionPerformed() {
        int result = JOptionPane.showConfirmDialog(this, "确定要登出吗?", "确认", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            if (onLogoutCallback != null) {
                onLogoutCallback.run();
            }
        }
    }

    private void populateForm(StudentDetailDTO dto) {
        if (dto == null) {
            clearForm();
            return;
        }
        detailFields.get("studentId").setText(dto.getStudentId());
        detailFields.get("name").setText(dto.getName());
        detailFields.get("gender").setText(dto.getGender());
        detailFields.get("majorName").setText(dto.getMajorName());
        detailFields.get("className").setText(dto.getClassName());
        detailFields.get("status").setText(dto.getStatus());
        detailFields.get("enrollDate").setText(dto.getEnrollDate() != null ? dto.getEnrollDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
    }

    private void clearForm() {
        detailFields.values().forEach(field -> field.setText(""));
    }

    private void setFormEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        if (currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            searchButton.setEnabled(enabled);
            createButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
        }
    }

    private void showErrorDialog(String title, Throwable e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof ApiException) {
            JOptionPane.showMessageDialog(this, cause.getMessage(), title, JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "发生未知错误: " + cause.getMessage(), title, JOptionPane.ERROR_MESSAGE);
        }
    }
}
