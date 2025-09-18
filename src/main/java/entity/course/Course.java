package entity.course;

import entity.schoolroll.Major;
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

    //专业（存储一个实体类）
    Major major;

    //学分
    double credits;
}
