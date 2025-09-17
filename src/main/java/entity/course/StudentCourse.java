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
    int enrollment_id;

    //外键，关联学生id
    String student_id;

    //外键，关联教师-课程id
    int teaching_id;

    //选课类别
    String enrollment_type;

    //最终得分
    double score;

    //选课状态
    String status;

    //选课时间
    LocalDateTime selectionTime;
}
