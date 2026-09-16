package threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================================
 * 面试专题：常见线程池种类、阿里规约避坑、工程规范与设计模式 (ThreadPoolTypesAndBestPractices)
 * ==============================================================================================
 * 本类针对大厂面试中关于“常见线程池特点、为何禁止使用Executors、工程用法规范及底层设计模式”进行系统化剖析：
 *
 * 7. 线程池种类有哪些?
 * 8. 线程池一般是怎么用的?
 * 10. 线程池用了哪些设计模式?
 *
 * ==============================================================================================
 * 一、JDK 常见线程池种类与底层原理
 * ==============================================================================================
 * 1. FixedThreadPool (定长线程池):
 *    - 构造：Executors.newFixedThreadPool(n)
 *    - 参数：corePoolSize = n, maximumPoolSize = n, keepAliveTime = 0, workQueue = new LinkedBlockingQueue<Runnable>()
 *    - 特点：线程数固定，所有线程均为核心常驻线程。
 *    - 致命隐患：LinkedBlockingQueue 默认无参构造容量为 Integer.MAX_VALUE（近 21 亿）！
 *      在高并发瞬时堆积场景下，队列会无止境堆积任务，最终导致 JVM 内存耗尽：java.lang.OutOfMemoryError: Java heap space。
 *
 * 2. CachedThreadPool (可缓存弹性线程池):
 *    - 构造：Executors.newCachedThreadPool()
 *    - 参数：corePoolSize = 0, maximumPoolSize = Integer.MAX_VALUE, keepAliveTime = 60s, workQueue = new SynchronousQueue<Runnable>()
 *    - 特点：无核心线程，来一个任务就尝试交付给现有空闲线程，若无空闲则立即新建非核心线程；空闲 60 秒后自动回收。
 *    - 致命隐患：maximumPoolSize 为 Integer.MAX_VALUE，如果短时间内涌入大量耗时任务，线程池会无休止创建线程，
 *      直接打满 CPU 和系统线程数上限，导致：java.lang.OutOfMemoryError: unable to create new native thread，机器瘫痪！
 *
 * 3. SingleThreadExecutor (单线程化线程池):
 *    - 构造：Executors.newSingleThreadExecutor()
 *    - 参数：corePoolSize = 1, maximumPoolSize = 1, keepAliveTime = 0, workQueue = new LinkedBlockingQueue<Runnable>()
 *    - 特点：池内始终有且仅有 1 个工作线程，若异常退出会自动重建，保证所有提交任务严格按照 FIFO 顺序串行执行。
 *    - 致命隐患：同样使用了无界阻塞队列 LinkedBlockingQueue，存在严重的 OOM 风险。
 *
 * 4. ScheduledThreadPool (定时/周期性调度线程池):
 *    - 构造：Executors.newScheduledThreadPool(core)
 *    - 底层：ScheduledThreadPoolExecutor，工作队列为 DelayedWorkQueue（基于堆结构的延迟队列）。
 *    - 特点：支持 scheduleAtFixedRate（固定频率）与 scheduleWithFixedDelay（固定延迟）周期性调度。
 *
 * 5. WorkStealingPool (工作窃取线程池，JDK 8+ 新增):
 *    - 构造：Executors.newWorkStealingPool()
 *    - 底层：基于 ForkJoinPool 实现，并行度默认为当前机器可用 CPU 核心数。
 *    - 特点：每个工作线程维护一个独立的双端队列（Deque）。当某个线程执行完自己队列的任务后，
 *      会主动从其他繁忙线程的队列尾部“窃取（Steal）”任务来执行，极大减少线程竞争，适合计算密集型、可拆分子任务的大型并行计算。
 *
 * ==============================================================================================
 * 二、《阿里巴巴 Java 开发手册》为什么严禁使用 Executors 创建线程池？
 * ==============================================================================================
 * 手册明确规定：
 * 【强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式。
 * 核心原因：
 * 1. FixedThreadPool 和 SingleThreadExecutor：
 *    允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM（Java heap space）。
 * 2. CachedThreadPool 和 ScheduledThreadPool：
 *    允许的创建线程数量为 Integer.MAX_VALUE，可能会创建大量的线程，从而导致 OOM（unable to create new native thread）。
 * 3. 规避资源耗尽的风险，要求开发者显式根据业务估算并设定有界队列与最大线程数，掌控系统底线。
 *
 * ==============================================================================================
 * 三、生产环境中线程池一般是怎么用的？(工程规范 5 守则)
 * ==============================================================================================
 * 1. 守则一：手动 new ThreadPoolExecutor，显式设置有界队列容量与最大线程数；
 * 2. 守则二：自定义 ThreadFactory，为线程命名有意义的前缀（如 `order-service-pool-%d`），
 *    极大方便线上问题排查（jstack 堆栈日志一眼区分不同业务池）；
 * 3. 守则三：按业务物理隔离线程池，杜绝大杂烩（核心交易池、慢 SQL/报表池、第三方 RPC 调用池互不影响）；
 * 4. 守则四：设置合理的未捕获异常处理器（UncaughtExceptionHandler），防止任务异常死掉且无日志；
 * 5. 守则五：生命周期优雅停机（Graceful Shutdown），在 Spring 容器销毁或应用退出时等待存量任务执行完。
 *
 * ==============================================================================================
 * 四、线程池中应用了哪些经典设计模式？
 * ==============================================================================================
 * 1. 【工厂模式 (Factory Pattern)】：
 *    - ThreadFactory 接口：将“创建线程”的细节与“使用线程”解耦。调用方可定制线程名称、优先级、守护状态、线程组等。
 * 2. 【策略模式 (Strategy Pattern)】：
 *    - RejectedExecutionHandler 接口：将不同的饱和拒绝算法（抛异常、调用者运行、静默丢弃、弃老存新、自定义降级）
 *      封装成独立的策略类，支持运行时灵活替换与插拔。
 * 3. 【生产者-消费者模式 (Producer-Consumer Pattern)】：
 *    - 业务调用线程通过 execute()/submit() 充当生产者角色；
 *    - BlockingQueue（ArrayBlockingQueue / LinkedBlockingQueue）充当解耦缓冲区；
 *    - 线程池内部的 Worker 线程充当消费者角色，循环从队列中 poll()/take() 消费任务执行。
 * 4. 【状态模式 (State Pattern)】：
 *    - ctl 原子变量的高 3 位精确维护了：RUNNING -> SHUTDOWN -> STOP -> TIDYING -> TERMINATED 5 大状态；
 *    - 各种操作（如 execute, shutdown, addWorker）严格根据当前所处状态判定执行动作。
 * 5. 【模板方法模式 (Template Method Pattern)】：
 *    - ThreadPoolExecutor 预留了三个保护级的空钩子（Hook）方法供子类重写：
 *      - `protected void beforeExecute(Thread t, Runnable r)`：任务执行前回调；
 *      - `protected void afterExecute(Runnable r, Throwable t)`：任务执行后回调；
 *      - `protected void terminated()`：线程池完全终止关闭时回调。
 *    - 极大方便了子类实现：全链路 TraceId 传递、任务耗时性能监控打点、异常统计等。
 *
 * @author Java面试题通关指南
 */
public class ThreadPoolTypesAndBestPractices {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 1. 线程池常见种类结构与阿里规约避坑剖析");
        System.out.println("======================================================================");
        explainExecutorsTypesAndRisks();

        System.out.println("\n======================================================================");
        System.out.println(" 2. 企业级规范化线程池使用最佳实践 (命名工厂 + 异常隔离 + 物理隔离)");
        System.out.println("======================================================================");
        testEnterpriseStandardThreadPool();

        System.out.println("\n======================================================================");
        System.out.println(" 3. 线程池设计模式实战：利用【模板方法模式】钩子实现全链路监控与耗时统计");
        System.out.println("======================================================================");
        testTemplateMethodPatternHooks();
    }

    /**
     * 演示一：解释 Executors 的五种类型及其潜在风险
     */
    public static void explainExecutorsTypesAndRisks() {
        System.out.println(">> Executors.newFixedThreadPool(n): 隐患 -> 默认无界队列 LinkedBlockingQueue(Integer.MAX_VALUE)，突发流量堆积导致 OOM: Java heap space。");
        System.out.println(">> Executors.newCachedThreadPool(): 隐患 -> 最大线程数 Integer.MAX_VALUE，高并发任务阻塞时无节制创建线程导致 OOM: unable to create new native thread。");
        System.out.println(">> Executors.newSingleThreadExecutor(): 隐患 -> 单工作线程但使用无界队列，任务积压导致 OOM。");
        System.out.println(">> Executors.newScheduledThreadPool(): 隐患 -> 基于 DelayedWorkQueue，最大线程数同样为 Integer.MAX_VALUE。");
        System.out.println(">> Executors.newWorkStealingPool(): JDK 8+ 基于 ForkJoinPool 的工作窃取算法，提升 CPU 密集型任务利用率。");
    }

    /**
     * 演示二：企业级规范化线程池最佳实践
     */
    public static void testEnterpriseStandardThreadPool() throws InterruptedException {
        // 1. 规范自定义命名 ThreadFactory
        class NamedThreadFactory implements ThreadFactory {
            private final String prefix;
            private final AtomicInteger threadIndex = new AtomicInteger(1);

            public NamedThreadFactory(String prefix) {
                this.prefix = prefix;
            }

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, prefix + "-" + threadIndex.getAndIncrement());
                // 统一设置为非守护线程
                t.setDaemon(false);
                // 统一设置线程优先级
                t.setPriority(Thread.NORM_PRIORITY);
                // 统一绑定未捕获异常处理器
                t.setUncaughtExceptionHandler((thread, ex) ->
                        System.err.printf("!! [全局异常捕获] 线程 %s 执行时抛出未受检异常: %s%n",
                                thread.getName(), ex.getMessage()));
                return t;
            }
        }

        // 2. 手动创建有界 ThreadPoolExecutor（核心业务物理隔离）
        ThreadPoolExecutor orderServicePool = new ThreadPoolExecutor(
                4,                                      // 核心线程数
                8,                                      // 最大线程数
                30L, TimeUnit.SECONDS,                 // 存活时间
                new ArrayBlockingQueue<>(100),          // 严格有界队列，杜绝 OOM
                new NamedThreadFactory("order-audit-pool"),
                new ThreadPoolExecutor.CallerRunsPolicy() // 反压策略
        );

        System.out.println("[规范化线程池启动] 线程名前缀: order-audit-pool, 核心数: 4, 最大数: 8, 队列容量: 100");

        // 提交正常任务
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            orderServicePool.execute(() -> {
                System.out.printf("  [订单审核处理] 订单号: %d | 运行线程: %s%n", id, Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            });
        }

        // 提交一个会抛出异常的任务，验证异常捕获隔离性
        orderServicePool.execute(() -> {
            System.out.printf("  [订单结算异常任务] 线程: %s, 模拟发生运行时异常%n", Thread.currentThread().getName());
            throw new RuntimeException("数据库扣款超时，事务回滚！");
        });

        orderServicePool.shutdown();
        orderServicePool.awaitTermination(2, TimeUnit.SECONDS);
    }

    /**
     * 演示三：重写 ThreadPoolExecutor 钩子方法（模板方法模式）实现性能监控与链路追踪
     */
    public static void testTemplateMethodPatternHooks() throws InterruptedException {
        // 自定义监控扩展线程池
        class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {
            // 使用 ThreadLocal 记录每个任务的开始时间戳
            private final ThreadLocal<Long> startTimeLocal = new ThreadLocal<>();

            public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                               long keepAliveTime, TimeUnit unit,
                                               BlockingQueue<Runnable> workQueue) {
                super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                        r -> new Thread(r, "monitored-worker"));
            }

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                startTimeLocal.set(System.currentTimeMillis());
                System.out.printf("  [Hook: beforeExecute] 线程 [%s] 即将执行任务，记录起始时间戳...%n", t.getName());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                try {
                    long cost = System.currentTimeMillis() - startTimeLocal.get();
                    if (t != null) {
                        System.err.printf("  [Hook: afterExecute] 任务执行失败，耗时: %d ms, 异常: %s%n", cost, t.getMessage());
                    } else {
                        System.out.printf("  [Hook: afterExecute] 任务执行完毕，耗时: %d ms | 总已完成任务数: %d%n",
                                cost, this.getCompletedTaskCount() + 1);
                    }
                } finally {
                    startTimeLocal.remove(); // 规范清除 ThreadLocal 防止内存泄露
                    super.afterExecute(r, t);
                }
            }

            @Override
            protected void terminated() {
                super.terminated();
                System.out.println("  [Hook: terminated] 线程池已彻底终止退出，释放所有底层资源！");
            }
        }

        MonitoredThreadPoolExecutor monitoredPool = new MonitoredThreadPoolExecutor(
                2, 2, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5)
        );

        monitoredPool.execute(() -> {
            try {
                Thread.sleep(200); // 模拟业务处理
            } catch (InterruptedException ignored) {
            }
        });

        monitoredPool.shutdown();
        monitoredPool.awaitTermination(2, TimeUnit.SECONDS);
    }
}
