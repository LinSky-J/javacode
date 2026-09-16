package jmm;

import java.util.concurrent.CountDownLatch;

/**
 * Java 内存模型 (Java Memory Model, JMM) 深度解析与实战验证
 *
 * 面试原题：
 * - Java的内存模型(JMM)介绍一下
 *
 * 核心考点：
 * 1. 为什么需要 JMM？（硬件多核缓存一致性问题、指令重排、跨平台统一规范）
 * 2. JMM 抽象结构：主内存 (Main Memory) 与工作内存 (Working Memory)
 * 3. 并发编程三大特性：可见性 (Visibility)、原子性 (Atomicity)、有序性 (Ordering)
 * 4. Happens-Before 先行发生原则（八大规则）
 * 5. volatile 底层原理：Lock 前缀指令、内存屏障 (Memory Barrier / Fence)
 */
public class JavaMemoryModelDeepDive {

    // 用于演示可见性问题的共享变量
    private static boolean flagWithoutVolatile = true;
    private static volatile boolean flagWithVolatile = true;

    // 用于演示原子性缺失的计数器
    private static int unsafeCount = 0;
    private static volatile int volatileCount = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 内存模型 (JMM) 深度剖析");
        System.out.println("================================================================================");
        explainJmmCoreConcept();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 1】JMM 可见性问题与 volatile 内存可见性保障");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateVisibility();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 2】volatile 不保证原子性实测");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateAtomicityIssue();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【核心理论】Happens-Before 先行发生原则与指令重排序");
        System.out.println("--------------------------------------------------------------------------------");
        explainHappensBeforeAndOrdering();
    }

    /**
     * JMM 核心概念系统化阐述
     */
    public static void explainJmmCoreConcept() {
        System.out.println("1. 为什么需要 JMM？");
        System.out.println("   - 现代计算机多核 CPU 拥有多级高速缓存（L1, L2, L3 Cache），写操作常在 Store Buffer 异步缓冲。");
        System.out.println("   - 各操作系统和 CPU 架构（x86/ARM）的内存访问模型存在显著差异（强内存模型 vs 弱内存模型）。");
        System.out.println("   - Java 作为跨平台语言，在 JVM 层面制定了 JSR-133 规范，即 JMM，统一了多线程并发访问共享内存的行为。");
        System.out.println("2. JMM 抽象内存结构：");
        System.out.println("   - 主内存 (Main Memory)：所有线程共享的物理内存区域，存储所有类变量、实例字段等共享变量。");
        System.out.println("   - 工作内存 (Working Memory)：每个线程私有的抽象概念，涵盖 CPU 寄存器、写缓冲区与各级 Cache。");
        System.out.println("   - 规定：线程不能直接读写主内存中的变量，必须先将变量拷贝到自己的工作内存中成为副本，");
        System.out.println("          操作完成后再刷新回主内存。不同线程之间无法直接访问对方的工作内存。");
        System.out.println("3. JMM 规定的 8 种原子交互操作（早期规范定义，帮助理解底层流程）：");
        System.out.println("   lock(锁定) -> read(读取) -> load(载入) -> use(使用) -> ");
        System.out.println("   assign(赋值) -> store(存储) -> write(写入) -> unlock(解锁)。");
    }

    /**
     * 演示 JMM 可见性
     */
    public static void demonstrateVisibility() throws InterruptedException {
        System.out.println("测试说明：启动后台子线程循环读取标志位，主线程 100ms 后将其置为 false。");

        // 验证 volatile 变量能即时被其它线程感知
        Thread worker = new Thread(() -> {
            long loopCount = 0;
            while (flagWithVolatile) {
                loopCount++;
                // 循环内不进行 System.out.println 或 Thread.sleep，因为这些操作底层含同步块/系统调用可能触发工作内存刷新
            }
            System.out.println("   [SubThread] 成功感知到 flagWithVolatile 被修改为 false！安全退出，自旋次数: " + loopCount);
        }, "Worker-Volatile");

        worker.start();
        Thread.sleep(100);
        flagWithVolatile = false;
        System.out.println("   [MainThread] 已将 flagWithVolatile 修改为 false，等待子线程退出...");
        worker.join(1000);

        if (worker.isAlive()) {
            System.out.println("   [警告] 子线程仍处于死循环，发生可见性缺失！");
            worker.interrupt();
        } else {
            System.out.println("   [结论] volatile 变量通过生成汇编 Lock 前缀指令与写屏障，强制刷新工作内存到主内存，实现强可见性。");
        }
    }

    /**
     * 演示 volatile 不具备复合操作原子性
     */
    public static void demonstrateAtomicityIssue() throws InterruptedException {
        int threadCount = 10;
        int incrementsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafeCount++;       // 非 volatile 普通自增
                    volatileCount++;     // volatile 修饰的自增
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        int expected = threadCount * incrementsPerThread;
        System.out.println("期望累计值: " + expected);
        System.out.println("普通变量 count: " + unsafeCount + " (通常 < " + expected + ")");
        System.out.println("volatile 变量 count: " + volatileCount + " (同样 < " + expected + ")");
        System.out.println("【核心剖析】：count++ 包含 3 步字节码指令：");
        System.out.println("   1. getstatic: 从主内存加载变量值到操作数栈顶；");
        System.out.println("   2. iconst_1 + iadd: 在栈顶执行加 1 计算；");
        System.out.println("   3. putstatic: 将计算结果写回操作数栈并刷新主内存。");
        System.out.println("   volatile 仅能保证第 1 步取到的值是最新的，但第 2 步和第 3 步之间多个线程发生交错覆盖，导致更新丢失！");
    }

    /**
     * Happens-Before 原则与指令重排序深度解析
     */
    public static void explainHappensBeforeAndOrdering() {
        System.out.println("1. 指令重排序：");
        System.out.println("   - 编译器优化重排：在不改变单线程语义的前提下，重新安排语句执行顺序。");
        System.out.println("   - 指令级并行重排 (ILP)：现代处理器利用多发射、流水线，允许乱序执行。");
        System.out.println("   - 内存系统重排：由于写缓冲区 (Store Buffer) 的存在，加载和存储可能乱序执行。");
        System.out.println("2. 内存屏障 (Memory Barrier) 类型：");
        System.out.println("   - LoadLoad: 保证 Load1 读先于 Load2 读。");
        System.out.println("   - StoreStore: 保证 Store1 写在 Store2 写之前刷新至内存。");
        System.out.println("   - LoadStore: 保证 Load1 读先于 Store2 写。");
        System.out.println("   - StoreLoad: 全能屏障，开销最大，保证 Store1 写先于 Load2 读。");
        System.out.println("3. JMM 的 Happens-Before 八大先行发生规则：");
        System.out.println("   (1) 程序次序规则 (Program Order Rule)：单线程内书写在前的操作先行发生于书写在后的操作。");
        System.out.println("   (2) 管程锁定规则 (Monitor Lock Rule)：同一个锁的 unlock 操作先行发生于后面对该锁的 lock 操作。");
        System.out.println("   (3) volatile 变量规则 (Volatile Variable Rule)：对 volatile 变量的写操作先行发生于后面对该变量的读操作。");
        System.out.println("   (4) 线程启动规则 (Thread Start Rule)：Thread.start() 先行发生于该线程 run() 方法内的任何操作。");
        System.out.println("   (5) 线程终止规则 (Thread Termination Rule)：线程内的所有操作均先行发生于对此线程终止的检测（join/isAlive）。");
        System.out.println("   (6) 线程中断规则 (Thread Interruption Rule)：对线程 interrupt() 的调用先行发生于被中断线程检测到中断事件的发生。");
        System.out.println("   (7) 对象终结规则 (Finalizer Rule)：一个对象的初始化完成（构造函数执行结束）先行发生于它的 finalize() 方法的开始。");
        System.out.println("   (8) 传递性规则 (Transitivity)：如果操作 A 先行发生于操作 B，且操作 B 先行发生于操作 C，则 A 先行发生于 C。");
    }
}
