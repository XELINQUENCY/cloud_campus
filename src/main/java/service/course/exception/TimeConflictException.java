package service.course.exception;

public class TimeConflictException extends CourseSelectionException {
    public TimeConflictException(String message) {
        super(message);
    }
}