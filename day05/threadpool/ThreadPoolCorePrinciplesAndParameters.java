package threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================================
 * 面试专题：Java 线程池核心原理与七大参数深度剖析 (ThreadPoolCorePrinciplesAndParameters)
 * ==============================================================================================
 * 本类针对大厂面试中关于“线程池基本概念、底层实现与工作流转”的高频核心考点进行深度解析与实战验证：
 *
 * 1. 线程池怎么使用？
 * 2. 介绍一下线程池的工作原理
 * 3. 线程池的参数有哪些?
 * 6. 核心线程数设置为0可不可以?
 * 9. 线程池和三个线程同时并发比有什么优势?
 *
 * ==============================================================================================
 * 一、线程池核心参数详解 (ThreadPoolExecutor 七大参数)
 * ==============================================================================================
 * public ThreadPoolExecutor(
 *     int corePoolSize,                      // 1. 核心线程数（常驻线程数量）
 *     int maximumPoolSize,                   // 2. 最大线程数（线程池能容纳的最大并发线程数）
 *     long keepAliveTime,                    // 3. 非核心线程空闲存活时间
 *     TimeUnit unit,                         // 4. 存活时间的时间单位
 *     BlockingQueue<Runnable> workQueue,     // 5. 任务阻塞队列（存放待执行任务的缓冲区）
 *     ThreadFactory threadFactory,           // 6. 线程创建工厂（定制线程名称、优先级、Daemon等）
 *     RejectedExecutionHandler handler       // 7. 拒绝策略（队列满且工作线程达最大数时的兜底策略）
 * )
 *
 * ==============================================================================================
 * 二、线程池底层工作原理与状态流转
 * ==============================================================================================
 * 1. 状态与线程数管理：
 *    - ThreadPoolExecutor 内部使用单个 AtomicInteger 类型的变量 ctl（Control）同时记录：
 *      - 高 3 位：表示线程池当前的运行状态（RUNNING, SHUTDOWN, STOP, TIDYING, TERMINATED）
 *      - 低 29 位：表示当前线程池内有效工作线程数（workerCount，上限为 (2^29 - 1) ≈ 5亿）
 *    - 优势：利用单个原子变量保证线程池状态（runState）与工作线程数（workerCount）在 CAS 操作下的原子一致性。
 *
 * 2. 任务提交执行流转四大阶段 (execute 源码流转)：
 *    (1) 【阶段一：核心线程】提交任务时，如果当前 workerCount < corePoolSize：
 *        - 调用 addWorker(command, true)，直接创建并启动一个核心 Worker 线程来执行任务。
 *    (2) 【阶段二：入阻塞队列】如果 workerCount >= corePoolSize，且线程池处于 RUNNING 状态：
 *        - 尝试将任务放入阻塞队列：workQueue.offer(command)。
 *        - 入队成功后进行双重检查（Double-check）：如果线程池已关闭，则回滚移除任务并执行拒绝策略；
 *          若当前有效工作线程数为 0（例如 corePoolSize=0），则必须启动一个非核心线程消费队列任务。
 *    (3) 【阶段三：启动非核心线程】如果入队失败（说明工作队列已满）：
 *        - 检查如果当前 workerCount < maximumPoolSize，则调用 addWorker(command, false) 创建非核心线程处理该任务。
 *    (4) 【阶段四：触发拒绝策略】如果当前 workerCount >= maximumPoolSize：
 *        - 无法再创建线程，直接触发 RejectedExecutionHandler.rejectedExecution(command, this)。
 *
 * ==============================================================================================
 * 三、核心线程数设置为 0 可不可以？
 * ==============================================================================================
 * 【结论】：完全可以！
 * 1. 源码依据：
 *    在 ThreadPoolExecutor.execute(Runnable command) 源码中：
 *    ```java
 *    if (isRunning(c) && workQueue.offer(command)) {
 *        int recheck = ctl.get();
 *        if (!isRunning(recheck) && remove(command))
 *            reject(command);
 *        else if (workerCountOf(recheck) == 0) // <--- 关键源码！
 *            addWorker(null, false);           // 即使核心线程数为0，只要队列有任务，就会创建非核心 Worker 消费队列！
 *    }
 *    ```
 * 2. 经典应用：
 *    - Executors.newCachedThreadPool() 正是 corePoolSize = 0, maximumPoolSize = Integer.MAX_VALUE,
 *      keepAliveTime = 60s, workQueue = new SynchronousQueue<Runnable>()。
 *    - 当核心线程数为 0 且队列非空时，提交任务进入队列后，线程池检测到 workerCount == 0，
 *      会自动调用 addWorker(null, false) 创建临时线程去从队列中拉取任务执行。
 *    - 临时线程空闲超过 keepAliveTime 后自动销毁，线程池中的线程数能自然缩容回 0，极大地节省闲时系统资源。
 *
 * ==============================================================================================
 * 四、线程池与直接启动 3 个线程同时并发比有什么优势？
 * ==============================================================================================
 * 1. 降低资源消耗（线程复用）：
 *    - 频繁创建/销毁线程代价高昂：每个 Java 线程在 JVM 中默认分配 1MB 栈内存（-Xss1m），在 Linux 上
 *      对应一个 pthread 内核级线程，涉及昂贵的用户态与内核态切换和上下文切换开销。
 * 2. 提高响应速度：
 *    - 任务到达时，核心线程早已预热就绪，任务可立即得到执行，省去了线程创建（毫秒级）的延迟。
 * 3. 增强系统的稳定性与可控性（防止 OOM）：
 *    - 突发流量下，若直接 new Thread()，成千上万个线程并发会耗尽内存导致 OutOfMemoryError，打满 CPU 导致雪崩；
 *      线程池通过有界队列和最大线程数限制，平滑削峰。
 * 4. 统一管理与监控扩展：
 *    - 线程池提供标准统计接口（getActiveCount, getCompletedTaskCount 等）以及扩展钩子（beforeExecute, afterExecute）。
 *
 * @author Java面试题通关指南
 */
public class ThreadPoolCorePrinciplesAndParameters {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 1. 演示标准线程池的使用与完整工作流转 (核心 -> 队列 -> 最大 -> 拒绝)");
        System.out.println("======================================================================");
        testStandardThreadPoolLifecycle();

        System.out.println("\n======================================================================");
        System.out.println(" 2. 深度验证：核心线程数 corePoolSize = 0 是否可行并自动启停 Worker？");
        System.out.println("======================================================================");
        testCorePoolSizeZeroBehavior();

        System.out.println("\n======================================================================");
        System.out.println(" 3. 性能与资源对比：直接 new Thread() vs 线程池复用性能实测");
        System.out.println("======================================================================");
        testDirectThreadVsThreadPoolPerformance();
    }

    /**
     * 测试一：标准线程池完整流转演示
     * 参数设定：
     * corePoolSize = 2, maximumPoolSize = 4, queueCapacity = 2
     * 预期行为：
     * - 任务 1, 2: 核心线程直接处理 (活跃线程 1 -> 2)
     * - 任务 3, 4: 放入阻塞队列等待 (队列大小 1 -> 2)
     * - 任务 5, 6: 队列满，启动非核心线程处理 (活跃线程 3 -> 4)
     * - 任务 7: 超过 maximumPoolSize 且队列已满，触发拒绝策略 (AbortPolicy 抛出异常)
     */
    public static void testStandardThreadPoolLifecycle() {
        AtomicInteger threadNumber = new AtomicInteger(1);

        // 自定义线程工厂，为线程命名并设置未捕获异常处理器
        ThreadFactory customThreadFactory = r -> {
            Thread t = new Thread(r, "standard-pool-worker-" + threadNumber.getAndIncrement());
            t.setUncaughtExceptionHandler((thread, ex) ->
                    System.err.printf("[异常捕获] 线程 %s 遇到未捕获异常: %s%n", thread.getName(), ex.getMessage()));
            return t;
        };

        // 核心2，最大4，空闲存活1秒，队列容量2，默认拒绝策略AbortPolicy
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                customThreadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );

        System.out.println("[线程池初始化完毕] corePoolSize=2, maxPoolSize=4, queueCapacity=2");

        for (int i = 1; i <= 7; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.printf("  [任务执行中] 任务 ID: %d | 运行线程: %s%n", taskId, Thread.currentThread().getName());
                    try {
                        Thread.sleep(800); // 模拟耗时任务
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                System.out.printf("-> 成功提交任务 %d | 当前线程池大小: %d, 活跃线程数: %d, 队列排队数: %d%n",
                        taskId, executor.getPoolSize(), executor.getActiveCount(), executor.getQueue().size());
            } catch (Exception e) {
                System.out.printf("XX 提交任务 %d 失败触发拒绝策略! 异常原因: %s%n", taskId, e.getClass().getSimpleName());
            }
        }

        // 优雅关闭线程池
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试二：验证核心线程数 corePoolSize = 0 的行为
     * 场景：核心线程数为0，最大线程数为3，队列容量为5，空闲保活时间为500毫秒。
     * 验证：提交任务能否正常执行？工作线程空闲后能否完全释放降到0？
     */
    public static void testCorePoolSizeZeroBehavior() throws InterruptedException {
        ThreadPoolExecutor zeroCorePool = new ThreadPoolExecutor(
                0,                                // 核心线程数为 0
                3,                                // 最大线程数为 3
                500L,                             // 空闲500ms即销毁
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(5),     // 有界阻塞队列
                r -> new Thread(r, "zero-core-worker"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        System.out.println("[测试启动] 初始线程池状态 -> 当前线程数: " + zeroCorePool.getPoolSize());

        // 提交 3 个任务
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            zeroCorePool.execute(() -> {
                System.out.printf("  [核心为0池] 任务 %d 执行中, 线程: %s%n", taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }
            });
            System.out.printf("-> 提交任务 %d, 队列排队数: %d, 当前线程数: %d%n",
                    taskId, zeroCorePool.getQueue().size(), zeroCorePool.getPoolSize());
        }

        // 等待所有任务执行完毕并触发超时回收
        System.out.println("等待任务完成并观察线程空闲回收...");
        Thread.sleep(1500);

        System.out.printf("[结论验证] 任务完成且经过 keepAliveTime 后，当前线程池大小为: %d (已自动完全释放回收!)%n",
                zeroCorePool.getPoolSize());

        zeroCorePool.shutdown();
    }

    /**
     * 测试三：对比直接 new Thread() 与 线程池复用 的性能差异
     */
    public static void testDirectThreadVsThreadPoolPerformance() throws InterruptedException {
        final int taskCount = 1000;

        // 1. 直接为每个任务 new Thread 并启动
        long startDirect = System.currentTimeMillis();
        Thread[] threads = new Thread[taskCount];
        for (int i = 0; i < taskCount; i++) {
            threads[i] = new Thread(() -> {
                // 模拟简单的短平快计算任务
                int sum = 0;
                for (int j = 0; j < 1000; j++) sum += j;
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        long durationDirect = System.currentTimeMillis() - startDirect;
        System.out.printf("直接 new Thread() 执行 %d 个短任务耗时: %d ms%n", taskCount, durationDirect);

        // 2. 使用固定容量核心线程池复用线程
        long startPool = System.currentTimeMillis();
        ThreadPoolExecutor fixedPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        for (int i = 0; i < taskCount; i++) {
            fixedPool.execute(() -> {
                int sum = 0;
                for (int j = 0; j < 1000; j++) sum += j;
            });
        }
        fixedPool.shutdown();
        fixedPool.awaitTermination(5, TimeUnit.SECONDS);
        long durationPool = System.currentTimeMillis() - startPool;
        System.out.printf("使用 ThreadPoolExecutor(核心数8) 复用执行 %d 个短任务耗时: %d ms%n", taskCount, durationPool);

        System.out.printf(">> 性能对比结论：线程池避免了 %d 次操作系统线程创建与销毁开销，性能提升显著！%n", taskCount);
    }
}
