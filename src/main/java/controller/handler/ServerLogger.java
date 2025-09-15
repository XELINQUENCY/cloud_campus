package controller.handler;

/**
 * 一个函数式接口，用于抽象日志记录操作。
 * 允许将日志记录的具体实现（如输出到控制台、文件或UI）
 * 从业务逻辑处理器中解耦。
 */
@FunctionalInterface
public interface ServerLogger {
    /**
     * 记录一条日志消息。
     * @param message 要记录的消息内容。
     */
    void log(String message);
}