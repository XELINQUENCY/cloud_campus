package service.course.impl;

import DAO.course.TeachingCourseDAO;
import service.course.exception.CourseSelectionException;
import mapper.TeachingCourseMapper;
import service.course.AdminCourseService;
import service.course.StudentCourseService;
import entity.course.TeacherCourse;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class AdminCourseServiceImpl implements AdminCourseService {

    private final SqlSessionFactory sqlSessionFactory;
    private final StudentCourseService studentCourseService;
    private final TeachingCourseDAO teachingCourseDAO;

    public AdminCourseServiceImpl(SqlSessionFactory sqlSessionFactory, StudentCourseService studentCourseService, TeachingCourseDAO teachingCourseDAO) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.studentCourseService = studentCourseService;
        this.teachingCourseDAO = teachingCourseDAO;
    }

    @Override
    public void addCourseForStudent(String studentId, int teachingId, String semester) {
        studentCourseService.selectCourse(studentId, teachingId, semester);
    }

    @Override
    public void removeCourseForStudent(String studentId, int teachingId) {
        studentCourseService.dropCourse(studentId, teachingId);
    }

    @Override
    public void updateCourseCapacity(int teachingId, int newCapacity) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            TeachingCourseMapper teachingCourseMapper = sqlSession.getMapper(TeachingCourseMapper.class);

            TeacherCourse course = teachingCourseMapper.findCapacityById(teachingId);
            if (course == null) throw new CourseSelectionException("课程班级不存在！");
            if (newCapacity < course.getCurrentStudents()) throw new CourseSelectionException("新容量不能小于当前已选学生数: " + course.getCurrentStudents());

            // 假设您已在TeachingCourseMapper.xml中添加了updateCapacity方法
            // teachingCourseMapper.updateCapacity(teachingId, newCapacity);

            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            throw e;
        } finally {
            sqlSession.close();
        }
    }
}