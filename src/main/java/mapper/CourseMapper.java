package mapper;

import entity.Course;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CourseMapper {
    /**
     * 根据课程ID查找课程
     * @param courseId 课程ID
     * @return 课程对象
     */
    Course findById(String courseId);

    /**
     * (悲观锁) 查询并锁定一行课程记录，用于处理高并发选课，防止超选
     * @param courseId 课程ID
     * @return 课程对象
     */
    Course findByIdForUpdate(String courseId);

    /**
     * 查询学生可选的课程列表
     * @param grade 学生的适用年级
     * @param major 学生的适用专业
     * @return 可选的、且容量未满的课程列表
     */
    List<Course> findAvailableCourses(@Param("grade") String grade, @Param("major") String major);

    /**
     * 更新课程的当前已选人数（原子操作）
     * @param courseId 课程ID
     * @param changeAmount 人数变化量 (+1 代表选课, -1 代表退课)
     * @return 受影响的行数
     */
    int updateCurrentNum(@Param("courseId") String courseId, @Param("changeAmount") int changeAmount);

    /**
     * (管理员) 维护课程（增删改）
     */
    int insert(Course course);
    int update(Course course);
    int delete(String courseId);
}

