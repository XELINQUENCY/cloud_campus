package DAO.course;

import mapper.TeachingCourseMapper;
import DAO.MyBatisUtil; // 确保这是您提供的 MyBatisUtil
import view.CourseOfferingVO;
import entity.course.TeacherCourse;

import java.util.List;

public class TeachingCourseDAO {
    public List<CourseOfferingVO> findAvailableCourses(String semester, String courseName, String teacherName, String department) {
        return MyBatisUtil.executeQuery(
                TeachingCourseMapper.class,
                mapper -> mapper.findAvailableCourses(semester, courseName, teacherName, department)
        );
    }

    public TeacherCourse findCapacityById(int teachingId) {
        return MyBatisUtil.executeQuery(
                TeachingCourseMapper.class,
                mapper -> mapper.findCapacityById(teachingId)
        );
    }

    public int incrementStudentCount(int teachingId) {
        return MyBatisUtil.executeUpdate(
                TeachingCourseMapper.class,
                mapper -> mapper.incrementStudentCount(teachingId)
        );
    }

    public int decrementStudentCount(int teachingId) {
        return MyBatisUtil.executeUpdate(
                TeachingCourseMapper.class,
                mapper -> mapper.decrementStudentCount(teachingId)
        );
    }
}
