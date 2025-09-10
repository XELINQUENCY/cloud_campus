package entity;

import enums.CourseStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class Course {
    private String courseId;
    private String courseName;
    private String applyGrade;
    private String applyMajor;

    private int maxCapacity;
    private int currentNum;
    private Map<Integer, Integer> classTime;
    private CourseStatus status;
    private LocalDateTime createTime;
}
