package keywords.finalkeyword;

/**
 * 具体子类处理器：微服务快捷支付处理器。
 *
 * 继承 BasePaymentProcessor：
 * 1. 成功重写父类的非 final 方法 validateCustomParameters()。
 * 2. 继承并直接使用父类的 final 方法 executeTransactionFlow()。
 * 3. 若尝试在此处重写 executeTransactionFlow()，编译器将直接报错：
 *    "executeTransactionFlow(...) cannot override executeTransactionFlow(...) in BasePaymentProcessor; overridden method is final"。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class SubPaymentProcessor extends BasePaymentProcessor {

    /**
     * 重写父类非 final 方法，加入单笔最大额度限制
     */
    @Override
    public boolean validateCustomParameters(double amount) {
        System.out.println("   [子类重写方法] 快捷支付专用校验：金额必须 > 0 且单笔不得超过 50,000 元");
        return amount > 0 && amount <= 50000;
    }

    /*
     * 编译错误实证说明：
     * 以下代码如果取消注释，编译器将直接拒绝编译：
     *
     * @Override
     * public void executeTransactionFlow(String channelName, double amount) {
     *     // 编译报错：'executeTransactionFlow(String, double)' cannot override '...'; overridden method is final
     * }
     */
}
