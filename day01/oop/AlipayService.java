package oop;

/**
 * 支付宝支付具体实现类。
 *
 * 实现了 PaymentService 接口，封装支付宝通道的特异性扣款流程。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class AlipayService implements PaymentService {

    @Override
    public void pay(double amount) {
        System.out.println("   [支付宝支付通道] 调用支付宝网关 SDK，账户资金冻结与划扣: " + amount + " 元成功！");
    }

    @Override
    public String getChannelName() {
        return "支付宝支付";
    }
}
