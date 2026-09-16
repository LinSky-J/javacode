package lifecycle;

import java.util.concurrent.locks.LockSupport;

/**
 * Java 线程 6 大生命周期状态深度剖析与代码实证
 *
 * 面试原题：
 * - Java线程的状态有哪些?
 *
 * 核心考点：
 * 1. Thread.State 枚举定义的 6 大状态：NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
 * 2. Java 状态机流转图与操作系统 5 状态（新建、就绪、运行、阻塞、死亡）的对应差异
 * 3. 各状态触发条件、代码实操触发与 getState() 实时观测
 */
public class JavaThreadStatesTransitionDemo {

    private static final Object MONITOR_LOCK = new Object();

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 线程的 6 大状态与流转模型");
        System.out.println("================================================================================");
        explainSixStatesAndOsMapping();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战状态机验证】通过代码实时捕获并打印线程全部 6 种状态");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateAllSixStatesTransitions();
    }

    /**
     * 6 大状态与 OS 对应理论
     */
    public static void explainSixStatesAndOsMapping() {
        System.out.println("在 JDK java.lang.Thread.State 枚举中，明确定义了 6 种线程状态：");
        System.out.println("1. NEW (初始)：");
        System.out.println("   - 线程刚刚 new 出来，尚未调用 start() 方法。此时只是一个普通的 Java 堆对象。");
        System.out.println("2. RUNNABLE (运行/可运行)：");
        System.out.println("   - 涵盖操作系统层面的【Ready (就绪)】和【Running (运行中)】两种状态！");
        System.out.println("   - 线程正在 JVM 中执行，或者正在操作系统就绪队列中等待分配 CPU 时间片。");
        System.out.println("3. BLOCKED (阻塞)：");
        System.out.println("   - 专门指线程【等待获取 Monitor 监视器锁】的状态（仅限于进入或重入 synchronized 同步块/方法）。");
        System.out.println("   - 此时线程位于 ObjectMonitor 的 EntryList 或 _cxq 竞争队列中。");
        System.out.println("4. WAITING (无限期等待)：");
        System.out.println("   - 线程调用了无超时的等待方法：Object.wait()、Thread.join()、LockSupport.park()。");
        System.out.println("   - 不会消耗 CPU，必须等待其他线程显式通知唤醒（notify / notifyAll / unpark）。");
        System.out.println("5. TIMED_WAITING (限期/超时等待)：");
        System.out.println("   - 线程调用了带有超时期限的方法：Thread.sleep(ms)、Object.wait(ms)、Thread.join(ms)、LockSupport.parkNanos()。");
        System.out.println("   - 在达到指定等待时间后由 OS/JVM 自动唤醒，或提前被其他线程通知唤醒。");
        System.out.println("6. TERMINATED (终止/死亡)：");
        System.out.println("   - 线程的 run() 方法正常执行完毕退出，或者因抛出未捕获的异常而提前终结。");
        System.out.println();
        System.out.println("【面试核心差异追问】：Java 的 RUNNABLE 为什么把 Ready 和 Running 合并？");
        System.out.println("   - 因为现代操作系统的 CPU 时间片调度极其迅速（毫秒级别甚至微秒级别），线程在 Ready 与 Running");
        System.out.println("     之间的切换极快，在 JVM 层区分两者毫无意义，因而统称为 RUNNABLE 状态。");
    }

    /**
     * 代码实操验证所有 6 种状态
     */
    public static void demonstrateAllSixStatesTransitions() throws Exception {
        // 1. 验证 NEW
        Thread target = new Thread(() -> {
            try {
                // (2) 状态演变：从 RUNNABLE -> TIMED_WAITING
                Thread.sleep(100);

                // (3) 状态演变：进入 synchronized 块等待锁 -> BLOCKED
                synchronized (MONITOR_LOCK) {
                    // (4) 状态演变：获取到锁后调用 wait() -> WAITING
                    MONITOR_LOCK.wait();
                }
            } catch (InterruptedException ignored) {}
            // (5) 执行完毕退出 -> TERMINATED
        }, "StateDemo-Thread");

        System.out.println("1. 创建后未 start(): \t状态 = " + target.getState() + " (预期: NEW)");

        // 先占用锁，确保 target 稍后进入 synchronized 时会陷入 BLOCKED
        synchronized (MONITOR_LOCK) {
            // 2. 启动线程，验证 RUNNABLE
            target.start();
            System.out.println("2. 调用 start() 启动后: \t状态 = " + target.getState() + " (预期: RUNNABLE)");

            // 稍作等待，让 target 进入 Thread.sleep(100)
            Thread.sleep(30);
            System.out.println("3. 目标线程执行 sleep: \t状态 = " + target.getState() + " (预期: TIMED_WAITING)");

            // 等待 target 睡醒（100ms），由于 main 线程依然持有 MONITOR_LOCK，target 必然进入 BLOCKED
            Thread.sleep(120);
            System.out.println("4. 尝试获取被占用的锁: \t状态 = " + target.getState() + " (预期: BLOCKED)");
        }
        // main 线程退出同步块释放 MONITOR_LOCK，target 成功获取锁并调用 wait()，进入 WAITING

        Thread.sleep(50);
        System.out.println("5. 调用 wait() 主动等待: \t状态 = " + target.getState() + " (预期: WAITING)");

        // main 线程通过 notify 唤醒 target
        synchronized (MONITOR_LOCK) {
            MONITOR_LOCK.notify();
        }

        // 等待 target 运行结束
        target.join();
        System.out.println("6. 线程任务执行结束: \t状态 = " + target.getState() + " (预期: TERMINATED)");
    }
}
