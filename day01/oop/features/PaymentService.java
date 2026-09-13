package oop.features;

/**
 * 支付契约接口：专门用于演示【多态解决了什么问题】。
 *
 * 核心设计定位：
 * 1. 消除大量的冗余 if-else / switch 强耦合分支。
 * 2. 践行面向对象顶级设计原则——【开闭原则（Open-Closed Principle, OCP）】：
 *    对扩展开放，对修改关闭。
 * 3. 规范高层调用模块与底层支付通道之间的纯粹抽象契约。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public interface PaymentService {

    /**
     * 扣款支付抽象行为
     *
     * @param amount 支付金额
     */
    void pay(double amount);

    /**
     * 获取当前支付渠道名称
     *
     * @return 支付渠道中文名
     */
    String getChannelName();
}
