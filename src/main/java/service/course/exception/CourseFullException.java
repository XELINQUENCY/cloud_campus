package service.course.exception;

public class CourseFullException extends CourseSelectionException {
    public CourseFullException(String message) {
        super(message);
    }
}