package service.schoolroll.impl;

import DAO.schoolroll.StudentDAO; // 假设这是您的DAO接口
import DAO.UserDAO;      // 假设这是您的DAO接口
import com.sun.jdi.InternalException;
import dto.schoolroll.StudentDetailDTO;
import entity.StudentQueryCriteria;
import entity.User;
import entity.schoolroll.Student;
import enums.StudentStatus;
import enums.UserRole;
import service.schoolroll.StudentService;
import service.schoolroll.exception.BadRequestException;
import service.schoolroll.exception.ForbiddenException;
import service.schoolroll.exception.NotFoundException;

import java.util.List;

/**
 * 负责处理所有学生学籍相关的业务逻辑。
 * 本 Service 类从旧的 Handler 中提取了业务逻辑，
 * 并适配了新的 Student 实体和基于 UserRole 的权限模型。
 */
public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    public StudentServiceImpl() {
    }

    /**
     * 获取单个学生的学籍信息。
     * @param studentId 要查询的学号
     * @param currentUser 当前操作的用户
     * @return 学生学籍实体
     * @throws ForbiddenException 如果用户无权访问该学籍
     * @throws NotFoundException 如果学籍不存在
     */
    public Student getStudent(String studentId, User currentUser) throws ForbiddenException, NotFoundException {
        if (!canUserAccessStudent(currentUser, studentId)) {
            throw new ForbiddenException("用户无权访问该学籍信息。");
        }
        Student student = studentDAO.getByStudentId(studentId);
        if (student == null) {
            throw new NotFoundException("学号为 " + studentId + " 的学生不存在。");
        }
        return student;
    }

    /**
     * 创建一个新的学生学籍记录 (仅限管理员)。
     * @param newStudent 包含新学生信息的实体对象
     * @param currentUser 当前操作的用户
     * @throws ForbiddenException 如果操作者不是管理员
     * @throws BadRequestException 如果传入的学生数据无效
     */
    public void createStudent(Student newStudent, User currentUser) throws ForbiddenException, BadRequestException {
        if (!currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            throw new ForbiddenException("只有管理员才能创建学生学籍。");
        }
        if (newStudent == null || newStudent.getStudentId() == null || newStudent.getName() == null) {
            throw new BadRequestException("无效的学生数据，学号和姓名不能为空。");
        }
        // 可以在此添加更多业务校验，例如检查学号是否已存在
        studentDAO.insert(newStudent);
    }

    /**
     * 更新一个已存在的学生学籍信息。
     * @param studentToUpdate 包含待更新信息的学生实体
     * @param currentUser 当前操作的用户
     * @throws ForbiddenException 如果用户无权修改该学籍
     * @throws BadRequestException 如果传入的数据无效
     */
    public void updateStudent(Student studentToUpdate, User currentUser) throws ForbiddenException, BadRequestException {
        if (studentToUpdate == null || studentToUpdate.getStudentId() == null) {
            throw new BadRequestException("更新数据无效，必须提供学号。");
        }
        if (!canUserAccessStudent(currentUser, studentToUpdate.getStudentId())) {
            throw new ForbiddenException("用户无权修改该学籍信息。");
        }
        if(currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            studentDAO.updateByAdmin(studentToUpdate);
        }else{
            studentDAO.updateByStudent(studentToUpdate);
        }
    }

    /**
     * 删除一个学生学籍记录 (仅限管理员)。
     * @param studentId 要删除的学号
     * @param currentUser 当前操作的用户
     * @throws ForbiddenException 如果操作者不是管理员
     */
    public void deleteStudent(String studentId, User currentUser) throws ForbiddenException {
        if (!currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            throw new ForbiddenException("只有管理员才能删除学生学籍。");
        }
        int res = studentDAO.updateStatusById(studentId, StudentStatus.WITHDRAWN.getDisplayName());
        if (res != 1) {
            throw new InternalException("删除异常！");
        }
    }

    public List<Student> searchStudent(StudentQueryCriteria sQC, User currentUser) throws ForbiddenException {
        if (!currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            throw new ForbiddenException("您不能查询学生");
        }
        return studentDAO.getByConditions(sQC.getStudentId(), sQC.getName(),
                sQC.getClassId(), sQC.getMajorId(), sQC.getEnrollYear(), sQC.getStatus());
    }

    @Override
    public StudentDetailDTO getStudentDetails(String studentId, User currentUser) throws ForbiddenException, NotFoundException {
        if (!canUserAccessStudent(currentUser, studentId)) {
            throw new ForbiddenException("用户无权访问该学籍信息。");
        }
        StudentDetailDTO studentDetailDTO = studentDAO.getWithDetailById(studentId);
        if (studentDetailDTO == null) {
            throw new NotFoundException("学号为 " + studentId + " 的学生不存在。");
        }
        return studentDetailDTO;
    }

    @Override
    public List<StudentDetailDTO> searchStudentDetails(StudentQueryCriteria sQC, User currentUser) throws ForbiddenException {
        if (!currentUser.hasRole(UserRole.ACADEMIC_ADMIN)) {
            throw new ForbiddenException("只有管理员才能查询学籍信息。");
        }
        return studentDAO.getWithDetailsByConditions(sQC);
    }

    // ===================================================================
    // Private Helper Methods - 内部权限判断逻辑
    // ===================================================================

    /**
     * 核心权限检查逻辑：判断一个用户是否有权访问某个学生的学籍。
     * @param user 要检查权限的用户
     * @param targetStudentId 目标学生的学号
     * @return 如果有权访问，返回 true，否则返回 false
     */
    private boolean canUserAccessStudent(User user, String targetStudentId) {
        if (user == null || targetStudentId == null || targetStudentId.isEmpty()) {
            return false;
        }

        // 规则1：管理员拥有所有权限
        if (user.hasRole(UserRole.ACADEMIC_ADMIN)) {
            return true;
        }

        // 规则2：学生只能访问自己的学籍
        if (user.hasRole(UserRole.STUDENT)) {
            String selfStudentId = getStudentIdByUserId(user.getId());
            return targetStudentId.equals(selfStudentId);
        }

        // 其他角色默认无权限
        return false;
    }

    /**
     * 根据用户系统ID获取其对应的学号。
     * 这部分逻辑源自旧的 Handler，现在封装在 Service 内部。
     * @param userId 用户的系统ID (通常是UUID或自增整数)
     * @return 关联的学号
     */
    public String getStudentIdByUserId(String userId) {
        try {
            // 优先从数据库中查找用户与学号的直接关联
            // 假设 User 实体中有一个 getStudentId() 字段，或者 UserDAO 有一个方法可以查询
            User user = userDAO.findById(userId); // 假设 userDAO 可以通过 String ID 查找
            Student student = studentDAO.getStudentByUserId(userId);
            if (user != null && student != null) {
                return student.getStudentId();
            }
        } catch (Exception e) {
            // 如果查询失败或没有直接关联，可以回退到旧的格式化逻辑，或者直接返回null
            // 这里我们为了兼容保留格式化逻辑
            return "S" + String.format("%03d", Integer.parseInt(userId));
        }
        // 如果没有找到关联，可以返回一个不可能匹配的字符串或null
        return null;
    }
}
