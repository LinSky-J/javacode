package keywords.statickeyword;

/**
 * 集群服务器节点实体类：专门用于演示 static 变量、static 方法以及 static 代码块的生命周期与执行顺序。
 *
 * 核心考点：
 * 1. 静态变量（类变量）：全集群所有 ServerNode 节点实例共享同一份 totalOnlineConnections 计数。
 * 2. 静态代码块：在类加载（Class Loading）的初始化阶段执行，全生命周期只执行且仅执行一次！
 * 3. 经典面试顺序题：静态变量/静态块 -> 实例变量/实例块 -> 构造函数。
 * 4. 静态方法：属于类本身，严禁使用 this/super，无法直接访问非静态实例成员。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ServerNode {

    /**
     * 静态变量：全集群全局在线长连接总计数（所有节点实例共享此唯一内存）
     */
    public static int totalOnlineConnections = 0;

    /**
     * 静态变量：集群所属可用区
     */
    public static String clusterRegion = "华东-杭州生产中心";

    /**
     * 静态代码块：类加载初始化阶段执行，只执行一次
     */
    static {
        System.out.println("   [1. 静态初始化阶段] ServerNode 静态代码块触发：加载集群路由元数据，初始化底层网络协议栈（类生命周期内仅执行一次）！");
    }

    /**
     * 实例变量：当前节点唯一标识（每个对象私有独立）
     */
    private String nodeId;

    /**
     * 实例变量：当前节点本地承载的连接数（每个对象私有独立）
     */
    private int localConnections;

    /**
     * 实例初始化代码块：每次 new 对象时在构造方法前执行
     */
    {
        System.out.println("   [2. 实例初始化阶段] 实例代码块触发：为当前节点分配本地内存连接池缓冲区...");
    }

    /**
     * 构造方法：初始化实例属性
     *
     * @param nodeId 节点 ID
     */
    public ServerNode(String nodeId) {
        this.nodeId = nodeId;
        this.localConnections = 0;
        System.out.println("   [3. 构造函数阶段] ServerNode 构造函数执行完成，节点 ID: " + nodeId);
    }

    /**
     * 实例方法：用户接入当前节点
     * 实例方法中可以同时自由访问实例变量和静态变量！
     *
     * @param username 用户名
     */
    public void acceptConnection(String username) {
        this.localConnections++;
        totalOnlineConnections++; // 静态变量累加
        System.out.println("   [用户接入] 用户 " + username + " 接入节点 " + nodeId
                + " | 本节点连接数: " + localConnections
                + " | 全集群总连接数: " + totalOnlineConnections);
    }

    /**
     * 静态方法：广播全集群系统通知
     * 规范要求：直接通过 ServerNode.broadcastNotice(...) 调用。
     * 限制：静态方法内绝对不能访问 this.nodeId 或 this.localConnections！
     *
     * @param notice 通知内容
     */
    public static void broadcastNotice(String notice) {
        System.out.println("   [静态方法/全网广播] [" + clusterRegion + "] 广播集群通告: " + notice
                + "，当前集群全网负载连接数: " + totalOnlineConnections);
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getLocalConnections() {
        return localConnections;
    }
}
