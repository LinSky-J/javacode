package threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================================
 * 面试专题：Java 线程池 12 问大厂面试速记与全景复盘 (ThreadPoolInterviewMasterSummary)
 * ==============================================================================================
 * 本类将本次面试专题中的全部 12 道高频题目进行结构化提炼，形成直击底层、大厂面试背诵金句，
 * 并深度拓展 3 大极高频连环炮追问（异常退出补全机制、线程池预热机制、核心线程超时回收机制）。
 *
 * ==============================================================================================
 * 12 大核心问题通关口诀与黄金回答
 * ==============================================================================================
 *
 * Q1: 线程池怎么使用？
 * -> 答：阿里规约强制手动通过 new ThreadPoolExecutor(7大参数) 创建；显式指定有界阻塞队列、业务命名
 *        ThreadFactory 和合适的拒绝策略；通过 execute(Runnable) 提交无返回值任务，或 submit(Callable)
 *        提交有返回值任务；使用完后遵循 Graceful Shutdown 优雅停机三步模板。
 *
 * Q2: 介绍一下线程池的工作原理
 * -> 答：内部通过单个 AtomicInteger ctl 同时控制运行状态（高3位）与工作线程数（低29位）。
 *        任务提交四大步骤：① 核心线程未满 -> 创建核心 Worker 执行；② 核心线程满 -> 尝试放入阻塞队列排队；
 *        ③ 队列满且总线程数未达最大 -> 创建非核心 Worker 执行；④ 达到最大线程数且队列满 -> 触发拒绝策略。
 *
 * Q3: 线程池的参数有哪些?
 * -> 答：7 大参数：corePoolSize（核心线程数）、maximumPoolSize（最大线程数）、keepAliveTime（空闲保活时间）、
 *        unit（时间单位）、workQueue（任务阻塞队列）、threadFactory（线程工厂）、handler（拒绝策略）。
 *
 * Q4: 线程池工作队列满了有哪些拒绝策略?
 * -> 答：JDK 四大内置策略：
 *        ① AbortPolicy（默认）：抛出 RejectedExecutionException 阻断；
 *        ② CallerRunsPolicy：退回调用者主线程同步执行，形成天然反压（Backpressure）减缓生产速度；
 *        ③ DiscardPolicy：静默丢弃，不报错；
 *        ④ DiscardOldestPolicy：弹出队列头部的最老任务并重新尝试提交。
 *        生产自定义策略：降级持久化到外部数据库/本地磁盘重试，或异步转存 Kafka/RocketMQ，并触发告警打点。
 *
 * Q5: 有线程池参数设置的经验吗?
 * -> 答：杜绝死搬公式（CPU密集型 N+1，IO密集型 2N）。
 *        真实生产遵循利特尔法则与等待时间比：线程数 = CPU核心数 * 期望利用率 * (1 + 等待时间W / 计算时间C)。
 *        队列容量按“峰值QPS * 容忍延迟”设定严格有界，杜绝无界队列引发 OOM；
 *        关键核心业务物理隔离不同线程池；线上基于 Hippo4j/Dynamic-Tp 接入配置中心实现动态热调优。
 *
 * Q6: 核心线程数设置为 0 可不可以?
 * -> 答：完全可以！源码在任务入队后检测 `workerCountOf(recheck) == 0`，若为 0 则自动调用 `addWorker(null, false)`
 *        创建一个非核心 Worker 专门消费队列任务；CachedThreadPool（core=0, max=MAX_VALUE, SynchronousQueue）即是代表；
 *        任务完成空闲超时后线程数能自动完全缩容为 0，极大节约闲时系统资源。
 *
 * Q7: 线程池种类有哪些?
 * -> 答：Executors 提供的 5 种：FixedThreadPool（定长）、CachedThreadPool（可缓存）、SingleThreadExecutor（单线程）、
 *        ScheduledThreadPool（定时周期调度）、WorkStealingPool（JDK 8+ 工作窃取）。
 *        阿里规约严禁使用 Executors，因前两者使用无界队列易导致 OOM: Java heap space，后两者最大线程数为 MAX_VALUE 易导致
 *        OOM: unable to create new native thread。
 *
 * Q8: 线程池一般是怎么用的?
 * -> 答：遵循 5 大工程规范：手动 new ThreadPoolExecutor；自定义命名 ThreadFactory（如 order-pool-%d）；
 *        按业务域强物理隔离；配置 UncaughtExceptionHandler 捕获未受检异常；应用生命周期管理绑定优雅停机。
 *
 * Q9: 线程池和三个线程同时并发比有什么优势?
 * -> 答：三大维度优势：
 *        ① 降低资源消耗（线程复用，省去每个线程 1MB 栈内存分配及内核态切换开销）；
 *        ② 提升响应速度（核心线程预热常驻，任务到来无需等待线程创建）；
 *        ③ 提高系统可控性与稳定性（有界队列与拒绝策略提供平滑削峰防 OOM 保护，且提供统一监控和钩子方法）。
 *
 * Q10: 线程池用了哪些设计模式?
 * -> 答：① 工厂模式（ThreadFactory 创建定制线程）；② 策略模式（RejectedExecutionHandler 封装拒绝算法）；
 *        ③ 生产者-消费者模式（execute提交任务为生产者，BlockingQueue为缓冲区，Worker为消费者）；
 *        ④ 状态模式（ctl 维护 RUNNING/SHUTDOWN/STOP/TIDYING/TERMINATED 五大状态迁移）；
 *        ⑤ 模板方法模式（beforeExecute / afterExecute / terminated 预留钩子供子类扩展监控与链路追踪）。
 *
 * Q11: 线程池中 shutdown()，shutdownNow() 这两个方法有什么作用?
 * -> 答：① shutdown()：状态切为 SHUTDOWN，拒收新任务，继续执行已在队列和正在执行的任务，仅中断空闲线程；
 *        ② shutdownNow()：状态切为 STOP，拒收新任务，向所有工作线程发送 interrupt() 中断信号，
 *           调用 drainQueue() 清空队列并将未执行任务以 List<Runnable> 返回供补偿落库。
 *
 * Q12: 提交给线程池中的任务可以被撤回吗？
 * -> 答：完全可以！
 *        ① Future.cancel(false)：排队阶段直接标记 CANCELLED，Worker 取出时直接跳过；
 *        ② Future.cancel(true)：运行中任务通过向底层线程发送 interrupt() 中断信号，任务内部响应中断提前退出；
 *        ③ ThreadPoolExecutor.remove(Runnable)：直接从阻塞队列物理移除尚未开始的任务；
 *        ④ purge()：批量清理队列中残存的已取消 FutureTask 释放内存。
 *
 * @author Java面试题通关指南
 */
public class ThreadPoolInterviewMasterSummary {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 线程池 12 问通关复盘与 3 大进阶高频连环炮追问实测");
        System.out.println("======================================================================");

        System.out.println("\n--- [连环追问 1] 线程在执行任务时抛出未捕获 RuntimeException，线程会死亡吗？核心线程会被补充吗？---");
        testWorkerExceptionHandlingAndRecreation();

        System.out.println("\n--- [连环追问 2] 线程池创建后是立即创建核心线程吗？如何预热全部核心线程？---");
        testThreadPoolPrestartMechanism();

        System.out.println("\n--- [连环追问 3] 核心线程一定常驻不被回收吗？如何配置允许核心线程超时释放？---");
        testAllowCoreThreadTimeOut();
    }

    /**
     * 追问 1：Worker 线程发生未捕获异常时的底层处理
     * 源码分析：
     * Worker.run() 循环执行任务，当 task.run() 抛出异常逃出用户代码块时，runWorker 的 while 循环被打破，
     * 走到 finally 中的 processWorkerExit(w, completedAbruptly)。
     * 此时：
     * 1. 抛出异常的原 Worker 线程会彻底终结并被 JVM 销毁回收；
     * 2. processWorkerExit 内部会紧接着调用 addWorker(null, false) 创建一个全新的 Worker 补充进来，
     *    使得线程池常驻核心线程数始终不低于 corePoolSize！
     */
    public static void testWorkerExceptionHandlingAndRecreation() throws InterruptedException {
        AtomicInteger threadIndex = new AtomicInteger(1);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5),
                r -> new Thread(r, "resilient-worker-" + threadIndex.getAndIncrement())
        );

        System.out.printf("[初始状态] 线程池大小: %d%n", pool.getPoolSize());

        // 提交一个抛出异常的任务
        pool.execute(() -> {
            System.out.printf("  [任务1] 当前运行线程: %s -> 即将抛出运行时异常！%n", Thread.currentThread().getName());
            throw new RuntimeException("模拟业务未捕获异常！");
        });

        Thread.sleep(200);

        // 再次提交一个正常任务，观察执行该任务的线程名称
        pool.execute(() -> {
            System.out.printf("  [任务2] 当前运行线程: %s -> 正常执行中！%n", Thread.currentThread().getName());
        });

        Thread.sleep(200);
        System.out.printf("[最终结论] 原线程发生未受检异常已死亡，但线程池自动补充了新 Worker 线程保证池容量稳定！%n");

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 追问 2：线程池预热机制
     * 默认情况下，线程池是懒加载的，初始 workerCount == 0。
     * 可以使用：
     * - prestartCoreThread(): 预先启动 1 个核心线程
     * - prestartAllCoreThreads(): 预先启动全部核心线程
     */
    public static void testThreadPoolPrestartMechanism() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                3, 5, 10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                r -> new Thread(r, "prestart-worker")
        );

        System.out.printf("[预热前] 线程池实际线程数: %d%n", pool.getPoolSize());

        // 预热全部核心线程
        int startedCount = pool.prestartAllCoreThreads();
        System.out.printf("[调用 prestartAllCoreThreads 成功预热核心线程数]: %d | 当前线程池大小: %d%n",
                startedCount, pool.getPoolSize());

        pool.shutdown();
    }

    /**
     * 追问 3：允许核心线程超时回收
     * 默认 core 线程调用 workQueue.take() 永久阻塞。
     * 调用 allowCoreThreadTimeOut(true) 后，核心线程在空闲达到 keepAliveTime 后也会调用 poll(timeout) 超时销毁。
     */
    public static void testAllowCoreThreadTimeOut() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2, 4, 300L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10),
                r -> new Thread(r, "timeout-worker")
        );

        // 开启核心线程超时回收
        pool.allowCoreThreadTimeOut(true);
        pool.prestartAllCoreThreads();

        System.out.printf("[开启 allowCoreThreadTimeOut 后预热核心线程]: 当前线程数: %d%n", pool.getPoolSize());
        System.out.println(">> 等待 500ms 超过 keepAliveTime (300ms)...");

        Thread.sleep(600);

        System.out.printf("[空闲超时后结果]: 核心线程数已被完全释放归零！当前线程数: %d%n", pool.getPoolSize());

        pool.shutdown();
    }
}
