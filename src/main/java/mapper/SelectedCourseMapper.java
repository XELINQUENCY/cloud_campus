package mapper;

import entity.SelectedCourse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SelectedCourseMapper {
    /**
     * 插入一条新的选课记录
     * @param record 选课记录对象
     * @return 受影响的行数
     */
    int insert(SelectedCourse record);

    /**
     * 批量插入选课记录（用于志愿抽选结束后）
     * @param records 选课记录列表
     * @return 受影响的行数
     */
    int batchInsert(List<SelectedCourse> records);

    /**
     * 学生退课时，删除对应的选课记录 [cite: 477]
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 受影响的行数
     */
    int deleteByStudentAndCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);

    /**
     * 查询某个学生已选的所有课程记录 [cite: 477]
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<SelectedCourse> findByStudentId(@Param("studentId") String studentId);

    /**
     * 检查某个学生是否已选了某门课
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 如果存在记录，返回该记录；否则返回null
     */
    SelectedCourse findByStudentAndCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);
}
