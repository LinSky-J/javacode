package heap;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * JVM 堆内存分代结构、大对象分配策略与完整对象分配流转机制深度实测
 *
 * 涵盖面试核心题目：
 * 1. 堆分为哪几部分呢?
 * 2. 如果有个大对象一般是在哪个区域？
 */
public class JvmHeapStructureAndObjectAllocation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          JVM 堆内存分代划分、大对象流转与完整分配机制深度实测          ");
        System.out.println("======================================================================");

        explainHeapPartitionArchitecture();
        explainLargeObjectAllocationArea();
        explainCompleteObjectAllocationFlow();
        inspectRuntimeMemoryPools();

        System.out.println("======================================================================");
        System.out.println("                 JVM 堆结构与大对象分配策略解析完成                  ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：堆分为哪几部分呢?
     *
     * 核心内容：
     * 1. 传统物理分代（Serial/Parallel/CMS）：新生代（Eden + S0 + S1）、老年代、元空间。
     * 2. 现代 Region 化逻辑分代（G1 / ZGC）：Region 切分与 Humongous 区域。
     */
    public static void explainHeapPartitionArchitecture() {
        System.out.println("\n--- 1. JVM 堆内存核心划分与比例架构 ---");
        System.out.println("一、传统分代收集器架构下的堆内存划分（如 CMS、Parallel Scavenge）：");
        System.out.println("   1. 新生代（Young Generation / New Generation）：约占堆总容量的 1/3。");
        System.out.println("      - Eden 区（伊甸园区）：绝大多数新创建的对象在此降生，占比通常为 8/10（80%）。");
        System.out.println("      - Survivor 0 区（From Survivor 区）：占新生代的 1/10（10%）。");
        System.out.println("      - Survivor 1 区（To Survivor 区）：占新生代的 1/10（10%）。");
        System.out.println("      - 核心比例：Eden : S0 : S1 默认比例为 8 : 1 : 1（可通过 -XX:SurvivorRatio=8 配置）。");
        System.out.println("      - 回收算法：复制算法（Copying Algorithm）。Eden 与 From 区的存活对象在 Minor GC 时复制到 To 区。");
        System.out.println("   2. 老年代（Old Generation / Tenured）：约占堆总容量的 2/3。");
        System.out.println("      - 存放经过多次 Minor GC 依然存活的长寿命对象，以及系统启动时的常驻服务对象、大对象。");
        System.out.println("      - 回收算法：标记-清除（Mark-Sweep）或标记-整理（Mark-Compact）算法，触发 Major GC / Full GC。");
        System.out.println("   3. 永久代 / 元空间的地位澄清：");
        System.out.println("      - JDK 7 之前永久代（PermGen）物理占用堆空间，但逻辑上属于方法区实现。");
        System.out.println("      - JDK 8 彻底废弃永久代，引入元空间（Metaspace），使用 OS 本地直接内存，不属于 JVM 堆！");

        System.out.println("\n二、现代低延迟收集器架构下的堆内存划分（G1、ZGC、Shenandoah）：");
        System.out.println("   1. G1 收集器（Garbage-First）：");
        System.out.println("      - 打破了连续物理分代界限，将堆切分成 2048 个大小均等（1MB ~ 32MB，且为 2 的幂）的独立【Region】。");
        System.out.println("      - 每个 Region 依然动态扮演 Eden、Survivor、Old 的逻辑角色，按需分配；");
        System.out.println("      - 新增了专门的【Humongous Region（巨型对象区）】。");
        System.out.println("   2. ZGC（Z Garbage Collector）：");
        System.out.println("      - 采用基于 Region（称为 Page）的分页技术，分为小型（2MB）、中型（32MB）、大型（动态变化），支持 TB 级堆内存与微秒级暂停。");
        System.out.println("      - JDK 21 正式引入了 Generational ZGC（分代 ZGC），兼具高吞吐与极低延迟。");
    }

    /**
     * 问题 2：如果有个大对象一般是在哪个区域？
     *
     * 核心规则：
     * 1. 直接进入老年代（Old Generation / Tenured）。
     * 2. -XX:PretenureSizeThreshold 参数控制（针对 Serial / ParNew）。
     * 3. G1 收集器下的 Humongous Region。
     * 4. 为什么要这样设计：避免大对象在年轻代复制算法中产生巨大的内存拷贝开销，避免过早打满 Survivor 区导致年轻代对象提前晋升老年代。
     */
    public static void explainLargeObjectAllocationArea() {
        System.out.println("\n--- 2. 大对象（Large Object）的分配区域与设计原由 ---");
        System.out.println("权威回答：");
        System.out.println("1. 默认与常规情况下：大对象直接在【老年代（Old Generation）】分配！");
        System.out.println("   - 在 G1 收集器中，如果对象大小超过单个 Region 的 50%，直接被判定为巨型对象（Humongous Object），");
        System.out.println("     连续分配在一个或多个【Humongous Region】中，在逻辑管理上归属于老年代！");
        System.out.println("2. 什么是大对象？");
        System.out.println("   - 需要大量连续内存空间的 Java 对象。典型代表：超长数组（如 byte[]、char[]）、未分页的大列表、超长字符串、大位图。");
        System.out.println("3. JVM 参数控制：");
        System.out.println("   - -XX:PretenureSizeThreshold：设置大对象直接晋升老年代的字节阈值（例如 -XX:PretenureSizeThreshold=3145728，即 3MB）。");
        System.out.println("   - 只要对象大小超过该阈值，JVM 无论 Eden 区空间是否充足，直接跳过新生代进入老年代分配！");
        System.out.println("4. 为什么大对象要直接进入老年代？（深层原因剖析）");
        System.out.println("   原因一：避免高昂的内存复制开销");
        System.out.println("     - 新生代采用的是【复制算法】。如果大对象分配在 Eden，每次 Minor GC 存活下来时，都需要在 Eden 和 Survivor 区之间来回深拷贝复制，造成 CPU 极大浪费。");
        System.out.println("   原因二：防止破坏 Survivor 空间，避免年轻代短命对象提前晋升");
        System.out.println("     - Survivor 区通常容量较小（默认只占年轻代 10%）。一个大对象就会迅速占满 Survivor 空间，");
        System.out.println("       导致年轻代触发【动态年龄判定（Dynamic Tenuring Age）】或 Survivor 空间担保机制，");
        System.out.println("       迫使原本很多短命的年轻对象被迫提前晋升到老年代，加剧老年代碎片化和 Full GC 频率！");
    }

    /**
     * 补充面试高频：对象的完整分配流程（从代码 new 到堆内存终点）
     */
    public static void explainCompleteObjectAllocationFlow() {
        System.out.println("\n--- 3. 对象完整分配决策流转链（面试通关金牌模型） ---");
        System.out.println("当代码中执行 User user = new User() 时，JVM 的分配决策漏斗：");
        System.out.println("步骤 1：逃逸分析与栈上分配（Stack Allocation）");
        System.out.println("   - JIT 编译器进行逃逸分析（Escape Analysis）。如果对象没有发生方法逃逸与线程逃逸，");
        System.out.println("     通过【标量替换】将对象成员变量打散在栈帧局部变量表分配，方法结束栈帧销毁即释放，彻底不经过堆！");
        System.out.println("步骤 2：判断是否为大对象");
        System.out.println("   - 如果逃逸出方法，判断对象大小是否达到 -XX:PretenureSizeThreshold 或 G1 Humongous 阈值。");
        System.out.println("   - 达到阈值：直接进入【老年代】分配。");
        System.out.println("步骤 3：TLAB 线程本地分配缓冲区（Thread Local Allocation Buffer）");
        System.out.println("   - 如果不是大对象，优先在当前线程私有的 TLAB（位于 Eden 区内为各线程预分配的小块私有内存）上分配；");
        System.out.println("   - 核心优势：多线程高并发分配对象时，使用指针碰撞（Bump the Pointer），无锁化分配，零线程竞争！");
        System.out.println("步骤 4：Eden 区分配");
        System.out.println("   - 若 TLAB 空间不足或已用尽，通过 CAS 悲观加锁机制在 Eden 区公共空间分配。");
        System.out.println("步骤 5：GC 与老年代晋升（Aging & Promotion）");
        System.out.println("   - Eden 空间满触发 Minor GC：");
        System.out.println("     a. 存活对象年龄 +1 复制到 To Survivor 区；");
        System.out.println("     b. 年龄达到 -XX:MaxTenuringThreshold（默认 15）晋升老年代；");
        System.out.println("     c. 动态年龄判定：Survivor 区相同年龄所有对象总大小超过 Survivor 空间的 50%，大于等于该年龄的对象直接晋升老年代；");
        System.out.println("     d. 空间分配担保：若 To Survivor 无法容纳存活对象，由老年代进行担保，直接晋升老年代。");
    }

    /**
     * 现场探查当前 JVM 各内存池（Eden, Survivor, Old, Metaspace）运行状态
     */
    public static void inspectRuntimeMemoryPools() {
        System.out.println("\n--- 4. 当前运行期 JVM 真实内存池（Memory Pools）实测探查 ---");
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            MemoryUsage usage = pool.getUsage();
            long initMb = usage.getInit() / 1024 / 1024;
            long usedMb = usage.getUsed() / 1024 / 1024;
            long maxMb = usage.getMax() < 0 ? -1 : usage.getMax() / 1024 / 1024;
            System.out.println(String.format("   [%-30s | 类型: %-12s] 已用: %4d MB | 初始: %4d MB | 最大: %s MB",
                    pool.getName(),
                    pool.getType().toString(),
                    usedMb,
                    initMb,
                    maxMb < 0 ? "无限/依赖系统" : String.valueOf(maxMb)));
        }
    }
}
