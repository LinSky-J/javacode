package concurrentsafety;

/**
 * synchronized 深度剖析、锁升级全过程、JVM 锁优化及与 ReentrantLock 全面对比
 *
 * 面试原题：
 * - synchronized锁静态方法和普通方法区别?
 * - synchronized和reentrantlock区别？及其应用场景?
 * - 怎么理解可重入锁?
 * - synchronized 支持重入吗？如何实现的?
 * - syncronized锁升级的过程讲一下
 * - JVM对Synchornized的优化?
 *
 * 核心考点：
 * 1. 静态同步方法 (Class 锁) vs 普通同步方法 (this 实例锁) 作用域差异与互斥实测
 * 2. synchronized 与 ReentrantLock 的 7 大维度核心对比及应用场景选型
 * 3. 可重入锁设计本质与 HotSpot ObjectMonitor::_recursions 计数器源码机制
 * 4. 对象头 Mark Word 结构与锁升级 4 态流转（无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁）
 * 5. JVM 锁优化三剑客：自适应自旋 (Adaptive Spinning)、锁消除 (Lock Elimination)、锁粗化 (Lock Coarsening)
 */
public class SynchronizedDeepDiveAndLockEscalation {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】synchronized 锁静态方法 vs 普通方法");
        System.out.println("================================================================================");
        demonstrateStaticVsInstanceLock();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【面试核心答辩】可重入锁机制剖析与 synchronized 底层实现原理");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateReentrantFeature();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【大厂必考深度】synchronized 锁升级全过程 (无锁->偏向->轻量->重量)");
        System.out.println("--------------------------------------------------------------------------------");
        explainLockEscalationProcess();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【JVM 底层黑科技】JVM 对 synchronized 的三大深度优化");
        System.out.println("--------------------------------------------------------------------------------");
        explainJvmLockOptimizations();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【终极选型对照】synchronized vs ReentrantLock 7 大核心差异对比");
        System.out.println("--------------------------------------------------------------------------------");
        printSyncVsReentrantLockComparison();
    }

    /**
     * 静态方法锁 vs 普通方法锁实测
     */
    static class SyncService {
        // 静态方法：锁的是 SyncService.class 对象
        public static synchronized void staticMethod(String threadName) {
            System.out.printf("   [%s] 获得【Class 锁】，执行静态方法开始...\n", threadName);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            System.out.printf("   [%s] 退出【Class 锁】\n", threadName);
        }

        // 普通方法：锁的是当前 this 实例对象
        public synchronized void instanceMethod(String threadName) {
            System.out.printf("   [%s] 获得【this 实例锁】，执行成员方法开始...\n", threadName);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            System.out.printf("   [%s] 退出【this 实例锁】\n", threadName);
        }
    }

    private static void demonstrateStaticVsInstanceLock() throws InterruptedException {
        SyncService obj1 = new SyncService();
        SyncService obj2 = new SyncService();

        System.out.println("1. 静态方法锁：针对不同实例 obj1 和 obj2，静态方法依然互斥（因都竞争 SyncService.class）:");
        Thread t1 = new Thread(() -> SyncService.staticMethod("Thread-1"));
        Thread t2 = new Thread(() -> SyncService.staticMethod("Thread-2"));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("2. 普通方法锁：针对不同实例 obj1 和 obj2，成员同步方法彼此独立，互不阻塞并发执行:");
        Thread t3 = new Thread(() -> obj1.instanceMethod("Thread-3 (obj1)"));
        Thread t4 = new Thread(() -> obj2.instanceMethod("Thread-4 (obj2)"));
        t3.start();
        t4.start();
        t3.join();
        t4.join();
    }

    /**
     * 可重入锁机制实操与原理解析
     */
    static class ReentrantBase {
        public synchronized void doBase() {
            System.out.println("      -> [ReentrantBase.doBase] 成功重入父类同步方法！");
        }
    }

    static class ReentrantChild extends ReentrantBase {
        public synchronized void doChild() {
            System.out.println("   [ReentrantChild.doChild] 获得锁，准备调用内部同步方法 doOther()...");
            doOther();
            System.out.println("   [ReentrantChild.doChild] 准备调用父类同步方法 super.doBase()...");
            super.doBase();
        }

        public synchronized void doOther() {
            System.out.println("      -> [ReentrantChild.doOther] 成功重入本类其他同步方法！");
        }
    }

    private static void demonstrateReentrantFeature() {
        ReentrantChild child = new ReentrantChild();
        child.doChild();

        System.out.println("\n【synchronized 可重入底层 HotSpot 源码原理】：");
        System.out.println("   - 在 JVM HotSpot 的 ObjectMonitor 结构中，包含两个核心属性：");
        System.out.println("     * _owner: 记录当前持有锁的线程指针；");
        System.out.println("     * _recursions: 记录当前锁被重入的嵌套深度次数。");
        System.out.println("   - 当线程进入 synchronized 块时（monitorenter）：");
        System.out.println("     * 若 _owner == NULL，CAS 占用锁并将 _owner 设为当前线程，_recursions = 1；");
        System.out.println("     * 若 _owner == Thread.currentThread()，表示当前线程再次进入，无需阻塞，仅执行 _recursions++！");
        System.out.println("   - 当线程退出 synchronized 块时（monitorexit）：");
        System.out.println("     * 执行 _recursions--，只有当计数器减至 0 时，锁才算真正释放，_owner 置空！");
        System.out.println("   - 核心意义：彻底避免了同一线程在嵌套调用自身同步方法或继承链方法时出现【自己把自个儿死锁】！");
    }

    /**
     * 锁升级全过程深度剖析
     */
    public static void explainLockEscalationProcess() {
        System.out.println("JDK 1.6 之前 synchronized 是重量级锁；JDK 1.6 后引入 Mark Word 锁升级机制：");
        System.out.println("对象头 Mark Word 的 2 位锁标志位与 1 位偏向标志（64 位 JVM）：");
        System.out.println("1. 无锁状态 (001)：");
        System.out.println("   - 对象刚被创建，无任何线程竞争。Mark Word 记录对象的 HashCode、GC 分代年龄等。");
        System.out.println("2. 偏向锁 (101)：");
        System.out.println("   - 【核心假定】：大部分情况下锁不仅不存在多线程竞争，而且总是由同一个线程多次重入获得。");
        System.out.println("   - 【加锁过程】：当线程第一次进入同步块，CAS 将 Mark Word 中的 Thread ID 替换为当前线程 ID；");
        System.out.println("     后续该线程再次重入时，仅需比对当前线程 ID 与 Mark Word 是否一致，零 CAS 开销，性能极致！");
        System.out.println("3. 轻量级锁 (000)：");
        System.out.println("   - 【触发时机】：当有另一个线程尝试竞争偏向锁，偏向锁被撤销（Revocation），升级为轻量级锁。");
        System.out.println("   - 【加锁过程】：JVM 在当前线程的虚拟机栈帧中开辟一块名为锁记录 (Lock Record) 的空间，将对象头 Mark Word 拷贝进去（Displaced Mark Word）；");
        System.out.println("     然后通过 CAS 尝试将对象的 Mark Word 更新为指向 Lock Record 的指针。竞争线程通过自旋等待锁释放，避免线程挂起。");
        System.out.println("4. 重量级锁 (010)：");
        System.out.println("   - 【触发时机】：若竞争持续加剧，自旋超过阈值（或多个线程同时激烈争用），锁膨胀为重量级锁。");
        System.out.println("   - 【加锁过程】：JVM 向操作系统申请互斥量（Mutex），Mark Word 指针指向底层 ObjectMonitor；");
        System.out.println("     未抢到锁的线程进入 EntryList 挂起阻塞，陷入内核态（Ring 0），带来上下文切换开销。");
        System.out.println("   - 锁升级是单向不可逆的（无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁），极大地降低了无竞争与低竞争下的同步开销！");
    }

    /**
     * JVM 锁优化三大黑科技
     */
    public static void explainJvmLockOptimizations() {
        System.out.println("JVM JIT 编译器在运行时对 synchronized 的三大核心优化：");
        System.out.println("1. 自适应自旋锁 (Adaptive Spinning)：");
        System.out.println("   - 自旋等待避免了操作系统线程切换的微秒级开销；自适应意味着自旋次数不再固定，");
        System.out.println("     而是根据前一次在同一个锁上的自旋时间以及锁持有者的状态动态调整。前次成功则增加自旋，前次频繁失败则直接挂起。");
        System.out.println("2. 锁消除 (Lock Elimination)：");
        System.out.println("   - JIT 编译器进行【逃逸分析 (Escape Analysis)】，如果判定某个对象只在方法内部使用，绝不会逃逸到外部被其他线程访问，");
        System.out.println("     那么运行期直接将该对象的 synchronized 锁指令全部剔除消除（例如在单线程局部变量中拼接 StringBuffer）！");
        System.out.println("3. 锁粗化 (Lock Coarsening)：");
        System.out.println("   - 如果 JIT 检测到一连串连续操作反复对同一个对象加锁解锁（例如在一个循环体内频繁 synchronized），");
        System.out.println("     JVM 会把加锁同步的范围扩大到整个循环外部，只需加锁一次，大幅降低反复加锁解锁开销。");
    }

    /**
     * synchronized vs ReentrantLock 对比表
     */
    public static void printSyncVsReentrantLockComparison() {
        System.out.printf("%-15s | %-32s | %-32s\n", "对比维度", "synchronized 关键字", "ReentrantLock 显式锁");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-32s | %-32s\n", "实现层次", "JVM 原生指令 (monitorenter/exit)", "Java API 层 (基于 AQS 源码)");
        System.out.printf("%-15s | %-32s | %-32s\n", "灵活性与异常", "自动加锁与自动释放，代码清爽", "必须手动 lock() 并在 finally unlock()");
        System.out.printf("%-15s | %-32s | %-32s\n", "公平性支持", "仅支持非公平锁", "支持公平锁与非公平锁切换");
        System.out.printf("%-15s | %-32s | %-32s\n", "响应中断", "不可中断，拿不到锁死等", "支持 lockInterruptibly() 响应中断");
        System.out.printf("%-15s | %-32s | %-32s\n", "超时尝试", "不支持尝试与超时", "支持 tryLock(timeout) 超时放弃");
        System.out.printf("%-15s | %-32s | %-32s\n", "条件唤醒队列", "单一对象 Monitor (wait/notify)", "支持多个 Condition (精准单播定向唤醒)");
        System.out.printf("%-15s | %-32s | %-32s\n", "选型场景", "简单同步场景、低争用业务", "高争用、需要公平、超时放弃、可中断等复杂控制");
    }
}
