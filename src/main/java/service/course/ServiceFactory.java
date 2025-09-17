package service.course;

import DAO.course.StudentCourseDAO;
import DAO.course.TeachingCourseDAO;
import service.course.impl.AdminCourseServiceImpl;
import service.course.impl.CourseBrowseServiceImpl;
import service.course.impl.StudentCourseServiceImpl;
import DAO.MyBatisUtil;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * 服务工厂，使用单例模式创建和管理所有服务实例。
 */
public class ServiceFactory {

    private static final ServiceFactory INSTANCE = new ServiceFactory();

    private final AdminCourseService adminCourseService;
    private final CourseBrowseService courseBrowseService;
    private final StudentCourseService studentCourseService;

    private ServiceFactory() {
        // 获取核心依赖
        SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();

        // 创建 DAO 实例
        StudentCourseDAO studentCourseDAO = new StudentCourseDAO();
        TeachingCourseDAO teachingCourseDAO = new TeachingCourseDAO();

        // 创建 Service 实例并注入依赖
        this.courseBrowseService = new CourseBrowseServiceImpl(teachingCourseDAO);
        this.studentCourseService = new StudentCourseServiceImpl(sqlSessionFactory, studentCourseDAO, teachingCourseDAO);
        this.adminCourseService = new AdminCourseServiceImpl(sqlSessionFactory, this.studentCourseService, teachingCourseDAO);
    }

    public static ServiceFactory getInstance() {
        return INSTANCE;
    }

    public AdminCourseService getAdminCourseService() {
        return adminCourseService;
    }

    public CourseBrowseService getCourseBrowseService() {
        return courseBrowseService;
    }

    public StudentCourseService getStudentCourseService() {
        return studentCourseService;
    }
}