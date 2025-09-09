package entity;

import enums.CourseStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class Courses {
    private String courseId;
    private String courseName;
    private String applyGrade;
    private String applyMajor;

    private int maxCapacity;
    private int currentCapacity;
    private Map<Integer, Integer> classTime;
    private CourseStatus status;
    private LocalDateTime createTime;
}
