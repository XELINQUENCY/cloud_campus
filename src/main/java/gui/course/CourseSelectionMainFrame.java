// 文件路径: src/main/java/gui/course/CourseSelectionMainFrame.java
package gui.course;

import client.ApiClientFactory;
import client.course.ICourseClientSrv;
import entity.User;
import enums.UserRole;
import view.CourseOfferingVO;
import view.StudentCourseDetailVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 选课系统主界面GUI。
 * 根据用户角色（学生/教务管理员）显示不同的功能面板。
 */
public class CourseSelectionMainFrame extends JFrame {

    private final User currentUser;
    private final ICourseClientSrv courseClient;
    private final Runnable onLogoutCallback;

    public CourseSelectionMainFrame(User user, Runnable onLogoutCallback) {
        this.currentUser = user;
        this.courseClient = ApiClientFactory.getCourseClient();
        this.onLogoutCallback = onLogoutCallback;

        setTitle("选课系统");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 顶部面板 ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("欢迎, " + currentUser.getName() + " (" + (currentUser.hasRole(UserRole.ACADEMIC_ADMIN) ? "教务管理员" : "学生") + ")");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        JButton logoutButton = new JButton("返回主界面");
        logoutButton.addActionListener(_ -> logout());
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- 中心选项卡面板 ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 两个角色共享“课程浏览”面板
        tabbedPane.addTab("  课程浏览/选课  ", new CourseBrowsePanel());

        // 根据角色添加特定面板
        if (currentUser.hasRole(UserRole.STUDENT)) {
            tabbedPane.addTab("  我的课表  ", new MyCoursesPanel());
        }

        if (currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            tabbedPane.addTab("  教务管理  ", new AdminManagementPanel());
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "确定要退出登录吗?", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            if (onLogoutCallback != null) {
                onLogoutCallback.run();
            }
        }
    }

    // =================================================================================
    // ==                             内部面板类                                     ==
    // =================================================================================

    /**
     * 课程浏览面板
     */
    class CourseBrowsePanel extends JPanel {
        private final JTextField courseNameField;
        private final JTextField teacherNameField;
        private final JTextField departmentField;
        private final JComboBox<String> semesterComboBox;
        private final JButton searchButton;
        private final JTable courseTable;
        private final CourseTableModel tableModel;

        public CourseBrowsePanel() {
            setLayout(new BorderLayout(10, 10));

            // --- 搜索区 ---
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.setBorder(BorderFactory.createTitledBorder("筛选条件"));

            semesterComboBox = new JComboBox<>(new String[]{"2025秋季", "2026春季"}); // 示例学期
            courseNameField = new JTextField(15);
            teacherNameField = new JTextField(10);
            departmentField = new JTextField(10);
            searchButton = new JButton("查询");

            searchPanel.add(new JLabel("学期:"));
            searchPanel.add(semesterComboBox);
            searchPanel.add(new JLabel("课程名:"));
            searchPanel.add(courseNameField);
            searchPanel.add(new JLabel("教师:"));
            searchPanel.add(teacherNameField);
            searchPanel.add(new JLabel("院系:"));
            searchPanel.add(departmentField);
            searchPanel.add(searchButton);

            add(searchPanel, BorderLayout.NORTH);

            // --- 表格区 ---
            tableModel = new CourseTableModel();
            courseTable = new JTable(tableModel);
            courseTable.setRowHeight(30);
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            courseTable.setDefaultRenderer(Object.class, centerRenderer);

            // 操作列
            int actionCol = tableModel.getColumnCount() - 1;
            courseTable.getColumnModel().getColumn(actionCol).setCellRenderer(new ButtonRenderer());
            courseTable.getColumnModel().getColumn(actionCol).setCellEditor(new ButtonEditor(new JCheckBox()));

            add(new JScrollPane(courseTable), BorderLayout.CENTER);

            searchButton.addActionListener(e -> loadCourses());
            loadCourses(); // 初始加载
        }

        private void loadCourses() {
            String semester = (String) semesterComboBox.getSelectedItem();
            String courseName = courseNameField.getText();
            String teacherName = teacherNameField.getText();
            String department = departmentField.getText();

            searchButton.setEnabled(false);
            new SwingWorker<List<CourseOfferingVO>, Void>() {
                @Override
                protected List<CourseOfferingVO> doInBackground() throws Exception {
                    return courseClient.browseCourses(semester, courseName, teacherName, department);
                }

                @Override
                protected void done() {
                    try {
                        tableModel.setCourses(get());
                    } catch (Exception e) {
                        handleApiError(e, "加载课程列表失败");
                    } finally {
                        searchButton.setEnabled(true);
                    }
                }
            }.execute();
        }

        class CourseTableModel extends AbstractTableModel {
            private final String[] columnNames = {"课程号", "课程名", "专业", "教师", "学分", "时间", "地点", "容量", "已选", "操作"};
            private List<CourseOfferingVO> courses = new ArrayList<>();

            public void setCourses(List<CourseOfferingVO> courses) {
                this.courses = courses;
                fireTableDataChanged();
            }

            @Override public int getRowCount() { return courses.size(); }
            @Override public int getColumnCount() { return columnNames.length; }
            @Override public String getColumnName(int col) { return columnNames[col]; }
            @Override public boolean isCellEditable(int row, int col) { return col == columnNames.length - 1; }

            @Override
            public Object getValueAt(int row, int col) {
                CourseOfferingVO vo = courses.get(row);
                return switch (col) {
                    case 0 -> vo.getCourse().getCourseId();
                    case 1 -> vo.getCourse().getCourseName();
                    case 2 -> vo.getCourse().getMajor().getMajorName();
                    case 3 -> vo.getTeacher().getTeacherName();
                    case 4 -> vo.getCourse().getCredits();
                    case 5 -> "周" + vo.getWeekday() + " " + vo.getClassPeriod() + "节";
                    case 6 -> vo.getLocation();
                    case 7 -> vo.getMaxCapacity();
                    case 8 -> vo.getCurrentStudents();
                    case 9 -> vo; // 将整个对象传给按钮编辑器
                    default -> null;
                };
            }
        }

        class ButtonRenderer extends JButton implements TableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setText("选课");
                setEnabled(currentUser.hasRole(UserRole.STUDENT)); // 只有学生能看到可选的按钮
                return this;
            }
        }

        class ButtonEditor extends DefaultCellEditor {
            private final JButton button;
            private CourseOfferingVO currentCourse;

            public ButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                button = new JButton("选课");
                button.addActionListener(e -> fireEditingStopped());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.currentCourse = (CourseOfferingVO) value;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                int choice = JOptionPane.showConfirmDialog(CourseBrowsePanel.this,
                        "确定要选择《" + currentCourse.getCourse().getCourseName() + "》吗？", "选课确认", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return courseClient.selectCourse(currentCourse.getTeachingId(), currentCourse.getSemester());
                        }

                        @Override
                        protected void done() {
                            try {
                                JOptionPane.showMessageDialog(CourseBrowsePanel.this, get(), "成功", JOptionPane.INFORMATION_MESSAGE);
                                loadCourses(); // 刷新课程列表
                            } catch (Exception e) {
                                handleApiError(e, "选课失败");
                            }
                        }
                    }.execute();
                }
                return currentCourse;
            }
        }
    }

    /**
     * 我的课表面板
     */
    class MyCoursesPanel extends JPanel {
        private final JTable myCourseTable;
        private final MyCourseTableModel tableModel;
        private final JButton refreshButton;
        private final JComboBox<String> semesterComboBox;

        public MyCoursesPanel() {
            setLayout(new BorderLayout(10, 10));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            semesterComboBox = new JComboBox<>(new String[]{"2025秋季", "2026春季"});
            refreshButton = new JButton("刷新课表");
            topPanel.add(new JLabel("选择学期:"));
            topPanel.add(semesterComboBox);
            topPanel.add(refreshButton);
            add(topPanel, BorderLayout.NORTH);

            tableModel = new MyCourseTableModel();
            myCourseTable = new JTable(tableModel);
            myCourseTable.setRowHeight(30);

            int actionCol = tableModel.getColumnCount() - 1;
            myCourseTable.getColumnModel().getColumn(actionCol).setCellRenderer(new DropCourseButtonRenderer());
            myCourseTable.getColumnModel().getColumn(actionCol).setCellEditor(new DropCourseButtonEditor(new JCheckBox()));

            add(new JScrollPane(myCourseTable), BorderLayout.CENTER);

            refreshButton.addActionListener(e -> loadMyCourses());
            semesterComboBox.addActionListener(e -> loadMyCourses());
            loadMyCourses();
        }

        private void loadMyCourses() {
            String semester = (String) semesterComboBox.getSelectedItem();
            refreshButton.setEnabled(false);
            new SwingWorker<List<StudentCourseDetailVO>, Void>() {
                @Override
                protected List<StudentCourseDetailVO> doInBackground() throws Exception {
                    return courseClient.getMyCourses(semester);
                }
                @Override
                protected void done() {
                    try {
                        tableModel.setCourses(get());
                    } catch (Exception e) {
                        handleApiError(e, "加载我的课表失败");
                    } finally {
                        refreshButton.setEnabled(true);
                    }
                }
            }.execute();
        }

        class MyCourseTableModel extends AbstractTableModel {
            private final String[] columnNames = {"课程名", "专业", "教师", "学分", "时间", "地点", "选课类型", "成绩", "操作"};
            private List<StudentCourseDetailVO> courses = new ArrayList<>();

            public void setCourses(List<StudentCourseDetailVO> courses) {
                this.courses = courses;
                fireTableDataChanged();
            }

            @Override public int getRowCount() { return courses.size(); }
            @Override public int getColumnCount() { return columnNames.length; }
            @Override public String getColumnName(int col) { return columnNames[col]; }
            @Override public boolean isCellEditable(int row, int col) { return col == columnNames.length - 1; }

            @Override
            public Object getValueAt(int row, int col) {
                StudentCourseDetailVO vo = courses.get(row);
                switch (col) {
                    case 0: return vo.getCourse().getCourseName();
                    case 1: return vo.getCourse().getMajor().getMajorName();
                    case 2: return vo.getTeacher().getTeacherName();
                    case 3: return vo.getCourse().getCredits();
                    case 4: return "周" + vo.getWeekday() + " " + vo.getClassPeriod() + "节";
                    case 5: return vo.getLocation();
                    case 6: return vo.getEnrollmentType();
                    case 7: return vo.getScore() != null && vo.getScore() > 0 ? vo.getScore().toString() : "未出";
                    case 8: return vo; // 传给按钮
                    default: return null;
                }
            }
        }

        class DropCourseButtonRenderer extends JButton implements TableCellRenderer {
            public DropCourseButtonRenderer() { super("退课"); setForeground(Color.RED); }
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { return this; }
        }

        class DropCourseButtonEditor extends DefaultCellEditor {
            private final JButton button;
            private StudentCourseDetailVO currentCourse;

            public DropCourseButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                button = new JButton("退课");
                button.setForeground(Color.RED);
                button.addActionListener(e -> fireEditingStopped());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                this.currentCourse = (StudentCourseDetailVO) value;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                int choice = JOptionPane.showConfirmDialog(MyCoursesPanel.this,
                        "确定要退选《" + currentCourse.getCourse().getCourseName() + "》吗？", "退课确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return courseClient.dropCourse(currentCourse.getTeachingId());
                        }

                        @Override
                        protected void done() {
                            try {
                                JOptionPane.showMessageDialog(MyCoursesPanel.this, get(), "成功", JOptionPane.INFORMATION_MESSAGE);
                                loadMyCourses(); // 刷新
                            } catch (Exception e) {
                                handleApiError(e, "退课失败");
                            }
                        }
                    }.execute();
                }
                return currentCourse;
            }
        }
    }

    /**
     * 教务管理面板
     */
    class AdminManagementPanel extends JPanel {

        public AdminManagementPanel() {
            setLayout(new GridLayout(3, 1, 10, 10));
            setBorder(new EmptyBorder(50, 50, 50, 50));

            JButton addCourseBtn = new JButton("为学生补选课程");
            addCourseBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
            addCourseBtn.addActionListener(e -> handleAdminAction("add"));

            JButton removeCourseBtn = new JButton("为学生退选课程");
            removeCourseBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
            removeCourseBtn.addActionListener(e -> handleAdminAction("remove"));

            JButton updateCapacityBtn = new JButton("调整课程容量");
            updateCapacityBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
            updateCapacityBtn.addActionListener(e -> handleAdminAction("capacity"));

            add(addCourseBtn);
            add(removeCourseBtn);
            add(updateCapacityBtn);
        }

        private void handleAdminAction(String action) {
            String studentId = null;
            String teachingIdStr = null;
            String semester = null;
            String newCapacityStr = null; // 在外部声明

            if (action.equals("add") || action.equals("remove")) {
                studentId = JOptionPane.showInputDialog(this, "请输入学生ID:");
                if (studentId == null || studentId.isEmpty()) return;
                teachingIdStr = JOptionPane.showInputDialog(this, "请输入教学班ID (teachingId):");
                if (teachingIdStr == null || teachingIdStr.isEmpty()) return;
            }
            if (action.equals("add")) {
                semester = JOptionPane.showInputDialog(this, "请输入学期 (例如: 2025秋季):");
                if (semester == null || semester.isEmpty()) return;
            }
            if(action.equals("capacity")) {
                teachingIdStr = JOptionPane.showInputDialog(this, "请输入教学班ID (teachingId):");
                if (teachingIdStr == null || teachingIdStr.isEmpty()) return;
                newCapacityStr = JOptionPane.showInputDialog(this, "请输入新的课程容量:");
                if (newCapacityStr == null || newCapacityStr.isEmpty()) return;
            }

            // 【重点修改】
            // 将需要在内部类中使用的变量声明为 final
            final String finalStudentId = studentId;
            final String finalTeachingIdStr = teachingIdStr;
            final String finalSemester = semester;
            final String finalNewCapacityStr = newCapacityStr;

            try {
                // 如果 finalTeachingIdStr 为 null，说明操作不是 add, remove, 或 capacity，但以防万一还是检查一下
                if (finalTeachingIdStr == null) {
                    // 对于 "capacity" 操作，如果 teachingIdStr 为 null, 已经在前面返回了。
                    // 这里的检查主要是为了代码健壮性。
                    if (action.equals("add") || action.equals("remove") || action.equals("capacity")) {
                        return; // teachingId是必须的，如果为空则不继续
                    }
                }

                final int teachingId = Integer.parseInt(finalTeachingIdStr);

                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        switch (action) {
                            case "add":
                                return courseClient.addCourseForStudent(finalStudentId, teachingId, finalSemester);
                            case "remove":
                                return courseClient.removeCourseForStudent(finalStudentId, teachingId);
                            case "capacity":
                                // 在这里解析 finalNewCapacityStr
                                int newCapacity = Integer.parseInt(finalNewCapacityStr);
                                return courseClient.updateCourseCapacity(teachingId, newCapacity);
                            default:
                                return "未知操作";
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            JOptionPane.showMessageDialog(AdminManagementPanel.this, get(), "操作成功", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception e) {
                            handleApiError(e, "管理员操作失败");
                        }
                    }
                }.execute();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID或容量必须是有效的数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 统一的API错误处理器
    private void handleApiError(Exception e, String title) {
        Throwable cause = e;
        if (e instanceof ExecutionException) {
            cause = e.getCause();
        }
        JOptionPane.showMessageDialog(this, cause.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }
}