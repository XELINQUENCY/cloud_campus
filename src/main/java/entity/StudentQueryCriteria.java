package entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentQueryCriteria {
    String studentId;
    String name;
    String classId;
    String majorId;
    Integer enrollYear;
    String status;
}
