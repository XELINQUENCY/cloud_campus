package entity.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourse {

    //选课记录编号
    int enrollmentId;

    //外键，关联学生id
    String studentId;

    //外键，关联教师-课程id
    int teachingId;

    //选课类别
    String enrollmentType;

    //最终得分
    double score;

    //选课状态
    String status;

    //选课时间
    LocalDateTime selectionTime;
}
