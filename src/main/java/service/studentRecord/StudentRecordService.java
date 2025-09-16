package service.studentRecord;

import entity.User;
import entity.studentRecord.StudentRecord;
import java.util.List;

/**
 * 学籍管理服务接口
 * 定义了所有与学籍相关的业务操作，包括权限校验。
 */
public interface StudentRecordService {

    /**
     * 根据学号查询学籍记录。
     * @param authenticatedUser 发起请求的、已经过身份验证的用户实体。
     * @param studentId 要查询的学号。
     * @return 找到的学籍记录。
     * @throws Exception 如果用户无权访问或记录不存在。
     */
    StudentRecord getRecord(User authenticatedUser, String studentId) throws Exception;

    /**
     * 创建一条新的学籍记录 (仅管理员)。
     * @param authenticatedUser 发起请求的用户。
     * @param recordToCreate 包含新学籍信息的实体对象。
     * @return 操作成功返回 true。
     * @throws Exception 如果用户不是管理员或输入数据无效。
     */
    boolean createRecord(User authenticatedUser, StudentRecord recordToCreate) throws Exception;

    /**
     * 更新学籍记录。
     * @param authenticatedUser 发起请求的用户。
     * @param recordToUpdate 包含待更新学籍信息的实体对象。
     * @return 操作成功返回 true。
     * @throws Exception 如果用户无权更新该记录。
     */
    boolean updateRecord(User authenticatedUser, StudentRecord recordToUpdate) throws Exception;

    /**
     * 根据学号删除学籍记录 (仅管理员)。
     * @param authenticatedUser 发起请求的用户。
     * @param studentIdToDelete 要删除的学号。
     * @return 操作成功返回 true。
     * @throws Exception 如果用户不是管理员。
     */
    boolean deleteRecord(User authenticatedUser, String studentIdToDelete) throws Exception;

    /**
     * 查询指定学生已修的课程列表。
     * @param authenticatedUser 发起请求的用户。
     * @param studentId 要查询的学号。
     * @return 课程 ID 的列表。
     * @throws Exception 如果用户无权访问。
     */
    List<String> listCompletedCourses(User authenticatedUser, String studentId) throws Exception;

    /**
     * 为学生添加一门已修课程。
     * @param authenticatedUser 发起请求的用户。
     * @param studentId 要操作的学号。
     * @param courseId 要添加的课程号。
     * @return 操作成功返回 true。
     * @throws Exception 如果用户无权操作。
     */
    boolean addCompletedCourse(User authenticatedUser, String studentId, String courseId) throws Exception;

    /**
     * 为学生移除一门已修课程。
     * @param authenticatedUser 发起请求的用户。
     * @param studentId 要操作的学号。
     * @param courseId 要移除的课程号。
     * @return 操作成功返回 true。
     * @throws Exception 如果用户无权操作。
     */
    boolean removeCompletedCourse(User authenticatedUser, String studentId, String courseId) throws Exception;
}