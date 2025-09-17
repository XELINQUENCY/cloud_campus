package service.course;

import view.CourseOfferingVO;
import java.util.List;

public interface CourseBrowseService {
    /**
     * 根据条件查询可用的教学班列表。
     */
    List<CourseOfferingVO> findAvailableCourses(String semester, String courseName, String teacherName, String department);
}