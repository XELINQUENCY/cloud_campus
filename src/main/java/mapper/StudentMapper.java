package mapper;

import dto.schoolroll.StudentDetailDTO;
import entity.schoolroll.Student;
import org.apache.ibatis.annotations.Param;
import service.schoolroll.StudentService;

import java.util.List;

public interface StudentMapper {

    Student getStudentByUserId(String UserId);

    // ========== 管理员：新增学生 ==========
    int insert(Student student);

    // ========== 学生：修改自己的部分信息 ==========
    int updateByStudent(Student student);

    // ========== 管理员：修改学生信息（除学号外）==========
    int updateByAdmin(Student student);

    // ========== 管理员：更新学生状态（软删除）==========
    int updateStatusById(@Param("studentId") String studentId, @Param("status") String status);

    // ========== 查询方法 ==========
    Student selectByStudentId(String studentId);
    List<Student> selectByName(@Param("name") String name);
    List<Student> selectByClassId(@Param("classId") String classId);
    List<Student> selectByMajorId(@Param("majorId") String majorId);
    List<Student> selectByEnrollYear(@Param("year") int year);

    // ========== 组合条件查询 ==========
    List<Student> selectByConditions(@Param("studentId") String studentId,
                                     @Param("name") String name,
                                     @Param("classId") String classId,
                                     @Param("majorId") String majorId,
                                     @Param("enrollYear") Integer enrollYear,
                                     @Param("status") String status);

    // ========== 连表查询：学生+班级名+专业名 ==========
    StudentDetailDTO selectStudentWithDetail(@Param("studentId") String studentId);

}
