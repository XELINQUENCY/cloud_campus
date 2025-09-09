package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ElectiveVolunteer {
    private int volunteerId;

    private String studentId;
    private String courseId;
    private String volunteerLevel;
    private LocalDateTime submitTime;

}
