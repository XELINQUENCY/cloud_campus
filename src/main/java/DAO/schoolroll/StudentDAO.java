package DAO.schoolroll;

import DAO.MyBatisUtil;
import dto.schoolroll.StudentDetailDTO;
import entity.StudentQueryCriteria;
import entity.schoolroll.Student;
import mapper.StudentMapper;

import java.util.List;

public class StudentDAO {
    // ========== 管理员：新增学生 ==========
    public int insert(Student student) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.insert(student));
    }

    // ========== 学生：修改自己的部分信息 ==========
    public int updateByStudent(Student student) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.updateByStudent(student));
    }

    // ========== 管理员：修改学生信息（除学号外）==========
    public int updateByAdmin(Student student) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.updateByAdmin(student));
    }

    // ========== 管理员：更新学生状态（软删除）==========
    public int updateStatusById(String studentId, String status) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.updateStatusById(studentId, status));
    }

    // ========== 查询方法 ==========
    public Student getByStudentId(String studentId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByStudentId(studentId));
    }
    public List<Student> getByName(String name) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByName(name));
    }
    public List<Student> getByClassId(String classId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByClassId(classId));
    }
    public List<Student> getByMajorId(String majorId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByMajorId(majorId));
    }
    public List<Student> getByEnrollYear(int year) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByEnrollYear(year));
    }

    // ========== 组合条件查询 ==========
    public List<Student> getByConditions(String studentId, String name, String classId, String majorId, Integer enrollYear, String status) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectByConditions(studentId, name, classId, majorId, enrollYear, status));
    }

    // ========== 连表查询：学生+班级名+专业名 ==========
    public StudentDetailDTO getWithDetailById(String studentId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectStudentWithDetail(studentId));
    }

    public List<StudentDetailDTO> getWithDetailsByConditions(StudentQueryCriteria sQC) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.selectDetailsByConditions(sQC));
    }

    public Student getStudentByUserId(String userId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.getStudentByUserId(userId));
    }
}
