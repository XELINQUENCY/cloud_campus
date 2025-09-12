package service;

import DAO.CourseDAO;
import DAO.SelectedCourseDAO;
import entity.Course;
import entity.SelectedCourse;

import java.util.List;

public class SelectionService{

    // Service 依赖 DAO，通过构造函数注入
    private final CourseDAO courseDAO;
    private final SelectedCourseDAO selectedCourseDAO;

    public SelectionService(CourseDAO courseDAO, SelectedCourseDAO selectedCourseDAO) {
        this.courseDAO = courseDAO;
        this.selectedCourseDAO = selectedCourseDAO;
    }

    public boolean verifyIdentity(String userId, String password, String role){
        return true;
    }

    public boolean selectCourseRealTime(int studentId, String courseId) throws Exception {
        // --- 开始业务逻辑校验 ---

        // 1. 检查课程是否存在
        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new Exception("课程不存在 (Course not found)");
        }

        // 2. 检查课程是否已满
        if (course.getCurrentSelected() >= course.getMaxCapacity()) {
            throw new Exception("课程已满 (Course is full)");
        }

        // 3. 检查学生是否已经选过这门课
        List<SelectedCourse> myCourses = selectedCourseDAO.findByStudentId(studentId);
        for (SelectedCourse sc : myCourses) {
            if (sc.getCourseId().equals(courseId)) {
                throw new Exception("您已经选过此课程 (Course already selected)");
            }
        }

        // 4. (扩展) 在这里可以增加更多校验，如时间冲突检查、前置课程检查等...

        // --- 业务逻辑校验通过，开始执行数据库操作 ---

        // 这是一个事务性操作：需要同时更新两个表
        // 在实际项目中，这里会启动事务

        // a. 增加课程的已选人数
        int updatedRows = courseDAO.incrementCurrentNum(courseId);
        if (updatedRows == 0) {
            // 如果更新失败（可能并发导致），则选课失败
            throw new Exception("选课失败，请重试 (Failed to select course, please retry)");
        }

        // b. 在选课记录表中插入一条新纪录
        SelectedCourse newRecord = new SelectedCourse();
        newRecord.setStudentId(studentId);
        newRecord.setCourseId(courseId);
        newRecord.setSelectStatus(SelectStatus.SELECTED);
        int savedRows = selectedCourseDAO.save(newRecord);

        // 如果保存失败，理论上应该回滚上面的操作（事务的重要性）
        if (savedRows == 0) {
            // 此处应有事务回滚逻辑
            throw new Exception("保存选课记录失败 (Failed to save selection record)");
        }

        return true;
    }

    @Override
    public List<SelectedCourse> getMyCourses(int studentId) {
        return selectedCourseDAO.findByStudentId(studentId);
    }
}