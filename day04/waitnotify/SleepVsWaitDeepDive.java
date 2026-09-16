package waitnotify;

/**
 * Thread.sleep() 与 Object.wait() 全方位深度对比及 CPU 释放实证
 *
 * 面试原题：
 * - sleep 和 wait的区别是什么?
 * - sleep会释放cpu吗?
 *
 * 核心考点：
 * 1. 核心追问：sleep 会释放 CPU 吗？（会让出 CPU 调度时间片，但绝对不释放锁！）
 * 2. 为什么 wait() 定义在 Object 类，而 sleep() 定义在 Thread 类？
 * 3. 锁释放行为的本质差异（sleep 抱锁入睡 vs wait 释放锁让渡竞争）
 * 4. 同步监视器要求：wait() 必须在 synchronized 块中（否则抛 IllegalMonitorStateException）
 * 5. 状态机差异与唤醒条件差异
 */
public class SleepVsWaitDeepDive {

    private static final Object LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】sleep 与 wait 的本质区别及 CPU 释放机制");
        System.out.println("================================================================================");
        explainSleepVsWaitCore();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 1】sleep 抱锁入睡实测（验证其释放 CPU 但坚决不释放锁）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateSleepHoldsLock();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 2】wait 释放锁让渡实测（验证其既释放 CPU 又立即释放锁）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateWaitReleasesLock();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 3】非 synchronized 环境调用 wait 抛 IllegalMonitorStateException");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateIllegalMonitorStateException();
    }

    /**
     * 理论核心答辩
     */
    public static void explainSleepVsWaitCore() {
        System.out.println("1. 追问：sleep 会释放 CPU 吗？");
        System.out.println("   - 【答案】：绝对会释放 CPU！");
        System.out.println("   - 深度原理解析：当线程调用 Thread.sleep() 时，操作系统会将当前线程从 CPU 运行队列中剥离，");
        System.out.println("     放入定时等待队列，让出 CPU 执行权，供其他就绪线程使用，或使 CPU 进入空闲节能状态（Idle）。");
        System.out.println("   - 混淆源头：许多初学者将【不释放锁】误解为【不释放 CPU】。记住：sleep 释放 CPU 调度权，但紧抱持有的锁不放！");
        System.out.println();
        System.out.println("2. sleep 与 wait 的 5 大核心区别对比表：");
        System.out.printf("%-15s | %-32s | %-32s\n", "对比维度", "Thread.sleep()", "Object.wait()");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-32s | %-32s\n", "所属类与类型", "Thread 类的静态本地方法 (static native)", "Object 类的实例最终本地方法 (final native)");
        System.out.printf("%-15s | %-32s | %-32s\n", "是否释放锁", "【不释放锁】，哪怕睡到超时也一直占着", "【释放锁】，主动交出当前对象的 Monitor 锁");
        System.out.printf("%-15s | %-32s | %-32s\n", "调用环境限制", "任何地方均可自由调用，无限制", "必须在当前对象的 synchronized 块内调用");
        System.out.printf("%-15s | %-32s | %-32s\n", "唤醒方式", "休眠时间结束自动唤醒，或被 interrupt", "需同一对象 notify/notifyAll 或超时唤醒");
        System.out.printf("%-15s | %-32s | %-32s\n", "线程状态", "TIMED_WAITING", "WAITING (无参) / TIMED_WAITING (有参)");
        System.out.println();
        System.out.println("3. 为什么 wait/notify 定义在 Object 类，而 sleep 定义在 Thread 类？");
        System.out.println("   - 因为在 Java 中，【任何一个对象都可以充当监视器锁（Monitor）】！每个对象头中都有 Mark Word，");
        System.out.println("     指向底层 ObjectMonitor。等待队列（WaitSet）是绑定在具体的【锁对象】上的，而不是线程上的。");
        System.out.println("   - 一个线程可以同时持有多个对象的锁，调用某个对象的 wait() 只释放这一个对象的锁，其他锁继续持有；");
        System.out.println("   - 而 sleep() 是线程自身的休眠控制，不涉及任何对象锁的协同，因此是 Thread 类的静态方法。");
    }

    /**
     * 演示 sleep 不释放锁
     */
    public static void demonstrateSleepHoldsLock() throws InterruptedException {
        Thread threadA = new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("   [Thread-A] 获取到锁，准备调用 Thread.sleep(300) 抱锁入睡...");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                System.out.println("   [Thread-A] 300ms 睡醒，即将退出 synchronized 释放锁");
            }
        }, "Sleep-Thread-A");

        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(50); // 确保 A 先拿到锁
            } catch (InterruptedException ignored) {}
            long startTime = System.currentTimeMillis();
            System.out.println("   [Thread-B] 尝试获取锁...");
            synchronized (LOCK) {
                long waitTime = System.currentTimeMillis() - startTime;
                System.out.printf("   [Thread-B] 成功获取到锁！共计被阻塞了 %d ms\n", waitTime);
            }
        }, "Sleep-Thread-B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
        System.out.println("   -> 实测证实：Thread-B 被足足阻塞了 200多毫秒，证明 Thread-A sleep 期间没有释放锁！");
    }

    /**
     * 演示 wait 释放锁
     */
    public static void demonstrateWaitReleasesLock() throws InterruptedException {
        Thread threadA = new Thread(() -> {
            synchronized (LOCK) {
                System.out.println("   [Thread-A] 获取到锁，调用 LOCK.wait() 释放锁并进入 WAITING...");
                try {
                    LOCK.wait();
                } catch (InterruptedException ignored) {}
                System.out.println("   [Thread-A] 被成功唤醒并重新抢到了锁！");
            }
        }, "Wait-Thread-A");

        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(50); // 确保 A 先进 wait
            } catch (InterruptedException ignored) {}
            System.out.println("   [Thread-B] 尝试获取锁...");
            synchronized (LOCK) {
                System.out.println("   [Thread-B] 成功瞬间获取到锁！证明 wait() 已经把锁释放了！");
                System.out.println("   [Thread-B] 发送 LOCK.notify() 唤醒等待中的 Thread-A...");
                LOCK.notify();
            }
        }, "Wait-Thread-B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
        System.out.println("   -> 实测证实：Thread-A 一旦 wait()，Thread-B 毫无延迟地拿到了锁，证明 wait() 会释放锁！");
    }

    /**
     * 演示未在 synchronized 块中调用 wait 抛出异常
     */
    public static void demonstrateIllegalMonitorStateException() {
        Object rawObject = new Object();
        try {
            System.out.println("尝试在普通代码段（未加 synchronized）直接调用 rawObject.wait():");
            rawObject.wait();
        } catch (IllegalMonitorStateException ex) {
            System.out.println("   [捕获预期异常] " + ex.getClass().getName());
            System.out.println("   -> 原因剖析：调用 wait() 的前提是当前线程必须是该对象 Monitor 的持有者（Owner），");
            System.out.println("      否则无法将线程注册到该对象的 WaitSet 队列并操作其释放锁，JVM 强制抛出异常！");
        } catch (InterruptedException ignored) {}
    }
}
