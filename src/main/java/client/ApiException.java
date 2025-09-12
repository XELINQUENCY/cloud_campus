package client;

/**
 * 自定义异常类，用于封装API调用过程中发生的错误。
 * 这使得GUI层可以捕获一种特定类型的异常，并从中获取对用户友好的错误信息。
 */
public class ApiException extends Exception {

    /**
     * 构造函数
     * @param message 错误信息，将直接显示给用户。
     */
    public ApiException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * @param message 错误信息
     * @param cause   原始的底层异常（如IOException），用于调试。
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
