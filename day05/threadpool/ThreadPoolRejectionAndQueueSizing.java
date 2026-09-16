package threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================================
 * 面试专题：线程池四大拒绝策略深度剖析与参数调优实战经验 (ThreadPoolRejectionAndQueueSizing)
 * ==============================================================================================
 * 本类针对大厂面试中关于“拒绝策略分类、反压机制、企业级容灾及参数容量调优经验”的高频核心考点进行系统化解析：
 *
 * 4. 线程池工作队列满了有哪些拒绝策略?
 * 5. 有线程池参数设置的经验吗?
 *
 * ==============================================================================================
 * 一、JDK 四大内置拒绝策略源码与机制解析
 * ==============================================================================================
 * 1. AbortPolicy (默认策略):
 *    - 行为：直接抛出 java.util.concurrent.RejectedExecutionException。
 *    - 特点：绝不静默丢失任务，及时阻断调用链。
 *    - 适用场景：核心交易或关键数据业务，配合外层 try-catch 告警、重试机制。
 *
 * 2. CallerRunsPolicy (调用者运行策略 / 经典反压 Backpressure):
 *    - 行为：不抛出异常，也不由线程池中的 Worker 执行，而是直接让提交任务的主线程（Caller Thread）去执行此任务！
 *    - 源码机制：`public void rejectedExecution(Runnable r, ThreadPoolExecutor e) { if (!e.isShutdown()) { r.run(); } }`
 *    - **核心威力（反压机制）**：
 *      当生产者提交速度远高于消费者处理速度时，任务退回由生产者主线程同步执行。
 *      在此期间，生产者无法继续产生/提交新的任务（提交动作被阻塞在该任务的 run() 中），
 *      从而给线程池中排队的任务留出了喘息和消化的时间，天然形成了速度自适应闭环！
 *    - 适用场景：不允许任务丢失、系统吞吐量允许暂时降级、追求系统高可用不崩溃的业务。
 *
 * 3. DiscardPolicy (静默丢弃策略):
 *    - 行为：直接什么都不做，空实现：`public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {}`。
 *    - 特点：静默丢弃当前最新提交的任务，不报任何异常。
 *    - 适用场景：允许数据丢失的次要业务，例如实时日志收集、设备非关键心跳上报、监控打点等。
 *
 * 4. DiscardOldestPolicy (弃老存新策略):
 *    - 行为：从工作队列头部弹出（poll）等待时间最长的一个老任务将其丢弃，然后再次尝试将当前新任务提交到线程池。
 *    - 源码机制：`e.getQueue().poll(); e.execute(r);`
 *    - 适用场景：喜新厌旧的时效性业务，如股票行情实时刷新、游戏帧同步数据包（最新的状态价值远高于旧状态）。
 *
 * ==============================================================================================
 * 二、企业生产级自定义拒绝策略实践
 * ==============================================================================================
 * 生产环境中往往不会简单抛出异常或丢弃，通常会定制 RejectedExecutionHandler 实现：
 * 1. 【降级落库/落盘重试】：将拒绝的任务序列化持久化到 MySQL / Redis / 本地磁盘，由后台定时任务定时重试补偿；
 * 2. 【异步投递 MQ 削峰】：当线程池满载时，转存至 Kafka / RocketMQ 消息队列，利用 MQ 的持久化和削峰填谷能力；
 * 3. 【监控打点与紧急告警】：对接 Prometheus 计数器统计 reject_count，并通过钉钉/企业微信群机器人实时推送告警。
 *
 * ==============================================================================================
 * 三、线程池参数设置的真实工程经验 (面试高分回答模板)
 * ==============================================================================================
 * 1. 经典死公式的局限性：
 *    - 教科书常见公式：
 *      - CPU 密集型任务：`核心线程数 = CPU 核心数 + 1`（+1 的原因是即使某个线程发生缺页中断等暂停，多出的一个线程也能利用 CPU）。
 *      - IO 密集型任务：`核心线程数 = 2 * CPU 核心数`。
 *    - **为什么生产环境绝不能生搬硬套死公式？**
 *      因为现代微服务系统中，大部分业务是混合型（既有复杂的计算与对象序列化，又有数据库查询、Redis读写、远程 RPC/HTTP 调用）。
 *      真实的 IO 等待时间可能占整个请求生命周期的 80%~95% 以上！
 *
 * 2. 严谨的科学计算公式（利特尔法则与等待时间比）：
 *    $$线程数 = N_{CPU} \times U_{CPU} \times (1 + \frac{W}{C})$$
 *    - $N_{CPU}$：系统 CPU 核心数；
 *    - $U_{CPU}$：期望的目标 CPU 利用率（如 80% 即 0.8）；
 *    - $W$：平均等待时间（Waiting Time，如 RPC/DB/IO 耗时 80ms）；
 *    - $C$：平均计算时间（Computing Time，如 CPU 计算耗时 20ms）；
 *    - 示例：若 8 核 CPU，期望利用率 80%，W/C = 80ms / 20ms = 4：
 *      线程数 = 8 * 0.8 * (1 + 4) = 32。
 *
 * 3. 阻塞队列容量（queueCapacity）如何设置？
 *    - **严禁无界队列**（如 new LinkedBlockingQueue() 默认长度为 Integer.MAX_VALUE，极易 OOM 内存溢出）！
 *    - 经验公式：
 *      $$队列容量 = 峰值 QPS \times 核心业务最大容忍耗时$$
 *      例如：预估峰值 QPS 为 1000，核心接口容忍排队延迟 0.5s，则队列容量可设定为 500。
 *      如果容量设得过大，任务在队列里积压严重，调用端早超时断开了，排队执行毫无意义；
 *      如果容量设得过小，瞬时抖动容易频繁触发拒绝策略。
 *
 * 4. 线程池物理隔离原则：
 *    - 核心业务池与非核心业务池隔离；
 *    - 快接口池与慢接口（如慢 SQL、第三方外部不可控 API）隔离，防止第三方慢接口拖死共享线程池，导致全站雪崩。
 *
 * 5. 业界前沿：动态线程池（Dynamic Thread Pool）：
 *    - 任何静态参数在面临大促、网络抖动时都难以做到尽善尽美。
 *    - 业内成熟方案（如美团动态线程池方案、开源 Hippo4j、Dynamic-Tp）：
 *      将 corePoolSize, maximumPoolSize, workQueueCapacity 托管于配置中心（Nacos/Apollo），
 *      利用 ThreadPoolExecutor 提供的 setCorePoolSize / setMaximumPoolSize 实现不停机热修改，
 *      配合实时指标监控看板（活跃度、排队深度、拒绝数）实现自适应调节。
 *
 * @author Java面试题通关指南
 */
public class ThreadPoolRejectionAndQueueSizing {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 1. 深度对比 JDK 四大内置拒绝策略执行行为");
        System.out.println("======================================================================");
        testAbortPolicy();
        testCallerRunsPolicy();
        testDiscardPolicy();
        testDiscardOldestPolicy();

        System.out.println("\n======================================================================");
        System.out.println(" 2. 模拟企业级生产自定义拒绝策略 (降级存储 + 钉钉/Prometheus监控告警)");
        System.out.println("======================================================================");
        testEnterpriseCustomRejectionHandler();

        System.out.println("\n======================================================================");
        System.out.println(" 3. 演示动态线程池 (Dynamic Thread Pool) 热调整参数特性");
        System.out.println("======================================================================");
        testDynamicThreadPoolAdjustment();
    }

    /**
     * 策略一：AbortPolicy（直接抛出异常）
     */
    private static void testAbortPolicy() {
        System.out.println("\n--- [1] 验证 AbortPolicy ---");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "abort-worker"),
                new ThreadPoolExecutor.AbortPolicy()
        );

        try {
            pool.execute(() -> sleepMillis(500)); // 任务1: 占满核心线程
            pool.execute(() -> sleepMillis(500)); // 任务2: 占满队列
            pool.execute(() -> sleepMillis(500)); // 任务3: 超额，触发拒绝
        } catch (Exception e) {
            System.out.println(">> AbortPolicy 成功捕获预期异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * 策略二：CallerRunsPolicy（调用者线程执行，天然反压）
     */
    private static void testCallerRunsPolicy() {
        System.out.println("\n--- [2] 验证 CallerRunsPolicy (反压机制) ---");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "caller-runs-worker"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        pool.execute(() -> {
            System.out.printf("  [任务1] 执行线程: %s%n", Thread.currentThread().getName());
            sleepMillis(300);
        });
        pool.execute(() -> {
            System.out.printf("  [任务2] 进队列排队, 稍后执行%n");
            sleepMillis(300);
        });
        // 任务3触发拒绝策略：此时由调用者线程 (即 main 线程) 亲自执行！
        pool.execute(() -> {
            System.out.printf("  [任务3-超额触发拒绝] 执行线程: %s (主线程被迫执行任务，起到反压限速作用!)%n",
                    Thread.currentThread().getName());
            sleepMillis(300);
        });

        pool.shutdown();
    }

    /**
     * 策略三：DiscardPolicy（静默丢弃）
     */
    private static void testDiscardPolicy() {
        System.out.println("\n--- [3] 验证 DiscardPolicy (静默丢弃) ---");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "discard-worker"),
                new ThreadPoolExecutor.DiscardPolicy()
        );

        pool.execute(() -> sleepMillis(300)); // 任务1
        pool.execute(() -> sleepMillis(300)); // 任务2
        pool.execute(() -> System.out.println("  [任务3] 如果能看到我，说明没被丢弃")); // 任务3被静默丢弃

        System.out.println(">> DiscardPolicy 任务3已提交，无任何异常抛出，任务被静默丢弃。");
        pool.shutdown();
    }

    /**
     * 策略四：DiscardOldestPolicy（弃老存新）
     */
    private static void testDiscardOldestPolicy() {
        System.out.println("\n--- [4] 验证 DiscardOldestPolicy (丢弃队列头部的老任务) ---");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2), // 队列容量2
                r -> new Thread(r, "discard-oldest-worker"),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        pool.execute(() -> {
            System.out.println("  [正在执行] 任务 1");
            sleepMillis(400);
        });
        pool.execute(() -> System.out.println("  [执行] 任务 2 (较老入队任务)"));
        pool.execute(() -> System.out.println("  [执行] 任务 3 (较新入队任务)"));

        // 提交任务4：队列满，触发 DiscardOldestPolicy，任务2（队列头部最老者）将被弹出丢弃，任务4入队！
        pool.execute(() -> System.out.println("  [执行] 任务 4 (挤掉老任务的新任务)"));

        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 实战：生产级自定义拒绝策略 (降级存储与监控)
     */
    private static void testEnterpriseCustomRejectionHandler() {
        AtomicInteger rejectedCounter = new AtomicInteger(0);

        // 自定义拒绝策略
        RejectedExecutionHandler customHandler = (task, executor) -> {
            int currentRejects = rejectedCounter.incrementAndGet();
            System.err.printf("[自定义拒绝策略报警] 线程池过载! 已累计拒绝: %d 个任务 | 当前活跃线程: %d, 队列排队数: %d%n",
                    currentRejects, executor.getActiveCount(), executor.getQueue().size());

            // 1. 模拟持久化到外部补偿存储 (如 Redis/MySQL)
            System.out.printf("  -> [业务降级] 任务已序列化写入外部补偿库/本地磁盘，等待兜底扫描补偿。%n");
            // 2. 模拟发送钉钉/企业微信告警通知
            System.out.printf("  -> [告警通知] 触发 Prometheus 监控指标递增，并向值班群推送告警通知。%n");
        };

        ThreadPoolExecutor enterprisePool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                r -> new Thread(r, "enterprise-worker"),
                customHandler
        );

        enterprisePool.execute(() -> sleepMillis(400)); // 占满线程
        enterprisePool.execute(() -> sleepMillis(400)); // 占满队列
        enterprisePool.execute(() -> sleepMillis(400)); // 触发自定义拒绝

        enterprisePool.shutdown();
    }

    /**
     * 实战：动态线程池热修改核心参数
     */
    private static void testDynamicThreadPoolAdjustment() {
        ThreadPoolExecutor dynamicPool = new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> new Thread(r, "dynamic-worker")
        );

        System.out.printf("[调整前] 核心线程数: %d, 最大线程数: %d%n",
                dynamicPool.getCorePoolSize(), dynamicPool.getMaximumPoolSize());

        // 模拟从配置中心 (Nacos / Apollo) 收到动态调优变更通知
        System.out.println(">> 模拟大促来临，配置中心下发最新线程池参数 -> 核心调至 8, 最大调至 16");
        // 【大厂经典踩坑点】：如果直接 setCorePoolSize(8)，由于当前 maximumPoolSize=4，
        // 源码中校验 `if (corePoolSize > maximumPoolSize) throw new IllegalArgumentException();` 会直接报错！
        // 规范规则：扩容时必须【先扩大最大线程数，再扩大核心线程数】；缩容时必须【先缩小核心线程数，再缩小最大线程数】！
        dynamicPool.setMaximumPoolSize(16);
        dynamicPool.setCorePoolSize(8);

        System.out.printf("[调整后] 核心线程数: %d, 最大线程数: %d (热调整顺序正确，无需重启服务! )%n",
                dynamicPool.getCorePoolSize(), dynamicPool.getMaximumPoolSize());

        dynamicPool.shutdown();
    }

    private static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
