package exceptions;

import java.io.IOException;

/**
 * 账户支付服务业务类：专门用于演示【抛出异常为什么不用throws?】的反模式与企业级最佳实践。
 *
 * 核心考点：
 * 1. 反模式（盲目使用 throws）：
 *    - 在方法签名上声明 throws IOException / SQLException，严重污染调用方接口契约；
 *    - 暴露底层技术实现细节（破坏面向对象封装性）；
 *    - 丧失在异常发生的第一现场进行降级处理、故障隔离与参数校验的最佳时机。
 *
 * 2. 最佳实践（异常转译与异常链 Exception Chaining）：
 *    - 在当前业务层捕获不可控的技术受检异常；
 *    - 包装为业务含义清晰的运行时异常（BusinessException），并传入原始异常 root cause；
 *    - 切断受检异常向下传播链条，交给全局异常处理器统一兜底。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class AccountPaymentService {

    /**
     * 反模式示范：盲目使用 throws 抛出技术异常
     * 弊端：调用方必须显式处理 IOException，若底层改用其他存储或网络组件，调用方代码将被迫大面积重构！
     *
     * @param amount 支付金额
     * @throws IOException 技术底层受检异常
     */
    public void payWithThrowsAntiPattern(double amount) throws IOException {
        System.out.println("   [反模式执行] 触发底层网络连接...");
        throw new IOException("网络底层连接超时（套接字超时）！");
    }

    /**
     * 企业级标准实践：异常转译（Exception Translation）与异常链绑定
     * 优势：
     * 1. 业务调用方无需在方法上写恶心的 throws 声明；
     * 2. 保留底层原始异常堆栈（root cause）；
     * 3. 赋予明确的业务错误码与友好提示。
     *
     * @param amount 支付金额
     */
    public void payWithExceptionTranslation(double amount) {
        try {
            System.out.println("   [最佳实践执行] 尝试建立银行通道划扣连接...");
            // 模拟底层技术异常
            throw new IOException("银行网关 Socket 连接重置");
        } catch (IOException e) {
            System.out.println("   [局部现场捕获] 捕获到底层技术异常，准备执行异常转译...");
            // 异常转译：将底层 IOException 包装为上层可识别的业务异常，并保留原始异常原因 e
            BusinessException businessEx = new BusinessException(500102, "银行通道暂时不可用，请稍后重试或切换支付方式");
            businessEx.initCause(e); // 挂载原始异常链
            throw businessEx; // 抛出运行时业务异常
        }
    }
}
