package DAO.course;

import entity.course.TeacherCourse;
import mapper.StudentCourseMapper;
import DAO.MyBatisUtil; // 确保这是您提供的 MyBatisUtil
import view.StudentCourseDetailVO;
import entity.course.StudentCourse;

import java.util.List;

public class StudentCourseDAO {

    public Integer countEnrollmentsInSemester(String studentId, String courseId, String semester) {
        return MyBatisUtil.executeQuery(
                StudentCourseMapper.class,
                mapper -> mapper.countEnrollmentsInSemester(studentId, courseId, semester)
        );
    }

    public Integer countCompletedCoursesInPastSemesters(String studentId, String courseId, String currentSemester) {
        return MyBatisUtil.executeQuery(
                StudentCourseMapper.class,
                mapper -> mapper.countCompletedCoursesInPastSemesters(studentId, courseId, currentSemester)
        );
    }

    public int insertStudentCourse(StudentCourse studentCourse) {
        return MyBatisUtil.executeUpdate(
                StudentCourseMapper.class,
                mapper -> mapper.insertStudentCourse(studentCourse)
        );
    }

    public int deleteStudentCourse(String studentId, int teachingId) {
        return MyBatisUtil.executeUpdate(
                StudentCourseMapper.class,
                mapper -> mapper.deleteStudentCourse(studentId, teachingId)
        );
    }

    public List<StudentCourseDetailVO> findSelectedCoursesByStudent(String studentId, String semester) {
        return MyBatisUtil.executeQuery(
                StudentCourseMapper.class,
                mapper -> mapper.findSelectedCoursesByStudent(studentId, semester)
        );
    }

    public List<StudentCourseDetailVO> findGradeHistoryByStudent(String studentId) {
        return MyBatisUtil.executeQuery(
                StudentCourseMapper.class,
                mapper -> mapper.findGradeHistoryByStudent(studentId)
        );
    }

    public List<TeacherCourse> findSchedulesByStudentAndSemester(String studentId, String semester) {
        return MyBatisUtil.executeQuery(
                StudentCourseMapper.class,
                mapper -> mapper.findSchedulesByStudentAndSemester(studentId, semester)
        );
    }
}
