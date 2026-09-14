package day02.java8;

/**
 * 订单校验函数式接口。
 * 显式使用 @FunctionalInterface 标注，确保接口内部仅有一个抽象方法。
 *
 * @author InterviewGuide
 * @version 1.0
 */
@FunctionalInterface
public interface OrderValidator {

    /**
     * 校验订单是否合法
     *
     * @param orderId 订单编号
     * @param amount  订单金额
     * @return 校验通过返回 true，否则返回 false
     */
    boolean validate(String orderId, double amount);
}
