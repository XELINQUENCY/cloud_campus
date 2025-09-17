package service.schoolroll.exception;

/**
 * 表示因客户端请求无效（如参数缺失、格式错误）导致的异常。
 * Controller 层捕获此异常时，通常应返回 HTTP 400 Bad Request 状态码。
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
