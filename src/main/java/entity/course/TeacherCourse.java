package entity.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourse {

    // 主键：教授记录 ID
    int teachingId;

    // 外键，关联到 course 表
    String courseId;

    // 外键，关联到 teacher 表
    String teacherId;

    // 具体的教学安排信息

    // 班级最大容量
    int maxCapacity;

    // 当前学生数量
    int currentStudents;

    // 上课周数
    String weeks;

    // 上课星期
    int weekday;

    // 上课时间段（0304节）
    String classPeriod;

    // 上课地址
    String location;

    // 学期 例如: '2025-Fall' 或 '2026-Spring'
    String semester;
}
