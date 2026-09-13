package reflection;

/**
 * 订单服务契约接口：用于演示反射在接口代理、RPC 远程调用以及 IoC 容器中的应用场景。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public interface OrderService {

    /**
     * 创建订单业务行为
     *
     * @param orderId 订单唯一编号
     * @param amount 订单金额
     * @return 订单处理结果描述
     */
    String createOrder(String orderId, double amount);

    /**
     * 查询订单状态
     *
     * @param orderId 订单编号
     * @return 订单状态中文描述
     */
    String queryOrderStatus(String orderId);
}
