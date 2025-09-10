package mapper;

import entity.Student;
import entity.StudentQueryCriteria;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {
    /**
     * 根据学号查询学生信息
     * @param studentId 学号 [cite: 378]
     * @return 学生对象
     */
    Student findById(@Param("studentId")String studentId);

    /**
     * 根据多条件组合查询学生信息
     * @param criteria 封装的条件查询类
     * @return 符合条件的学生列表
     */
    List<Student> queryStudents(StudentQueryCriteria criteria);

    /**
     * 更新学生的学籍状态
     * @param studentId 学号
     * @param newStatus 新的学籍状态 [cite: 378, 387]
     * @return 受影响的行数
     */
    int updateStatus(@Param("studentId")String studentId, @Param("newStatus")String newStatus);

    /**
     * (管理员) 录入一个新学生
     * @param student 学生对象
     * @return 受影响的行数
     */
    int insert(Student student);
}
