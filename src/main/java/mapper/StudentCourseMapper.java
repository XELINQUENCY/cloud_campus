package mapper;

import entity.course.StudentCourse;
import org.apache.ibatis.annotations.Param;
import view.StudentCourseDetailVO;

import java.util.List;

/**
 * 学生选课记录 Mapper 接口
 */
public interface StudentCourseMapper {

    /**
     * 查询某个学生对某一门抽象课程(course_id)的历史修读次数。
     * 用于在选课时判断是“首修”还是“重修”。
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 修读次数
     */
    Integer countStudentCourseHistory(@Param("studentId") String studentId, @Param("courseId") String courseId);

    /**
     * 插入一条新的学生选课记录。
     * @param studentCourse 包含学生ID、教学班ID、选课类型等信息的对象
     * @return 受影响的行数
     */
    int insertStudentCourse(StudentCourse studentCourse);

    /**
     * 学生退课，根据学生ID和教学班ID删除一条选课记录。
     * @param studentId 学生ID
     * @param teachingId 教学班ID
     * @return 受影响的行数
     */
    int deleteStudentCourse(@Param("studentId") String studentId, @Param("teachingId") int teachingId);

    /**
     * 查询某个学生在指定学期的个人课表。
     * @param studentId 学生ID
     * @param semester 学期
     * @return 学生已选课程的详细信息列表
     */
    List<StudentCourseDetailVO> findSelectedCoursesByStudent(@Param("studentId") String studentId, @Param("semester") String semester);

    /**
     * 查询某个学生的全部修读历史和成绩单。
     * @param studentId 学生ID
     * @return 包含所有历史课程信息的列表
     */
    List<StudentCourseDetailVO> findGradeHistoryByStudent(@Param("studentId") String studentId);
}
