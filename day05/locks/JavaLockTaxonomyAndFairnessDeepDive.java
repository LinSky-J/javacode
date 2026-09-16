package locks;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Java 锁分类谱系全景、实践用锁原则、乐观锁多种实现与公平/非公平锁深度机制
 *
 * 面试原题：
 * - Java中有哪些常用的锁，在什么场景下使用？
 * - 怎么在实践中用锁的？
 * - 悲观锁和乐观锁的区别?
 * - Java中想实现一个乐观锁，都有哪些方式?
 * - 什么是公平锁和非公平锁?
 * - 非公平锁吞吐量为什么比公平锁大？
 * - Synchronized是公平锁吗?
 * - ReentrantLock是怎么实现公平锁的？
 *
 * 核心考点：
 * 1. Java 锁多维度分类树谱（乐观/悲观、公平/非公平、可重入、独占/共享、可中断）
 * 2. 生产环境用锁 5 大黄金法则（减小锁粒度、读写分离、tryLock 防死锁等）
 * 3. 乐观锁的 4 种实现方式（CAS 原子类、版本号 version、StampedLock 乐观读、DB 隐式 CAS）
 * 4. 非公平锁吞吐量碾压公平锁的底层原因（避免上下文切换唤醒延迟）
 * 5. ReentrantLock 源码级实现：hasQueuedPredecessors() 前驱排队判断
 */
public class JavaLockTaxonomyAndFairnessDeepDive {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 锁分类全家桶与使用场景");
        System.out.println("================================================================================");
        explainLockTaxonomyAndUsage();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【面试核心答辩】悲观锁 vs 乐观锁 & Java 实现乐观锁的 4 种方式");
        System.out.println("--------------------------------------------------------------------------------");
        explainPessimisticVsOptimistic();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 1】StampedLock 乐观读模式 (Optimistic Read) 实操");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateStampedLockOptimistic();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 2】公平锁 vs 非公平锁原理、吞吐量差异与 ReentrantLock 源码实现");
        System.out.println("--------------------------------------------------------------------------------");
        explainFairnessAndReentrantLockMechanism();
    }

    /**
     * 锁分类树谱与场景
     */
    public static void explainLockTaxonomyAndUsage() {
        System.out.println("1. Java 常用锁分类维度全景表：");
        System.out.printf("%-16s | %-32s | %-28s\n", "分类维度", "对立概念与常见代表", "核心特性与场景");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("%-16s | %-32s | %-28s\n", "态度倾向", "悲观锁 (synchronized) vs 乐观锁 (CAS/版本号)", "写多竞争激烈选悲观，读多冲突极少选乐观");
        System.out.printf("%-16s | %-32s | %-28s\n", "排队规则", "公平锁 (FairSync) vs 非公平锁 (NonfairSync)", "非公平锁吞吐量极高，公平锁防饥饿");
        System.out.printf("%-16s | %-32s | %-28s\n", "重入特性", "可重入锁 (synchronized, ReentrantLock)", "允许同线程多次进入，防止自身死锁");
        System.out.printf("%-16s | %-32s | %-28s\n", "共享权限", "排他独占锁 (WriteLock) vs 共享锁 (ReadLock)", "读共享并行提升读性能，写独占排他");
        System.out.printf("%-16s | %-32s | %-28s\n", "响应中断", "可中断锁 (lockInterruptibly) vs 不可中断锁 (synchronized)", "复杂等待需要超时放弃时选可中断显式锁");
        System.out.println();
        System.out.println("2. 怎么在工业级实践中用锁？（5 大黄金原则）：");
        System.out.println("   - 原则 1：【减小锁粒度】仅对操作共享变量的几行核心代码加锁，严禁在锁内调用外部 RPC、数据库 I/O 或大循环；");
        System.out.println("   - 原则 2：【读写分离】读多写少业务首选 ReentrantReadWriteLock 或 StampedLock；");
        System.out.println("   - 原则 3：【锁分段/槽化】如 LongAdder Cell[]、ConcurrentHashMap 分桶锁，分散单点竞争；");
        System.out.println("   - 原则 4：【优先使用 tryLock(timeout)】避免线程在无法获取锁时永久挂起，有效防范死锁；");
        System.out.println("   - 原则 5：【严禁用常量池对象作为锁】如 String 字符串常量、Integer 包装类缓存 (-128~127)，可能引发全局不可控死锁！");
    }

    /**
     * 悲观锁 vs 乐观锁
     */
    public static void explainPessimisticVsOptimistic() {
        System.out.println("1. 悲观锁与乐观锁的区别：");
        System.out.println("   - 悲观锁：假定并发冲突必然发生。每次访问数据时均加互斥排他锁（阻止其他线程并发读写）。");
        System.out.println("   - 乐观锁：假定并发冲突极少发生。访问数据时不加锁，仅在最终提交更新时比对版本号或旧值。");
        System.out.println("2. Java 中想实现乐观锁的 4 大方式：");
        System.out.println("   - 方式 1：JUC Atomic 原子类（底层利用 CPU 的 cmpxchg 汇编指令实现无锁 CAS）；");
        System.out.println("   - 方式 2：数据实体携带 version 版本号字段（内存中比对，发生变动则重试或拒绝）；");
        System.out.println("   - 方式 3：StampedLock 乐观读模式（tryOptimisticRead 获取时间戳，读取后调用 validate 校验是否被修改）；");
        System.out.println("   - 方式 4：数据库行级隐式 CAS（UPDATE t_order SET status=1, version=version+1 WHERE id=? AND version=?）。");
    }

    /**
     * StampedLock 乐观读实战
     */
    static class OptimisticPoint {
        private double x, y;
        private final StampedLock sl = new StampedLock();

        public void move(double deltaX, double deltaY) {
            long stamp = sl.writeLock(); // 独占写锁
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        public double distanceFromOrigin() {
            // 步骤 1：获取乐观读 stamp（完全无锁，不阻塞写操作）
            long stamp = sl.tryOptimisticRead();
            double curX = x, curY = y;
            // 步骤 2：校验读期间是否有写锁发生
            if (!sl.validate(stamp)) {
                // 有写操作干扰，升级为传统悲观读锁重新读取
                stamp = sl.readLock();
                try {
                    curX = x;
                    curY = y;
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return Math.sqrt(curX * curX + curY * curY);
        }
    }

    private static void demonstrateStampedLockOptimistic() {
        OptimisticPoint point = new OptimisticPoint();
        point.move(3.0, 4.0);
        double dist = point.distanceFromOrigin();
        System.out.printf("   [StampedLock] 成功通过乐观读无阻塞计算距离: %.2f (预期: 5.00)\n", dist);
        System.out.println("   -> 特性：极大优化传统 ReadWriteLock 读读互斥导致的写线程饥饿问题。");
    }

    /**
     * 公平锁 vs 非公平锁源码原理
     */
    public static void explainFairnessAndReentrantLockMechanism() {
        System.out.println("1. 什么是公平锁与非公平锁？");
        System.out.println("   - 公平锁 (Fair Lock)：多个线程按照申请锁的绝对时间先后顺序（FIFO），排队获取锁。");
        System.out.println("   - 非公平锁 (Non-fair Lock)：新到达的线程不顾等待队列，直接尝试插队争抢锁，抢不到才去排队。");
        System.out.println("2. 非公平锁吞吐量为什么比公平锁大？（面试深度核心剖析）：");
        System.out.println("   - 核心在于【线程唤醒的延迟代价】与【上下文切换损耗】！");
        System.out.println("   - 在公平锁模式下：持有锁的线程释放锁后，JVM 唤醒队列中第一个挂起线程，这个从内核态唤醒到线程就绪、");
        System.out.println("     再到分配 CPU 时间片，需要经历微秒级的时间窗口（约 1~2 微秒）；在此期间 CPU 是空闲的。");
        System.out.println("   - 在非公平锁模式下：正好一个处于 Running 状态的新线程前来争锁，直接将锁分配给它立刻执行，");
        System.out.println("     充分利用了 CPU 缓存与时间片（俗称'搭便车'），成倍降低了线程挂起与唤醒的次数，吞吐量极大提高！");
        System.out.println("3. Synchronized 是公平锁吗？");
        System.out.println("   - 【绝对不是】！synchronized 从设计上就是非公平锁，新到来的线程首先自旋抢锁，抢不到才入队列。");
        System.out.println("4. ReentrantLock 是怎么实现公平锁的？（AQS 源码级别）：");
        System.out.println("   - ReentrantLock 内部提供了两个静态内部类：FairSync 与 NonfairSync；");
        System.out.println("   - 在 FairSync.tryAcquire() 源码中，相比非公平锁多调用了一个关键方法：");
        System.out.println("     if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) { ... }");
        System.out.println("   - hasQueuedPredecessors() 底层检测：判断 AQS 的 CLH 变体同步队列中，是否存在排在当前线程之前的前驱节点；");
        System.out.println("     如果有其他线程比自己更早排队，哪怕当前锁处于空闲（state==0），当前线程也坚决不插队，直接进入队尾排队！");
    }
}
