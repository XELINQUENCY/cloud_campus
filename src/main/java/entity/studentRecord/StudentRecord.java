package entity.studentRecord;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentRecord {
    private String studentId;
    private String name;
    private String grade;
    private String majorId;
    private List<String> completedCourses;
}