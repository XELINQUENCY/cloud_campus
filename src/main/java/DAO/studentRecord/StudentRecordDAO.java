package DAO.studentRecord;

import DAO.MyBatisUtil;
import entity.studentRecord.StudentRecord;
import mapper.StudentRecordMapper;

import java.util.List;

public class StudentRecordDAO {
    public int insert(StudentRecord record) {
        return MyBatisUtil.executeUpdate(StudentRecordMapper.class, mapper-> mapper.insert(record));
    }

    public int update(StudentRecord record) {
        return MyBatisUtil.executeUpdate(StudentRecordMapper.class, mapper-> mapper.update(record));
    }

    public int delete(String studentId) {
        return MyBatisUtil.executeUpdate(StudentRecordMapper.class, mapper-> mapper.delete(studentId));
    }

    public StudentRecord findById(String studentId) {
        return MyBatisUtil.executeQuery(StudentRecordMapper.class, mapper-> mapper.findById(studentId));
    }

    public List<StudentRecord> findByMajor(String majorId) {
        return MyBatisUtil.executeQuery(StudentRecordMapper.class, mapper-> mapper.findByMajor(majorId));
    }

    public List<String> listCompletedCourses(String studentId) {
        return MyBatisUtil.executeQuery(StudentRecordMapper.class, mapper-> mapper.listCompletedCourses(studentId));
    }

    public int addCompletedCourse(String studentId, String courseId) {
        return MyBatisUtil.executeUpdate(StudentRecordMapper.class, mapper-> mapper.addCompletedCourse(studentId, courseId));
    }

    public int removeCompletedCourse(String studentId, String courseId) {
        return MyBatisUtil.executeUpdate(StudentRecordMapper.class, mapper-> mapper.removeCompletedCourse(studentId, courseId));
    }
}


