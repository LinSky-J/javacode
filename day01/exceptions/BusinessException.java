package exceptions;

/**
 * 自定义企业级业务异常类：演示在实际业务开发中通过 throw 抛出可控非受检异常。
 *
 * 核心考点：
 * 1. 继承自 RuntimeException（非受检运行时异常），业务代码无需强制使用 try-catch 包裹，
 *    通常配合全局异常处理器（如 Spring 的 @RestControllerAdvice）实现标准化错误拦截。
 * 2. 携带标准化业务错误码（errorCode）与提示信息（errorMessage）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    private final int errorCode;
    private final String errorMessage;

    public BusinessException(int errorCode, String errorMessage) {
        super("[错误码: " + errorCode + "] " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
