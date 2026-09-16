package multithread;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 死锁产生四大必要条件、线上诊断工具与两大工业级解决方案（顺序加锁 & tryLock 超时退避）
 *
 * 面试原题：
 * - 什么情况会产生死锁问题？如何解决?
 *
 * 核心考点：
 * 1. 产生死锁的四大必要条件（互斥、请求与保持、不剥夺、循环等待）
 * 2. 解决方案一：破坏循环等待条件（全局统一锁排序规则严格升序加锁）
 * 3. 解决方案二：破坏不可剥夺条件（ReentrantLock.tryLock 超时主动放弃 + 随机退避重试）
 * 4. 线上生产死锁排查三剑客（jstack、ThreadMXBean、Arthas thread -b）
 */
public class DeadlockPreventionAndResolutionDeepDive {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】什么情况会产生死锁？四大必要条件剖析");
        System.out.println("================================================================================");
        explainDeadlockFourConditions();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战破解方案 1】全局锁排序（严格按资源 ID 升序加锁，破坏循环等待）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateOrderedLockingResolution();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战破解方案 2】ReentrantLock.tryLock(timeout) 超时退避重试（破坏不可剥夺）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateTryLockTimeoutResolution();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【生产排查指南】线上死锁定位与诊断三剑客");
        System.out.println("--------------------------------------------------------------------------------");
        explainProductionDiagnostics();
    }

    /**
     * 四大必要条件
     */
    public static void explainDeadlockFourConditions() {
        System.out.println("死锁 (Deadlock) 是指两个或多个线程在执行过程中，因互相争夺对方持有的锁资源而造成的互相等待现象。");
        System.out.println("死锁必须同时满足以下【四大充分必要条件】，缺一不可：");
        System.out.println("1. 互斥条件 (Mutual Exclusion)：资源在一段时间内只能由一个线程独占持有，其他申请者必须等待；");
        System.out.println("2. 请求与保持条件 (Hold and Wait)：线程已经保持了至少一个资源，但又提出了新的资源请求，并因新资源被占而阻塞，同时对自己已有资源绝不放手；");
        System.out.println("3. 不可剥夺条件 (No Preemption)：线程已获得的锁资源在未完成使用之前，其他线程或系统不能强行剥夺，只能由持有者自己主动释放；");
        System.out.println("4. 循环等待条件 (Circular Wait)：发生死锁时，必然存在一个进程-资源的环形等待链（如 A 占有 1 等 2，B 占有 2 等 1）。");
    }

    /**
     * 方案 1：全局锁排序，破坏循环等待
     */
    static class Account {
        private final int id;
        private double balance;
        final Object lock = new Object();

        public Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        // 银行转账业务：严格按账户 ID 升序加锁
        public static void transferSafe(Account from, Account to, double amount) {
            Account firstLock = from.id < to.id ? from : to;
            Account secondLock = from.id < to.id ? to : from;

            synchronized (firstLock.lock) {
                synchronized (secondLock.lock) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.printf("   [TransferSafe] 成功完成转账: 从 Account-%d 向 Account-%d 转入 %.1f 元\n",
                            from.id, to.id, amount);
                }
            }
        }
    }

    private static void demonstrateOrderedLockingResolution() throws InterruptedException {
        Account acc1 = new Account(101, 1000);
        Account acc2 = new Account(102, 1000);

        // 线程 1：从 101 转给 102
        Thread t1 = new Thread(() -> Account.transferSafe(acc1, acc2, 100));
        // 线程 2：从 102 转给 101（反向转账，传统写法极易死锁，但由于内部排序，依然严格先锁 101 再锁 102！）
        Thread t2 = new Thread(() -> Account.transferSafe(acc2, acc1, 200));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("   -> 成果：即使存在反向交叉转账，由于全局排序锁定，彻底消除了循环等待环路！");
    }

    /**
     * 方案 2：tryLock 带超时主动放弃重试
     */
    static class TryLockResource {
        private final ReentrantLock lockA = new ReentrantLock();
        private final ReentrantLock lockB = new ReentrantLock();
        private final Random random = new Random();

        public void workerTask(String name, boolean reverse) {
            ReentrantLock first = reverse ? lockB : lockA;
            ReentrantLock second = reverse ? lockA : lockB;

            while (true) {
                boolean firstAcquired = false;
                boolean secondAcquired = false;
                try {
                    firstAcquired = first.tryLock(50, TimeUnit.MILLISECONDS);
                    if (firstAcquired) {
                        secondAcquired = second.tryLock(50, TimeUnit.MILLISECONDS);
                    }

                    if (firstAcquired && secondAcquired) {
                        System.out.printf("   [%s] 成功同时获取到两把锁，安全执行业务！\n", name);
                        return; // 业务执行成功退出
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    // 如果只拿到了第一把锁而第二把超时未获取，主动释放第一把锁（破坏不可剥夺）！
                    if (firstAcquired && !secondAcquired) {
                        first.unlock();
                    } else if (firstAcquired && secondAcquired) {
                        second.unlock();
                        first.unlock();
                    }
                }

                // 随机退避，避免活锁 (Livelock)
                try {
                    Thread.sleep(random.nextInt(30) + 10);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private static void demonstrateTryLockTimeoutResolution() throws InterruptedException {
        TryLockResource resource = new TryLockResource();
        Thread t1 = new Thread(() -> resource.workerTask("Worker-Forward", false));
        Thread t2 = new Thread(() -> resource.workerTask("Worker-Reverse", true));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("   -> 成果：tryLock 配合超时放弃与随机退避，有效规避了死锁与活锁风险！");
    }

    /**
     * 线上诊断指南
     */
    public static void explainProductionDiagnostics() {
        System.out.println("生产环境遭遇死锁时的定位三剑客：");
        System.out.println("1. JDK 自带 jstack 堆栈排查：");
        System.out.println("   - 第一步：使用 jps -l 找到出现高卡顿或接口超时的 Java 进程 PID；");
        System.out.println("   - 第二步：执行 jstack -l <pid> > thread_dump.log 输出线程堆栈；");
        System.out.println("   - 第三步：拉到日志末尾，JVM 会自动扫描并打印：");
        System.out.println("     'Found one Java-level deadlock:' 并精确指出涉及死锁的线程名称、等待的锁内存地址及代码行号！");
        System.out.println("2. JVM 编程式监控报警：");
        System.out.println("   - 利用 ManagementFactory.getThreadMXBean().findDeadlockedThreads() 编写后台定时巡检任务，发现死锁立即触发告警。");
        System.out.println("3. 阿里开源诊断神器 Arthas：");
        System.out.println("   - 终端输入 thread -b 命令，Arthas 会一键高亮输出当前正在阻塞其他线程的那个死锁'罪魁祸首'线程！");
    }
}
