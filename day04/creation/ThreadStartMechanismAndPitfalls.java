package creation;

/**
 * 线程启动底层机制、start() 与 run() 差异及常见陷阱
 *
 * 面试原题：
 * - 怎么启动线程？
 *
 * 核心考点：
 * 1. 启动线程的唯一正确方式：调用 Thread.start()，绝非直接调用 run()
 * 2. start() 与 run() 的底层机制本质区别（开启操作系统新线程栈 vs 当前线程普通方法调用）
 * 3. 为什么同一个线程不能重复调用 start()？（threadStatus != 0 校验与 IllegalThreadStateException）
 * 4. 线程启动后什么时候真正执行？（就绪状态到运行状态的 OS 调度时间片分配）
 */
public class ThreadStartMechanismAndPitfalls {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】怎么启动线程？start() 与 run() 的本质剖析");
        System.out.println("================================================================================");
        explainStartVsRunMechanism();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 1】直接调用 run() vs 正确调用 start() 的执行现场对比");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateRunVsStart();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 2】重复调用 start() 触发 IllegalThreadStateException 底层原理");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateDuplicateStartException();
    }

    /**
     * start() 与 run() 的系统化理论解答
     */
    public static void explainStartVsRunMechanism() {
        System.out.println("1. 怎么启动线程？");
        System.out.println("   - 正确启动方式：创建 Thread 对象后，调用其 start() 方法！");
        System.out.println("2. start() 与 run() 的三大核心区别：");
        System.out.println("   (1) 是否创建新线程：");
        System.out.println("       * start()：通过 JNI 调用 JVM 本地方法 start0()，请求操作系统内核创建一个全新的原生线程，");
        System.out.println("         分配独立的线程栈与程序计数器（PC 寄存器），并在获得 CPU 时间片后自动异步回调 run()。");
        System.out.println("       * run()：仅仅是普通 Java 类中的一个普通成员方法，直接调用只是在【当前线程】（如 main 线程）内");
        System.out.println("         按顺序同步执行，完全不会创建任何新线程，失去了多线程并发意义！");
        System.out.println("   (2) 执行状态与生命周期：");
        System.out.println("       * start() 会让线程经历 NEW -> RUNNABLE 的状态跃迁；");
        System.out.println("       * run() 不会改变线程对象的任何内部状态。");
        System.out.println("   (3) 调用次数限制：");
        System.out.println("       * start() 只能调用 1 次，否则抛出 IllegalThreadStateException；");
        System.out.println("       * run() 作为普通方法，可以被任意线程重复调用无数次。");
    }

    /**
     * 实战对比 start() 与 run()
     */
    public static void demonstrateRunVsStart() throws InterruptedException {
        Runnable task = () -> {
            System.out.printf("   [Task 执行中] 当前线程名称: %s, 线程ID: %d, 是否为主线程: %b\n",
                    Thread.currentThread().getName(),
                    Thread.currentThread().getId(),
                    Thread.currentThread().getName().equals("main"));
        };

        Thread t1 = new Thread(task, "WorkerThread-A");

        System.out.println("步骤 A：错误调用方式 —— 直接调用 t1.run()：");
        t1.run(); // 仅仅是方法调用

        System.out.println("步骤 B：正确调用方式 —— 调用 t1.start()：");
        t1.start(); // 真正启动独立操作系统线程
        t1.join();

        System.out.println("【观测结论】：直接调用 run() 时，执行者赫然是 main 线程；只有调用 start() 才在独立子线程中异步运行。");
    }

    /**
     * 验证重复 start() 异常与源码剖析
     */
    public static void demonstrateDuplicateStartException() {
        Thread t2 = new Thread(() -> {
            System.out.println("   [t2 执行中] 业务处理完成");
        }, "WorkerThread-B");

        System.out.println("第 1 次调用 t2.start():");
        t2.start();

        try {
            System.out.println("尝试紧接着进行第 2 次调用 t2.start():");
            t2.start();
        } catch (IllegalThreadStateException ex) {
            System.out.println("   [捕获预期异常] " + ex.getClass().getName() + ": " + ex.getMessage());
            System.out.println("【JDK 源码级剖析】：");
            System.out.println("   在 Thread.java 源码中：");
            System.out.println("   public synchronized void start() {");
            System.out.println("       if (threadStatus != 0)");
            System.out.println("           throw new IllegalThreadStateException();");
            System.out.println("       ...");
            System.out.println("   }");
            System.out.println("   - 变量 threadStatus 记录线程内部状态，NEW 状态时值为 0；");
            System.out.println("   - 只要 start() 被调用过一次，该状态值会被更新（比如进入 RUNNABLE 或后续 TERMINATED）；");
            System.out.println("   - 无论线程当前是在运行、阻塞、甚至是已经彻底死亡，threadStatus 都不再是 0，");
            System.out.println("     因此 Java 严禁线程生命周期回退与重复启动！");
        }
    }
}
