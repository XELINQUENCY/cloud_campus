package service.studentRecord.impl;

import DAO.UserDAO;
import DAO.studentRecord.StudentRecordDAO;
import entity.User;
import entity.studentRecord.StudentRecord;
import enums.UserRole; // 假设你有这个枚举来定义角色
import service.studentRecord.StudentRecordService;

import java.util.List;

public class StudentRecordServiceImpl implements StudentRecordService {

    private final UserDAO userDAO = new UserDAO();
    private final StudentRecordDAO studentRecordDAO = new StudentRecordDAO();

    // 内部辅助方法，用于检查权限
    private boolean isAdmin(User user) {
        return user != null && user.hasRole(UserRole.ACADEMIC_ADMIN); // 假设 hasRole 和 UserRole 枚举存在
    }

    private boolean isStudent(User user) {
        return user != null && user.hasRole(UserRole.STUDENT);
    }

    /**
     * 内部辅助方法，根据用户ID获取其对应的学号。
     * (这是原代码 getStudentIdByUserId 的逻辑)
     */
    private String getStudentIdFromUser(User user) {
        String studentId = studentRecordDAO.findById(user.getId()).getStudentId();
        if (studentId != null && !studentId.isEmpty()) {
            return studentId;
        }
        // 作为备用方案
        return "S" + String.format("%03d", Integer.parseInt(user.getId()));
    }


    @Override
    public StudentRecord getRecord(User authenticatedUser, String studentId) throws Exception {
        if (authenticatedUser == null) {
            throw new Exception("未认证的用户。");
        }

        boolean canAccess = false;
        if (isAdmin(authenticatedUser)) {
            canAccess = true; // 管理员可以访问任何学籍
        } else if (isStudent(authenticatedUser)) {
            // 学生只能访问自己的学籍
            String userOwnStudentId = getStudentIdFromUser(authenticatedUser);
            canAccess = studentId.equals(userOwnStudentId);
        }

        if (!canAccess) {
            throw new Exception("权限不足，无法访问该学籍信息。");
        }

        StudentRecord record = studentRecordDAO.findById(studentId);
        if (record == null) {
            throw new Exception("学籍记录未找到。");
        }
        return record;
    }

    @Override
    public boolean createRecord(User authenticatedUser, StudentRecord recordToCreate) throws Exception {
        if (!isAdmin(authenticatedUser)) {
            throw new Exception("权限不足，只有管理员才能创建学籍记录。");
        }
        if (recordToCreate == null || recordToCreate.getStudentId() == null || recordToCreate.getName() == null) {
            throw new IllegalArgumentException("无效的学籍数据，学号和姓名不能为空。");
        }
        return studentRecordDAO.insert(recordToCreate) > 0;
    }

    @Override
    public boolean updateRecord(User authenticatedUser, StudentRecord recordToUpdate) throws Exception {
        if (authenticatedUser == null) {
            throw new Exception("未认证的用户。");
        }
        if (recordToUpdate == null || recordToUpdate.getStudentId() == null) {
            throw new IllegalArgumentException("无效的学籍数据，学号不能为空。");
        }

        boolean canUpdate = false;
        if (isAdmin(authenticatedUser)) {
            canUpdate = true; // 管理员可以修改任何学籍
        } else if (isStudent(authenticatedUser)) {
            String userOwnStudentId = getStudentIdFromUser(authenticatedUser);
            canUpdate = recordToUpdate.getStudentId().equals(userOwnStudentId);
            // 可以在此添加更细粒度的控制，例如学生只能修改自己的某些字段
        }

        if (!canUpdate) {
            throw new Exception("权限不足，无法修改该学籍记录。");
        }
        return studentRecordDAO.update(recordToUpdate) > 0;
    }

    @Override
    public boolean deleteRecord(User authenticatedUser, String studentIdToDelete) throws Exception {
        if (!isAdmin(authenticatedUser)) {
            throw new Exception("权限不足，只有管理员才能删除学籍记录。");
        }
        return studentRecordDAO.delete(studentIdToDelete) > 0;
    }

    @Override
    public List<String> listCompletedCourses(User authenticatedUser, String studentId) throws Exception {
        // 查询权限与 getRecord 相同
        getRecord(authenticatedUser, studentId); // 复用 getRecord 的权限检查逻辑
        return studentRecordDAO.listCompletedCourses(studentId);
    }

    @Override
    public boolean addCompletedCourse(User authenticatedUser, String studentId, String courseId) throws Exception {
        // 添加权限与 updateRecord 相同
        updateRecord(authenticatedUser, new StudentRecord(studentId, null, null, null)); // 构造一个假对象以复用权限检查
        return studentRecordDAO.addCompletedCourse(studentId, courseId) > 0;
    }

    @Override
    public boolean removeCompletedCourse(User authenticatedUser, String studentId, String courseId) throws Exception {
        // 移除权限与 updateRecord 相同
        updateRecord(authenticatedUser, new StudentRecord(studentId, null, null, null)); // 构造一个假对象以复用权限检查
        return studentRecordDAO.removeCompletedCourse(studentId, courseId) > 0;
    }
}