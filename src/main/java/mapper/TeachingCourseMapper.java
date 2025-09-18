package mapper;

import entity.course.TeacherCourse;
import org.apache.ibatis.annotations.Param;
import view.CourseOfferingVO;

import java.util.List;

/**
 * 教学安排/教学班 Mapper 接口
 */
public interface TeachingCourseMapper {

    /**
     * 根据学期和筛选条件，查询所有可供选择的教学班列表。
     * @param semester 学期，例如 "2025-Fall"
     * @param courseName 课程名 (模糊查询)
     * @param teacherName 教师名 (模糊查询)
     * @param department 开课院系
     * @return 教学班详细信息列表
     */
    List<CourseOfferingVO> findAvailableCourses(
            @Param("semester") String semester,
            @Param("courseName") String courseName,
            @Param("teacherName") String teacherName,
            @Param("department") String department
    );

    String findCourseNameByTeachingId(int teachingId);

    /**
     * 根据教学班ID查询容量信息。
     * @param teachingId 教学班ID
     * @return 包含容量和当前人数的 TeacherCourse 对象
     */
    TeacherCourse findTeacherCourseById(@Param("teachingId") int teachingId);

    /**
     * 对指定的教学班，将当前已选人数加一。
     * 这是一个原子操作，通过 WHERE 条件防止超选。
     * @param teachingId 教学班ID
     * @return 受影响的行数 (1 表示成功, 0 表示因满员或其他原因失败)
     */
    int incrementStudentCount(@Param("teachingId") int teachingId);

    /**
     * 对指定的教学班，将当前已选人数减一。
     * @param teachingId 教学班ID
     * @return 受影响的行数
     */
    int decrementStudentCount(@Param("teachingId") int teachingId);
}
