package service.course.impl;

import DAO.course.StudentCourseDAO;
import DAO.course.TeachingCourseDAO;
import service.course.exception.CourseFullException;
import service.course.exception.CourseSelectionException;
import mapper.StudentCourseMapper;
import mapper.TeachingCourseMapper;
import service.course.StudentCourseService;
import view.StudentCourseDetailVO;
import entity.course.StudentCourse;
import entity.course.TeacherCourse;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import java.util.List;

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
        // **手动开启事务**
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            // **从手动管理的 session 中获取 Mapper**
            TeachingCourseMapper teachingCourseMapper = sqlSession.getMapper(TeachingCourseMapper.class);
            StudentCourseMapper studentCourseMapper = sqlSession.getMapper(StudentCourseMapper.class);

            // 1. 检查课程容量 (读操作)
            TeacherCourse targetCourse = teachingCourseMapper.findCapacityById(teachingId);
            if (targetCourse == null) throw new CourseSelectionException("课程不存在！");
            if (targetCourse.getCurrentStudents() >= targetCourse.getMaxCapacity()) throw new CourseFullException("课程容量已满！");

            // 2. 检查时间冲突 (读操作) - 此处省略具体逻辑

            // 3. 判断是首修还是重修 (读操作)
            String courseId = targetCourse.getCourseId(); // 假设TeacherCourse实体有courseId
            Integer historyCount = studentCourseMapper.countStudentCourseHistory(studentId, courseId);
            String enrollmentType = (historyCount == null || historyCount == 0) ? "首修" : "重修";

            // 4. 执行选课 (写操作)
            StudentCourse sc = new StudentCourse();
            sc.setStudentId(studentId);
            sc.setTeachingId(teachingId);
            sc.setEnrollmentType(enrollmentType);
            if (studentCourseMapper.insertStudentCourse(sc) == 0) throw new CourseSelectionException("插入选课记录失败，请重试！");
            if (teachingCourseMapper.incrementStudentCount(teachingId) == 0) throw new CourseFullException("选课失败，课程容量刚刚被他人选满！");

            // **所有操作成功，手动提交事务**
            sqlSession.commit();
        } catch (Exception e) {
            // **发生任何异常，手动回滚事务**
            sqlSession.rollback();
            throw e; // 将原始异常继续向上抛出
        } finally {
            // **无论成功或失败，都必须关闭 session**
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
            throw e;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<StudentCourseDetailVO> findMyCourses(String studentId, String semester) {
        // 只读操作，安全地使用 DAO -> MyBatisUtil
        return studentCourseDAO.findSelectedCoursesByStudent(studentId, semester);
    }
}