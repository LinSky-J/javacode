package oop.abstractinterface;

/**
 * 演示现代接口方法演进体系的支付插件契约接口。
 *
 * 涵盖知识点：
 * 1. 静态全局常量（public static final）：编译期或类加载期常量
 * 2. 抽象方法（Java 1.0+）：强制实现类提供具体逻辑
 * 3. default 默认方法（Java 8 引入）：允许有方法体，支持已有接口平滑扩容
 * 4. static 静态方法（Java 8 引入）：接口专属的工具辅助方法
 * 5. private 私有方法（Java 9 引入）：提炼 default 默认方法中的公共代码，隐藏实现细节
 *
 * @author InterviewGuide
 * @version 1.0
 */
public interface PaymentPlugin {

    /**
     * 1. 静态常量（隐式修饰符：public static final）
     */
    String PLUGIN_NAME = "EnterprisePaymentPlugin";

    /**
     * 2. 抽象方法（隐式修饰符：public abstract，Java 1.0+ 经典规范）
     *
     * @param amount 支付金额
     */
    void executePayment(double amount);

    /**
     * 3. 默认方法（Java 8 引入：使用 default 关键字修饰，包含完整的方法体）
     *
     * 解决的痛点：
     * 如果需要在已有的大型接口中新增一个方法，若没有 default 方法，
     * 所有成千上万个已有的实现类都必须强制改写，否则无法通过编译！
     * default 默认方法使得接口扩容具备极佳的向后兼容性（如 Collection.stream()）。
     *
     * @param message 待记录的事务信息
     */
    default void logTransaction(String message) {
        String logPrefix = getLogPrefix(); // 调用 Java 9 的 private 私有方法
        System.out.println("   [接口 default 默认方法] " + logPrefix + " - 事务记录: " + message);
    }

    /**
     * 4. 静态方法（Java 8 引入：使用 static 关键字修饰）
     *
     * 规范：直接通过 接口名.静态方法名() 调用，不能通过实现类对象调用。
     */
    static void printPluginVersion() {
        System.out.println("   [接口 static 静态方法] 插件唯一标识: " + PLUGIN_NAME + "，架构规范版本: 3.0-Enterprise");
    }

    /**
     * 5. 私有方法（Java 9 引入：使用 private 关键字修饰）
     *
     * 作用：仅供接口内部的 default 方法或 static 方法调用复用代码，对外界彻底隐藏。
     *
     * @return 格式化的审计日志前缀
     */
    private String getLogPrefix() {
        return "[PLUGIN-AUDIT-" + System.currentTimeMillis() + "]";
    }
}
