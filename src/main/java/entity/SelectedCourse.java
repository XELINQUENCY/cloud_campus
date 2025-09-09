package entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SelectedCourse {
    private int recordId;
    private String studentId;
    private String courseId;
    private String selectMode;
    private String selectStatus;
    private LocalDateTime createTime;
}
