package lifecycle;

import java.util.concurrent.locks.ReentrantLock;

/**
 * BLOCKED 状态与 WAITING 状态的本质差异深度剖析与实战验证
 *
 * 面试原题：
 * - blocked和waiting有啥区别
 *
 * 核心考点：
 * 1. 触发源头：被动争抢锁 (BLOCKED) vs 主动等待条件通知 (WAITING)
 * 2. JVM 底层数据结构：ObjectMonitor 的 EntryList / _cxq (BLOCKED) vs WaitSet (WAITING)
 * 3. 锁持有状态：尚未获取锁 (BLOCKED) vs 主动释放持有的锁 (WAITING)
 * 4. 唤醒机制：锁释放后由 JVM 自动转移唤醒 vs 依赖外部显式 notify()/unpark()
 * 5. 面试深水坑：ReentrantLock.lock() 阻塞时线程到底是什么状态？（WAITING 而非 BLOCKED！）
 */
public class BlockedVsWaitingStateComparison {

    private static final Object SYNCHRONIZED_LOCK = new Object();
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】BLOCKED 与 WAITING 的核心区别深度解析");
        System.out.println("================================================================================");
        explainBlockedVsWaiting();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战对照 1】synchronized 锁竞争 (BLOCKED) vs Object.wait() (WAITING)");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateSynchronizedBlockedVsWait();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【大厂必考陷阱】ReentrantLock 争锁失败时线程状态到底是 BLOCKED 还是 WAITING？");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateReentrantLockBlockingState();
    }

    /**
     * 理论核心系统阐述
     */
    public static void explainBlockedVsWaiting() {
        System.out.println("在 Java 线程状态模型中，BLOCKED 和 WAITING 都是阻塞挂起（不占用 CPU），但有本质不同：");
        System.out.println("1. 状态定义与触发源头不同：");
        System.out.println("   - BLOCKED（被动互斥）：线程【试图进入或重入 synchronized 同步块/方法】，但 Monitor 锁");
        System.out.println("     当前已被其他线程独占持有。该线程处于完全被动的竞争锁阻塞态。");
        System.out.println("   - WAITING（主动等待）：线程【已经持有锁或自愿挂起】，主动调用了 Object.wait()、");
        System.out.println("     Thread.join() 或 LockSupport.park()。该线程是在主动等待一个外部条件的发生或信号通知。");
        System.out.println("2. 底层在 JVM ObjectMonitor 中的队列分布不同：");
        System.out.println("   - BLOCKED 线程驻留在【EntryList 竞争队列】或【_cxq 竞争单向链表】中；");
        System.out.println("   - WAITING 线程驻留在【WaitSet 等待集合】中。");
        System.out.println("3. 锁的持有与释放行为不同：");
        System.out.println("   - BLOCKED：从始至终尚未拿到锁，正在被动争抢锁；");
        System.out.println("   - WAITING：调用 wait() 前必须已持有锁，调用 wait() 后会【主动释放当前持有的锁】，让给其他线程。");
        System.out.println("4. 唤醒与流转路径不同：");
        System.out.println("   - BLOCKED：持有锁的线程退出 synchronized 块后，JVM 自动从 EntryList 唤醒候选线程争抢锁，无需任何显式通知；");
        System.out.println("   - WAITING：必须由其他线程显式调用 notify() / notifyAll() 或 LockSupport.unpark()。");
        System.out.println("     特别注意：WAITING 线程被 notify 唤醒后，并不直接变为 RUNNABLE，而是先被移入 EntryList 变成 BLOCKED，");
        System.out.println("     重新竞争获得锁之后才能变为 RUNNABLE！");
    }

    /**
     * 实操验证 synchronized 下的 BLOCKED 与 WAITING
     */
    public static void demonstrateSynchronizedBlockedVsWait() throws InterruptedException {
        // 线程 1：持有锁并在内部调用 wait() 进入 WAITING
        Thread waiterThread = new Thread(() -> {
            synchronized (SYNCHRONIZED_LOCK) {
                try {
                    // 调用 wait() 释放锁并进入 WAITING
                    SYNCHRONIZED_LOCK.wait();
                } catch (InterruptedException ignored) {}
            }
        }, "Waiter-Thread");

        // 线程 2：尝试获取被占用的锁进入 BLOCKED
        Thread blockerThread = new Thread(() -> {
            synchronized (SYNCHRONIZED_LOCK) {
                // 空任务，只要获取到锁即可退出
            }
        }, "Blocker-Thread");

        // 主线程先占用锁
        synchronized (SYNCHRONIZED_LOCK) {
            waiterThread.start();
            blockerThread.start();

            // 等待两个子线程尝试运行
            Thread.sleep(100);

            // blockerThread 尝试获取锁，但锁在 main 手中 -> BLOCKED
            System.out.println("   [Blocker-Thread 尝试进 synchronized] 当前状态: " + blockerThread.getState() + " (预期: BLOCKED)");
        }
        // main 释放锁，waiterThread 获得锁并执行 wait()

        Thread.sleep(100);
        System.out.println("   [Waiter-Thread 成功获取锁并调用 wait] 当前状态: " + waiterThread.getState() + " (预期: WAITING)");

        // 唤醒 waiterThread
        synchronized (SYNCHRONIZED_LOCK) {
            SYNCHRONIZED_LOCK.notify();
        }
        waiterThread.join();
        blockerThread.join();
    }

    /**
     * 揭示 ReentrantLock 争锁时的底层状态
     */
    public static void demonstrateReentrantLockBlockingState() throws InterruptedException {
        REENTRANT_LOCK.lock(); // main 线程先持有 ReentrantLock

        Thread jcthread = new Thread(() -> {
            REENTRANT_LOCK.lock(); // 尝试获取显式锁
            try {
                // 临界区
            } finally {
                REENTRANT_LOCK.unlock();
            }
        }, "JUC-Lock-Competitor");

        jcthread.start();
        Thread.sleep(100);

        System.out.println("   [JUC 竞争线程] 调用 ReentrantLock.lock() 获取锁失败时的真实状态: " + jcthread.getState());
        System.out.println("【核心剖析】：");
        System.out.println("   - 很多人想当然认为只要是'等待锁'就一定是 BLOCKED；");
        System.out.println("   - 但在 JVM 中，【BLOCKED 状态是 synchronized 内置 Monitor 锁的专属状态】！");
        System.out.println("   - J.U.C 并发包的 ReentrantLock 底层基于 AQS，当获取锁失败进入同步队列后，");
        System.out.println("     最终调用的是 LockSupport.park(this) 将线程挂起，因此其真实状态是【WAITING】！");

        REENTRANT_LOCK.unlock();
        jcthread.join();
    }
}
