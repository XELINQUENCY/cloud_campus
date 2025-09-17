package view;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 视图对象 (View Object)，用于封装学生已选课程的详细信息。
 * 它继承自 CourseOfferingVO，并增加了学生选课特有的属性。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentCourseDetailVO extends CourseOfferingVO {

    // --- 来自 StudentCourse 的附加信息 ---
    private Double score; // 使用 Double 类型以允许成绩为空 (NULL)
    private String status;
    private String enrollmentType;
}
