// 文件路径: src/main/java/com/yourproject/service/StudentCourseService.java
package service.course;

import view.StudentCourseDetailVO;
import java.util.List;

public interface StudentCourseService {
    /**
     * 学生选课 (需要事务保证)。
     */
    void selectCourse(String studentId, int teachingId, String semester);

    /**
     * 学生退课 (需要事务保证)。
     */
    void dropCourse(String studentId, int teachingId);

    /**
     * 查询学生在指定学期的个人课表。
     */
    List<StudentCourseDetailVO> findMyCourses(String studentId, String semester);
}