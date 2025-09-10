package DAO;

import entity.Course;
import mapper.CourseMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class CourseDAO {
    public Course getCourse(String courseId) {
        return MyBatisUtil.executeQuery(CourseMapper.class, mapper->mapper.findById(courseId));
    }

    public int chooseCourseAndUpdate(Course course) {
        // 使用默认的 openSession() 开启手动事务
        try (SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession()) {
            CourseMapper mapper = sqlSession.getMapper(CourseMapper.class);

            // 1. 悲观锁：查询并锁定课程记录
            Course c = mapper.findByIdForUpdate(course.getCourseId());

            // 2. 业务逻辑判断 (例如：检查课程是否存在，是否还有余量)
            if (c != null && c.getCurrentNum() < c.getMaxCapacity()) {
                // 3. 执行更新操作
                int result = mapper.updateCurrentNum(c.getCourseName(), 1);
                // 4. 检查更新是否成功影响了1行
                if (result == 1) {
                    // 所有操作成功，提交事务
                    sqlSession.commit();
                    return result; // 返回 1 代表成功
                } else {
                    // 更新影响了0行或多于1行，这通常是异常情况，应该回滚
                    sqlSession.rollback();
                    return 0; // 返回 0 代表失败
                }
            } else {
                // 课程不存在或已满，无需操作数据库，直接返回失败
                // 因为没有执行写操作，所以回滚是可选的，但执行一下是好习惯
                sqlSession.rollback();
                return 0; // 返回 0 代表失败 (课程不存在或已满)
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 返回 0 代表异常发生
        }
    }

    public List<Course> getAvailableCourses(String grade, String major) {
        return MyBatisUtil.executeQuery(CourseMapper.class, mapper->mapper.findAvailableCourses(grade, major));
    }

    public int insertCourse(Course course) {
        return MyBatisUtil.executeUpdate(CourseMapper.class, mapper->mapper.insert(course));
    }

    public int updateCourse(Course course) {
        return MyBatisUtil.executeUpdate(CourseMapper.class, mapper->mapper.update(course));
    }

    public int deleteCourse(String courseId) {
        return MyBatisUtil.executeUpdate(CourseMapper.class, mapper->mapper.delete(courseId));
    }
}
