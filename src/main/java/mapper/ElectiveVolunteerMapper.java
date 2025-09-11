package mapper;

import entity.ElectiveVolunteer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ElectiveVolunteerMapper {
    /**
     * 批量插入学生的志愿记录
     * @param volunteers 志愿列表
     * @return 受影响的行数
     */
    int batchInsert(List<ElectiveVolunteer> volunteers);

    /**
     * 删除某个学生的所有志愿（用于重新提交志愿）
     * @param studentId 学生ID
     * @return 受影响的行数
     */
    int deleteByStudentId(@Param("studentId") String studentId);

    /**
     * (管理员) 获取某课程所有学生的志愿信息，用于执行抽选算法
     * @return 所有的志愿记录列表
     */
    List<ElectiveVolunteer> findByCourseId(@Param("courseId") String courseId);
}
