package waitnotify;

import java.util.LinkedList;
import java.util.Queue;

/**
 * wait 状态恢复全流程、notify vs notifyAll 差异与 notify 选线程底层机理
 *
 * 面试原题：
 * - wait 状态下的线程如何进行恢复到 running 状态?
 * - notify 和 notifyAll的区别?
 * - notify 选择哪个线程?
 *
 * 核心考点：
 * 1. wait 线程恢复到 running 的经典四步流转（WAITING -> notify移入EntryList -> BLOCKED争锁 -> RUNNABLE -> 获取CPU Running）
 * 2. HotSpot ObjectMonitor 底层队列：WaitSet、EntryList、_cxq
 * 3. notify 选择哪个线程？（JVM 规范定义为'任意/非确定性'；HotSpot 源码由 Knob_Notify 策略决定）
 * 4. notify vs notifyAll 的区别与信号丢失风险
 * 5. 为什么 wait 必须用 while 循环包裹？（防范虚假唤醒 Spurious Wakeup）
 */
public class WaitNotifyMechanismAndRecovery {

    private static final Object MONITOR = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】wait 恢复机制、notify 选线程与 notifyAll 深度解析");
        System.out.println("================================================================================");
        explainWaitNotifyInternals();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【状态流转实测】观察 wait() 线程从 WAITING -> BLOCKED -> RUNNABLE 的恢复过程");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateWaitRecoveryLifecycle();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战经典模型】使用 while 循环防止虚假唤醒的标准阻塞队列实现");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateWhileLoopSpuriousWakeupProtection();
    }

    /**
     * 理论核心系统剖析
     */
    public static void explainWaitNotifyInternals() {
        System.out.println("1. wait 状态下的线程如何恢复到 running 状态？（经典四步流转）");
        System.out.println("   (1) 第一步：线程调用 lock.wait()，释放持有锁，被 JVM 放入该 ObjectMonitor 的【WaitSet 等待集合】中，状态为 WAITING。");
        System.out.println("   (2) 第二步：其他线程调用 lock.notify() 或 notifyAll()，JVM 将该线程从 WaitSet 中移出，");
        System.out.println("       并转移到【EntryList 竞争队列】或【_cxq 队列】中，此时线程状态从 WAITING 转为【BLOCKED（阻塞等待锁）】！");
        System.out.println("   (3) 第三步：唤醒者线程退出 synchronized 释放锁，被唤醒线程与其他新来的线程共同竞争获取 Monitor 锁；");
        System.out.println("       成功抢占到锁后，线程状态从 BLOCKED 转为【RUNNABLE（就绪态）】。");
        System.out.println("   (4) 第四步：操作系统内核调度器分派 CPU 时间片，线程正式恢复为【Running（运行中）】，从当初 wait() 处继续向下执行！");
        System.out.println();
        System.out.println("2. notify 选择哪个线程？");
        System.out.println("   - 【JVM 规范官方定义】：选择是【完全任意且非确定性的 (arbitrary)】，没有任何顺序保证！");
        System.out.println("   - 【HotSpot 源码内部实现】：在 objectMonitor.cpp 的 ObjectMonitor::notify 方法中，");
        System.out.println("     由内部参数 Knob_Notify 决定具体的出队与转移策略（如策略 0 进 EntryList 头部、策略 1 进 EntryList 尾部、");
        System.out.println("     策略 2 进 _cxq 单向链表头部等）。在不同 JVM 版本或不同平台上其表现可能类似 LIFO 甚至随机。");
        System.out.println("   - 结论：业务代码绝对不能依赖 notify 唤醒的具体顺序！");
        System.out.println();
        System.out.println("3. notify 和 notifyAll 的本质区别：");
        System.out.println("   - notify()：只从 WaitSet 中唤醒单个线程。");
        System.out.println("     * 风险：极易引发【信号丢失 (Signal Loss)】！例如生产者唤醒了另一个生产者，新生产者因队列满继续等待，");
        System.out.println("       而真正的消费者没有收到任何通知，系统陷入死锁！");
        System.out.println("   - notifyAll()：唤醒 WaitSet 中的所有等待线程，全部移入 EntryList 竞争锁。");
        System.out.println("     * 优点：安全可靠，确保等待条件的真正目标线程一定会收到通知并竞争执行。");
    }

    /**
     * 实战演示 wait 线程恢复全生命周期
     */
    public static void demonstrateWaitRecoveryLifecycle() throws InterruptedException {
        Thread worker = new Thread(() -> {
            synchronized (MONITOR) {
                try {
                    System.out.println("   [Worker] 获取到锁，调用 MONITOR.wait()，进入 WAITING...");
                    MONITOR.wait();
                    System.out.println("   [Worker] 成功重新竞争到锁！从 wait() 处苏醒并恢复 Running 执行！");
                } catch (InterruptedException ignored) {}
            }
        }, "RecoveryWorker");

        worker.start();
        Thread.sleep(50); // 确保 Worker 已经进入 wait()
        System.out.println("   -> 观测 1: Worker 当前状态 = " + worker.getState() + " (预期: WAITING)");

        // 主线程获取锁，并在持有锁的过程中调用 notify()
        synchronized (MONITOR) {
            System.out.println("   [MainThread] 获取锁，调用 MONITOR.notify()...");
            MONITOR.notify();

            // 关键：此时 notify 已经发出，但 main 线程尚未释放锁！
            Thread.sleep(50);
            System.out.println("   -> 观测 2: notify 已发出但锁未释放，Worker 状态 = " + worker.getState() + " (预期: BLOCKED)");
            System.out.println("   [MainThread] 准备退出同步块，正式释放锁...");
        }

        // main 释放锁后，worker 抢到锁并运行结束
        worker.join();
        System.out.println("   -> 观测 3: Worker 执行完毕，状态 = " + worker.getState() + " (预期: TERMINATED)");
    }

    /**
     * 标准阻塞队列实现：实战验证为什么 wait 必须用 while 循环
     */
    static class SimpleBoundedQueue<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;

        public SimpleBoundedQueue(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void put(T item) throws InterruptedException {
            // 必须使用 while，防止虚假唤醒或者被 notifyAll 唤醒后容量依然满的问题
            while (queue.size() == capacity) {
                System.out.printf("   [Producer] 队列已满 (%d/%d)，调用 wait() 等待消费...\n", queue.size(), capacity);
                wait();
            }
            queue.add(item);
            System.out.printf("   [Producer] 成功生产: %s, 当前队列大小: %d\n", item, queue.size());
            notifyAll(); // 推荐 notifyAll 避免丢失消费信号
        }

        public synchronized T take() throws InterruptedException {
            // 必须使用 while 检查条件
            while (queue.isEmpty()) {
                System.out.println("   [Consumer] 队列为空，调用 wait() 等待生产...");
                wait();
            }
            T item = queue.poll();
            System.out.printf("   [Consumer] 成功消费: %s, 剩余队列大小: %d\n", item, queue.size());
            notifyAll();
            return item;
        }
    }

    private static void demonstrateWhileLoopSpuriousWakeupProtection() throws InterruptedException {
        SimpleBoundedQueue<String> boundedQueue = new SimpleBoundedQueue<>(1);

        Thread producer = new Thread(() -> {
            try {
                boundedQueue.put("商品-001");
                boundedQueue.put("商品-002");
            } catch (InterruptedException ignored) {}
        }, "Producer-1");

        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(100);
                boundedQueue.take();
                Thread.sleep(50);
                boundedQueue.take();
            } catch (InterruptedException ignored) {}
        }, "Consumer-1");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("【虚假唤醒防范总结】：");
        System.out.println("   - 如果用 if 判断条件，当线程被唤醒后，可能由于其他线程优先消耗了条件，");
        System.out.println("     直接向下执行会导致队列下溢（空指针/负数）或上溢崩溃！");
        System.out.println("   - 因此，【wait() 永远必须在 while(condition) 循环中调用】，反复校验条件满足才可执行！");
    }
}
