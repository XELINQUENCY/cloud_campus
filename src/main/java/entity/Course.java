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
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public String getApplyGrade() {
		return applyGrade;
	}
	public void setApplyGrade(String applyGrade) {
		this.applyGrade = applyGrade;
	}
	public String getApplyMajor() {
		return applyMajor;
	}
	public void setApplyMajor(String applyMajor) {
		this.applyMajor = applyMajor;
	}
	public int getMaxCapacity() {
		return maxCapacity;
	}
	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	public int getCurrentNum() {
		return currentNum;
	}
	public void setCurrentNum(int currentNum) {
		this.currentNum = currentNum;
	}
	public Map<Integer, Integer> getClassTime() {
		return classTime;
	}
	public void setClassTime(Map<Integer, Integer> classTime) {
		this.classTime = classTime;
	}
	public CourseStatus getStatus() {
		return status;
	}
	public void setStatus(CourseStatus status) {
		this.status = status;
	}
	public LocalDateTime getCreateTime() {
		return createTime;
	}
	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
	
}
