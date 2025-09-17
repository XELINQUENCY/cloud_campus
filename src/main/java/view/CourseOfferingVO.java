package view;

import entity.course.Course;
import entity.course.Teacher;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视图对象 (View Object)，用于封装可供选择的课程班级的详细信息。
 * 它聚合了 TeacherCourse, Course, 和 Teacher 的信息。
 */
@Data
@NoArgsConstructor
public class CourseOfferingVO {

    // --- 来自 TeacherCourse 的核心信息 ---
    private int teachingId;
    private int maxCapacity;
    private int currentStudents;
    private String weeks;
    private int weekday;
    private String classPeriod;
    private String location;
    private String semester;

    // --- 关联的实体对象 ---
    private Course course;
    private Teacher teacher;
}
