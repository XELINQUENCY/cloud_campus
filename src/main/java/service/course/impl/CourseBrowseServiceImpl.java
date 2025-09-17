package service.course.impl;

import DAO.course.TeachingCourseDAO;
import service.course.CourseBrowseService;
import view.CourseOfferingVO;
import java.util.List;

public class CourseBrowseServiceImpl implements CourseBrowseService {

    private final TeachingCourseDAO teachingCourseDAO;

    public CourseBrowseServiceImpl(TeachingCourseDAO teachingCourseDAO) {
        this.teachingCourseDAO = teachingCourseDAO;
    }

    @Override
    public List<CourseOfferingVO> findAvailableCourses(String semester, String courseName, String teacherName, String department) {
        // 只读操作，安全地使用 DAO -> MyBatisUtil
        return teachingCourseDAO.findAvailableCourses(semester, courseName, teacherName, department);
    }
}