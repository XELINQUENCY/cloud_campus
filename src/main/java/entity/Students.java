package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Students {
    private String studentId;
    private String Name;
    private String Gender;
    private String classId;
    private String majorId;
    private String status;
    private LocalDate enrollDate;
}
