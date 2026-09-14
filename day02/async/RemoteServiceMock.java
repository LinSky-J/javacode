package async;

/**
 * 远程微服务接口调用模拟类。
 * 模拟电商或金融交易场景中常见的耗时 RPC / HTTP 远程查询。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class RemoteServiceMock {

    /**
     * 模拟查询用户基础信息（耗时约 50ms）
     */
    public static String queryUserInfo(String userId) {
        simulateLatency(50);
        return "UserInfo{id='" + userId + "', name='张三', level='VIP3'}";
    }

    /**
     * 模拟调用风控中心接口（耗时约 40ms）
     */
    public static boolean queryRiskScore(String userId) {
        simulateLatency(40);
        return true; // 风控评分合格
    }

    /**
     * 模拟查询用户可用优惠券（耗时约 60ms）
     */
    public static double queryAvailableCoupon(String userId) {
        simulateLatency(60);
        return 50.0; // 减免 50 元
    }

    /**
     * 模拟一个可能抛出异常的远程接口（用于演示异步异常捕获与降级熔断）
     */
    public static String queryExternalCreditReport(String userId) {
        simulateLatency(30);
        throw new RuntimeException("第三方征信机构服务超时连接中断（HTTP 504 Gateway Timeout）");
    }

    private static void simulateLatency(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
