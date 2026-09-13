package oop;

/**
 * 快捷支付插件实现类。
 *
 * 实现了 PaymentPlugin 接口，重写抽象方法 executePayment()，
 * 并自动继承 PaymentPlugin 接口中定义的 default 默认方法。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class FastPayPlugin implements PaymentPlugin {

    @Override
    public void executePayment(double amount) {
        System.out.println("   [快捷支付插件实现] 触发一键快捷支付逻辑，成功划扣资金: " + amount + " 元！");
    }
}
