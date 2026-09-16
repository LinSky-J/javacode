package creation;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Java 多线程本质认知、使用注意事项与死锁排查实践
 *
 * 面试原题：
 * - java多线程是什么？需要注意什么？
 * - 使用多线程要注意哪些问题?
 *
 * 核心考点：
 * 1. Java 多线程的本质与价值（利用多核并行、提升 I/O 密集型吞吐、异步解耦）
 * 2. 多线程带来的 5 大风险与挑战：
 *    - 安全性问题（竞态条件 Race Condition、破坏 JMM 三大特性）
 *    - 活跃性问题（死锁 Deadlock、活锁 Livelock、饥饿 Starvation）
 *    - 性能开销问题（频繁上下文切换 Context Switch、CPU 缓存失效）
 *    - 资源耗尽与泄露（线程数过大导致 OOM、ThreadLocal 内存泄露）
 *    - 对象逸出问题（构造器中 this 逸出暴露半成品对象）
 * 3. 死锁的四大必要条件及线上诊断（jstack / ThreadMXBean）
 */
public class MultithreadingPitfallsAndPractices {

    private static final Object RESOURCE_A = new Object();
    private static final Object RESOURCE_B = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 多线程认知与使用注意事项全解");
        System.out.println("================================================================================");
        explainMultithreadingConceptsAndRisks();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【活跃性隐患实战】模拟死锁 (Deadlock) 并用 JVM ThreadMXBean 自动检测");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateDeadlockAndDetection();
    }

    /**
     * 系统化解答多线程概念及核心注意事项
     */
    public static void explainMultithreadingConceptsAndRisks() {
        System.out.println("1. Java 多线程是什么？");
        System.out.println("   - 定义：多线程是指在单个程序（进程）内部同时运行多个并发执行流（线程）。");
        System.out.println("   - 资源分布：多个线程共享进程的堆内存（Heap）和方法区/元空间（Metaspace），但每个线程");
        System.out.println("     拥有独立的程序计数器（PC 寄存器）、虚拟机栈（VM Stack）和本地方法栈（Native Method Stack）。");
        System.out.println("   - 价值：充分利用多核 CPU 算力，显著提升高并发 I/O 请求吞吐量，异步解耦复杂耗时任务。");
        System.out.println();
        System.out.println("2. 使用多线程必须注意的 5 大问题与防范方案：");
        System.out.println("   (1) 线程安全与数据一致性问题（Safety Hazards）：");
        System.out.println("       * 问题：多个线程无序交错读写共享数据，导致脏写、更新丢失、数据混乱。");
        System.out.println("       * 防范：优先设计不可变类 (Immutable)；无共享状态设计；合理使用锁 (synchronized/ReentrantLock) 或原子类。");
        System.out.println("   (2) 活跃性风险（Liveness Hazards）：");
        System.out.println("       * 死锁 (Deadlock)：两个或多个线程互相持有对方所需的锁并陷入永久等待。");
        System.out.println("       * 活锁 (Livelock)：线程不断主动响应变化并谦让释放资源，但始终无法推进业务。");
        System.out.println("       * 饥饿 (Starvation)：非公平锁或优先级调度导致某些线程极长时间无法获得 CPU 运行。");
        System.out.println("   (3) 性能与吞吐倒退（Performance Hazards）：");
        System.out.println("       * 问题：线程并非越多越好！过多的线程会导致 CPU 频繁进行上下文切换（Context Switch），");
        System.out.println("         CPU 缓存行失效、TLB 刷新，甚至使得吞吐量远低于单线程！");
        System.out.println("       * 防范：根据任务类型（CPU 密集型: N+1，I/O 密集型: 2N 或 N * (1 + 等待时间/计算时间)）科学配置线程池。");
        System.out.println("   (4) 资源耗尽与内存泄露（Resource Exhaustion & Memory Leak）：");
        System.out.println("       * 问题：默认栈 1MB，无限 new Thread 会耗尽系统句柄，抛出 unable to create new native thread。");
        System.out.println("       * ThreadLocal 泄露：线程池复用核心线程时，ThreadLocalMap 中 Value 属于强引用，不手动 remove() 会导致内存泄露！");
        System.out.println("   (5) this 引用逸出（Publication Hazards）：");
        System.out.println("       * 问题：严禁在类的构造方法中启动线程或将 this 作为回调发布出去，可能导致其它线程看到尚未完成初始化的半成品对象！");
    }

    /**
     * 演示死锁模拟与自动诊断
     */
    public static void demonstrateDeadlockAndDetection() throws InterruptedException {
        System.out.println("死锁四大必要条件：");
        System.out.println("   1. 互斥条件：资源具有排他性，同一时间只能由一个线程持有；");
        System.out.println("   2. 请求与保持条件：一个线程因请求资源而阻塞时，对已获得的资源保持不放；");
        System.out.println("   3. 不可剥夺条件：线程已获得的资源在未使用完之前不能被强行剥夺；");
        System.out.println("   4. 循环等待条件：若干线程之间形成一种头尾相接的循环等待资源关系。");
        System.out.println();
        System.out.println("正在启动两个具有相反加锁顺序的线程，制造死锁...");

        Thread threadA = new Thread(() -> {
            synchronized (RESOURCE_A) {
                System.out.println("   [Thread-A] 成功持有 RESOURCE_A，休眠 50ms 准备获取 RESOURCE_B...");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
                synchronized (RESOURCE_B) {
                    System.out.println("   [Thread-A] 成功持有 RESOURCE_B");
                }
            }
        }, "Deadlock-Thread-A");

        Thread threadB = new Thread(() -> {
            synchronized (RESOURCE_B) {
                System.out.println("   [Thread-B] 成功持有 RESOURCE_B，休眠 50ms 准备获取 RESOURCE_A...");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
                synchronized (RESOURCE_A) {
                    System.out.println("   [Thread-B] 成功持有 RESOURCE_A");
                }
            }
        }, "Deadlock-Thread-B");

        threadA.setDaemon(true);
        threadB.setDaemon(true);
        threadA.start();
        threadB.start();

        // 给予足够时间触发死锁
        Thread.sleep(300);

        System.out.println("\n【线上排查工具模拟】利用 JVM ThreadMXBean 检测系统死锁：");
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();

        if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
            System.out.println("   [报警！检测到死锁存在！] 涉及死锁的线程总数: " + deadlockedThreadIds.length);
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds);
            for (ThreadInfo info : threadInfos) {
                System.out.printf("   -> 线程: [%s], 等待锁: [%s], 该锁当前被线程 [%s] 持有\n",
                        info.getThreadName(),
                        info.getLockInfo(),
                        info.getLockOwnerName());
            }
            System.out.println("【生产解决死锁方案】：");
            System.out.println("   1. 破坏循环等待：规定统一的加锁顺序（如先拿 A 再拿 B）；");
            System.out.println("   2. 破坏请求与保持：使用 ReentrantLock 的 tryLock(timeout) 机制，获取不到锁主动释放已有资源；");
            System.out.println("   3. 生产线上排查：利用命令行 jps -l 定位 PID，使用 jstack <pid> 直接导出线程死锁堆栈。");
        } else {
            System.out.println("   未检测到死锁。");
        }
    }
}
