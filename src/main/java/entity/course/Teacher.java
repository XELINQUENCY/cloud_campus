package entity.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {

    // 主键：教师号
    String teacherId;

    // 教师姓名
    String teacherName;

    // 职称
    String title;

    // 联系方式
    String contactInfo;
}
