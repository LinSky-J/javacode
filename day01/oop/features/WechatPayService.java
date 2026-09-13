package oop.features;

/**
 * 微信支付具体实现类。
 *
 * 实现了 PaymentService 接口，封装微信支付通道的特异性扣款流程。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class WechatPayService implements PaymentService {

    @Override
    public void pay(double amount) {
        System.out.println("   [微信支付通道] 触发微信统一收单接口，微信零钱/银行卡扣款: " + amount + " 元成功！");
    }

    @Override
    public String getChannelName() {
        return "微信支付";
    }
}
