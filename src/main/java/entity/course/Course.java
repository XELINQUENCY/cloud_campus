package entity.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    //课程ID
    String courseId;

    //课程名字
    String courseName;

    //专业
    String department;

    //学分
    double credit;
}
