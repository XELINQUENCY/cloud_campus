package service.schoolroll.exception;

/**
 * 表示因权限不足而禁止操作的异常。
 * Controller 层捕获此异常时，通常应返回 HTTP 403 Forbidden 状态码。
 */
public class ForbiddenException extends Exception {
    public ForbiddenException(String message) {
        super(message);
    }
}
