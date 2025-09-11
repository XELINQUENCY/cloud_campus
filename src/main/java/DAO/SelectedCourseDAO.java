package DAO;

import entity.SelectedCourse;
import mapper.SelectedCourseMapper;
import mapper.StudentMapper;

import java.util.List;

public class SelectedCourseDAO {
    public int insert(SelectedCourse selectedCourse) {
        return MyBatisUtil.executeUpdate(SelectedCourseMapper.class, mapper->mapper.insert(selectedCourse));
    }

    public int batchInsert(List<SelectedCourse> records) {
        return MyBatisUtil.executeUpdate(SelectedCourseMapper.class, mapper->mapper.batchInsert(records));
    }

    public int deleteByStudentAndCourse(String studentId, String courseId) {
        return MyBatisUtil.executeUpdate(SelectedCourseMapper.class, mapper->mapper.deleteByStudentAndCourse(studentId, courseId));
    }

    public List<SelectedCourse> findByStudentId(String studentId) {
        return MyBatisUtil.executeQuery(SelectedCourseMapper.class, mapper->mapper.findByStudentId(studentId));
    }

    public SelectedCourse findByStudentAndCourse(String studentId, String courseId) {
        return MyBatisUtil.executeQuery(SelectedCourseMapper.class, mapper->mapper.findByStudentAndCourse(studentId, courseId));
    }
}