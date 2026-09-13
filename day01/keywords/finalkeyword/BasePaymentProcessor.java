package keywords.finalkeyword;

/**
 * 基础支付处理器：专门用于演示 final 修饰方法（Method）的语义与设计模式。
 *
 * 核心考点：
 * 1. 语法约束：
 *    - 当一个方法被 final 修饰时，子类可以正常继承该方法并调用，但是【绝对不能在子类中重写（Override）该方法】！
 * 2. 设计目的：
 *    - 锁定核心算法骨架（模板方法模式）：父类提供一个 final 的骨架调度流程方法，
 *      防止子类在扩展时恶意或无意修改整个业务流程的核心运转逻辑。
 * 3. 常见辨析：
 *    - private 方法隐式就是 final 的（因为子类不可见，根本无法重写）。
 *    - final 方法【依然可以被重载（Overload）】！重载与重写是两个完全不同的概念。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class BasePaymentProcessor {

    /**
     * 可选扩展步骤：普通方法，允许子类重写特异性参数校验规则
     */
    public boolean validateCustomParameters(double amount) {
        System.out.println("   [基类普通方法] 执行通用基础参数校验: 金额必须大于 0");
        return amount > 0;
    }

    /**
     * 核心安全审计与扣款骨架方法（final 锁定）：
     * 严禁任何子类通过 @Override 重写该方法，防止跳过安全风控合规审计！
     */
    public final void executeTransactionFlow(String channelName, double amount) {
        System.out.println("   [基类 final 锁定骨架方法] === 启动交易流: " + channelName + "，交易金额: " + amount + " 元 ===");

        // 1. 调用子类可定制的参数校验
        if (!validateCustomParameters(amount)) {
            System.out.println("   [交易终止] 参数校验未通过！");
            return;
        }

        // 2. 强制执行合规审计（锁死，子类绝不可篡改或越过）
        System.out.println("   [底层强制合规风控] 记录交易审计日志至区块链与不可篡改数据库...");

        // 3. 执行最终划扣
        System.out.println("   [底层资金通道划拨] 划扣 " + amount + " 元完成！\n");
    }
}
