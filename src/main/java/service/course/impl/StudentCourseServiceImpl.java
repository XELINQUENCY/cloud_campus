// 文件路径: src/main/java/com/yourproject/service/impl/StudentCourseServiceImpl.java
package service.course.impl;

import DAO.course.StudentCourseDAO;
import DAO.course.TeachingCourseDAO;
import service.course.exception.CourseFullException;
import service.course.exception.CourseSelectionException;
import service.course.exception.TimeConflictException;
import mapper.StudentCourseMapper;
import mapper.TeachingCourseMapper;
import service.course.StudentCourseService;
import service.course.ScheduleParser; // 导入新的工具类
import view.StudentCourseDetailVO;
import entity.course.StudentCourse;
import entity.course.TeacherCourse;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StudentCourseServiceImpl implements StudentCourseService {

    private final SqlSessionFactory sqlSessionFactory;
    private final StudentCourseDAO studentCourseDAO;
    private final TeachingCourseDAO teachingCourseDAO;

    public StudentCourseServiceImpl(SqlSessionFactory sqlSessionFactory, StudentCourseDAO studentCourseDAO, TeachingCourseDAO teachingCourseDAO) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.studentCourseDAO = studentCourseDAO;
        this.teachingCourseDAO = teachingCourseDAO;
    }

    @Override
    public void selectCourse(String studentId, int teachingId, String semester) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false); // 开启事务，手动提交
        try {
            // 从手动管理的 session 中获取 Mapper 实例
            TeachingCourseMapper teachingCourseMapper = sqlSession.getMapper(TeachingCourseMapper.class);
            StudentCourseMapper studentCourseMapper = sqlSession.getMapper(StudentCourseMapper.class);

            // === 步骤 1: 获取要选择的新课程的完整信息 ===
            TeacherCourse newCourse = teachingCourseMapper.findTeacherCourseById(teachingId);
            if (newCourse == null) {
                throw new CourseSelectionException("课程班级不存在！");
            }
            String courseId = newCourse.getCourseId();

            // === 步骤 2: 检查是否在本学期重复选课 ===
            Integer currentSemesterEnrollments = studentCourseMapper.countEnrollmentsInSemester(studentId, courseId, semester);
            if (currentSemesterEnrollments != null && currentSemesterEnrollments > 0) {
                throw new CourseSelectionException("您在本学期已选修过此课程，不可重复选择。");
            }

            // === 步骤 3: 检查课程容量 ===
            if (newCourse.getCurrentStudents() >= newCourse.getMaxCapacity()) {
                throw new CourseFullException("课程容量已满！");
            }

            // === 步骤 4: 检查上课时间冲突 ===
            // 4.1 获取学生在该学期已选的所有课程安排
            List<TeacherCourse> existingSchedules = studentCourseMapper.findSchedulesByStudentAndSemester(studentId, semester);

            // 4.2 解析新课程的时间字符串为数字集合
            Set<Integer> newWeeks = ScheduleParser.parseRangeString(newCourse.getWeeks());
            Set<Integer> newPeriods = ScheduleParser.parseRangeString(newCourse.getClassPeriod());

            // 4.3 遍历已选课程，进行逐一比较
            for (TeacherCourse existingCourse : existingSchedules) {
                // a. 检查星期是否相同，如果星期不同，则肯定不冲突，跳过
                if (existingCourse.getWeekday() == newCourse.getWeekday()) {

                    // b. 解析已选课程的时间字符串
                    Set<Integer> existingWeeks = ScheduleParser.parseRangeString(existingCourse.getWeeks());
                    Set<Integer> existingPeriods = ScheduleParser.parseRangeString(existingCourse.getClassPeriod());

                    // c. 检查周次和节次是否有交集。Collections.disjoint 如果没有交集返回true。
                    boolean weeksOverlap = !Collections.disjoint(newWeeks, existingWeeks);
                    boolean periodsOverlap = !Collections.disjoint(newPeriods, existingPeriods);

                    if (weeksOverlap && periodsOverlap) {
                        // 如果星期、周次、节次都冲突，则获取冲突课程的名称并抛出异常
                        String conflictCourseName = teachingCourseMapper.findCourseNameByTeachingId(existingCourse.getTeachingId());
                        throw new TimeConflictException(
                                "与已选课程 [" + conflictCourseName + "] 在周" + newCourse.getWeekday() + "的" +
                                        newCourse.getClassPeriod() + "节次发生时间冲突！"
                        );
                    }
                }
            }

            // === 步骤 5: 判断是首修还是重修 ===
            Integer pastCompletedCount = studentCourseMapper.countCompletedCoursesInPastSemesters(studentId, courseId, semester);
            String enrollmentType = (pastCompletedCount != null && pastCompletedCount > 0) ? "重修" : "首修";

            // === 步骤 6: 执行选课数据库操作 ===
            StudentCourse sc = new StudentCourse();
            sc.setStudentId(studentId);
            sc.setTeachingId(teachingId);
            sc.setEnrollmentType(enrollmentType);
            if (studentCourseMapper.insertStudentCourse(sc) == 0) {
                throw new CourseSelectionException("插入选课记录失败，请重试！");
            }

            // === 步骤 7: 增加课程已选人数 (乐观锁) ===
            if (teachingCourseMapper.incrementStudentCount(teachingId) == 0) {
                throw new CourseFullException("选课失败，课程容量刚刚被他人选满！");
            }

            // 所有操作成功，手动提交事务
            sqlSession.commit();

        } catch (Exception e) {
            // 发生任何异常，手动回滚事务
            sqlSession.rollback();

            // 为了让上层能捕获到具体的业务异常，我们直接重新抛出
            if (e instanceof CourseSelectionException || e instanceof TimeConflictException || e instanceof CourseFullException) {
                throw (RuntimeException) e;
            }
            // 对于其他未知异常，进行包装
            throw new CourseSelectionException("选课时发生未知错误: " + e.getMessage());
        } finally {
            // 无论成功或失败，都必须关闭 session
            sqlSession.close();
        }
    }

    @Override
    public void dropCourse(String studentId, int teachingId) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            StudentCourseMapper studentCourseMapper = sqlSession.getMapper(StudentCourseMapper.class);
            TeachingCourseMapper teachingCourseMapper = sqlSession.getMapper(TeachingCourseMapper.class);

            int deletedRows = studentCourseMapper.deleteStudentCourse(studentId, teachingId);
            if (deletedRows > 0) {
                teachingCourseMapper.decrementStudentCount(teachingId);
            }
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            throw new CourseSelectionException("退课失败: " + e.getMessage());
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<StudentCourseDetailVO> findMyCourses(String studentId, String semester) {
        // 这是一个只读操作，可以安全地使用DAO层
        return studentCourseDAO.findSelectedCoursesByStudent(studentId, semester);
    }
}