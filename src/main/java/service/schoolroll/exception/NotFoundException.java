package service.schoolroll.exception;

/**
 * 表示请求的资源未找到的异常。
 * Controller 层捕获此异常时，通常应返回 HTTP 404 Not Found 状态码。
 */
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
