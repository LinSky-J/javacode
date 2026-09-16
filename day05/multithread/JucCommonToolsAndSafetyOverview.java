package multithread;

import java.util.concurrent.*;

/**
 * JUC 常用类全家桶、多线程安全保证体系、CountDownLatch 深度与同步替代方案
 *
 * 面试原题：
 * - juc包下你常用的类?
 * - 怎么保证多线程安全?
 * - Java并发工具你知道哪些?
 * - CountDownLatch 是做什么的讲一讲？
 * - 除了用synchronized，还有什么方法可以实现线程同步?
 *
 * 核心考点：
 * 1. JUC (java.util.concurrent) 六大核心子体系分类与生产常用类
 * 2. 保证多线程安全的三大核心思想（互斥加锁、无锁CAS、消除共享与数据隔离）
 * 3. 除了 synchronized 之外的 7 种线程同步替代方案
 * 4. CountDownLatch 深度剖析（底层 AQS 共享锁 state 倒计数、发令枪 vs 汇聚两大模式、与 CyclicBarrier 的区别）
 */
public class JucCommonToolsAndSafetyOverview {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】JUC 核心类库全景与多线程安全保证方案");
        System.out.println("================================================================================");
        explainJucOverviewAndSafetyPrinciples();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【面试核心答辩】除了 synchronized 还有哪些方式实现线程同步？");
        System.out.println("--------------------------------------------------------------------------------");
        explainSynchronizationAlternatives();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战代码验证】CountDownLatch 双模式实测：发令枪并发模式 + 结果汇聚模式");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateCountDownLatchTwoPatterns();
    }

    /**
     * JUC 常用类库与线程安全体系
     */
    public static void explainJucOverviewAndSafetyPrinciples() {
        System.out.println("1. JUC (java.util.concurrent) 包下常用的类（大厂六大分类法）：");
        System.out.println("   (1) 显式锁与条件队列 (locks)：");
        System.out.println("       * ReentrantLock（可重入互斥锁）、ReentrantReadWriteLock（读写分离锁）、StampedLock（乐观读锁）、Condition（精准条件队列）。");
        System.out.println("   (2) 原子类 (atomic)：");
        System.out.println("       * AtomicInteger、AtomicLong、AtomicReference、AtomicStampedReference（解决 ABA）、LongAdder（分段高并发累加）。");
        System.out.println("   (3) 高性能并发容器：");
        System.out.println("       * ConcurrentHashMap（线程安全哈希表）、CopyOnWriteArrayList（读多写极少列表）、ConcurrentLinkedQueue（无锁非阻塞队列）。");
        System.out.println("   (4) 阻塞队列 (BlockingQueue)：");
        System.out.println("       * ArrayBlockingQueue（有界数组队列）、LinkedBlockingQueue（链表队列）、DelayQueue（延迟任务）、SynchronousQueue（直接交接）。");
        System.out.println("   (5) 同步协作工具类：");
        System.out.println("       * CountDownLatch（倒计数闭锁）、CyclicBarrier（循环栅栏）、Semaphore（信号量限流）、Exchanger（双线程数据交换）。");
        System.out.println("   (6) 线程池与异步执行框架：");
        System.out.println("       * ThreadPoolExecutor（核心线程池）、ScheduledThreadPoolExecutor（定时调度）、CompletableFuture（异步编排流水线）、ForkJoinPool。");
        System.out.println();
        System.out.println("2. 怎么保证多线程安全？（三大核心设计思想）：");
        System.out.println("   - 思想一：互斥同步（Pessimistic Locking，阻塞型）：");
        System.out.println("     * 使用 synchronized 或 ReentrantLock 保护临界区，同一时间只允许单线程操作共享变量。");
        System.out.println("   - 思想二：非阻塞无锁并发（Optimistic Concurrency，自旋型）：");
        System.out.println("     * 基于硬件 CPU 级别的 CAS 原子操作（AtomicInteger、LongAdder），避免线程上下文切换开销。");
        System.out.println("   - 思想三：避免共享与数据隔离（消除竞争源头）：");
        System.out.println("     * 不可变对象 (Immutable)：用 final 修饰变量与只读集合，天生线程安全。");
        System.out.println("     * 线程私有存储 (ThreadLocal)：每个线程独享一份副本，彻底消除资源争用。");
    }

    /**
     * 除了 synchronized 之外的同步方式
     */
    public static void explainSynchronizationAlternatives() {
        System.out.println("在工业级开发中，实现线程同步有 6 大主流替代方案：");
        System.out.println("1. JUC 显式锁 (ReentrantLock / ReentrantReadWriteLock)：");
        System.out.println("   - 支持尝试加锁 tryLock()、超时等待、响应中断 lockInterruptibly()、公平锁与非公平锁切换。");
        System.out.println("2. 信号量机制 (Semaphore)：");
        System.out.println("   - 控制同时访问特定资源的并发线程数量（如数据库连接池、服务限流）。");
        System.out.println("3. 协作同步工具 (CountDownLatch / CyclicBarrier)：");
        System.out.println("   - 协调多线程之间的执行先后时序（如一等多、多等多）。");
        System.out.println("4. 阻塞队列 (BlockingQueue)：");
        System.out.println("   - 利用队列内部封装的 Condition 实现天然的生产者-消费者阻塞同步。");
        System.out.println("5. CAS 无锁原子类 (AtomicInteger / LongAdder)：");
        System.out.println("   - 适用于计数器、状态流转等低开销无阻塞同步场景。");
        System.out.println("6. 线程归并 (Thread.join)：");
        System.out.println("   - 阻塞等待目标线程执行完成，实现简单的父子线程时序同步。");
    }

    /**
     * CountDownLatch 双模式实测
     */
    public static void demonstrateCountDownLatchTwoPatterns() throws Exception {
        System.out.println("CountDownLatch 核心原理剖析：");
        System.out.println("   - 基于 AQS 共享锁实现；构造方法 CountDownLatch(int count) 将 AQS 的 state 设为 count；");
        System.out.println("   - countDown() 使用 CAS 将 state 循环减 1；当 state == 0 时，唤醒所有在 await() 挂起的线程；");
        System.out.println("   - 特性：【一次性计数器】，一旦归零便不可复用（若需循环复用请用 CyclicBarrier）。");
        System.out.println();

        // 模式 1：发令枪模式（多等一）—— 模拟 3 位运动员同时在起跑线等待发令枪响
        CountDownLatch startGun = new CountDownLatch(1);
        // 模式 2：汇聚等待模式（一等多）—— 模拟裁判等待 3 位运动员全部冲过终点线
        CountDownLatch finishLine = new CountDownLatch(3);

        System.out.println("【场景模拟】3 名运动员准备就绪，等待发令枪响并发起跑...");
        for (int i = 1; i <= 3; i++) {
            final int runnerId = i;
            new Thread(() -> {
                try {
                    System.out.printf("   [Runner-%d] 到位，等待发令枪响...\n", runnerId);
                    startGun.await(); // 模式 1：所有运动员阻塞等待发令枪归零
                    System.out.printf("   [Runner-%d] 枪响！全力奔跑中...\n", runnerId);
                    Thread.sleep(50 * runnerId); // 模拟耗时
                    System.out.printf("   [Runner-%d] 冲过终点！\n", runnerId);
                } catch (InterruptedException ignored) {
                } finally {
                    finishLine.countDown(); // 模式 2：每人冲过终点线，计数器减 1
                }
            }, "Runner-" + runnerId).start();
        }

        Thread.sleep(100); // 确保所有运动员进入 await
        System.out.println("   [裁判员] 砰！发令枪响！调用 startGun.countDown()");
        startGun.countDown(); // 瞬间唤醒所有运动员并发起跑

        System.out.println("   [裁判员] 调用 finishLine.await() 等待所有人冲线...");
        finishLine.await(); // 裁判阻塞等待所有人完成
        System.out.println("   [裁判员] 全员抵达终点，比赛圆满结束，开始颁奖！");
    }
}
