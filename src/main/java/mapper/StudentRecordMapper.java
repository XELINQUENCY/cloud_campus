package mapper;

import entity.studentRecord.StudentRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentRecordMapper {
    int insert(StudentRecord record);
    int update(StudentRecord record);
    int delete(@Param("studentId") String studentId);
    StudentRecord findById(@Param("studentId") String studentId);
    List<StudentRecord> findByMajor(@Param("majorId") String majorId);

    // completed courses
    List<String> listCompletedCourses(@Param("studentId") String studentId);
    int addCompletedCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);
    int removeCompletedCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);
}


