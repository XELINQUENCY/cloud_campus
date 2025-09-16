package dto.schoolroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetailDTO {
    private String studentId;
    private String name;
    private String gender;
    private String classId;
    private String className;   // 来自 StudentClass
    private String majorId;
    private String majorName;   // 来自 Major
    private String status;
    private LocalDate enrollDate;
}
