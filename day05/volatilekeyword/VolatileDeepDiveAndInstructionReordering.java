package volatilekeyword;

/**
 * volatile 关键字底层原理、指令重排序机制、线程安全边界及与 synchronized 全方位对比
 *
 * 面试原题：
 * - voliatle关键字有什么作用?
 * - 指令重排序的原理是什么?
 * - volatile可以保证线程安全吗?
 * - volatile和sychronized比较?
 *
 * 核心考点：
 * 1. volatile 两大核心基石作用：保证内存可见性 (Lock 前缀指令) 与 禁止指令重排序 (内存屏障)
 * 2. 指令重排序的三大层级（编译器优化重排、指令流水线重排、写缓冲区内存重排）
 * 3. 硬件内存屏障四剑客：LoadLoad, StoreStore, LoadStore, StoreLoad
 * 4. volatile 无法保证绝对线程安全的原因剖析（缺失复合操作原子性）
 * 5. DCL (Double-Checked Locking) 单例模式中 volatile 为什么绝不可少？（半成品对象指令重排）
 * 6. volatile vs synchronized 核心维度全景对比决策表
 */
public class VolatileDeepDiveAndInstructionReordering {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】volatile 关键字两大作用与底层汇编原理");
        System.out.println("================================================================================");
        explainVolatileCoreRoles();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【核心理论深剖】指令重排序原理与 4 大内存屏障 (Memory Barrier)");
        System.out.println("--------------------------------------------------------------------------------");
        explainInstructionReorderingAndBarriers();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【大厂必考陷阱】volatile 可以保证线程安全吗？");
        System.out.println("--------------------------------------------------------------------------------");
        explainThreadSafetyLimitation();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战经典剖析】DCL 单例模式中 volatile 为何绝对不可或缺？（半成品对象逸出）");
        System.out.println("--------------------------------------------------------------------------------");
        explainDclSingletonHazard();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【对比决策全景】volatile vs synchronized 终极对比表");
        System.out.println("--------------------------------------------------------------------------------");
        printVolatileVsSyncComparison();
    }

    /**
     * volatile 核心作用
     */
    public static void explainVolatileCoreRoles() {
        System.out.println("1. volatile 的两大核心作用：");
        System.out.println("   (1) 保证内存可见性 (Visibility)：");
        System.out.println("       * 被 volatile 修饰的变量，一旦被某个线程修改，JVM 会生成带【lock 前缀指令】的汇编代码；");
        System.out.println("       * 强制将写缓冲区 (Store Buffer) 中的最新值刷新回主内存，同时触发 CPU 的 MESI 缓存一致性协议，");
        System.out.println("         将其他 CPU 核心对应的 Cache Line 标记为失效（Invalid），迫使其下次读取必须穿透到主内存重新加载！");
        System.out.println("   (2) 禁止指令重排序 (Ordering)：");
        System.out.println("       * JMM 规定在对 volatile 变量进行读写时，会在前后插入相应的【内存屏障 (Memory Barrier)】，");
        System.out.println("         禁止编译器与 CPU 对指令进行乱序重排，确保执行顺序与代码预期严格一致。");
    }

    /**
     * 指令重排序原理与屏障
     */
    public static void explainInstructionReorderingAndBarriers() {
        System.out.println("1. 为什么会有指令重排序？");
        System.out.println("   - 现代 CPU 处理速度远超内存读写速度；为了压榨 CPU 流水线利用率，避免 CPU 等待内存响应，");
        System.out.println("     编译器与硬件处理器会在【不改变单线程执行结果（as-if-serial 语义）】的前提下，对指令执行次序进行重新编排。");
        System.out.println("2. 指令重排的三大层级：");
        System.out.println("   - 第一层：编译器优化重排序（JIT 编译器根据数据依赖关系调整语句顺序）；");
        System.out.println("   - 第二层：指令级并行重排序 (ILP)（多发射 CPU 乱序执行无依赖指令）；");
        System.out.println("   - 第三层：内存系统重排序（写缓冲区 Store Buffer 异步写入，使得读写在不同核看来乱序）。");
        System.out.println("3. JMM 规定的 4 大硬件内存屏障：");
        System.out.println("   - LoadLoad:  [Load1] -> LoadLoad  -> [Load2]  (保证 Load1 读先于 Load2 读加载)");
        System.out.println("   - StoreStore:[Store1]-> StoreStore -> [Store2] (保证 Store1 写在 Store2 写之前刷新至主存)");
        System.out.println("   - LoadStore: [Load1] -> LoadStore -> [Store2] (保证 Load1 读先于 Store2 写提交)");
        System.out.println("   - StoreLoad: [Store1]-> StoreLoad -> [Load2]  (全能屏障，强制写刷新后再读，开销最大)");
    }

    /**
     * 线程安全边界阐述
     */
    public static void explainThreadSafetyLimitation() {
        System.out.println("面试官常问：'volatile 可以保证线程安全吗？'");
        System.out.println("核心回答：【不能单独保证绝对的线程安全！】");
        System.out.println("原因深剖：");
        System.out.println("1. 线程安全的三要素是：原子性 (Atomicity)、可见性 (Visibility)、有序性 (Ordering)；");
        System.out.println("2. volatile 只能百分百保证【可见性】与【有序性】，对于单次简单读写是原子的；");
        System.out.println("3. 但对于复合操作（如 count++、if(a==1) a=2 等），包含'读取-修改-写回'多步指令，");
        System.out.println("   volatile 无法阻断多个线程交错执行，因而无法保证原子性，依然会发生数据丢失更新！");
        System.out.println("4. 唯一适用场景：纯状态标志位读取（如 boolean flag）、DCL 双重检查锁。");
    }

    /**
     * DCL 单例模式中 volatile 的必要性
     */
    static class DclSingleton {
        private static volatile DclSingleton instance; // 必须加 volatile！

        private DclSingleton() {}

        public static DclSingleton getInstance() {
            if (instance == null) {                         // 第 1 次检查
                synchronized (DclSingleton.class) {
                    if (instance == null) {                 // 第 2 次检查
                        instance = new DclSingleton();      // 关键赋值语句！
                    }
                }
            }
            return instance;
        }
    }

    public static void explainDclSingletonHazard() {
        System.out.println("为什么 DCL 单例中 private static volatile DclSingleton instance 必须加 volatile？");
        System.out.println("在执行 instance = new DclSingleton() 时，Java 字节码层面实际分为 3 步：");
        System.out.println("   1. memory = allocate();   // 1. 分配对象的堆内存空间");
        System.out.println("   2. ctorInstance(memory);  // 2. 调用构造函数初始化成员属性");
        System.out.println("   3. instance = memory;     // 3. 将 instance 引用指向刚分配的内存地址");
        System.out.println("由于步骤 2 和步骤 3 没有直接数据依赖，JIT 编译器和 CPU 可能发生【指令重排序】：");
        System.out.println("   变为：1 -> 3 -> 2！");
        System.out.println("【灾难场景】：");
        System.out.println("   - 线程 A 刚执行完步骤 3（地址已赋值，但步骤 2 尚未初始化完毕！），此时 instance != null；");
        System.out.println("   - 此时线程 B 进入 getInstance()，在第 1 次检查发现 instance != null，直接高兴地 return instance；");
        System.out.println("   - 结果线程 B 拿到了一个【未初始化的半成品对象 (Half-Initialized Object)】，访问字段引发 NPE 或业务数据错乱！");
        System.out.println("【解决】：加上 volatile 关键字，底层插入 StoreStore 与 StoreLoad 屏障，坚决禁止 1-3-2 重排！");
    }

    /**
     * volatile vs synchronized 对比表
     */
    public static void printVolatileVsSyncComparison() {
        System.out.printf("%-16s | %-32s | %-32s\n", "对比维度", "volatile", "synchronized");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("%-16s | %-32s | %-32s\n", "本质定位", "轻量级同步修饰符 (变量级)", "重量级排他互斥锁 (代码块/方法级)");
        System.out.printf("%-16s | %-32s | %-32s\n", "原子性保障", "【不保证】复合操作原子性", "【保证】原子性，独占执行代码块");
        System.out.printf("%-16s | %-32s | %-32s\n", "可见性保障", "【保证】强可见性 (强制刷新)", "【保证】通过 unlock 刷新主存");
        System.out.printf("%-16s | %-32s | %-32s\n", "有序性保障", "【保证】内存屏障禁止指令重排", "【保证】临界区排他串行化执行");
        System.out.printf("%-16s | %-32s | %-32s\n", "是否阻塞线程", "【绝不阻塞】，轻量高效", "【会阻塞】，竞争失败挂起进内核态");
        System.out.printf("%-16s | %-32s | %-32s\n", "性能开销", "极小 (仅插入内存屏障指令)", "相对较大 (涉及锁膨胀与上下文切换)");
        System.out.printf("%-16s | %-32s | %-32s\n", "适用场景", "状态标志位、DCL 防重排", "高并发复合写、有复杂共享状态保护");
    }
}
