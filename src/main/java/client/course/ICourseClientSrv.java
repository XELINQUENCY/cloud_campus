package client.course;

import client.ApiException;
import view.CourseOfferingVO;
import view.StudentCourseDetailVO;

import java.util.List;

/**
 * 选课模块客户端服务统一接口。
 * 定义了所有选课相关的客户端操作，包括普通用户和管理员。
 */
public interface ICourseClientSrv {

    // --- 公共操作 ---
    /**
     * 根据条件查询当前学期可用的教学班列表。
     * @param semester 学期，例如 "2025-Fall"
     * @param courseName 课程名 (可选)
     * @param teacherName 教师名 (可选)
     * @param department 开课院系 (可选)
     * @return 符合条件的课程列表
     * @throws ApiException 如果API调用失败
     */
    List<CourseOfferingVO> browseCourses(String semester, String courseName, String teacherName, String department) throws ApiException;

    // --- 学生操作 ---
    /**
     * 查询当前学生在指定学期的已选课程（个人课表）。
     * @param semester 学期
     * @return 学生的已选课程详情列表
     * @throws ApiException 如果API调用失败
     */
    List<StudentCourseDetailVO> getMyCourses(String semester) throws ApiException;

    /**
     * 学生执行选课操作。
     * @param teachingId 教学班ID
     * @param semester 当前学期
     * @return 操作成功后的提示信息
     * @throws ApiException 如果选课失败（如课程已满、时间冲突等）
     */
    String selectCourse(int teachingId, String semester) throws ApiException;

    /**
     * 学生执行退课操作。
     * @param teachingId 教学班ID
     * @return 操作成功后的提示信息
     * @throws ApiException 如果退课失败
     */
    String dropCourse(int teachingId) throws ApiException;


    // --- 管理员操作 ---
    /**
     * 管理员为指定学生添加一门课程。
     * @param studentId 学生ID
     * @param teachingId 教学班ID
     * @param semester 学期
     * @return 操作成功后的提示信息
     * @throws ApiException 如果操作失败
     */
    String addCourseForStudent(String studentId, int teachingId, String semester) throws ApiException;

    /**
     * 管理员为指定学生移除一门课程。
     * @param studentId 学生ID
     * @param teachingId 教学班ID
     * @return 操作成功后的提示信息
     * @throws ApiException 如果操作失败
     */
    String removeCourseForStudent(String studentId, int teachingId) throws ApiException;

    /**
     * 管理员更新课程的容量。
     * @param teachingId 教学班ID
     * @param newCapacity 新的容量
     * @return 操作成功后的提示信息
     * @throws ApiException 如果更新失败
     */
    String updateCourseCapacity(int teachingId, int newCapacity) throws ApiException;
}
