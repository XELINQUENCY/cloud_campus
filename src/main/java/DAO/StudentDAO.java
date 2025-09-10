package DAO;

import entity.Student;
import entity.StudentQueryCriteria;
import mapper.StudentMapper;

import java.util.List;

public class StudentDAO {
    public Student findStudentById(String studentId) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.findById(studentId));
    }

    public List<Student> queryStudents(StudentQueryCriteria criteria) {
        return MyBatisUtil.executeQuery(StudentMapper.class, mapper->mapper.queryStudents(criteria));
    }

    public int updateStudentStatus(String studentId, String newStatus) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.updateStatus(studentId, newStatus));
    }

    public int insertStudent(Student student) {
        return MyBatisUtil.executeUpdate(StudentMapper.class, mapper->mapper.insert(student));
    }
}
