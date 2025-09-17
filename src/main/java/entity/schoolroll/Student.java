package entity.schoolroll;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Student {
    private String studentId;
    private String mainUserId;
    private String name;
    private String gender;
    private String classId;
    private String majorId;
    private String status;
    private LocalDate enrollDate;
}
