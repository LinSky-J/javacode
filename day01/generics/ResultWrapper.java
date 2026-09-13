package generics;

/**
 * 泛型类实战演示：企业级统一 API 响应包装类。
 *
 * 核心考点：
 * 1. 泛型类定义：
 *    - 在类名后增加类型参数声明，如 class ResultWrapper<T>。
 *    - T 称为类型形参（Type Parameter），代表调用方未来传入的任意具体对象类型。
 * 2. 静态泛型方法辨析：
 *    - 静态方法【不能直接使用类声明的泛型形参 T】！
 *      因为静态成员随着类加载而初始化，此时用户根本没有创建对象，T 的具体类型尚未确定。
 *    - 静态方法如果想要使用泛型，必须自己独立在修饰符和返回值之间声明泛型形参，如 public static <E> ResultWrapper<E> success(E data)。
 *
 * @param <T> 业务数据载体类型
 * @author InterviewGuide
 * @version 1.0
 */
public class ResultWrapper<T> {

    private int code;
    private String message;
    private T data; // 泛型实例成员变量

    public ResultWrapper(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 静态泛型方法：成功响应快捷构建工厂
     * 注意：方法前部的 <E> 是该静态方法独立声明的泛型类型参数，与类级别的 <T> 完全独立！
     *
     * @param <E> 响应数据类型
     * @param data 业务数据
     * @return 统一格式包装对象
     */
    public static <E> ResultWrapper<E> success(E data) {
        return new ResultWrapper<>(200, "操作成功", data);
    }

    /**
     * 静态错误响应构建方法
     *
     * @param <E> 响应数据类型
     * @param code 状态码
     * @param message 错误信息
     * @return 统一格式包装对象
     */
    public static <E> ResultWrapper<E> fail(int code, String message) {
        return new ResultWrapper<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ResultWrapper{code=" + code + ", message='" + message + "', data=" + data + "}";
    }
}
