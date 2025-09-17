package service.course.exception;

// 自定义业务异常的基类
public class CourseSelectionException extends RuntimeException {
    public CourseSelectionException(String message) {
        super(message);
    }
}