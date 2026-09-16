package concurrentsafety;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * CAS 底层原理、三大致命缺陷与 Java 工业级应对方案
 *
 * 面试原题：
 * - CAS 有什么缺点?
 * - 为什么不能所有的锁都用CAS?
 * - CAS 有什么问题，Java是怎么解决的?
 *
 * 核心考点：
 * 1. CAS 核心机制与 CPU 硬件汇编 lock cmpxchg 原语
 * 2. 缺陷一：ABA 问题深度剖析与 AtomicStampedReference 版本戳实战解决
 * 3. 缺陷二：高并发长时间自旋导致 CPU 100% 消耗与 LongAdder 分段槽化解决
 * 4. 缺陷三：只能保证单变量原子性与 AtomicReference 复合对象封装
 * 5. 架构反思：为什么不能所有锁都用 CAS？（自旋空耗 CPU vs 内核态挂起权衡）
 */
public class CasPrinciplesDrawbacksAndSolutions {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】CAS 底层机制与三大缺陷全景剖析");
        System.out.println("================================================================================");
        explainCasPrincipleAndThreeDrawbacks();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战复现 1】经典 ABA 问题复现（普通 CAS 无法感知中间篡改）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateAbaProblem();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战解决 2】AtomicStampedReference 引入版本戳彻底解决 ABA 问题");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateAbaResolutionWithStamp();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【架构核心追问】为什么不能所有的锁都用 CAS？");
        System.out.println("--------------------------------------------------------------------------------");
        explainWhyNotAllLocksUseCas();
    }

    /**
     * CAS 理论与三大缺陷
     */
    public static void explainCasPrincipleAndThreeDrawbacks() {
        System.out.println("1. 什么是 CAS？");
        System.out.println("   - CAS 全称 Compare-And-Swap（比较并交换），是一种硬件级别的原子指令（如 x86 的 lock cmpxchg）；");
        System.out.println("   - 包含三个参数：内存地址 V、预期原值 A、更新目标值 B。当且仅当 V 中的当前值等于 A 时，才原子地将其更新为 B，否则不执行更新。");
        System.out.println();
        System.out.println("2. CAS 的三大致命缺陷及 Java 的解决方案：");
        System.out.println("   (1) 缺陷一：ABA 问题");
        System.out.println("       * 问题本质：变量初值为 A，中途被其他线程修改为 B，随后又被修改回 A。CAS 检查时发现依然是 A，误判为从未被改动过。");
        System.out.println("         在无锁链表栈（Treiber Stack）等场景中，会导致节点指针悬挂与内存破坏！");
        System.out.println("       * 解决之道：引入版本号戳 (Version / Stamp)。Java 提供了 AtomicStampedReference（引用 + int 版本戳）");
        System.out.println("         和 AtomicMarkableReference（引用 + boolean 标记），更新时同时比对值与版本号！");
        System.out.println("   (2) 缺陷二：循环时间长，CPU 开销极大（自旋风暴）");
        System.out.println("       * 问题本质：在高并发争用激烈时，CAS 失败率极高，自旋死循环会导致单个 CPU 核心占用率飙升至 100%，");
        System.out.println("         同时引发缓存一致性流量风暴（Bus Lock Storm）。");
        System.out.println("       * 解决之道：");
        System.out.println("         a. 结合 AQS：自旋达到一定阈值后调用 LockSupport.park 挂起线程，让出 CPU；");
        System.out.println("         b. 空间换时间：引入 LongAdder / Striped64，将单个热点拆分为 Cell[] 分段累加槽，分散并发压力。");
        System.out.println("   (3) 缺陷三：只能保证一个共享变量的原子操作");
        System.out.println("       * 问题本质：CAS 指令只能针对单一内存地址（4/8 字节）生效，无法同时原子更新多个变量（如同时扣款 A 并给 B 加款）。");
        System.out.println("       * 解决之道：");
        System.out.println("         a. 使用 AtomicReference：将多个变量封装进一个不可变对象（POJO/Record）中，通过 CAS 替换整个对象引用；");
        System.out.println("         b. 退化使用互斥锁（synchronized 或 ReentrantLock）保护多变量临界区。");
    }

    /**
     * 演示 ABA 发生
     */
    private static void demonstrateAbaProblem() throws InterruptedException {
        AtomicInteger atomicInt = new AtomicInteger(100);

        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(100); // 保证让 t2 先搞一次 ABA
            } catch (InterruptedException ignored) {}
            // t1 预期值是 100，尝试更新为 200
            boolean success = atomicInt.compareAndSet(100, 200);
            System.out.printf("   [Thread-1] 发生 ABA 后尝试 CAS(100, 200)，结果: %b, 当前实际值: %d\n",
                    success, atomicInt.get());
            System.out.println("   -> 现象：Thread-1 完全无法感知 100 曾经被篡改过，导致潜伏的业务数据逻辑风险！");
        }, "ABA-Victim");

        Thread t2 = new Thread(() -> {
            atomicInt.compareAndSet(100, 101); // 100 -> 101
            System.out.println("   [Thread-2] 将值篡改为: 101");
            atomicInt.compareAndSet(101, 100); // 101 -> 100 (重置回 A)
            System.out.println("   [Thread-2] 又将值还原回: 100 (完成了 A -> B -> A)");
        }, "ABA-Manipulator");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * 演示利用 AtomicStampedReference 解决 ABA
     */
    private static void demonstrateAbaResolutionWithStamp() throws InterruptedException {
        // 初始值 = 100, 初始版本 stamp = 1
        AtomicStampedReference<Integer> stampedRef = new AtomicStampedReference<>(100, 1);

        Thread t1 = new Thread(() -> {
            int stamp = stampedRef.getStamp(); // 记下当前版本 1
            Integer value = stampedRef.getReference(); // 记下当前值 100
            System.out.printf("   [T1] 启动时观察到值: %d, 初始版本 stamp: %d\n", value, stamp);

            try {
                Thread.sleep(100); // 等待 T2 篡改
            } catch (InterruptedException ignored) {}

            // T1 尝试根据当初记录的版本 stamp=1 执行 CAS 更新
            boolean success = stampedRef.compareAndSet(value, 200, stamp, stamp + 1);
            System.out.printf("   [T1] 尝试执行带有版本戳的 CAS(100, 200)，结果: %b\n", success);
            System.out.printf("   [T1] 此时系统最新状态: 引用值=%d, 最终版本 stamp=%d\n",
                    stampedRef.getReference(), stampedRef.getStamp());
            System.out.println("   -> 结论证实：由于版本号已由 1 递增至 3，哪怕引用值相同，CAS 也被成功精准拦截阻断！");
        }, "Stamped-Victim");

        Thread t2 = new Thread(() -> {
            int s1 = stampedRef.getStamp();
            stampedRef.compareAndSet(100, 101, s1, s1 + 1);
            System.out.printf("   [T2] 执行 100 -> 101，版本递增至: %d\n", stampedRef.getStamp());

            int s2 = stampedRef.getStamp();
            stampedRef.compareAndSet(101, 100, s2, s2 + 1);
            System.out.printf("   [T2] 执行 101 -> 100，版本递增至: %d (完成 ABA 伪装)\n", stampedRef.getStamp());
        }, "Stamped-Manipulator");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * 为什么不能所有的锁都用 CAS
     */
    public static void explainWhyNotAllLocksUseCas() {
        System.out.println("大厂高频架构考题：'既然 CAS 这么高效无锁，为什么不能把所有锁全部替换为 CAS？'");
        System.out.println("核心解答：");
        System.out.println("1. 临界区业务耗时导致的性能倒挂：");
        System.out.println("   - CAS 本质是【自旋死循环 (Busy-Waiting)】，它假定临界区极短，极快就能成功；");
        System.out.println("   - 如果临界区涉及复杂业务计算、数据库查询、磁盘读写或第三方 HTTP 调用（耗时数毫秒甚至数秒），");
        System.out.println("     数十个并发线程将在此期间毫无意义地死循环空转，瞬间把所有 CPU 核心打满 100%，系统瘫痪！");
        System.out.println("2. 上下文切换代价 vs 自旋代价的平衡点：");
        System.out.println("   - 操作系统的线程挂起与唤醒（内核态上下文切换）虽然需要 1~2 微秒，");
        System.out.println("   - 但一旦挂起，线程不再占用任何 CPU 计算资源；在长耗时与高争用场景下，挂起线程的总体代价远低于无休止的 CPU 燃烧！");
        System.out.println("3. 结论：短临界区、低争用用 CAS（如 AtomicInteger）；长临界区、复杂操作必须用重量级/排他锁！");
    }
}
