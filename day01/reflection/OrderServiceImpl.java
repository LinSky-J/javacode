package reflection;

/**
 * 订单服务具体实现类：包含私有属性、公共方法和私有业务方法，供反射全方位实测。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class OrderServiceImpl implements OrderService {

    /**
     * 私有成员属性：微服务数据库连接配置（用于演示 Spring @Value / @Autowired 反射私有注入）
     */
    private String databaseUrl = "jdbc:mysql://localhost:3306/order_db";

    /**
     * 默认无参构造函数（Spring IoC 容器反射实例化的基础）
     */
    public OrderServiceImpl() {
        System.out.println("   [OrderServiceImpl 构造方法触发] 无参构造函数被调用！");
    }

    @Override
    public String createOrder(String orderId, double amount) {
        System.out.println("   [执行业务方法] 成功创建订单: " + orderId + "，金额: " + amount + " 元，连接库: " + databaseUrl);
        return "SUCCESS_ORDER_" + orderId;
    }

    @Override
    public String queryOrderStatus(String orderId) {
        return "ORDER_PAID_COMPLETED";
    }

    /**
     * 内部私有风控方法（演示反射破解私有调用）
     */
    private boolean checkRiskControl(String orderId) {
        System.out.println("   [内部私有风控] 正在对订单 " + orderId + " 执行反欺诈拦截风控检测...");
        return true;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
}
